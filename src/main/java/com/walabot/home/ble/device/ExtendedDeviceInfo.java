package com.walabot.home.ble.device;


import com.walabot.home.ble.config.DeviceConfig;

public class ExtendedDeviceInfo
{
	private DeviceInfo   _deviceInfo;
	private DeviceConfig _deviceConfig;
	private Units        _unit;

	public ExtendedDeviceInfo(DeviceInfo deviceInfo, DeviceConfig deviceConfig, Units unit)
	{
		_deviceInfo = deviceInfo;
		_deviceConfig = deviceConfig;
		_unit = unit;
	}

	public DeviceConfig getDeviceConfig()
	{
		return _deviceConfig;
	}

	public void setDeviceConfig(DeviceConfig deviceConfig)
	{
		_deviceConfig = deviceConfig;
	}

	public DeviceInfo getDeviceInfo()
	{
		return _deviceInfo;
	}

	public void setDeviceInfo(DeviceInfo deviceInfo)
	{
		_deviceInfo = deviceInfo;
	}

	public Units getUnit()
	{
		return _unit;
	}
}
