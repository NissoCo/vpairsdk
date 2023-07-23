package com.walabot.home.ble.sdk

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.walabot.home.ble.BleDevice
import com.walabot.home.ble.BleDiscoveryCallback
import com.walabot.home.ble.Result
import com.walabot.home.ble.WalabotHomeDeviceScanner
import com.walabot.home.ble.pairing.WifiNetworkMonitor
import com.walabot.home.ble.pairing.esp.EspApi
import com.walabot.home.ble.pairing.esp.EspBleApi
import com.walabot.home.ble.pairing.esp.ProtocolMediator
import com.walabot.home.ble.pairing.esp.WalabotDeviceDesc
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


class MassProvisioning(val context: Context, var cloudCredentials: CloudCredentials? = null) :
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

    private var wifiMonitor: WifiNetworkMonitor? = null
    var currentWifi: EspWifiItem? = null
    var listener: PairingListener? = null
    var pickedWifiCredentials: EspWifiItem? = null
    var pickedWifiPassword: String? = null


    @RequiresApi(Build.VERSION_CODES.M)
    fun scan() {
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
                listener?.onMissingPermission(Manifest.permission.BLUETOOTH_SCAN)
            }
            listener?.onMissingPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun connect(devices: List<BleDevice>) {
        pickedDevices = ArrayList(devices)
        pickedDevices?.let {
            connect(it.first())
        }
    }

    private fun connect(bleDevice: BleDevice) {
        val bleApi = EspBleApi(context, cloudCredentials, this)
        bleApis.add(bleApi)
        bleApi.connect(bleDevice)
        pickedDevices?.remove(bleDevice)
    }

    fun refreshWifiList(walabotDeviceDesc: WalabotDeviceDesc) {
        if (walabotDeviceDesc.protocolVersion == 3) {
            listener?.onEvent(EspPairingEvent.WifiScan)
            bleApis.first().sendWiFiScanRequest(object :
                EspApi.EspAPICallback<ProtocolMediator.WifiScanResult?> {
                override fun onSuccess(obj: ProtocolMediator.WifiScanResult?) {
                    obj?.convert()?.let {
                        listener?.shouldSelect(it)
                    }
                }

                override fun onFailure(throwable: Throwable?) {
                    listener?.onFinish(Result(throwable))
                }
            })
        } else {
            currentWifi?.let {
                listener?.shouldSelect(arrayOf(it).asList())
            }
        }

    }

    private fun startMassProvisioning() {
        val temp = pickedDevices?.toSet()
        temp?.forEach {
            connect(it)
        }
    }

    fun resumeConnection(selectedWifiDetails: EspWifiItem, password: String, bleApi: EspBleApi? = null) {
        if (pickedWifiCredentials == null) {
            currentWifi = selectedWifiDetails
            pickedWifiCredentials = selectedWifiDetails
            pickedWifiPassword = password
        }
        var currentApi = bleApi
        if (bleApi == null) {
            currentApi = bleApis.first()
        }
        if (bleApi == null) startMassProvisioning()
        currentApi?.sendCloudDetails(selectedWifiDetails, password)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResult(result: Result<EspPairingEvent>, espBleApi: EspBleApi?) {
        if (result.isSuccessfull) {
            when (result.result) {
                EspPairingEvent.Connected -> {
                    espBleApi?.deviceDescriptor?.let {
                        currentWifi?.let {
                            resumeConnection(pickedWifiCredentials!!, pickedWifiPassword!!, espBleApi)
                        } ?: kotlin.run {
                            refreshWifiList(it)
//                            wifiMonitor = WifiNetworkMonitor(context)
//                            wifiMonitor?.scanEvents = object : WifiNetworkMonitor.Scan {
//                                override fun onNetworkStateChange(info: WifiInfo?) {
//                                    wifiMonitor?.stopScan()
//                                    if (info != null) {
//                                        val cleanName = info.ssid.replace("\"", "")
//                                        currentWifi = EspWifiItemImpl(cleanName, info.bssid, info.rssi)
//                                    }
//                                    refreshWifiList(it)
//                                }
//
//                            }
//                            wifiMonitor?.startScanWifi()
                        }
                    }
                }
                EspPairingEvent.RebootedToFactory -> {
                    bleApis.remove(espBleApi)
                    if (bleApis.isEmpty()) {
                        scan()
                    }
                }
                else -> {

                }
            }
            listener?.onEvent(result.result, espBleApi?.deviceDescriptor?.mac)
        } else {
            bleApis.remove(espBleApi)
            listener?.onFinish(Result(result.throwable))
        }
    }

    fun stopPairing() {

    }
}