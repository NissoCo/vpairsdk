package com.example.vpairsdk_flutter.ble.pairing.esp;


import com.walabot.home.ble.pairing.esp.EspApi;

public class EspPairedDevice
{
	private WalabotDeviceDesc _deviceDesc;
	private String _deviceId;
	private EspApi.CheckOtaResult _checkOtaResult;

	public EspPairedDevice(WalabotDeviceDesc deviceDesc, String deviceId, EspApi.CheckOtaResult checkOtaResult)
	{
		_deviceDesc = deviceDesc;
		_deviceId = deviceId;
		_checkOtaResult = checkOtaResult;
	}

	public String getDeviceId()
	{
		return _deviceId;
	}

	public WalabotDeviceDesc getDeviceDesc()
	{
		return _deviceDesc;
	}

	public EspApi.CheckOtaResult getCheckOtaResult()
	{
		return _checkOtaResult;
	}
}
