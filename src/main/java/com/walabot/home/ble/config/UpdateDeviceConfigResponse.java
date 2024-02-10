package com.example.vpairsdk_flutter.ble.config;

import com.google.gson.annotations.SerializedName;

public class UpdateDeviceConfigResponse
{
	@SerializedName("newConfig")
	private DeviceConfig _DeviceConfig;

	public DeviceConfig getDeviceConfig()
	{
		return _DeviceConfig;
	}
}
