package com.walabot.home.ble.sdk

import com.walabot.home.ble.Result
import com.walabot.home.ble.pairing.esp.WalabotDeviceDesc
import java.security.Permission

enum class EspPairingEvent(val value: Int) {
    Connecting(11),
    Connected(1),
    SendingCloudDetails(21),
    SentCloudDetails(2),
    Pairing(31),
    Paired(3),
    StagePairWithCloud(4),
    NotifyPairingComplete(5),
    CheckOTA(998),
    PerformOTA(999),
    Rebooting(61),
    Rebooted(6),
    RebootingToFactory(71),
    RebootedToFactory(7),
    WifiScan(8);

    override fun toString(): String {
        return when(value) {
            1 -> "Establishing connection to the device"
            11 -> "Trying to establish connection to the device"
            2 -> "Device connection to Wi - Fi"
            21 -> "Trying to connect device to Wi - Fi"
            3 -> "Connecting the device to your account"
            31 -> "Trying to connect the device to your account"
            4 -> "Cloud pairing"
            5 -> "Notify pairing completed"
            6 -> "Rebooting the device"
            61 -> "Trying to reboot the device"
            7 -> "Rebooting the device to factory"
            71 -> "Trying to reboot the device to factory"
            8 -> "Device is scanning Wifi devices"
            998 -> "Checking for updates"
            999 -> "Updating your device"
            else -> ""
        }
    }
}

interface PairingListener {
    fun onFinish(result: Result<String>)
    fun onEvent(event: EspPairingEvent, deviceId: String? = null)
    fun shouldSelect(wifiList: List<EspWifiItem>)
    fun onMissingPermission(permission: String)
}