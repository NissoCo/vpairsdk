package com.walabot.home.ble

import android.content.Context
import android.os.Handler
import android.util.Log
import java.util.*

/**
 * Created by Arbel on 12/08/2019.
 */
class WalabotHomeDeviceScanner(context: Context, serviceUUID: UUID?) {
    private val _serviceUUID: UUID?
    private var _context: Context?
    private val _scanner: BleScanner
    private val _devices: HashMap<String, BleDevice?>
    private var _handler: Handler?
    private var _stopScanRunnable: Runnable? = null
    fun startScan(timeoutInMillis: Int, callback: BleDiscoveryCallback?): Boolean {
        _devices.clear()
        _handler!!.removeCallbacksAndMessages(null)
        _stopScanRunnable = null
        if (timeoutInMillis > 0) {
            _stopScanRunnable = Runnable {
                if (_stopScanRunnable != null) {
                    _scanner.stopScan()
                    _stopScanRunnable = null
                }
            }
            _handler!!.postDelayed(_stopScanRunnable!!, timeoutInMillis.toLong())
        }
        val retVal = _scanner.startScan(object : BleScannerCallback {
            override fun onFoundDevice(device: BleDevice?) {
                val existingDevice = _devices[device!!.address]
                if (existingDevice == null || existingDevice.name == null && device.name != null) {
                    _devices[device.address] = device
                    callback!!.onBleDiscovered(device, _devices.values)
                }
            }

            override fun onScanStopped() {
                _devices.clear()
                callback!!.onBleDiscoveryEnded()
            }
        }, _serviceUUID)
        if (!retVal) {
            callback?.onBleDiscoveryError(-1)
        }
        return retVal
    }

    fun stopScan(): Boolean {
        if (_stopScanRunnable != null) {
            _handler!!.removeCallbacks(_stopScanRunnable!!)
            _stopScanRunnable = null
        }
        _devices.clear()
        return _scanner.stopScan()
    }

    fun cleanup() {
        Log.i(WalabotHomeDeviceScanner::class.java.simpleName, "cleanup")
        stopScan()
        _devices.clear()
        _handler = null
        _context = null
    }

    init {
        Log.i(WalabotHomeDeviceScanner::class.java.simpleName, "init")
        _context = context.applicationContext
        _scanner = BleScanner(context.applicationContext)
        _devices = HashMap()
        _handler = Handler(context.mainLooper)
        _serviceUUID = serviceUUID
    }
}