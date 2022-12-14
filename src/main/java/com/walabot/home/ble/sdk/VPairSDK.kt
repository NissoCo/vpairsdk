package com.walabot.home.ble.sdk

import android.content.Context
import android.net.wifi.WifiInfo
import android.util.Log
import com.walabot.home.ble.Result
import com.walabot.home.ble.WHBle
import com.walabot.home.ble.pairing.ConfigParams
import com.walabot.home.ble.pairing.Gen2CloudOptions
import com.walabot.home.ble.pairing.WifiNetworkMonitor
import com.walabot.home.ble.pairing.esp.*
import com.walabot.home.ble.pairing.esp.EspBleApi.ESPBleAPIImpl

data class CloudCredentials(val userId: String?, val idToken: String?, var updateCloud: Boolean = true, var cloudParams: ConfigParams? = null)

class VPairSDK : WifiNetworkMonitor.Scan {
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
    var cloudCredentials = CloudCredentials(null, null, false, null)

    fun startPairing(context: Context, cloudCredentials: CloudCredentials) {
        wifiMonitor = WifiNetworkMonitor(context)
        wifiMonitor?.scanEvents = this
        wifiMonitor?.startScanWifi()
        pairingApi = EspBleApi(WHBle(context))
        this.cloudCredentials = cloudCredentials
        listener?.onEvent(EspPairingEvent.Connecting, connectedDevice)
        pairingApi?.connect(object : EspApi.EspAPICallback<WalabotDeviceDesc?> {
            override fun onSuccess(obj: WalabotDeviceDesc?) {
                listener?.onEvent(EspPairingEvent.Connected, connectedDevice)
                connectedDevice = obj
                refreshWifiList()
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    fun stopPairing() {
        pairingApi?.stop()
    }

    fun refreshWifiList() {
        connectedDevice?.let { walabotDeviceDesc ->
            if (walabotDeviceDesc.protocolVersion == 3) {
                listener?.onEvent(EspPairingEvent.WifiScan, connectedDevice)
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

    fun resumeConnection(selectedWifiDetails: EspWifiItem, password: String) {
        if (selectedWifiDetails.ssid.isEmpty()) {
            listener?.onFinish(Result(Throwable("SSID can't be empty")))
            return
        }
        if (!isDeviceConnected) {
            listener?.onFinish(Result(Throwable("Device not connected")))
            return
        }
        listener?.onEvent(EspPairingEvent.SendingCloudDetails, connectedDevice)
        pairingApi?.sendWifiCredentials(
            selectedWifiDetails.ssid.convert(),
            selectedWifiDetails.bssid.convert(),
            password.convert(), object : EspApi.EspAPICallback<WalabotDeviceDesc?> {
            override fun onSuccess(obj: WalabotDeviceDesc?) {
                listener?.onEvent(EspPairingEvent.SentCloudDetails, connectedDevice)
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
                    reboot()
                }
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun pair(host: String?) {
        listener?.onEvent(EspPairingEvent.Pairing, connectedDevice)
        pairingApi?.pair(host, cloudCredentials.userId, object : EspApi.EspAPICallback<EspPairingResponse?> {
            override fun onSuccess(obj: EspPairingResponse?) {
                listener?.onEvent(EspPairingEvent.Paired, connectedDevice)
                if (cloudCredentials.updateCloud) {
                    performParingWithCloud()
                } else {
                    notifyPairingComplete(host, obj?.code!!)
                }
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun performParingWithCloud() {

    }

    private fun notifyPairingComplete(host: String?, code: String) {
        pairingApi?.notifyPairingComplete(host, cloudCredentials.userId, code, object : EspApi.EspAPICallback<Void?> {
            override fun onSuccess(obj: Void?) {
                listener?.onEvent(EspPairingEvent.NotifyPairingComplete, connectedDevice)
                reboot()
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun reboot() {
        listener?.onEvent(EspPairingEvent.Rebooting, connectedDevice)
        pairingApi?.reboot(object : EspApi.EspAPICallback<Void?> {
            override fun onSuccess(obj: Void?) {
                listener?.onEvent(EspPairingEvent.Rebooted, connectedDevice)
                rebootToFactory()
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun rebootToFactory() {
        listener?.onEvent(EspPairingEvent.RebootingToFactory, connectedDevice)
        pairingApi?.rebootToFactory(object : EspApi.EspAPICallback<Void?> {
            override fun onSuccess(obj: Void?) {
                listener?.onEvent(EspPairingEvent.RebootedToFactory, connectedDevice)
                listener?.onFinish(Result(connectedDevice))
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