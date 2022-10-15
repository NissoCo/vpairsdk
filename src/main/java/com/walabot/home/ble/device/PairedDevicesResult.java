package com.walabot.home.ble.device;


import com.walabot.home.ble.Result;

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
