package com.example.vpairsdk_flutter.ble.sdk

import com.example.vpairsdk_flutter.ble.Message.DevInfo
import com.example.vpairsdk_flutter.ble.pairing.esp.ProtocolMediator
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

fun DevInfo.toJson(): Map<String, Any> {
    return mapOf(
        "devID" to devId,
        "sku" to sku,
        "snRadar" to snRadar,
        "snProduct" to snProduct,
        "hwRevRadar" to hwRevRadar,
        "hwRevProduct" to hwRevProduct,
        "swVer" to swVer,
        "swVerCode" to swVerCode,
        "isProvisionComitted" to isProvisionCommitted
    )
}