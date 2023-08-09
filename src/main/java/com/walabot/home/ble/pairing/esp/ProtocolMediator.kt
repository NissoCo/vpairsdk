package com.walabot.home.ble.pairing.esp

import com.google.protobuf.GeneratedMessageV3
import com.walabot.home.ble.Message.AP
import com.walabot.home.ble.sdk.Config

interface ProtocolMediator {
    fun wifiCredentials(
        apSsid: ByteArray?,
        bssid: ByteArray?,
        apPassword: ByteArray?
    ): GeneratedMessageV3?

    fun cloudDetails(cloudOptions: Config): GeneratedMessageV3?
    fun pair(uid: String?): GeneratedMessageV3?
    fun pairingComplete(uid: String?, code: String?): GeneratedMessageV3?
    fun parseResult(data: ByteArray?): MessageResult?
    fun parseWifiResult(data: ByteArray?): WifiResult?
    fun parsePairResult(data: ByteArray?): PairResult?
    fun parseWifiScanResult(data: ByteArray?): WifiScanResult?
    open class MessageResult {
        var result = 0

        // TODO - port this to an enum
        var esp_error = 0
        val isSuccessful: Boolean
            get() = result == 0
    }

    class WifiResult : MessageResult() {
        var ip: String? = null
        var mac: String? = null
    }

    class PairResult : MessageResult() {
        var code: String? = null
    }

    class WifiScanResult : MessageResult() {
        var accessPoints: List<AP>? = null
    }
}