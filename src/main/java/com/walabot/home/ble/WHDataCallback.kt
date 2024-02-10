package com.example.vpairsdk_flutter.ble

/**
 * Created by tomas on 06-Aug-17.
 */
interface WHDataCallback {
    fun onReadSuccess(data: ByteArray?)
    fun onReadFailed()
    fun onWriteSuccess(value: ByteArray?)
    fun onWriteFailed(value: ByteArray?)
    fun onNotificationEnabled(enabled: Boolean)
}