package com.example.vpairsdk_flutter.ble.pairing.esp;

import java.net.InetAddress;
import java.net.UnknownHostException;

// TODO - this code can move to a more general location
public class EspUtil
{
	static public String getStringFromBytes(byte[] bytes, char delimiter, int radix)
	{
		if (bytes == null || bytes.length == 0)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
		{
			int b = bytes[i] & 0xff;
			sb.append(Integer.toString(b, radix));
			if (i != bytes.length - 1)
			{
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}

	static public String getMacFromBytes(byte[] mac)
	{
		return getStringFromBytes(mac, ':', 16);
	}

	static public String getIpFromBytes(byte[] ip)
	{
		return getStringFromBytes(ip, '.', 10);
	}

	//TODO: should we use this instead of getIpFromBytes
	public static InetAddress parseInetAddr(byte[] inetAddrBytes)
	{
		String s = getIpFromBytes(inetAddrBytes);
		InetAddress inetAddress = null;
		try
		{
			inetAddress = InetAddress.getByName(s);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		return inetAddress;
	}

	public static String getIpFromInt(int ip)
	{
		byte[] bytes = new byte[]{
				(byte) ip,
				(byte) (ip >>> 8),
				(byte) (ip >>> 16),
				(byte) (ip >>> 24)
		};
		return getIpFromBytes(bytes);
	}

	public static String getMacFromInt64(long mac)
	{
		byte[] bytes = new byte[]{
				(byte) mac,
				(byte) (mac >>> 8),
				(byte) (mac >>> 16),
				(byte) (mac >>> 24),
				(byte) (mac >>> 32),
				(byte) (mac >>> 48)
		};
		return getMacFromBytes(bytes);
	}
}
