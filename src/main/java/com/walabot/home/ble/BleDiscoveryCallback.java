package com.walabot.home.ble;

import java.util.Collection;

/**
 * Created by Arbel on 12/08/2019.
 */
public interface BleDiscoveryCallback
{
	void onBleDiscovered(BleDevice newBleDevice, Collection<BleDevice> currentBleList);

	void onBleDiscoveryError(int err);

	void onBleDiscoveryEnded();
}
