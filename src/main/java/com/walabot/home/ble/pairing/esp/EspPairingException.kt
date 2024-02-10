package com.example.vpairsdk_flutter.ble.pairing.esp

import java.lang.Exception

class EspPairingException : Exception {
    var resultCode: Int
        private set
    var errorCode: Int
        private set

    constructor(message: String?, result: Int, code: Int) : super(message) {
        resultCode = result
        errorCode = code
    }

    constructor(
        error: EspPairingErrorType,
        result: OpResult?
    ) : super(error.message) {
        resultCode = 0
        errorCode = 0
        if (result != null) {
            resultCode = result.statusCode
        }
    }

    companion object {
        const val GENERAL_ERROR_CODE = -1
        const val ERROR_CODE_NOT_SENT = -1
        fun fromThrowable(t: Throwable?, type: EspPairingErrorType): EspPairingException {
            if (t is EspPairingException) {
                return t
            }
            val resultCode = -1
            val errorCode = -1
            return EspPairingException(type.message, resultCode, errorCode)
        }
    }
}