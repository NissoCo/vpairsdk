package com.walabot.home.ble.pairing.esp;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.walabot.home.ble.Message;
import com.walabot.home.ble.pairing.Gen2CloudOptions;
import com.walabot.home.ble.sdk.Config;

public class ProtobufMessagesV1 implements ProtocolMediator
{

	public GeneratedMessageV3 wifiCredentials(byte[] apSsid, byte[] bssid, byte[] apPassword)
	{
		return Message.WifiCred.newBuilder()
				.setSsid(new String(apSsid))
				.setPass(new String(apPassword))
				.build();
	}

	@Override
	public GeneratedMessageV3 cloudDetails(Config cloudOptions)
	{
		return Message.CloudDetails.newBuilder()
				.setHttpUrl(cloudOptions.getUrl())
				.setMqttUri(cloudOptions.getMqtt().getHostUrl())
				.setMqttPort(cloudOptions.getMqtt().getPort())
				.setProjectId(cloudOptions.getCloud().getName())
				.setNtpUrl(cloudOptions.getMqtt().getNtpUrl())
				.setCloudType(cloudOptions.getCloud().getType())
				.setCloudRegistry(cloudOptions.getCloud().getRegistryId())
				.setCloudRegion(cloudOptions.getCloud().getRegion())
				.setMqttUsername(cloudOptions.getMqtt().getUserName())
				.setMqttPassword(cloudOptions.getMqtt().getPwd())
				.setMqttClientId(cloudOptions.getMqtt().getClientId())
				.build();
	}

	@Override
	public GeneratedMessageV3 pair(String uid)
	{
		return Message.Pair.newBuilder()
				.setUid(uid)
				.build();
	}

	@Override
	public GeneratedMessageV3 pairingComplete(String uid, String code)
	{
		return Message.PairingComplete.newBuilder()
				.setUid(uid)
				.setCode(code)
				.build();
	}

	@Override
	public MessageResult parseResult(byte[] data)
	{
		if (data == null)
		{
			return null;
		}
		try
		{
			Message.ToAppMessage m = Message.ToAppMessage.parseFrom(data);
			MessageResult r = new ProtocolMediator.PairResult();
			r.setEsp_error(0);
			r.setResult(m.getResult().getNumber());
			if (r.getResult() != 0) {
				r.setResult(r.getResult() + 1000);
			}
			return r;
		}
		catch (InvalidProtocolBufferException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public WifiResult parseWifiResult(byte[] data)
	{
		if (data == null)
		{
			return null;
		}
		try
		{
			Message.ToAppMessage m = Message.ToAppMessage.parseFrom(data);
			if (m.getType() != Message.ToAppMessageType.CONNECT_WIFI_RESULT)
			{
				return null;
			}
			Message.WifiCredResult wifiCredResult = Message.WifiCredResult.parseFrom(m.getPayload());
			if (wifiCredResult == null)
			{
				return null;
			}
			WifiResult r = new WifiResult();
			r.setEsp_error(0);
			r.setMac(EspUtil.getMacFromBytes(wifiCredResult.getMac().toByteArray()));
			r.setIp(EspUtil.getIpFromBytes(wifiCredResult.getIp().toByteArray()));
			r.setResult(m.getResult().getNumber());
			if (r.getResult() != 0) {
				r.setResult(r.getResult() + 1000);
			}
			return r;
		}
		catch (InvalidProtocolBufferException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public PairResult parsePairResult(byte[] data)
	{
		if (data == null)
		{
			return null;
		}
		try
		{
			Message.ToAppMessage m = Message.ToAppMessage.parseFrom(data);
			if (m.getType() != Message.ToAppMessageType.PAIR_TO_PHONE_RESULT)
			{
				return null;
			}
			Message.PairResult pairResult = Message.PairResult.parseFrom(m.getPayload());
			if (pairResult == null)
			{
				return null;
			}
			PairResult r = new PairResult();
			r.setEsp_error(0);
			r.setCode(pairResult.getCode());
			r.setResult(m.getResult().getNumber());
			if (r.getResult() != 0) {
				r.setResult(r.getResult() + 1000);
			}
			return r;
		}
		catch (InvalidProtocolBufferException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public WifiScanResult parseWifiScanResult(byte[] data) {
		//ignored
		return null;
	}
}
