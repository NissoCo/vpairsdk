package com.walabot.home.ble.sdk

import com.walabot.home.ble.BleDevice
import com.walabot.home.ble.Result
import com.walabot.home.ble.pairing.esp.EspApi
import com.walabot.home.ble.pairing.esp.EspBleApi
import com.walabot.home.ble.pairing.esp.EspPairingResponse
import com.walabot.home.ble.pairing.esp.WalabotDeviceDesc


fun EspBleApi.connect(bleDevice: BleDevice) {
    callback.onResult(Result(EspPairingEvent.Connecting), this)
    connect(bleDevice, object : EspApi.EspAPICallback<WalabotDeviceDesc?> {
        override fun onSuccess(obj: WalabotDeviceDesc?) {
            callback.onResult(Result(EspPairingEvent.Connected), this@connect)
        }

        override fun onFailure(throwable: Throwable?) {
            callback.onResult(Result<EspPairingEvent?>(throwable).apply { this.result = EspPairingEvent.Connecting }, this@connect)
        }

    })
}

fun EspBleApi.sendCloudDetails(ssid: String, bssid: String, password: String) {
    if (ssid.isEmpty()) {
        callback.onResult(Result(Throwable("SSID can't be empty")), this)
        return
    }
    if (!isConnected) {
        callback.onResult(Result(Throwable("Device not connected")), this)
        return
    }
    callback.onResult(Result(EspPairingEvent.WifiConnecting), this)
    sendWifiCredentials(
        ssid.convert(),
        bssid.convert(),
        password.convert(), object : EspApi.EspAPICallback<WalabotDeviceDesc?> {
            override fun onSuccess(obj: WalabotDeviceDesc?) {
                callback.onResult(Result(EspPairingEvent.WifiConnected), this@sendCloudDetails)
                obj?.let {
                    updateCloud(it)
                }

            }

            override fun onFailure(throwable: Throwable?) {
                callback.onResult(Result<EspPairingEvent?>(throwable).apply { this.result = EspPairingEvent.WifiConnected }, this@sendCloudDetails)
            }
        })
}

private fun EspBleApi.updateCloud(deviceDesc: WalabotDeviceDesc) {
    callback.onResult(Result(EspPairingEvent.SendingCloudDetails), this@updateCloud)
        sendCloudDetails(config, object : EspApi.EspAPICallback<Void?> {
        override fun onSuccess(obj: Void?) {
            callback.onResult(Result(EspPairingEvent.SentCloudDetails), this@updateCloud)
            if (config.updateCloud) {
                pair(deviceDesc.host ?: "")
            } else {
                reboot()
            }
        }

        override fun onFailure(throwable: Throwable?) {
            callback.onResult(Result<EspPairingEvent?>(throwable).apply { this.result = EspPairingEvent.SendingCloudDetails }, this@updateCloud)
        }
    })
}

private fun EspBleApi.pair(host: String) {
    callback.onResult(Result(EspPairingEvent.Pairing), null)
    pair(host, config.userId, object : EspApi.EspAPICallback<EspPairingResponse?> {
        override fun onSuccess(obj: EspPairingResponse?) {
            callback.onResult(Result(EspPairingEvent.Paired), this@pair)
            if (config.updateCloud) {
                performPairingWithCloud(host, obj?.code)
            } else {
                notifyPairingComplete(host, obj?.code!!)
            }
        }

        override fun onFailure(throwable: Throwable?) {
            callback.onResult(Result<EspPairingEvent?>(throwable).apply { this.result = EspPairingEvent.Pairing }, this@pair)
        }
    })
}

private fun EspBleApi.performPairingWithCloud(host: String, code: String?) {
    callback.onResult(Result(EspPairingEvent.StagePairWithCloud), this)
    code?.let { it1 ->
        Connection().pairing(config.apiURL, code, config.accessToken!!) {
            if (it.isSuccess) {
                deviceId = it.getOrNull()?.get("deviceId") as? String
                callback.onResult(Result(EspPairingEvent.NotifyPairingComplete), this)
                notifyPairingComplete(host, it1)
            } else {
                callback.onResult(Result<EspPairingEvent?>(Throwable("Failed to pair with the cloud")).apply { this.result = EspPairingEvent.StagePairWithCloud }, this)
            }
        }
    }
}

private fun EspBleApi.notifyPairingComplete(host: String, code: String) {
    notifyPairingComplete(host, config.userId, code, object : EspApi.EspAPICallback<Void?> {
        override fun onSuccess(obj: Void?) {
            callback.onResult(Result(EspPairingEvent.NotifyPairingComplete), this@notifyPairingComplete)
            reboot()
        }

        override fun onFailure(throwable: Throwable?) {
            callback.onResult(Result<EspPairingEvent?>(throwable).apply { this.result = EspPairingEvent.NotifyPairingComplete }, this@notifyPairingComplete)
        }
    })
}

private fun EspBleApi.reboot() {
//    callback.onResult(Result(EspPairingEvent.Rebooting), this)
    reboot(object : EspApi.EspAPICallback<EspPairingEvent> {
        override fun onSuccess(obj: EspPairingEvent) {
            callback.onResult(Result(obj), this@reboot)
//            rebootToFactory()
        }

        override fun onFailure(throwable: Throwable?) {
            callback.onResult(Result<EspPairingEvent?>(throwable).apply { this.result = EspPairingEvent.Rebooting }, this@reboot)
        }
    })
}

private fun EspBleApi.rebootToFactory() {
    callback.onResult(Result(EspPairingEvent.Rebooting), this)
    rebootToFactory(object : EspApi.EspAPICallback<Void?> {
        override fun onSuccess(obj: Void?) {
            callback.onResult(Result(EspPairingEvent.RebootedToFactory), this@rebootToFactory)
            stop()
        }

        override fun onFailure(throwable: Throwable?) {
            callback.onResult(Result<EspPairingEvent?>(throwable).apply { this.result = EspPairingEvent.Rebooting }, this@rebootToFactory)
        }
    })
}