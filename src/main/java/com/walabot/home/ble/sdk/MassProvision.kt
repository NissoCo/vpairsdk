package com.walabot.home.ble.sdk

import android.content.Context
import android.net.wifi.WifiInfo
import com.walabot.home.ble.Result
import com.walabot.home.ble.WHBle
import com.walabot.home.ble.pairing.WifiNetworkMonitor
import com.walabot.home.ble.pairing.esp.EspApi
import com.walabot.home.ble.pairing.esp.EspBleApi
import com.walabot.home.ble.pairing.esp.WalabotDeviceDesc

class MassProvision: VPairSDK() {

    var pickedWifiCredentials: EspWifiItem? = null
    var pickedWifiPassword: String? = null
    var context: Context? = null

    override fun startPairing(context: Context, cloudCredentials: CloudCredentials) {
        if (currentWifi == null) {
            wifiMonitor = WifiNetworkMonitor(context)
            this.cloudCredentials = cloudCredentials
            this.context = context
        }
        pairingApi = EspBleApi(WHBle(context))
        listener?.onEvent(EspPairingEvent.Connecting)
        pairingApi?.connect(object : EspApi.EspAPICallback<WalabotDeviceDesc?> {
            override fun onSuccess(obj: WalabotDeviceDesc?) {
                listener?.onEvent(EspPairingEvent.Connected)
                currentWifi?.let {
                    resumeConnection(pickedWifiCredentials!!, pickedWifiPassword!!)
                } ?: kotlin.run {
                    wifiMonitor?.scanEvents = object : WifiNetworkMonitor.Scan {
                        override fun onNetworkStateChange(info: WifiInfo?) {
                            wifiMonitor?.stopScan()
                            if (info != null) {
                                val cleanName = info.ssid.replace("\"", "")
                                currentWifi = EspWifiItemImpl(cleanName, info.bssid, info.rssi)
                            }
                            refreshWifiList()
                        }

                    }
                    wifiMonitor?.startScanWifi()
                }
                connectedDevice = obj
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    override fun resumeConnection(selectedWifiDetails: EspWifiItem, password: String) {
        if (pickedWifiCredentials == null) {
            pickedWifiCredentials = selectedWifiDetails
            pickedWifiPassword = password
        }
        super.resumeConnection(selectedWifiDetails, password)
    }

    override fun rebootToFactory(deviceId: String) {
        listener?.onEvent(EspPairingEvent.RebootingToFactory, deviceId)
        pairingApi?.rebootToFactory(object : EspApi.EspAPICallback<Void?> {
            override fun onSuccess(obj: Void?) {
                listener?.onEvent(EspPairingEvent.RebootedToFactory, deviceId)
                context?.let {
                    pairingApi?.stop()
                    startPairing(it, cloudCredentials)
                }
            }

            override fun onFailure(throwable: Throwable?) {
                listener?.onFinish(Result(throwable))
            }
        })
    }

    override fun stopPairing() {
        super.stopPairing()
        context = null
    }
}