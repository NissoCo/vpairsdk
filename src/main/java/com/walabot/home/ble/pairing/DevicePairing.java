package com.walabot.home.ble.pairing;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.walabot.home.ble.Result;
import com.walabot.home.ble.device.DeviceRepository;


import java.util.HashMap;
import java.util.Map;

/**
 * Performs pairing after either getting pending code from link,
 * or from user's input.
 * Maintains pairing timestamp for every paired device.
 */
public class DevicePairing
{
	private static final long                    AFTER_PAIRING_GRACE_TIME_MILLIS = 60 * 1000; //one minute
	private DeviceAuth _deviceAuth;

	private final DeviceRepository _deviceRepository;
	private              MutableLiveData<String> _pendingPairingCodeLiveData;
	private              boolean                 _gotPairingCodeFromLink;
	private              Map<String, Long>       _pairingTimestampMap;


	public DevicePairing(DeviceAuth deviceAuth, DeviceRepository deviceRepository)
	{
		_deviceAuth = deviceAuth;
		_deviceRepository = deviceRepository;
		_pendingPairingCodeLiveData = new MutableLiveData<>();
		_pairingTimestampMap = new HashMap<>();
	}

	/**
	 * @return the pending pairing code LiveData
	 */
	public LiveData<String> getPendingPairingCodeLiveData()
	{
		return _pendingPairingCodeLiveData;
	}

	/**
	 * Performs pairing. Once pairing was completed successfully,
	 * saves the current timestamp for the paired device.
	 *
	 * @param pinCode - pin code to preform pairing between device and companion
	 * @return the PairingResponse LiveData
	 */

	public LiveData<Result<PairingResponse>> pairDevice(final String pinCode)
	{
		_gotPairingCodeFromLink = _pendingPairingCodeLiveData.getValue() != null;
		_pendingPairingCodeLiveData.setValue(null);

		return Transformations.map(_deviceRepository.addDevice(_deviceAuth.getUid(), _deviceAuth.getIdToken(), pinCode), response ->
		{
			if (response != null && response.getThrowable() == null)
			{
				_pairingTimestampMap.put(response.getResult().getDeviceId(), System.currentTimeMillis());
			}
			return response;
		});
	}

	public void cleanup()
	{

	}

	/**
	 * sets the pairing code to the pendingPairingCode LiveData.
	 *
	 * @param pendingPairingCode - pairing code
	 */
	public void setPendingPairingCode(String pendingPairingCode)
	{
		_pendingPairingCodeLiveData.setValue(pendingPairingCode);
	}

	/**
	 * @return returns whether there is a pending pairing code
	 */
	public boolean hasPendingPairingCode()
	{
		return _pendingPairingCodeLiveData.getValue() != null;
	}

	/**
	 * @return returns true if we got the pairing code from dynamic link,
	 * false if from user's input.
	 */
	public boolean isGotPairingCodeFromLink()
	{
		return _gotPairingCodeFromLink;
	}

	/**
	 * Checks if the time that has passed since the device was paired is longer than {@value #AFTER_PAIRING_GRACE_TIME_MILLIS}
	 *
	 * @param deviceId - device id
	 * @return true if device was paired less than {@value #AFTER_PAIRING_GRACE_TIME_MILLIS}, false otherwise.
	 */
	public boolean hasJustPaired(String deviceId)
	{
		long pairingTime = getPairingTimeMillis(deviceId);
		return System.currentTimeMillis() - pairingTime < AFTER_PAIRING_GRACE_TIME_MILLIS;
	}

	/**
	 * Returns the time the device was paired.
	 *
	 * @param deviceId - device id
	 * @return timestamp in Milliseconds that represents the time the device was paired with the companion.
	 */
	public long getPairingTimeMillis(String deviceId)
	{
		Long pairingTime = 0L;
		if (_pairingTimestampMap.containsKey(deviceId))
		{
			pairingTime = _pairingTimestampMap.get(deviceId);
		}
		return pairingTime.longValue();
	}
}
