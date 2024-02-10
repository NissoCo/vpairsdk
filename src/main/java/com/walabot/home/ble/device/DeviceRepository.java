package com.example.vpairsdk_flutter.ble.device;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.vpairsdk_flutter.ble.Result;
import com.example.vpairsdk_flutter.ble.config.DeviceConfig;
import com.example.vpairsdk_flutter.ble.config.DeviceConfigList;
import com.example.vpairsdk_flutter.ble.pairing.DevicePairingEvents;
import com.example.vpairsdk_flutter.ble.pairing.PairingResponse;

import java.util.ArrayList;

import kotlin.Unit;


public class DeviceRepository
{
	private final DevicePairingEvents _serverApi;
	private final DeviceCache        _deviceCache;
	private final DeviceDetailsCache _deviceDetailsCache;

	public DeviceRepository(DevicePairingEvents serverAPI)
	{
		_serverApi = serverAPI;
		_deviceCache = new DeviceCache();
		_deviceDetailsCache = new DeviceDetailsCache();
	}

	public LiveData<Result<PairingResponse>> addDevice(@NonNull String userId, @NonNull String idToken, @NonNull String pinCode)
	{
		MutableLiveData<Result<PairingResponse>> addDeviceLiveData = new MutableLiveData<>();
		_serverApi.pair(idToken, pinCode, new DevicePairingEvents.DevicePairingEventsListener<PairingResponse>() {
			@Override
			public void success(PairingResponse value) {
				getDevices(userId, idToken, true);
				getDeviceDetails(userId, idToken, value.getDeviceId());
				addDeviceLiveData.postValue(new Result<>(value));
			}

			@Override
			public void failure(@NonNull Throwable throwable) {
				addDeviceLiveData.postValue(new Result<>(throwable));
			}
		});
		return addDeviceLiveData;
	}

	public LiveData<Result<String>> removeDevice(@NonNull String userId, @NonNull String idToken, @NonNull String deviceId)
	{
		MutableLiveData<Result<String>> removeDeviceLiveData = new MutableLiveData<>();
		_serverApi.unpair(userId, idToken, deviceId, new DevicePairingEvents.DevicePairingEventsListener<String>() {
			@Override
			public void success(String value) {
				removeDeviceLiveData.postValue(new Result<>(value));
				getDevices(userId, idToken, true);
			}

			@Override
			public void failure(@NonNull Throwable throwable) {
				removeDeviceLiveData.postValue(new Result<>(throwable));
			}
		});
		return removeDeviceLiveData;
	}

	public LiveData<Result<PairedDevice>> updateDevice(@NonNull String userId, @NonNull String idToken, @NonNull UpdatedPairedDevice device)
	{
		MutableLiveData<Result<PairedDevice>> pairedDeviceLiveData = new MutableLiveData<>();
		_serverApi.updatePairedDeviceInfo(userId, device.getId(), idToken, device, new DevicePairingEvents.DevicePairingEventsListener<PairedDevice>() {
			@Override
			public void success(PairedDevice value) {
				getDevices(userId, idToken, true);
				getDeviceDetails(userId, idToken, value.getId(), true);
				pairedDeviceLiveData.postValue(new Result<>(value));
			}

			@Override
			public void failure(@NonNull Throwable throwable) {
				pairedDeviceLiveData.postValue(new Result<>(throwable));
			}
		});
		return pairedDeviceLiveData;
	}

	public LiveData<Result<UpdatedPairedDevice>> updateRoomType(@NonNull String userId, @NonNull String idToken, @NonNull UpdatedPairedDevice device)
	{
		//todo change
		MutableLiveData<Result<UpdatedPairedDevice>> pairedDeviceLiveData = new MutableLiveData<>();
		_serverApi.updatePairedDeviceRoomType(userId, idToken, device, new DevicePairingEvents.DevicePairingEventsListener<Unit>() {
			@Override
			public void success(Unit value) {
				pairedDeviceLiveData.postValue(new Result<>(device));
			}

			@Override
			public void failure(@NonNull Throwable throwable) {

			}
		});
		return pairedDeviceLiveData;
	}


