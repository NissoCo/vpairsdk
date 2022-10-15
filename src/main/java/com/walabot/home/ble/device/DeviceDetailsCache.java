package com.walabot.home.ble.device;

import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.Map;

public class DeviceDetailsCache
{
	private final Map<String, MutableLiveData<DeviceDetailsResult>> _devicesDetails;

	public DeviceDetailsCache()
	{
		_devicesDetails = new HashMap<>();
	}

	void updateCache(String deviceId, DeviceInfo deviceInfo)
	{
		MutableLiveData<DeviceDetailsResult> liveData = getFromCache(deviceId);
		liveData.postValue(new DeviceDetailsResult(deviceInfo));
	}

	void updateCache(String deviceId, Throwable throwable)
	{
		MutableLiveData<DeviceDetailsResult> liveData = getFromCache(deviceId);
		liveData.postValue(new DeviceDetailsResult(throwable));
	}

	MutableLiveData<DeviceDetailsResult> getFromCache(String deviceId)
	{
		MutableLiveData<DeviceDetailsResult> liveData = _devicesDetails.get(deviceId);
		if (liveData == null)
		{
			liveData = new MutableLiveData<>();
			_devicesDetails.put(deviceId, liveData);
		}
		return liveData;
	}
}
