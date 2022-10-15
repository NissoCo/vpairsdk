package com.walabot.home.ble.pairing

interface DeviceAuth {
    val uid: String?
    val idToken: String?
}