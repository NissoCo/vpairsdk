package com.walabot.home.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.walabot.home.ble.BleService.BleServiceDataCallback
import com.walabot.home.ble.BleService.BleServiceDeviceCallback
import java.util.*

/**
 * Created by tomas on 03-Aug-17.
 */
class WHBle(context: Context?) : BleServiceDataCallback, BleServiceDeviceCallback {
    private val _bleService: BleService
//    val scanner: WalabotHomeDeviceScanner
    var protocolVersion = 0
        private set
    var _dataInChar: BluetoothGattCharacteristic? = null
    var _dataOutChar: BluetoothGattCharacteristic? = null
    private var _connectionCb: WHConnectionCallback? = null
    private var _dataCb: WHDataCallback? = null
    private val _synObject = Any()
    fun connectToDevice(
        device: BluetoothDevice?, connectionCb: WHConnectionCallback?,
        dataCb: WHDataCallback?, connectionTimeoutMs: Long
    ) {
        synchronized(_synObject) {
            _dataInChar = null
            _dataOutChar = null
        }
        _connectionCb = connectionCb
        _dataCb = dataCb
        _bleService.connectToDevice(device!!, this, this, connectionTimeoutMs)
    }

    fun disconnect(device: BluetoothDevice, immediately: Boolean) {
        Log.i(LOG_TAG, "Disconnect " + device.address)
        synchronized(_synObject) {
            _dataInChar = null
            _dataOutChar = null
        }
        _bleService.disconnect(device, immediately)
    }

    val connectedDevice: BluetoothDevice?
        get() = _bleService.connectedDevice
    val isConnectionReady: Boolean
        get() {
            var retVal: Boolean
            synchronized(_synObject) { retVal = connectedDevice != null && _dataOutChar != null }
            return retVal
        }
    val selectedDevice: BluetoothDevice?
        get() = _bleService.selectedDevice

    fun cleanup() {
        synchronized(_synObject) {
            _dataOutChar = null
            _dataInChar = null
        }
        _bleService.cleanup()
    }

    fun requestConnectionPriority(priority: Int): Boolean {
        return _bleService.requestConnectionPriority(priority)
    }

    fun sendData(request: ByteArray?): Boolean {
        var retVal = false
        synchronized(_synObject) {
            if (_dataOutChar != null) {
                retVal = _bleService.writeData(_dataOutChar!!, request!!)
            }
        }
        return retVal
    }

    fun readData(): Boolean {
        var retVal = false
        synchronized(_synObject) {
            if (_dataInChar != null) {
                retVal = _bleService.readData(_dataInChar!!)
            }
        }
        return retVal
    }

    fun byteArrayAsHexString(arr: ByteArray?): String {
        return BleService.byteArrayAsHexString(arr!!)
    }

    override fun onDeviceConnected(gatt: BluetoothGatt?) {
        val walabotService = gatt!!.getService(SERVICE_WALABOT_HOME)
        val device = gatt.device
        if (walabotService != null) {
            _dataInChar = walabotService.getCharacteristic(CHAR_DATA_IN)
            _dataOutChar = walabotService.getCharacteristic(CHAR_DATA_WRITE_V3)
            if (_dataOutChar != null) {
                protocolVersion = 3
            } else {
                _dataInChar = walabotService.getCharacteristic(CHAR_DATA_IN)
                _dataOutChar = walabotService.getCharacteristic(CHAR_DATA_WRITE_V1)
                protocolVersion = 1
            }
        }
        if (_dataOutChar != null) {
            if (_connectionCb != null) {
                val retVal = _bleService.toggleIndication(_dataInChar!!, protocolVersion, true)
                if (retVal) {
                    _connectionCb!!.onDeviceConnected(gatt.device, protocolVersion)
                } else {
                    disconnect(device, true)
                }
            }
        } else {
            if (_connectionCb != null) {
                _connectionCb!!.onDeviceConnectionFailed(device)
            }
        }
    }

    override fun onCharacteristicWrite(
        characteristic: BluetoothGattCharacteristic?,
        data: ByteArray?
    ) {
        _dataCb!!.onWriteSuccess(data)
    }

    override fun onCharacteristicWriteFailed(characteristic: BluetoothGattCharacteristic?) {
        _dataCb!!.onWriteFailed(characteristic!!.value)
    }

    override fun onCharacteristicRead(
        characteristic: BluetoothGattCharacteristic?,
        data: ByteArray?
    ) {
        _dataCb!!.onReadSuccess(data)
    }

    override fun onCharacteristicReadFailed(characteristic: BluetoothGattCharacteristic?) {
        _dataCb!!.onReadFailed()
    }

    override fun onCharacteristicNotificationEnabled(enabled: Boolean) {
        _dataCb!!.onNotificationEnabled(enabled)
    }

    override fun onDeviceDisconnected(device: BluetoothDevice?) {
        _connectionCb!!.onDeviceDisconnected(device)
    }

    override fun onConnectionFailed(device: BluetoothDevice?) {
        _bleService.disconnect(device!!)
        _connectionCb!!.onDeviceConnectionFailed(device)
    }

    fun requestMtu(mtu: Int): Boolean {
        return _bleService.requestMtu(mtu)
    }

    override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
        _connectionCb!!.onMtuChanged(device, mtu)
    }

    val isAdapterOn: Boolean
        get() = _bleService.isAdapterOn

    fun setConnectionTimeout(connectionTimeoutMs: Long) {
        _bleService.setConnectionTimeout(connectionTimeoutMs)
    }

    companion object {
        private const val LOG_TAG = "WHBle"
        val SERVICE_WALABOT_HOME = UUID.fromString("21a07e04-1fbf-4bf6-b484-d319b8282a1c")
        val CHAR_DATA_WRITE_V1 = UUID.fromString("21a07e06-1fbf-4bf6-b484-d319b8282a1c")
        val CHAR_DATA_WRITE_V3 = UUID.fromString("21a07e08-1fbf-4bf6-b484-d319b8282a1c")
        val CHAR_DATA_IN = UUID.fromString("21a07e05-1fbf-4bf6-b484-d319b8282a1c")
    }

    init {
        _bleService = BleService(context!!)
//        scanner = WalabotHomeDeviceScanner(context, SERVICE_WALABOT_HOME)
    }
}