	public LiveData<PairedDevicesResult> getDevices(@NonNull String userId,
			@NonNull String idToken)
	{
		return getDevices(userId, idToken, false);
	}


	public LiveData<PairedDevicesResult> getDevices(@NonNull String userId,
			@NonNull String idToken, boolean forceRefresh)
	{
		MutableLiveData<PairedDevicesResult> cachedDevices = _deviceCache.getFromCache(userId);

		if (cachedDevices.getValue() == null || !cachedDevices.getValue().isValid())
		{
			forceRefresh = true;
		}
		if (forceRefresh)
		{
			_serverApi.getDevices(idToken, new DevicePairingEvents.DevicePairingEventsListener<PairedDevicesList>() {
				@Override
				public void success(PairedDevicesList value) {
					PairedDevicesList list = new PairedDevicesList();
					for (PairedDevice device : value)
					{
						// TODO: add constant in the navigator -> DEVICE_FILTER_TYPE that will return
						//  the device according to the falvor
						if (device.getDeviceType() == PairedDevice.TYPE_GEN_2)
						{
							list.add(device);
						}
					}
					_deviceCache.updateCache(userId, list);
				}

				@Override
				public void failure(@NonNull Throwable throwable) {
					_deviceCache.updateCacheError(userId, throwable);
				}
			});
		}
		return cachedDevices;
	}

	public LiveData<DeviceDetailsResult> getDeviceDetails(@NonNull String userId, @NonNull String idToken, @NonNull String deviceId)
	{
		return getDeviceDetails(userId, idToken, deviceId, false);
	}

	public LiveData<DeviceDetailsResult> getDeviceDetails(@NonNull String userId,
			@NonNull String idToken, @NonNull String deviceId, boolean forceRefresh)
	{
		LiveData<DeviceDetailsResult> cachedDetails = _deviceDetailsCache.getFromCache(deviceId);
		if (cachedDetails.getValue() == null || !cachedDetails.getValue().isValid())
		{
			forceRefresh = true;
		}
		if (forceRefresh)
		{
			_serverApi.getDeviceDetails(idToken, deviceId, new DevicePairingEvents.DevicePairingEventsListener<DeviceInfo>() {
				@Override
				public void success(DeviceInfo value) {
					_deviceDetailsCache.updateCache(deviceId, value);
				}

				@Override
				public void failure(@NonNull Throwable throwable) {
					_deviceDetailsCache.updateCache(deviceId, throwable);
				}
			});
		}

		return cachedDetails;
	}

	public LiveData<Result<DeviceConfigList>> getDeviceConfig(@NonNull String idToken, @NonNull String deviceId, int limit)
	{
		MutableLiveData<Result<DeviceConfigList>> deviceConfigLiveData = new MutableLiveData<>();
		_serverApi.getDeviceConfiguration(idToken, deviceId, limit, new DevicePairingEvents.DevicePairingEventsListener<DeviceConfigList>() {
			@Override
			public void success(DeviceConfigList value) {
				deviceConfigLiveData.postValue(new Result<>(value));
			}

			@Override
			public void failure(@NonNull Throwable throwable) {
				deviceConfigLiveData.postValue(new Result<>(throwable));
			}
		});
		return deviceConfigLiveData;
	}

	public LiveData<Result<DeviceConfig>> updateDeviceConfig(@NonNull String idToken, @NonNull String deviceId, DeviceConfig deviceConfig)
	{
		MutableLiveData<Result<DeviceConfig>> deviceConfigLiveData = new MutableLiveData<>();
		_serverApi.updateDeviceConfiguration(idToken, deviceId, deviceConfig, new DevicePairingEvents.DevicePairingEventsListener<DeviceConfig>() {
			@Override
			public void success(DeviceConfig value) {
				deviceConfigLiveData.postValue(new Result<>(value));
			}

			@Override
			public void failure(@NonNull Throwable throwable) {
				deviceConfigLiveData.postValue(new Result<>(throwable));
			}
		});
		return deviceConfigLiveData;
	}

	public void cleanup()
	{

	}

}
