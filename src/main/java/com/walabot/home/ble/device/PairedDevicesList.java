package com.walabot.home.ble.device;

import java.util.ArrayList;

import static com.walabot.home.ble.device.PairedDevice.TYPE_GEN_1;

import com.walabot.home.ble.device.PairedDevice;

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
