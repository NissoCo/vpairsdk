package com.walabot.home.ble.pairing

import com.walabot.home.ble.config.DeviceConfig
import com.walabot.home.ble.config.DeviceConfigList
import com.walabot.home.ble.device.DeviceInfo
import com.walabot.home.ble.device.PairedDevice
import com.walabot.home.ble.device.PairedDevicesList
import com.walabot.home.ble.device.UpdatedPairedDevice

data class PairingParams(var idToken: String? = null, var code: String? = null, var uid: String? = null, var deviceId: String? = null)


interface DevicePairingEvents {
//    fun pair(idToken: String, code: String, callback: (PairingResponse) -> Unit)
//    fun unpair(uid: String, idToken: String, deviceId: String, callback: (String) -> Unit)
//    fun updatePairedDeviceInfo(uid: String, String deviceId, UpdatedPairedDevice device, String idToken)
//    fun updatePairedDeviceRoomType(String uid, String deviceId, UpdatedPairedDevice device, String idToken)
//    fun getDeviceDetails(@NonNull String idToken, @NonNull String deviceId)
//    fun getDeviceConfiguration(String idToken, String deviceId, int limit)
//    fun updateDeviceConfiguration(String idToken, String deviceId, DeviceConfig deviceConfig)
    fun pair(idToken: String, code: String, callback: DevicePairingEventsListener<PairingResponse>)
    fun unpair(uid: String, idToken: String, deviceId: String, callback: DevicePairingEventsListener<String>)
    fun updatePairedDeviceInfo(uid: String, deviceId: String, idToken: String, device: UpdatedPairedDevice, callback: DevicePairingEventsListener<PairedDevice>)
    fun updatePairedDeviceRoomType(uid: String, idToken: String, device: UpdatedPairedDevice, callback: DevicePairingEventsListener<Unit>)
    fun getDeviceDetails(idToken: String, deviceId: String, callback: DevicePairingEventsListener<DeviceInfo>)
    fun getDeviceConfiguration(idToken: String, deviceId: String, limit: Int, callback: DevicePairingEventsListener<DeviceConfigList>)
    fun updateDeviceConfiguration(idToken: String, deviceId: String, deviceConfig: DeviceConfig, callback: DevicePairingEventsListener<DeviceConfig>)
    fun getDevices(idToken: String, callback: DevicePairingEventsListener<PairedDevicesList>)

    interface DevicePairingEventsListener<T> {
        fun success(value: T)
        fun failure(throwable: Throwable)
    }
}

