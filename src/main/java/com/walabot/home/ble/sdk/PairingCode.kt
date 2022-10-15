package com.walabot.home.ble.sdk

import com.walabot.home.ble.pairing.esp.WalabotDeviceDesc

data class PairingCode(var device: WalabotDeviceDesc, var code: String)