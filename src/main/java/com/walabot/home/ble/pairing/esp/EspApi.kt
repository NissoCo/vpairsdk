package com.walabot.home.ble.pairing.esp

import com.google.protobuf.ByteString
import com.walabot.home.ble.Message.ToDeviceMessageType
import com.google.protobuf.GeneratedMessageV3
import com.walabot.home.ble.Message.ToDeviceMessage
import com.walabot.home.ble.Message.ToAppMessage
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Parser
import com.walabot.home.ble.pairing.Gen2CloudOptions

interface EspApi {
    //	void connect(byte[] apSsid, byte[] apBssid, byte[] apPassword, EspAPICallback<WalabotDeviceDesc> cb);
    val isConnected: Boolean
    val deviceDescriptor: WalabotDeviceDesc?
    fun connect(cb: EspAPICallback<WalabotDeviceDesc?>?)
    fun sendWiFiScanRequest(cb: EspAPICallback<ProtocolMediator.WifiScanResult?>?)
    fun sendWifiCredentials(
        ssid: ByteArray?,
        bssid: ByteArray?,
        password: ByteArray?,
        cb: EspAPICallback<WalabotDeviceDesc?>?
    )

    fun sendCloudDetails(cloudOptions: Gen2CloudOptions?, cb: EspAPICallback<Void?>?)
    fun pair(hostAddress: String?, uid: String?, callback: EspAPICallback<EspPairingResponse?>?)
    fun notifyPairingComplete(
        hostAddress: String?,
        uid: String?,
        code: String?,
        cb: EspAPICallback<Void?>?
    )

    fun checkOta(cb: EspAPICallback<CheckOtaResult?>?)
    fun performOta(versionCode: Int, cb: EspAPICallback<Void?>?)
    fun reboot(cb: EspAPICallback<Void?>?)
    fun rebootToFactory(cb: EspAPICallback<Void?>?)
    fun stop()
    interface EspAPICallback<T> {
        fun onSuccess(obj: T)
        fun onFailure(throwable: Throwable?)
    }

    class CheckOtaResult(
        val isHasNewVersion: Boolean,
        val currentVersionCode: Int,
        val newVersionCode: Int
    )

    companion object {
        fun generateOutGoingMessage(
            type: ToDeviceMessageType?,
            payload: GeneratedMessageV3?
        ): ByteArray? {
            val builder = ToDeviceMessage.newBuilder()
                .setType(type)
            if (payload != null) {
                builder.payload = payload.toByteString()
            }
            return builder.build().toByteArray()
        }

        fun parseMessage(data: ByteArray?): ToAppMessage? {
            try {
                return ToAppMessage.parseFrom(data)
            } catch (e: InvalidProtocolBufferException) {
                e.printStackTrace()
            }
            return null
        }

        fun <T> parse(cls: Parser<T>, data: ByteString?): T? {
            try {
                return cls.parseFrom(data)
            } catch (e: InvalidProtocolBufferException) {
                e.printStackTrace()
            }
            return null
        }

        const val ESP_RESULT_OK = 0
        const val ESP_RESULT_V1_OK = 0
        const val ESP_RESULT_V1_INVALID_STATE = 1001 // unused
        const val ESP_RESULT_V1_ESP_ERROR = 1002 // generic error
        const val ESP_RESULT_V1_WIFI_WRONG_CREDENTIALS = 1003 // WIFI
        const val ESP_RESULT_V1_AUTHORIZED = 1004 // unused
        const val ESP_RESULT_V1_SERVER_NOT_FOUND = 1005 // unused
        const val ESP_RESULT_V1_NO_OTA_FOUND = 1006 // unused
        const val ESP_RESULT_V1_NETWORK_IS_LOCAL_ONLY = 1010 // INTERNET
        const val ESP_RESULT_V1_COULD_NOT_UPDATE_TIME = 1011 // WIFI
        const val ESP_RESULT_V1_UNAUTHORIZED = 1012 // not used yet?
        const val ESP_RESULT_V1_PAIR_CODE_FAIL = 1013 // not used yet?
        const val ESP_RESULT_V1_PAIR_CODE_DELETE_FAIL = 1014 // not used yet?
        const val ESP_RESULT_V3_SUCCESS = 0
        const val ESP_RESULT_V3_INVALID_REQUEST = 3001
        const val ESP_RESULT_V3_BLE_SIZE_ERROR = 3002
        const val ESP_RESULT_V3_WIFI_CREDENTIALS_ERROR = 3011
        const val ESP_RESULT_V3_LOW_WIFI_SIGNAL = 3012
        const val ESP_RESULT_V3_WIFI_LOCAL_ONLY = 3013
        const val ESP_RESULT_V3_NTP_SERVER_ERROR = 3021
        const val ESP_RESULT_V3_READING_PAIR_CODE_FAILED = 3031

        // BLE errors
        const val ERR_UNKNOWN = -1
        const val ERR_DISCONNECTED = -2
        const val ERR_WRITE_FAILED = -3
        const val ERR_READ_FAILED = -4
        const val ERR_MESSAGE_PARSING_FAILED = -5
    }
}