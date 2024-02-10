package com.example.vpairsdk_flutter.ble.device;


import com.example.vpairsdk_flutter.ble.Result;

/**
 * Created by Arbel on 06/02/2020.
 */
public class DeviceDetailsResult extends Result<DeviceInfo>
{

	private static final long CACHE_EXPIRATION_SEC = 60 * 60;
	public               long updateTime;

	public DeviceDetailsResult(DeviceInfo result)
	{
		super(result);
		updateTime = System.currentTimeMillis();
	}

	public DeviceDetailsResult(Throwable throwable)
	{
		super(throwable);
		updateTime = System.currentTimeMillis();
	}

	public boolean isValid()
	{
		return System.currentTimeMillis() - updateTime < CACHE_EXPIRATION_SEC || (getResult() != null || getThrowable() != null);
	}
}
