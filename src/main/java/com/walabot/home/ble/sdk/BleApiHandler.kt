package com.walabot.home.ble.sdk

import com.walabot.home.ble.BleDevice
import com.walabot.home.ble.Result
import com.walabot.home.ble.pairing.Gen2CloudOptions
import com.walabot.home.ble.pairing.esp.EspApi
import com.walabot.home.ble.pairing.esp.EspBleApi
import com.walabot.home.ble.pairing.esp.EspPairingResponse
import com.walabot.home.ble.pairing.esp.WalabotDeviceDesc


fun EspBleApi.connect(bleDevice: BleDevice) {
    connect(bleDevice, object : EspApi.EspAPICallback<WalabotDeviceDesc?> {
        override fun onSuccess(obj: WalabotDeviceDesc?) {
            callback.onResult(Result(EspPairingEvent.Connected), this@connect)
        }

        override fun onFailure(throwable: Throwable?) {
            callback.onResult(Result(throwable), this@connect)
        }

    })
}

fun EspBleApi.sendCloudDetails(wifiDetails: EspWifiItem, password: String) {
    if (wifiDetails.ssid.isEmpty()) {
        callback.onResult(Result(Throwable("SSID can't be empty")), this)
        return
    }
    if (!isConnected) {
        callback.onResult(Result(Throwable("Device not connected")), this)
        return
    }
    callback.onResult(Result(EspPairingEvent.SendingCloudDetails), this)
    sendWifiCredentials(
        wifiDetails.ssid.convert(),
        wifiDetails.bssid.convert(),
        password.convert(), object : EspApi.EspAPICallback<WalabotDeviceDesc?> {
            override fun onSuccess(obj: WalabotDeviceDesc?) {
                obj?.let {
                    updateCloud(it)
                }

            }

            override fun onFailure(throwable: Throwable?) {
                callback.onResult(Result(throwable), this@sendCloudDetails)
            }
        })
}

private fun EspBleApi.updateCloud(deviceDesc: WalabotDeviceDesc) {
    val options = Gen2CloudOptions()
    options.params = cloudCredentials.cloudParams
    sendCloudDetails(options, object : EspApi.EspAPICallback<Void?> {
        override fun onSuccess(obj: Void?) {
            callback.onResult(Result(EspPairingEvent.SentCloudDetails), this@updateCloud)
            if (cloudCredentials.updateCloud) {
                pair(deviceDesc.host ?: "")
            } else {
                reboot()
            }
        }

        override fun onFailure(throwable: Throwable?) {
            callback.onResult(Result(throwable), this@updateCloud)
        }
    })
}

private fun EspBleApi.pair(host: String) {
    callback.onResult(Result(EspPairingEvent.Pairing), null)
    pair(host, cloudCredentials.userId, object : EspApi.EspAPICallback<EspPairingResponse?> {
        override fun onSuccess(obj: EspPairingResponse?) {
            callback.onResult(Result(EspPairingEvent.Paired), this@pair)
            if (cloudCredentials.updateCloud) {
                performParingWithCloud(host, obj?.code)
            } else {
                notifyPairingComplete(host, obj?.code!!)
            }
        }

        override fun onFailure(throwable: Throwable?) {
            callback.onResult(Result(throwable), this@pair)
        }
    })
}

private fun EspBleApi.performParingWithCloud(host: String, code: String?) {
    callback.onResult(Result(EspPairingEvent.StagePairWithCloud), this)
    code?.let { it1 ->
        Connection().pairing(code, cloudCredentials.idToken!!) {
            if (it.isSuccess) {
                callback.onResult(Result(EspPairingEvent.NotifyPairingComplete), this)
                notifyPairingComplete(host, it1)
            } else {
                callback.onResult(Result(Throwable("Failed to pair with the cloud")), this)
            }
        }
    }
}

private fun EspBleApi.notifyPairingComplete(host: String, code: String) {
    notifyPairingComplete(host, cloudCredentials.userId, code, object : EspApi.EspAPICallback<Void?> {
        override fun onSuccess(obj: Void?) {
            callback.onResult(Result(EspPairingEvent.NotifyPairingComplete), this@notifyPairingComplete)
            reboot()
        }

        override fun onFailure(throwable: Throwable?) {
            callback.onResult(Result(throwable), this@notifyPairingComplete)
        }
    })
}

private fun EspBleApi.reboot() {
    callback.onResult(Result(EspPairingEvent.Rebooting), this)
    reboot(object : EspApi.EspAPICallback<Void?> {
        override fun onSuccess(obj: Void?) {
            callback.onResult(Result(EspPairingEvent.Rebooted), this@reboot)
            rebootToFactory()
        }

        override fun onFailure(throwable: Throwable?) {
            callback.onResult(Result(throwable), this@reboot)
        }
    })
}

private fun EspBleApi.rebootToFactory() {
    callback.onResult(Result(EspPairingEvent.RebootingToFactory), this)
    rebootToFactory(object : EspApi.EspAPICallback<Void?> {
        override fun onSuccess(obj: Void?) {
            callback.onResult(Result(EspPairingEvent.RebootedToFactory), this@rebootToFactory)
            stop()
        }

        override fun onFailure(throwable: Throwable?) {
            callback.onResult(Result(throwable), this@rebootToFactory)
        }
    })
}