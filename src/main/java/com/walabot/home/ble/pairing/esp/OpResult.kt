package com.example.vpairsdk_flutter.ble.pairing.esp

import com.walabot.home.ble.pairing.esp.EspApi

// TODO - this class needs to be refactored out, as code changed, and is no longer relevant
open class OpResult(var statusCode: Int, var data: ByteArray? = null) {
    val isSuccessful: Boolean
        get() = statusCode == EspApi.ESP_RESULT_OK
}