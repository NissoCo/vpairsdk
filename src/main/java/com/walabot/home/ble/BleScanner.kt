package com.example.vpairsdk_flutter.ble

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.example.vpairsdk_flutter.ble.BleScannerCallback
import java.util.*

/**
 * Created by Arbel on 12/08/2019.
 */
class BleScanner(private val _context: Context) {
    private var _scannerCallback: ScanCallback? = null
    private var _scannerCallbackCompat: LeScanCallback? = null
    private var _bkeScannerCallback: BleScannerCallback? = null
    @Synchronized
    fun startScan(cb: BleScannerCallback?, serviceUUID: UUID?): Boolean {
        _bkeScannerCallback = cb
        val bluetoothManager =
            _context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        var retVal = false
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    retVal = toggleSensorScan(true, serviceUUID)
                } else {
                    if (_scannerCallbackCompat == null) {
                        _scannerCallbackCompat = LeScanCallback { bluetoothDevice, i, scanRecord ->
                            onDeviceFound(
                                bluetoothDevice,
                                scanRecord
                            )
                        }
                        retVal = if (serviceUUID != null) {
                            bluetoothAdapter.startLeScan(
                                arrayOf(serviceUUID),
                                _scannerCallbackCompat
                            )
                        } else {
                            bluetoothAdapter.startLeScan(_scannerCallbackCompat)
                        }
                    }
                }
            } else {
            }
        }
        return retVal
    }

    private fun onDeviceFound(bluetoothDevice: BluetoothDevice, scanRecord: ByteArray?) {
        Log.i(
            BleScanner::class.java.simpleName,
            "Found device " + bluetoothDevice.name + " " + bluetoothDevice.address
        )
        if (scanRecord != null && scanRecord.size > 0) {
            //parseScanRecord(scanRecord);
            if (_bkeScannerCallback != null) {
                _bkeScannerCallback!!.onFoundDevice(
                    BleDevice(
                        bluetoothDevice.address,
                        bluetoothDevice.name,
                        scanRecord,
                        bluetoothDevice
                    )
                )
            }
        }
    }

    @Synchronized
    fun stopScan(): Boolean {
        var retVal = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = toggleSensorScan(false, null)
        } else {
            val bluetoothManager =
                _context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdater = bluetoothManager.adapter
            if (bluetoothAdater != null && _scannerCallbackCompat != null) {
                bluetoothAdater.stopLeScan(_scannerCallbackCompat)
                _scannerCallbackCompat = null
                retVal = true
            }
        }
        if (retVal && _bkeScannerCallback != null) {
            _bkeScannerCallback!!.onScanStopped()
        }
        return retVal
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun toggleSensorScan(enable: Boolean, serviceUUID: UUID?): Boolean {
        var retVal = false
        val scanner =
            (_context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeScanner
        if (scanner != null) {
            if (enable) {
                if (_scannerCallback == null) {
                    _scannerCallback = object : ScanCallback() {
                        override fun onScanResult(callbackType: Int, result: ScanResult) {
                            if (result != null) {
                                onDeviceFound(result.device, result.scanRecord!!.bytes)
                            }
//                            super.onScanResult(callbackType, result)
                        }

                        override fun onBatchScanResults(results: List<ScanResult>) {
                            if (results != null) {
                                Log.i("TOM", "ScanResults " + results.size)
                            }
//                            super.onBatchScanResults(results)
                        }

                        override fun onScanFailed(errorCode: Int) {
//                            super.onScanFailed(errorCode)
                        }
                    }
                    val filter = ArrayList<ScanFilter>()
                    val builder = ScanFilter.Builder()
                    if (serviceUUID != null) {
                        builder.setServiceUuid(ParcelUuid(serviceUUID))
                    }
                    filter.add(builder.build())
                    val build =
                        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build()
                    scanner.startScan(filter, build, _scannerCallback)
                    retVal = true
                }
            } else {
                if (_scannerCallback != null) {
                    scanner.stopScan(_scannerCallback)
                    _scannerCallback = null
                }
                retVal = true
            }
        }
        return retVal
    }
}