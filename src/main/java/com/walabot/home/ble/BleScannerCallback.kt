package com.example.vpairsdk_flutter.ble

/**
 * Created by Arbel on 12/08/2019.
 */
interface BleScannerCallback {
    fun onFoundDevice(device: BleDevice?)
    fun onScanStopped()
}