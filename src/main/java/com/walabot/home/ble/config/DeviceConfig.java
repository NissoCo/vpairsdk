package com.walabot.home.ble.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DeviceConfig implements Serializable
{
	@SerializedName("walabotConfig")
	private WalabotConfig _walabotConfig;
	@SerializedName("appConfig")
	private AppConfig     _appConfig;
	@SerializedName("direction")
	private String        _direction;

	public WalabotConfig getWalabotConfig()
	{
		return _walabotConfig;
	}

	public void setWalabotConfig(WalabotConfig walabotConfig)
	{
		_walabotConfig = walabotConfig;
	}

	public AppConfig getAppConfig()
	{
		return _appConfig;
	}

	public void setAppConfig(AppConfig appConfig)
	{
		_appConfig = appConfig;
	}

	public static DeviceConfig from(DeviceConfig deviceConfig)
	{
		DeviceConfig config = new DeviceConfig();
		config.setWalabotConfig(WalabotConfig.from(deviceConfig.getWalabotConfig()));
		config.setAppConfig(AppConfig.from(deviceConfig.getAppConfig()));
		return config;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DeviceConfig that = (DeviceConfig) o;
		return _walabotConfig.equals(that._walabotConfig) &&
				_appConfig.equals(that._appConfig);
	}
}

