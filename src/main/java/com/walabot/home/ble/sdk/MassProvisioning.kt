package com.walabot.home.ble.sdk

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.walabot.home.ble.BleDevice
import com.walabot.home.ble.Result
import com.walabot.home.ble.pairing.esp.*
import java.util.*
import kotlin.collections.ArrayList

fun Context.isBleEnabled(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    } else {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.isBleOn(): Boolean {
    val bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    return  bleManager.adapter.isEnabled
}


class MassProvisioning(val context: Context, var config: Config) :
    EspBleApi.OnResult {
    private val scanner: VayyarScanner by lazy {
        VayyarScanner(context, arrayListOf(UUID.fromString("21a07e04-1fbf-4bf6-b484-d319b8282a1c")))
    }

    val unpairedDevices: MutableSet<BleDevice> by lazy {
        mutableSetOf()
    }

    var pickedDevices: ArrayList<BleDevice>? = null

    private val bleApis: ArrayList<EspBleApi> by lazy {
        ArrayList()
    }

    val isBleOn: Boolean
    get() = context.isBleOn()

//    private var wifiMonitor: WifiNetworkMonitor? = null
//    var currentWifi: EspWifiItem? = null
    var eventsHandler: PairingEvents? = null
    var analyticsHandler: AnalyticsHandler? = null


    @RequiresApi(Build.VERSION_CODES.M)
    fun startMassProvision() {
        if (context.isBleEnabled()) {
            scanner.startScan {
                if (it.isSuccess) {
                    connect(it.getOrNull()!!)
                } else {
                    Log.d("scan error", it.exceptionOrNull().toString())
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                eventsHandler?.onMissingPermission(Manifest.permission.BLUETOOTH_SCAN)
            }
            eventsHandler?.onMissingPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun connect(devices: List<BleDevice>) {
        pickedDevices = ArrayList(devices)
        pickedDevices?.let {
            connect(it.first())
        }
    }

    private fun connect(bleDevice: BleDevice) {
        val bleApi = EspBleApi(context, config, this)
        bleApi.setAnalyticsHandler(analyticsHandler)
        bleApis.add(bleApi)
        bleApi.connect(bleDevice)
        pickedDevices?.remove(bleDevice)
    }

    private fun scanWifi(bleApi: EspBleApi, onSuccess: (List<EspWifiItem>) -> Unit) {
        bleApi.sendWiFiScanRequest(object :
            EspApi.EspAPICallback<ProtocolMediator.WifiScanResult?> {
            override fun onSuccess(obj: ProtocolMediator.WifiScanResult?) {
                obj?.convert()?.let {
                    onSuccess(it)
                }
            }

            override fun onFailure(throwable: Throwable?) {
                eventsHandler?.onEvent(
                    EspPairingEvent.WifiScan,
                    true,
                    throwable?.message ?: "",
                bleApi.devInfo,
                bleApi.deviceId)
            }
        })
    }

    public fun refreshWifiList(callback: ((List<EspWifiItem>) -> Unit)? = null) {
        eventsHandler?.onEvent(EspPairingEvent.WifiScan, false, EspPairingEvent.WifiScan.name, bleApis.first().devInfo, bleApis.first().deviceDescriptor?.mac ?: "")
        scanWifi(bleApis.first()) {
            if (callback == null) {
                eventsHandler?.shouldSelect(it)
            } else {
                callback(it)
            }
        }
    }

    private fun startMassProvisioning() {
        val temp = pickedDevices?.toSet()
        temp?.forEach {
            connect(it)
        }
    }

    fun resumeConnection(ssid: String, bssid: String, password: String, bleApi: EspBleApi? = null) {
        if (config.wifi.ssid == null) {
            if (config.wifi.ssid == null) {
                config.wifi.ssid = ssid
                config.wifi.bssid = bssid
                config.wifi.password = password
            }
        }
        var currentApi = bleApi
        if (bleApi == null) {
            currentApi = bleApis.first()
        }
        if (bleApi == null) startMassProvisioning()
        currentApi?.sendCloudDetails(ssid, bssid, password)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResult(result: Result<EspPairingEvent>, espBleApi: EspBleApi?) {
        if (result.isSuccessfull) {
            when (result.result) {
                EspPairingEvent.Connected -> {
                    espBleApi?.deviceDescriptor?.let {
                        config.wifi.ssid?.let {
                            resumeConnection(it, config.wifi.bssid ?: "", config.wifi.password ?: "", espBleApi)
                        } ?: kotlin.run {
                            refreshWifiList()
                        }
                    }
                }
                EspPairingEvent.RebootedToFactory -> {
                    bleApis.remove(espBleApi)
                    if (bleApis.isEmpty()) {
                        startMassProvision()
                    }
                }
                else -> {

                }
            }
            val message = if (result.isSuccessfull) result.result.name else result.throwable.message ?: ""
            eventsHandler?.onEvent(result.result, !result.isSuccessfull, message, espBleApi?.devInfo, espBleApi?.deviceDescriptor?.mac ?: "")
        } else {
            bleApis.remove(espBleApi)
            val error = result.throwable as EspPairingException
            when (error.errorCode) {
                3011 -> {
                    espBleApi?.let { api ->
                        scanWifi(api) {
                            eventsHandler?.onWifiCredentialsFail(it)
                        }
                    }
                }
            }
            eventsHandler?.onError(error)
        }
    }

    fun stopPairing() {

    }
}