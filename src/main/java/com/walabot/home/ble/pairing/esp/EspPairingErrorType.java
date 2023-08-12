package com.walabot.home.ble.pairing.esp;

public enum EspPairingErrorType
{
	CONNECT_FAILED("Failed to connect to device"),
	FAILED_TO_FIND_SERVICE("Failed find the correspding BLE service"),
	FAILED_TO_PARSE_CONNECT_RESULT("Failed to parse connect result"),
	FAILED_TO_SEND_CLOUD_DETAILS("Failed to send cloud details"),
	FAILED_TO_PARSE_PAIR_RESULT("failed to parse Pair result"),
	PAIRING_FAILED("Pairing failed"),
	NOTIFY_PAIRING_COMPLETE_FAILED("Failed to notify pairing complete"),
	FAILED_TO_PARSE_OTA_RESULT("Failed to parse OTA result"),
	OTA_CHECK_FAILED("Failed to check for OTA"),
	FAILED_TO_PERFORM_OTA("Failed to perform OTA"),
	OPERATIONAL_REBOOT_FAILED("Failed to perform operational reboot"),
	FACTORY_REBOOT_FAILED("Failed to perform factory reboot"),
	COMMIT_PROVISION_FAILED("Failed to perform commit provision");

	private final String _message;

	EspPairingErrorType(String message)
	{
		_message = message;
	}

	public String getMessage()
	{
		return _message;
	}
}
