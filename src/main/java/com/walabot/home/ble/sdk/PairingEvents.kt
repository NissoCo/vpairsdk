package com.walabot.home.ble.sdk

import com.walabot.home.ble.Message.DevInfo
import com.walabot.home.ble.Result
import com.walabot.home.ble.device.DeviceInfo

enum class EspPairingEvent(val value: Int) {
    Connecting(1),
    Connected(2),
    GettingDevInfo(3),
    GotDevInfo(4),
    WifiScan(5),
    WifiList(6),
    WifiConnecting(7),
    WifiConnected(8),
    SendingCloudDetails(9),
    SentCloudDetails(10),
    Pairing(11),
    Paired(12),
    StagePairWithCloud(13),
    NotifyPairingComplete(14),
    CheckOTA(15),
    PerformOTA(16),
    CommittingProvision(17),
    ProvisionCommited(18),
    Rebooting(19),
    RebootedToFactory(20),
    ;

    override fun toString(): String {
        return when(value) {
            1 -> "Trying to establish connection to the device"
            2 -> "Establishing connection to the device"
            3 -> "Trying to fetch device info"
            4 -> "Fetched device info"
            5 -> "Device is scanning Wifi devices"
            6 -> "Parsing wifi list for user"
            7 -> "Trying to connect device to Wi - Fi"
            8 -> "Device connection to Wi - Fi"
            9 -> "Sending cloud details"
            10 -> "Device connecting to Wi-Fi"
            11 -> "Device is pairing"
            12 -> "Connecting the device to your account"
            13 -> "Cloud pairing"
            14 -> "Notify pairing completed"
            15 -> "Checking for updates"
            16 -> "Updating your device"
            17 -> "Committing Provision"
            18 -> "Provision Committed"
            19 -> "Device is rebooting"
            20 -> "Rebooting the device to factory"
            else -> ""
        }
    }
}

interface PairingEvents {
    fun onError(error: Throwable)
    fun onWifiCredentialsFail(wifiList: List<EspWifiItem>)
    fun onFinish(result: Result<String>)
    fun onEvent(
        event: EspPairingEvent,
        isError: Boolean,
        message: String,
        deviceInfo: DevInfo?,
        deviceId: String)
    fun shouldSelect(wifiList: List<EspWifiItem>)
    fun onMissingPermission(permission: String)
}

interface PairingAnalytics {
    fun onAnalyticsEvent(message: String, deviceId: String)
}