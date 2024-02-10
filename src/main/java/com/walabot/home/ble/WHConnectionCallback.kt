package com.example.vpairsdk_flutter.ble

import android.bluetooth.BluetoothDevice

/**
 * Created by tomas on 06-Aug-17.
 */
interface WHConnectionCallback {
    fun onDeviceConnected(device: BluetoothDevice?, version: Int)
    fun onDeviceDisconnected(device: BluetoothDevice?)
    fun onDeviceConnectionFailed(device: BluetoothDevice?)
    fun onMtuChanged(device: BluetoothDevice?, mtu: Int)
}