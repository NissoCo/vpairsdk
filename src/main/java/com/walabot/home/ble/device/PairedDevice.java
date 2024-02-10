package com.example.vpairsdk_flutter.ble.device;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.lang.annotation.Retention;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class PairedDevice implements Serializable
{
	public static final int TYPE_GEN_1 = 0;
	public static final int TYPE_GEN_2 = 1;

	@SerializedName("name")
	private String _name;
	@SerializedName("id")
	private String _id;
	@SerializedName("imageUri")
	private String _imageUri;
	@SerializedName("roomType")
	private int    _roomType;
	@SerializedName("deviceType")
	private @DeviceType
	int _deviceType;
	@SerializedName("androidSerial")
	private String _androidSerial;

	@Retention(SOURCE)
	@IntDef({TYPE_GEN_1, TYPE_GEN_2})
	public @interface DeviceType
	{
	}

	public PairedDevice(String name, String id)
	{
		_name = name;
		_id = id;
	}

	public PairedDevice(String name, String id, int roomType)
	{
		_name = name;
		_id = id;
		_roomType = roomType;
	}

	public PairedDevice(String name)
	{
		_name = name;
	}

	public String getId()
	{
		return _id;
	}

	public String getName()
	{
		String name = _name;
		if (name == null)
		{
//			name = "Device";
			name = RoomType.values()[_roomType].name();//todo getStringRes
		}
		return name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public void setId(String id)
	{
		_id = id;
	}

	public void setRoomType(int roomType)
	{
		_roomType = roomType;
	}

	public String getImageUri()
	{
		return _imageUri;
	}

	public int getRoomType()
	{
		return _roomType;
	}

	public int getDeviceType()
	{
		return _deviceType;
	}

	public String getAndroidSerial()
	{
		return _androidSerial;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		PairedDevice that = (PairedDevice) o;

		if (_name != null ? !_name.equals(that._name) : that._name != null)
			return false;
		return _id != null ? _id.equals(that._id) : that._id == null;
	}

	@Override
	public int hashCode()
	{
		int result = _name != null ? _name.hashCode() : 0;
		result = 31 * result + (_id != null ? _id.hashCode() : 0);
		return result;
	}

	@NonNull
	@Override
	public String toString()
	{
		return _name;
	}

	public void setImageUri(String imageUri)
	{
		_imageUri = imageUri;
	}
}
