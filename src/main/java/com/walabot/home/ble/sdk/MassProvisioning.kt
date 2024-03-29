package com.walabot.home.ble.sdk

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiInfo
import android.os.Build
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

fun Context.isBleEnabled(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    }
    return true
}


class MassProvisioning(val context: Context, val cloudCredentials: CloudCredentials) :
    EspBleApi.OnResult {
    private val scanner: WalabotHomeDeviceScanner by lazy {
        WalabotHomeDeviceScanner(context, UUID.fromString("21a07e04-1fbf-4bf6-b484-d319b8282a1c"))
    }

    val unpairedDevices: MutableSet<BleDevice> by lazy {
        mutableSetOf()
    }

    private val bleApis: ArrayList<EspBleApi> by lazy {
        ArrayList()
    }

    private var wifiMonitor: WifiNetworkMonitor? = null
    var currentWifi: EspWifiItem? = null
    var listener: PairingListener? = null
    var pickedWifiCredentials: EspWifiItem? = null
    var pickedWifiPassword: String? = null


    fun scan() {
        if (context.isBleEnabled()) {
            scanner.startScan(10000, object : BleDiscoveryCallback {
                override fun onBleDiscovered(
                    newBleDevice: BleDevice?,
                    currentBleList: MutableCollection<BleDevice>?
                ) {
                    currentBleList?.let {
                        unpairedDevices.addAll(it)
                    }
                }

                override fun onBleDiscoveryError(err: Int) {

                }

                override fun onBleDiscoveryEnded() {
                    // The first ble device has been scanned
                    if (unpairedDevices.isNotEmpty()) {
                        connect(unpairedDevices.first())
                    }
                }

            })
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listener?.onMissingPermission(Manifest.permission.BLUETOOTH_SCAN)
            }
            listener?.onMissingPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun connect(bleDevice: BleDevice) {
        val bleApi = EspBleApi(context, cloudCredentials, this)
        bleApis.add(bleApi)
        bleApi.connect(bleDevice)
        unpairedDevices.remove(bleDevice)
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
        val temp = unpairedDevices.toSet()
        temp.forEach {
            connect(it)
        }
    }

    fun resumeConnection(selectedWifiDetails: EspWifiItem, password: String, bleApi: EspBleApi? = null) {
        if (pickedWifiCredentials == null) {
            pickedWifiCredentials = selectedWifiDetails
            pickedWifiPassword = password
        }
        var currentApi = bleApi
        if (bleApi == null) {
            currentApi = bleApis.first()
        }
        startMassProvisioning()
        currentApi?.sendCloudDetails(selectedWifiDetails, password)
    }


    override fun onResult(result: Result<EspPairingEvent>, espBleApi: EspBleApi?) {
        if (result.isSuccessfull) {
            when (result.result) {
                EspPairingEvent.Connected -> {
                    espBleApi?.deviceDescriptor?.let {
                        currentWifi?.let {
                            resumeConnection(pickedWifiCredentials!!, pickedWifiPassword!!, espBleApi)
                        } ?: kotlin.run {
                            wifiMonitor = WifiNetworkMonitor(context)
                            wifiMonitor?.scanEvents = object : WifiNetworkMonitor.Scan {
                                override fun onNetworkStateChange(info: WifiInfo?) {
                                    wifiMonitor?.stopScan()
                                    if (info != null) {
                                        val cleanName = info.ssid.replace("\"", "")
                                        currentWifi = EspWifiItemImpl(cleanName, info.bssid, info.rssi)
                                    }
                                    refreshWifiList(it)
                                }

                            }
                            wifiMonitor?.startScanWifi()
                        }
                    }
                }
                EspPairingEvent.RebootedToFactory -> {
                    bleApis.remove(espBleApi)
                }
                else -> {
                    listener?.onEvent(result.result, espBleApi?.deviceDescriptor?.mac)
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