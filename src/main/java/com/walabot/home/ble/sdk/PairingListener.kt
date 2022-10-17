package com.walabot.home.ble.sdk

import com.walabot.home.ble.Message
import com.walabot.home.ble.Message.PairResult
import com.walabot.home.ble.Result
import com.walabot.home.ble.pairing.esp.ProtocolMediator

enum class EspPairingEvent {
    Connecting,
    Connected,
    SendingCloudDetails,
    SentCloudDetails,
    Pairing,
    Paired,
    StagePairWithCloud,
    NotifyPairingComplete,
    CheckOTA,
    PerformOTA,
    Rebooting,
    Rebooted,
    RebootingToFactory,
    RebootedToFactory,
    WifiScan
}

interface PairingListener {
    fun onFinish(result: Result<ProtocolMediator.WifiScanResult>)
    fun onStartScan()
    fun onEvent(event: EspPairingEvent)
    fun shouldSelect(wifiList: List<EspWifiItem>)
}