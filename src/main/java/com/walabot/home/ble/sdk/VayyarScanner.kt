package com.example.vpairsdk_flutter.ble.sdk

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import com.example.vpairsdk_flutter.ble.BleDevice
import java.util.UUID

class VayyarScanner(val context: Context, val services: ArrayList<UUID>) {

//    private var handler: Handler? = null
    val devices: HashMap<String, BleDevice> by lazy {
        return@lazy HashMap()
    }

    private var sCallback: ScanCallback? = null

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    fun startScan(scanCallback: (Result<BleDevice>) -> Unit) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter.isEnabled) {
            val filters = services.map {
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(it))
                    .build()
            }
            val build = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()
            sCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    val existingDevice = devices[result?.device?.address]
                    if (existingDevice == null || existingDevice.name == null && result?.device?.name != null) {
                        val device = BleDevice(
                            result?.device?.address,
                            result?.device?.name,
                            result?.scanRecord?.bytes,
                            result?.device
                        )
                        devices[device.address] = device
                        scanCallback(Result.success(device))
                    }

                }

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {


                }

                override fun onScanFailed(errorCode: Int) {
                    scanCallback(Result.failure(Throwable("Scan Failed")))
                }
            }
            bluetoothAdapter.bluetoothLeScanner.startScan(filters, build, sCallback)
//            handler = Handler(Looper.getMainLooper())
//            handler?.postDelayed({
//                bluetoothAdapter.bluetoothLeScanner.stopScan(sCallback)
//                handler?.removeCallbacksAndMessages(null)
//                if (devices.isNotEmpty()) {
//                    scanCallback(Result.success(devices.values.toList()))
//                    devices.clear()
//                } else {
//                    startScan(scanCallback)
//                }
//            }, 3000)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        sCallback?.let {
            val bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter.bluetoothLeScanner.stopScan(it)
        }
//        handler?.removeCallbacksAndMessages(null)
        devices.clear()
    }
}