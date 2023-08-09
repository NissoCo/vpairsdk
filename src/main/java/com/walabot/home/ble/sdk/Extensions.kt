package com.walabot.home.ble.sdk

import android.bluetooth.le.ScanCallback
import com.walabot.home.ble.pairing.esp.EspApi
import com.walabot.home.ble.pairing.esp.EspBleApi
import com.walabot.home.ble.pairing.esp.ProtocolMediator
import java.io.UnsupportedEncodingException

fun String.convert(): ByteArray {
    return try {
        toByteArray(charset("UTF-8"))
    } catch (e: UnsupportedEncodingException) {
        throw IllegalArgumentException("the charset is invalid")
    }
}

class EspWifiItemImpl(
    override val ssid: String,
    override val bssid: String,
    override val rssi: Int
) : EspWifiItem {

}

fun ProtocolMediator.WifiScanResult.convert(): List<EspWifiItem>? {
    return accessPoints?.map {
        EspWifiItemImpl(it.ssid, it.bssid, it.rssi)
    }
}
