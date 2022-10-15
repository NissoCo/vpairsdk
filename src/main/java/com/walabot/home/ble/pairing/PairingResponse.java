package com.walabot.home.ble.pairing;

import com.google.gson.annotations.SerializedName;

public class PairingResponse
{
	@SerializedName("deviceId")
	private String _deviceId;

	public String getDeviceId()
	{
		return _deviceId;
	}
}
