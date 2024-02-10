package com.example.vpairsdk_flutter.ble.device;

import com.google.gson.annotations.SerializedName;

public class UpdatedPairedDevice
{
	@SerializedName("name")
	private String  _name;
	@SerializedName("imageBase64")
	private String  _imageBase64;
	@SerializedName("id")
	private String  _id;
	@SerializedName("roomType")
	private Integer _roomType;

	public UpdatedPairedDevice(String deviceName, String deviceId, Integer roomType)
	{
		_name = deviceName;
		_id = deviceId;
		_roomType = roomType;
	}

	public String getId()
	{
		return _id;
	}

	public String getImageBase64()
	{
		return _imageBase64;
	}

	public String getName()
	{
		return _name;
	}

	public void setRoomType(int roomType)
	{
		_roomType = roomType;
	}

	public Integer getRoomType()
	{
		return _roomType;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UpdatedPairedDevice that = (UpdatedPairedDevice) o;
		return _name.equals(that._name) &&
				_id.equals(that._id) &&
				_roomType.equals(that._roomType);
	}
}
