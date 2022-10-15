package com.walabot.home.ble.config;

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
