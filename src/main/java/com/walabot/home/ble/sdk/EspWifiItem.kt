package com.walabot.home.ble.sdk

interface EspWifiItem {
    val ssid: String
    val bssid: String
    val rssi: Int
}
