package com.example.vpairsdk_flutter.ble

import BleDataHeader
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import checkHeaderMessage
import flatMessageBuffer
import requestMtuSafe
import split
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Created by tomas on 03-Aug-17.
 */
//TODO: This class need a major reorganization
class BleService(private val _context: Context) {
    private val _mainThreadHandler: Handler
    var selectedDevice: BluetoothDevice? = null
        private set
    private var _gattClients: HashMap<String, BluetoothGatt>? = null
    private val _gattCallback: BluetoothGattCallback?
    private var _connectionCb: BleServiceDeviceCallback? = null
    private var _dataCb: BleServiceDataCallback? = null
    private val _autoConnect = true
    private var _connectionTimeoutMs: Long = 0
    private var _abortConnectionRunnable: Runnable? = null
    private val _messageBufferQueue: Queue<ByteArray>
    private var _mtu: Int
    private var _dataHeader: BleDataHeader? = null
    private var _protocolVersion = 3
    var _mtuChanged = false
    fun connectToDevice(
        device: BluetoothDevice,
        connectionCb: BleServiceDeviceCallback?,
        dataCb: BleServiceDataCallback?,
        connectionTimeoutMs: Long
    ) {
        scheduleConnectionTimeout(device, connectionTimeoutMs)
        _connectionCb = connectionCb
        _dataCb = dataCb
        selectedDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.address)
        if (_gattClients == null) {
            _gattClients = HashMap()
        }
        removeAllDevicesExcept(device, false)
        val gatt = _gattClients!![device.address]
        if (gatt != null) {
            if (!isConnected(gatt)) {
                _mainThreadHandler.post {
                    try {
                        gatt.close()
                        gatt.disconnect()
                        removeGatt(device.address)
                        addGatt(connectGatt(device))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        connectToDevice(device, _connectionCb, _dataCb, connectionTimeoutMs)
                    }
                }
            }
        } else {
            _mainThreadHandler.post {
                var newGatt = connectGatt(device)
                if (newGatt == null) {
                    newGatt = connectGatt(device)
                }
                addGatt(newGatt)
            }
        }
    }

    private fun cancelConnectionTimeout() {
        if (_abortConnectionRunnable != null) {
            _mainThreadHandler.removeCallbacks(_abortConnectionRunnable!!)
            _abortConnectionRunnable = null
        }
    }

    private fun scheduleConnectionTimeout(device: BluetoothDevice?, timeoutMs: Long) {
        _connectionTimeoutMs = timeoutMs
        cancelConnectionTimeout()
        if (timeoutMs > 0) {
            _abortConnectionRunnable = Runnable {
                Log.i(LOG_TAG, "Connection timeout expired")
                if (device == selectedDevice) {
                    val selectedDevice = selectedDevice
                    this.selectedDevice = null
                    removeAllDevicesExcept(null, true)
                    if (_connectionCb != null) _connectionCb!!.onConnectionFailed(selectedDevice)
                }
            }
            _mainThreadHandler.postDelayed(_abortConnectionRunnable!!, _connectionTimeoutMs)
        }
    }

    private fun connectGatt(device: BluetoothDevice?): BluetoothGatt? {
        var gatt: BluetoothGatt? = null
        if (device != null && selectedDevice != null) {
            gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(_context, false, _gattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(_context, false, _gattCallback)
            }
        }
        Log.i(
            LOG_TAG,
            "connectGatt to mac: " + (if (device != null) device.address else "Not device") + " currently selected device: " + if (selectedDevice != null) selectedDevice!!.address else " No current device"
        )
        return gatt
    }

    private fun isConnected(gatt: BluetoothGatt): Boolean {
        val dev = gatt.device
        if (dev != null) {
            val state =
                (_context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).getConnectionState(
                    dev,
                    BluetoothProfile.GATT
                )
            return state == BluetoothProfile.STATE_CONNECTED || state == BluetoothProfile.STATE_CONNECTING
        }
        return false
    }

    private fun removeAllDevicesExcept(device: BluetoothDevice?, forceClose: Boolean) {
        for (gatt in _gattClients!!.values) {
            if (device == null || gatt.device.address != device.address) {
                try {
                    _mainThreadHandler.post {
                        try {
                            Log.i(
                                LOG_TAG,
                                "Disconnecting gatt mac: " + gatt.device.address
                            )
                            gatt.disconnect()
                            if (forceClose) {
                                Log.i(
                                    LOG_TAG,
                                    "Forcibly closing gatt"
                                )
                                gatt.close()
                                _gattClients!!.remove(gatt.device.address)
                            }
                            /*
							if (Build.MANUFACTURER.toLowerCase().contains("samsung") && Build.VERSION.SDK_INT <= 21)
							{
								//SureFlapBle.getInstance().getLogger().log(Log.INFO, BleService.class.getSimpleName(), "Closing gatt mac: " + newGatt.getDevice().getAddress());
								//newGatt.close();
								//_gattClients.remove(newGatt.getDevice().getAddress());
							}
							*/
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * @param connectionPriority - can be one of [.CONNECTION_PRIORITY_LOW] [.CONNECTION_PRIORITY_BALANCED] #CONNECTION_PRIORITY_HIGH
     * @return
     */
    fun requestConnectionPriority(connectionPriority: Int): Boolean {
        if (Build.VERSION.SDK_INT >= 21) {
            val device = connectedDevice
            if (device != null) {
                val gatt = _gattClients!![device.address]
                if (gatt != null) {
                    return gatt.requestConnectionPriority(connectionPriority)
                }
            }
        }
        return false
    }

    fun requestMtu(mtu: Int): Boolean {
        if (Build.VERSION.SDK_INT >= 21) {
            val device = connectedDevice
            if (device != null) {
                val gatt = _gattClients!![device.address]
                if (gatt != null) {
                    gatt.requestMtu(mtu)
                    return true
                }
            }
        }
        return false
    }

    val isAdapterOn: Boolean
        get() = BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled

    fun setConnectionTimeout(connectionTimeoutMs: Long) {
        cancelConnectionTimeout()
        scheduleConnectionTimeout(selectedDevice, connectionTimeoutMs)
    }

    /**
     * The implementation for the various Bluetooth Gatt callbacks for handling
     * sensor connection and passed data management.
     */
    internal inner class GattCallbacks : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.i(
                LOG_TAG,
                "onConnectionStateChange mac: " + gatt.device.address + " " + "status: " + status + " newState " + newState
            )
            if (newState == BluetoothGatt.STATE_DISCONNECTED && status == BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED) {
                // This is an error that occurs in some devices because auto
                // connection, if we're here we should try to reconnect
                _mainThreadHandler.post(object : Runnable {
                    override fun run() {
                        _mtuChanged = false
                        _messageBufferQueue.clear()
                        val device = gatt.device
                        gatt.disconnect()
                        gatt.close()
                        if (_gattClients != null) {
                            val oldGatt = removeGatt(device.address)
                            oldGatt!!.close()
                        }
                        if (selectedDevice != null && device.address == selectedDevice!!.address) {
                            addGatt(connectGatt(device))
                        }
                    }
                })
            } else if (newState == BluetoothGatt.STATE_CONNECTED) {
                _mtuChanged = false
                _mainThreadHandler.post(object : Runnable {
                    override fun run() {
                        _messageBufferQueue.clear()
                        _dataHeader = null
                        if (selectedDevice != null && gatt.device.address == selectedDevice!!.address) {
                            removeAllDevicesExcept(selectedDevice, false)
                            if (gatt.discoverServices()) {
                                Log.i(
                                    LOG_TAG,
                                    "Discovering Gatt Services for " + gatt.device.address
                                )
                            } else {
                                cancelConnectionTimeout()
                                Log.i(
                                    LOG_TAG,
                                    "Error discovering Services for " + gatt.device.address
                                )
                            }
                        } else {
                            cancelConnectionTimeout()
                            gatt.disconnect()
                        }
                    }
                })
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED || newState == BluetoothGatt.STATE_DISCONNECTING) {
                _mainThreadHandler.post(object : Runnable {
                    override fun run() {
                        _mtuChanged = false
                        _messageBufferQueue.clear()
                        val device = gatt.device
                        if (status == 133) {
                            if (selectedDevice != null && device.address == selectedDevice!!.address) {
                                if (_gattCallback != null && selectedDevice != null && device.address == selectedDevice!!.address && BluetoothAdapter.getDefaultAdapter().isEnabled) {
                                    //In case the autoconnection is false we can try and connect when device is not in range
                                    if (_autoConnect) {
                                        val retVal = gatt.connect()
                                        Log.i(
                                            LOG_TAG,
                                            "status 133 trying again, gattConnect autoconnect: $retVal"
                                        )
                                        addGatt(gatt)
                                    } else {
                                        cancelConnectionTimeout()
                                        val gClient = removeGatt(device.address)
                                        gClient?.close()
                                        _connectionCb!!.onConnectionFailed(selectedDevice)
                                        //In case autoconnection is true we just use this section
                                        //addGatt(connectGatt(device));
                                    }
                                }
                            }
                        } else {
                            //A disconnection occurred, might be due to explicit request to disconnect
                            Log.i(LOG_TAG, "Closing gatt mac: " + gatt.device.address)
                            gatt.close()
                            val gClient = removeGatt(device.address)
                            gClient?.close()
                            if (selectedDevice != null && gatt.device.address == selectedDevice!!.address) {
                                //Unplanned disconnection
                                _mainThreadHandler.post(object : Runnable {
                                    override fun run() {
                                        if (_connectionCb != null) {
                                            _connectionCb!!.onDeviceDisconnected(selectedDevice)
                                        }
                                    }
                                })
                            }
                        }
                    }
                })
            }
            super.onConnectionStateChange(gatt, status, newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.i(LOG_TAG, "onServicesDiscovered for " + gatt.device.address)
            cancelConnectionTimeout()
            addGatt(gatt)
            requestMtuSafe(REQUESTED_MTU)
            _mainThreadHandler.postDelayed({
                if (!_mtuChanged) {
                    _connectionCb!!.onDeviceConnected(gatt)
                }
            }, 3100L)
            super.onServicesDiscovered(gatt, status)
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (_connectionCb != null) {
                _mtu = mtu
                _mtuChanged = true
                _connectionCb!!.onMtuChanged(gatt.device, mtu)
                _mainThreadHandler.post { _connectionCb!!.onDeviceConnected(gatt) }
                Log.d(LOG_TAG, "mtu changed: $_mtu")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.i(
                LOG_TAG,
                "onCharacteristicChanged mac: " + gatt.device.address + " char uuid: " + characteristic.uuid + " value " + byteArrayAsHexString(
                    characteristic.value
                )
            )
            if (selectedDevice != null && selectedDevice!!.address == gatt.device.address) {
                val data = characteristic.value
                if (data != null) {
                    if (_dataCb != null) {
                        if (_protocolVersion == 1) {
                            handleProtocol1CharacteristicChanged(gatt, characteristic, data)
                        } else {
                            handleProtocol3CharacteristicChanged(gatt, characteristic, data)
                        }
                    }
                }
            } else {
                gatt.disconnect()
            }
            super.onCharacteristicChanged(gatt, characteristic)
        }

        private fun handleProtocol1CharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            data: ByteArray
        ) {
            _dataCb!!.onCharacteristicRead(characteristic, data)
        }

        private fun handleProtocol3CharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            data: ByteArray
        ) {
            if (_dataHeader == null) {
                val header = checkHeaderMessage(data)
                if (header != null) {
                    _messageBufferQueue.clear()
                    _dataHeader = header
                }
            } else {
                _messageBufferQueue.add(data)
                if (_messageBufferQueue.size < _dataHeader!!.chunks) {
                    Log.d(LOG_TAG, "onCharacteristicChanged, chunk: " + _messageBufferQueue.size)
                } else {
                    val finalResult = flatMessageBuffer(_messageBufferQueue)
                    _messageBufferQueue.clear()
                    _dataHeader = null
                    _dataCb!!.onCharacteristicRead(characteristic, finalResult)
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.i(
                LOG_TAG,
                "onCharacteristicRead mac: " + gatt.device.address + "  uuid:" + characteristic.uuid + " " + status
            )
            super.onCharacteristicRead(gatt, characteristic, status)
            if (selectedDevice != null && selectedDevice!!.address == gatt.device.address && _dataCb != null) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val data = characteristic.value
                    Log.i(LOG_TAG, "characteristicData: " + byteArrayAsHexString(data))
                    _dataCb!!.onCharacteristicRead(characteristic, data)
                } else {
                    _dataCb!!.onCharacteristicReadFailed(characteristic)
                }
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            Log.i(
                LOG_TAG,
                "onDescriptorWrite mac: " + gatt.device.address + " uuid: " + descriptor.uuid + " " + status
            )
            super.onDescriptorWrite(gatt, descriptor, status)
            if (descriptor.uuid == DESC_CLIENT_CHAR_CONFIG) {
                _dataCb!!.onCharacteristicNotificationEnabled(
                    !Arrays.equals(
                        descriptor.value,
                        BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                    )
                )
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.i(
                LOG_TAG,
                "onCharacteristicWrite mac: " + gatt.device.address + " uuid: " + characteristic.uuid + " " + status
            )
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (selectedDevice != null && selectedDevice!!.address == gatt.device.address && _dataCb != null) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (_protocolVersion == 3 && !_messageBufferQueue.isEmpty()) {
                        writeDataInternal(characteristic, _messageBufferQueue.peek(), true)
                    } else {
                        _dataCb!!.onCharacteristicWrite(characteristic, characteristic.value)
                    }
                } else {
                    _dataCb!!.onCharacteristicWriteFailed(characteristic)
                }
            }
        }
    }

    @Synchronized
    private fun addGatt(gatt: BluetoothGatt?) {
        if (gatt != null) {
            if (_gattClients == null) {
                _gattClients = HashMap()
            }
            _gattClients!![gatt.device.address] = gatt
        }
    }

    @Synchronized
    private fun removeGatt(addr: String?): BluetoothGatt? {
        if (addr != null) {
            if (_gattClients != null) {
                var retVal: BluetoothGatt? = null
                try {
                    retVal = _gattClients!!.remove(addr)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return retVal
            }
        }
        return null
    }

    @JvmOverloads
    fun disconnect(device: BluetoothDevice, force: Boolean = false) {
        cancelConnectionTimeout()
        if (selectedDevice != null && selectedDevice!!.address == device.address) {
            selectedDevice = null
            removeAllDevicesExcept(null, force)
        }
    }

    val connectedDevice: BluetoothDevice?
        get() {
            var connectedDevice: BluetoothDevice? = null
            if (selectedDevice != null) {
                val gattClient = _gattClients!![selectedDevice!!.address]
                if (gattClient != null && isConnected(gattClient)) {
                    connectedDevice = gattClient.device
                }
            }
            return connectedDevice
        }

    fun cleanup() {
        removeAllDevicesExcept(null, true)
    }

    fun readData(characteristic: BluetoothGattCharacteristic): Boolean {
        Log.i(LOG_TAG, "readData")
        var retVal = false
        val device = connectedDevice
        if (device != null && characteristic != null) {
            val gatt = _gattClients!![device.address]
            if (gatt != null) {
                retVal = gatt.readCharacteristic(characteristic)
            }
        }
        Log.i(
            LOG_TAG,
            "readData from characteristic: " + characteristic.uuid + " retVal: " + retVal
        )
        return retVal
    }

    fun writeDataNoResponse(characteristic: BluetoothGattCharacteristic, data: ByteArray): Boolean {
        return writeDataInternal(characteristic, data, false)
    }

    fun writeData(characteristic: BluetoothGattCharacteristic, data: ByteArray): Boolean {
        return writeDataInternal(characteristic, data, true)
    }

    fun toggleNotification(characteristic: BluetoothGattCharacteristic, enable: Boolean): Boolean {
        return toggleNotificationIndicationInternal(characteristic, enable, false)
    }

    fun toggleIndication(
        characteristic: BluetoothGattCharacteristic,
        protocolVersion: Int,
        enable: Boolean
    ): Boolean {
        _protocolVersion = protocolVersion
        return toggleNotificationIndicationInternal(characteristic, enable, true)
    }

    private fun writeDataInternal(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray,
        withResponse: Boolean
    ): Boolean {
        var retVal = false
        characteristic.writeType =
            if (withResponse) BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT else BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        Log.i(LOG_TAG, "sendData")
        val device = connectedDevice
        if (device != null) {
            Log.i(
                LOG_TAG,
                "	target mac: " + device.address + " data: " + byteArrayAsHexString(data)
            )
            val gatt = _gattClients!![device.address]
            if (gatt != null) {
                retVal = if (_protocolVersion == 1) {
                    handleProtocol1Write(gatt, characteristic, data, withResponse)
                } else {
                    handleProtocol3Write(gatt, characteristic, data, withResponse)
                }
            }
        }
        Log.i(
            LOG_TAG,
            "Send data success: " + retVal + if (device != null) " Connected device $device" else " No connected Device"
        )
        return retVal
    }

    private fun handleProtocol1Write(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray,
        withResponse: Boolean
    ): Boolean {
        Log.d(LOG_TAG, String.format("writeDataInternal(v1), current mtu: %s", _mtu))
        characteristic.value = data
        return gatt.writeCharacteristic(characteristic)
    }

    private fun handleProtocol3Write(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray,
        withResponse: Boolean
    ): Boolean {
        if (_messageBufferQueue.isEmpty()) {
            _messageBufferQueue.addAll(data.split(_mtu - 4))
        }
        Log.d(
            LOG_TAG,
            String.format(
                "writeDataInternal(v3), current mtu: %s, number of chunks: %s",
                _mtu,
                _messageBufferQueue.size
            )
        )
        characteristic.value = _messageBufferQueue.poll()
        return gatt.writeCharacteristic(characteristic)
    }

    private fun toggleNotificationIndicationInternal(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean,
        indicate: Boolean
    ): Boolean {
        var retVal = false
        val device = connectedDevice
        if (device != null) {
            val gatt = _gattClients!![device.address]
            if (gatt != null) {
                retVal = gatt.setCharacteristicNotification(characteristic, enable)
                if (retVal) {
                    val descriptor = characteristic.getDescriptor(DESC_CLIENT_CHAR_CONFIG)
                    if (descriptor != null) {
                        retVal =
                            descriptor.setValue(if (enable) if (indicate) BluetoothGattDescriptor.ENABLE_INDICATION_VALUE else BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
                        if (retVal) {
                            retVal = gatt.writeDescriptor(descriptor)
                        }
                    }
                }
            }
        }
        return retVal
    }

    interface BleServiceDeviceCallback {
        fun onDeviceDisconnected(device: BluetoothDevice?)
        fun onDeviceConnected(gatt: BluetoothGatt?)
        fun onConnectionFailed(device: BluetoothDevice?)
        fun onMtuChanged(device: BluetoothDevice?, mtu: Int)
    }

    interface BleServiceDataCallback {
        fun onCharacteristicWrite(characteristic: BluetoothGattCharacteristic?, data: ByteArray?)
        fun onCharacteristicWriteFailed(characteristic: BluetoothGattCharacteristic?)
        fun onCharacteristicRead(characteristic: BluetoothGattCharacteristic?, data: ByteArray?)
        fun onCharacteristicReadFailed(characteristic: BluetoothGattCharacteristic?)

        //void onCharacteristicNotify(BluetoothGattCharacteristic characteristic, byte[] data);
        fun onCharacteristicNotificationEnabled(enabled: Boolean)
    }

    companion object {
        private const val LOG_TAG = "BleService"
        private val DESC_CLIENT_CHAR_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        private const val REQUESTED_MTU = 256
        val CONNECTION_PRIORITY_LOW =
            if (Build.VERSION.SDK_INT >= 21) BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER else 2
        val CONNECTION_PRIORITY_BALANCED =
            if (Build.VERSION.SDK_INT >= 21) BluetoothGatt.CONNECTION_PRIORITY_BALANCED else 0
        val CONNECTION_PRIORITY_HIGH =
            if (Build.VERSION.SDK_INT >= 21) BluetoothGatt.CONNECTION_PRIORITY_HIGH else 1

        @JvmStatic
		fun byteArrayAsHexString(arr: ByteArray): String {
            val sb = StringBuilder()
            for (b in arr) {
                sb.append(String.format("%02X ", b))
            }
            return sb.toString()
        }
    }

    init {
        _mainThreadHandler = Handler(_context.mainLooper)
        _gattCallback = GattCallbacks()
        _messageBufferQueue = ConcurrentLinkedQueue()
        _mtu = 24
    }
}