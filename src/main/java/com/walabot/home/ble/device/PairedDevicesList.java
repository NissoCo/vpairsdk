package com.example.vpairsdk_flutter.ble.device;

import java.util.ArrayList;

import static com.example.vpairsdk_flutter.ble.device.PairedDevice.TYPE_GEN_1;

import com.example.vpairsdk_flutter.ble.device.PairedDevice;

public class PairedDevicesList extends ArrayList<PairedDevice>
{
	public boolean containsGen1Device()
	{
		for (PairedDevice device : this)
		{
			if (device.getDeviceType() == TYPE_GEN_1)
			{
				return true;
			}
		}
		return false;
	}
}
