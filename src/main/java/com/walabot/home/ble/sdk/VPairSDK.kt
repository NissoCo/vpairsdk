package com.walabot.home.ble.sdk

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiInfo
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.walabot.home.ble.Result
import com.walabot.home.ble.WHBle
import com.walabot.home.ble.pairing.ConfigParams
import com.walabot.home.ble.pairing.Gen2CloudOptions
import com.walabot.home.ble.pairing.WifiNetworkMonitor
import com.walabot.home.ble.pairing.esp.*
import com.walabot.home.ble.pairing.esp.EspBleApi.ESPBleAPIImpl
import java.security.Permission

fun Context.isBleAuth(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    }
    return true
}

data class CloudCredentials(val userId: String?, val idToken: String?, var cloudParams: ConfigParams? = null, val updateCloud: Boolean = userId?.isNotEmpty() ?: false)

open class VPairSDK(val context: Context, val cloudCredentials: CloudCredentials) : WifiNetworkMonitor.Scan {
    var pairingApi: EspBleApi? = null
    var listener: PairingListener? = null
    var analyticsHandler: AnalyticsHandler? = null
    var currentWifi: EspWifiItem? = null
    private val isDeviceConnected: Boolean
    get() {
        return pairingApi?.isConnected ?: false
    }

    var connectedDevice: WalabotDeviceDesc? = null

    var wifiMonitor: WifiNetworkMonitor? = null

    init {
        pairingApi = EspBleApi(WHBle(context))
    }

    open fun startPairing() {
        if (context.isBleAuth()) {
            wifiMonitor = WifiNetworkMonitor(context)
            wifiMonitor?.scanEvents = this
            wifiMonitor?.startScanWifi()
            pairingApi = EspBleApi(WHBle(context))
            listener?.onEvent(EspPairingEvent.Connecting)
            pairingApi?.connect(object : EspApi.EspAPICallback<WalabotDeviceDesc?> {
                override fun onSuccess(obj: WalabotDeviceDesc?) {
                    listener?.onEvent(EspPairingEvent.Connected)
                    connectedDevice = obj
                    refreshWifiList()
                }

                override fun onFailure(throwable: Throwable?) {
                    listener?.onFinish(Result(throwable))
                }
            })
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listener?.onMissingPermission(Manifest.permission.BLUETOOTH_SCAN)
            }
            listener?.onMissingPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    open fun isBleOn(): Boolean {
        return pairingApi?.isBleEnabled ?: false
    }

    open fun stopPairing() {
        pairingApi?.stop()
        listener = null
        analyticsHandler = null
    }

    fun refreshWifiList() {
        connectedDevice?.let { walabotDeviceDesc ->
            if (walabotDeviceDesc.protocolVersion == 3) {
                listener?.onEvent(EspPairingEvent.WifiScan)
                pairingApi?.sendWiFiScanRequest(object :
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
        } ?: kotlin.run {
            listener?.onFinish(Result(Throwable("Device is not connected")))
        }

    }

    open fun resumeConnection(selectedWifiDetails: EspWifiItem, password: String) {
        if (selectedWifiDetails.ssid.isEmpty()) {
            listener?.onFinish(Result(Throwable("SSID can't be empty")))
            return
        }
        if (!isDeviceConnected) {
            listener?.onFinish(Result(Throwable("Device not connected")))
            return
        }
        listener?.onEvent(EspPairingEvent.SendingCloudDetails)
        pairingApi?.sendWifiCredentials(
            selectedWifiDetails.ssid.convert(),
            selectedWifiDetails.bssid.convert(),
            password.convert(), object : EspApi.EspAPICallback<WalabotDeviceDesc?> {
            override fun onSuccess(obj: WalabotDeviceDesc?) {
                listener?.onEvent(EspPairingEvent.SentCloudDetails)
                updateCloud(obj)
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun updateCloud(deviceDes: WalabotDeviceDesc?) {
        val options = Gen2CloudOptions()
        options.params = cloudCredentials.cloudParams
        pairingApi?.sendCloudDetails(options, object : EspApi.EspAPICallback<Void?> {
            override fun onSuccess(obj: Void?) {
                if (cloudCredentials.updateCloud) {
                    pair(deviceDes?.host)
                } else {
                    reboot(deviceDes?.name ?: "")
                }
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun pair(host: String?) {
        listener?.onEvent(EspPairingEvent.Pairing)
        pairingApi?.pair(host, cloudCredentials.userId, object : EspApi.EspAPICallback<EspPairingResponse?> {
            override fun onSuccess(obj: EspPairingResponse?) {
                listener?.onEvent(EspPairingEvent.Paired)
                if (cloudCredentials.updateCloud) {
                    performParingWithCloud(host, obj?.code)
                } else {
                    notifyPairingComplete(host, obj?.code!!, null)
                }
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun performParingWithCloud(host: String?, code: String?) {
        listener?.onEvent(EspPairingEvent.StagePairWithCloud)
        code?.let { it1 ->
            Connection().pairing(code, cloudCredentials.idToken!!) {
                if (it.isSuccess) {
                    listener?.onEvent(EspPairingEvent.NotifyPairingComplete)
                    notifyPairingComplete(host, it1, it.getOrNull()?.get("deviceId") as String?)
                } else {
                    listener?.onFinish(Result(Throwable("Failed to pair with the cloud")))
                }
            }
        }
    }

    private fun notifyPairingComplete(host: String?, code: String, deviceId: String?) {
        pairingApi?.notifyPairingComplete(host, cloudCredentials.userId, code, object : EspApi.EspAPICallback<Void?> {
            override fun onSuccess(obj: Void?) {
                listener?.onEvent(EspPairingEvent.NotifyPairingComplete)
                reboot(deviceId ?: "")
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun reboot(deviceId: String) {
        listener?.onEvent(EspPairingEvent.Rebooting, deviceId)
        pairingApi?.reboot(object : EspApi.EspAPICallback<Void?> {
            override fun onSuccess(obj: Void?) {
                listener?.onEvent(EspPairingEvent.Rebooted, deviceId)
                rebootToFactory(deviceId)
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    open fun rebootToFactory(deviceId: String) {
        listener?.onEvent(EspPairingEvent.RebootingToFactory, deviceId)
        pairingApi?.rebootToFactory(object : EspApi.EspAPICallback<Void?> {
            override fun onSuccess(obj: Void?) {
                listener?.onEvent(EspPairingEvent.RebootedToFactory, deviceId)
                listener?.onFinish(Result(deviceId))
                pairingApi?.stop()
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    override fun onNetworkStateChange(info: WifiInfo?) {
        wifiMonitor?.stopScan()
        if (info != null) {
            val cleanName = info.ssid.replace("\"", "")
            currentWifi = EspWifiItemImpl(cleanName, info.bssid, info.rssi)
        }
    }
}