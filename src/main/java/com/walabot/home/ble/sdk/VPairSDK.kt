package com.walabot.home.ble.sdk

import android.content.Context
import com.walabot.home.ble.Message
import com.walabot.home.ble.Result
import com.walabot.home.ble.WHBle
import com.walabot.home.ble.pairing.Gen2CloudOptions
import com.walabot.home.ble.pairing.esp.*

data class CloudCredentials(val userId: String?, val idToken: String?, var updateCloud: Boolean = true)

class VPairSDK {
    var pairingApi: EspBleApi? = null
    var listener: PairingListener? = null
    var analyticsHandler: AnalyticsHandler? = null

    val isDeviceConnected: Boolean
    get() {
        return pairingApi?.isConnected ?: false
    }

    var cloudCredentials = CloudCredentials(null, null, false)

    fun startPairing(context: Context, cloudCredentials: CloudCredentials) {
        pairingApi = EspBleApi(WHBle(context))
        this.cloudCredentials = cloudCredentials
        listener?.onEvent(EspPairingEvent.Connecting)
        pairingApi?.connect(object : EspApi.EspAPICallback<WalabotDeviceDesc?> {
            override fun onSuccess(obj: WalabotDeviceDesc?) {
                listener?.onEvent(EspPairingEvent.Connected)
                pairingApi?.sendWiFiScanRequest(object : EspApi.EspAPICallback<ProtocolMediator.WifiScanResult?> {
                    override fun onSuccess(obj: ProtocolMediator.WifiScanResult?) {
                        obj?.convert()?.let {
                            listener?.shouldSelect(it)
                        }
                    }

                    override fun onFailure(throwable: Throwable?) {
                        listener?.onFinish(Result(throwable))
                    }
                })
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
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
        pairingApi?.sendWifiCredentials(
            selectedWifiDetails.ssid.convert(),
            selectedWifiDetails.bssid.convert(),
            password.convert(), object : EspApi.EspAPICallback<WalabotDeviceDesc?> {
            override fun onSuccess(obj: WalabotDeviceDesc?) {
                listener?.onEvent(EspPairingEvent.SendCloudDetails)
                updateCloud(obj)
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun updateCloud(deviceDes: WalabotDeviceDesc?) {
        pairingApi?.sendCloudDetails(Gen2CloudOptions(), object : EspApi.EspAPICallback<Void?> {
            override fun onSuccess(obj: Void?) {
                pair(deviceDes?.host!!)
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun pair(host: String) {
        pairingApi?.pair(host, cloudCredentials.userId, object : EspApi.EspAPICallback<EspPairingResponse?> {
            override fun onSuccess(obj: EspPairingResponse?) {
                listener?.onEvent(EspPairingEvent.Pair)
                if (cloudCredentials.updateCloud) {
                    performParingWithCluod()
                } else {
                    notifyPairingComplete(host, obj?.code!!)
                }
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun performParingWithCluod() {

    }

    private fun notifyPairingComplete(host: String, code: String) {
        pairingApi?.notifyPairingComplete(host, cloudCredentials.userId, code, object : EspApi.EspAPICallback<Void?> {
            override fun onSuccess(obj: Void?) {
                listener?.onEvent(EspPairingEvent.NotifyPairingComplete)
                reboot()
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun reboot() {
        pairingApi?.reboot(object : EspApi.EspAPICallback<Void?> {
            override fun onSuccess(obj: Void?) {
                listener?.onEvent(EspPairingEvent.Reboot)
                rebootToFactory()
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    private fun rebootToFactory() {
        pairingApi?.rebootToFactory(object : EspApi.EspAPICallback<Void?> {
            override fun onSuccess(obj: Void?) {
                listener?.onEvent(EspPairingEvent.RebootToFactory)
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }
}