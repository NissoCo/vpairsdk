package com.example.vpairsdk_flutter.ble.device;


import com.example.vpairsdk_flutter.ble.Result;

public class PairedDevicesResult extends Result<PairedDevicesList>
{

	private static final long CACHE_EXPIRATION_SEC = 60 * 60;
	public               long updateTime;

	public PairedDevicesResult(PairedDevicesList result)
	{
		super(result);
		updateTime = System.currentTimeMillis();
	}

	public PairedDevicesResult(Throwable throwable)
	{
		super(throwable);
		updateTime = System.currentTimeMillis();
	}

	public boolean isValid()
	{
		return System.currentTimeMillis() - updateTime < CACHE_EXPIRATION_SEC || (getResult() != null || getThrowable() != null);
	}
}
