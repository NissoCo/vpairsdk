package com.example.vpairsdk_flutter.ble.device;

import com.example.vpairsdk_flutter.ble.device.PairedDevicesResult;
import com.example.vpairsdk_flutter.ble.device.PairedDevicesList;

import java.util.HashMap;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;

class DeviceCache
{
	private Map<String, MutableLiveData<PairedDevicesResult>> _devices;

	DeviceCache()
	{
		_devices = new HashMap<>();
	}

	void updateCache(String userId, PairedDevicesList devicesListWrapper)
	{
		MutableLiveData<PairedDevicesResult> liveData = getFromCache(userId);
		liveData.postValue(new PairedDevicesResult(devicesListWrapper));
	}

	void updateCacheError(String userId, Throwable throwable)
	{
		MutableLiveData<PairedDevicesResult> liveData = getFromCache(userId);
		liveData.postValue(new PairedDevicesResult(throwable));
	}

	MutableLiveData<PairedDevicesResult> getFromCache(String userId)
	{
		MutableLiveData<PairedDevicesResult> liveData = _devices.get(userId);
		if (liveData == null)
		{
			liveData = new MutableLiveData<>();
			_devices.put(userId, liveData);
		}
		return liveData;
	}
}
