package com.walabot.home.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Arbel on 12/08/2019.
 */
public class BleDevice implements Parcelable
{
	private String          _address;
	private String          _name;
	private byte[]          _rawAdvData;
	private BluetoothDevice _device;

	public BleDevice(String address, String name, byte[] bytes, BluetoothDevice device)
	{
		_address = address;
		_name = name;
		_rawAdvData = bytes;
		_device = device;

		_name = _name == null ? "" : _name;
	}

	protected BleDevice(Parcel in)
	{
		_address = in.readString();
		_name = in.readString();
		_rawAdvData = in.createByteArray();
		_device = in.readParcelable(BluetoothDevice.class.getClassLoader());
	}

	public String getAddress()
	{
		return _address;
	}

	public String getName()
	{
		return _name;
	}

	public byte[] getRawAdvData()
	{
		return _rawAdvData;
	}

	public BluetoothDevice getDevice()
	{
		return _device;
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(_address);
		dest.writeString(_name);
		dest.writeByteArray(_rawAdvData);
		dest.writeParcelable(_device, flags);
	}


	public static final Creator<BleDevice> CREATOR = new Creator<BleDevice>()
	{
		@Override
		public BleDevice createFromParcel(Parcel in)
		{
			return new BleDevice(in);
		}

		@Override
		public BleDevice[] newArray(int size)
		{
			return new BleDevice[size];
		}
	};
}

