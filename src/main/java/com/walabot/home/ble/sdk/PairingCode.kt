package com.example.vpairsdk_flutter.ble.sdk

import com.example.vpairsdk_flutter.ble.pairing.esp.WalabotDeviceDesc

data class PairingCode(var device: WalabotDeviceDesc, var code: String)