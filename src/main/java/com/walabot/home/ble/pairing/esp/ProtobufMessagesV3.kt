package com.walabot.home.ble.pairing.esp

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.InvalidProtocolBufferException
import com.walabot.home.ble.Message
import com.walabot.home.ble.Message.DevInfo
import com.walabot.home.ble.Message.ToAppMessage2
import com.walabot.home.ble.Message.WifiCredResult3
import com.walabot.home.ble.sdk.Config
import com.walabot.home.ble.sdk.toJson

class ProtobufMessagesV3 : ProtocolMediator {
    override fun wifiCredentials(
        apSsid: ByteArray?,
        bssid: ByteArray?,
        apPassword: ByteArray?
    ): GeneratedMessageV3? {
        return Message.WifiCred2.newBuilder()
            .setSsid(String(apSsid!!))
            .setPass(String(apPassword!!))
            .setMinRssi(-70)
            .build()
    }

    override fun cloudDetails(cloudOptions: Config): GeneratedMessageV3? {
        return Message.CloudDetails2.newBuilder()
            .setHttpUrl(cloudOptions.apiURL)
            .setProjectId(cloudOptions.cloud.projectName)
            .setNtpUrl(cloudOptions.mqtt.ntpUrl)
            .build()
    }

    override fun pair(uid: String?): GeneratedMessageV3? {
        return Message.Pair.newBuilder()
            .setUid(uid)
            .build()
    }

    override fun pairingComplete(uid: String?, code: String?): GeneratedMessageV3? {
        return Message.PairingComplete.newBuilder()
            .setUid(uid)
            .setCode(code)
            .build()
    }

    override fun parseWifiScanResult(data: ByteArray?): ProtocolMediator.WifiScanResult? {
        if (data == null || data.size == 0) {
            return null
        }
        try {
            val m = ToAppMessage2.parseFrom(data)
            if (m.type != Message.ToAppMessageType.AP_SCAN_RESULT) {
                return null
            }
            val scaResult = ProtocolMediator.WifiScanResult()
            scaResult.esp_error = 0
            scaResult.result = m.result.number
            scaResult.accessPoints = m.scan.accessPointList
            if (scaResult.result != 0) {
                scaResult.result += 3000
            }
            return scaResult
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }
        return null
    }

    override fun parseWifiResult(data: ByteArray?): ProtocolMediator.WifiResult? {
        if (data == null || data.isEmpty()) {
            return null
        }
        try {
            val m = WifiCredResult3.parseFrom(data)
            if (m.type != Message.ToAppMessageType.CONNECT_WIFI_RESULT) {
                return null
            }
            val r = ProtocolMediator.WifiResult()
            r.esp_error = m.espError
            r.mac = EspUtil.getMacFromInt64(m.mac)
            r.result = m.result.number
            if (r.result != 0) {
                r.result += 3000
            }
            return r
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }
        return null
    }

    override fun parseResult(data: ByteArray?): ProtocolMediator.MessageResult? {
        if (data == null || data.isEmpty()) {
            return null
        }
        try {
            val m = ToAppMessage2.parseFrom(data)
            val r: ProtocolMediator.MessageResult = ProtocolMediator.PairResult()
            r.esp_error = 0
            r.result = m.result.number
            if (r.result != 0) {
                r.result += 3000
            }
            return r
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }
        return null
    }

    override fun parsePairResult(data: ByteArray?): ProtocolMediator.PairResult? {
        if (data == null || data.size == 0) {
            return null
        }
        try {
            val m = ToAppMessage2.parseFrom(data)
            if (m.type != Message.ToAppMessageType.PAIR_TO_PHONE_RESULT) {
                return null
            }
            val r = ProtocolMediator.PairResult()
            r.esp_error = m.espError
            r.code = m.pair.code
            r.result = m.result.number
            if (r.result != 0) {
                r.result += 3000
            }
            return r
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }
        return null
    }

    override fun parseDevInfoResult(data: ByteArray?): Map<String, Any>? {
        return try {
            DevInfo.parseFrom(data).toJson()
        } catch (exp: Exception) {
            null
        }
    }
}