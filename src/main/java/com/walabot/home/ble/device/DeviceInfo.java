package com.example.vpairsdk_flutter.ble.device;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DeviceInfo
{

	@SerializedName("deviceId")
	private String             deviceId;
	@SerializedName("lastStatus")
	private String             lastStatus;
	@SerializedName("lastTelemetry")
	private long               lastTelemetry;
	@SerializedName("model")
	private String             model;
	@SerializedName("manufactorer")
	private String             manufactorer;
	@SerializedName("sdkVersion")
	private int                sdkVersion;
	@SerializedName("product")
	private String             product;
	@SerializedName("brand")
	private String             brand;
	@SerializedName("hardware")
	private String             hardware;
	@SerializedName("fingerprint")
	private String             fingerprint;
	@SerializedName("board")
	private String             board;
	@SerializedName("name")
	private String             name;
	@SerializedName("imageUri")
	private String             imageUri;
	@SerializedName("roomType")
	private int                roomType;
	@SerializedName("androidSerial")
	private String             _androidSerial;
	@SerializedName("currentApkMetadata")
	private CurrentApkMetaData currentApkMetaData;

	public DeviceInfo(String deviceId, String name, int roomType)
	{
		this.deviceId = deviceId;
		this.name = name;
		this.roomType = roomType;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public String getLastStatus()
	{
		return lastStatus;
	}

	public long getLastTelemetry()
	{
		return lastTelemetry;
	}

	public int getRoomType()
	{
		return roomType;
	}

	public void setRoomType(int roomType)
	{
		this.roomType = roomType;
	}

	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}

	public String getAndroidSerial()
	{
		return _androidSerial;
	}

	public CurrentApkMetaData getCurrentApkMetaData()
	{
		return currentApkMetaData;
	}

	public void setLastStatus(String lastStatus)
	{
		this.lastStatus = lastStatus;
	}

	public String getImageUri()
	{
		return imageUri;
	}

	public void setImageUri(String imageUri)
	{
		this.imageUri = imageUri;
	}

	public String getDeviceName()
	{
		String name = this.name;
		if (name == null)
		{
//			name = "Device";
			name = RoomType.values()[roomType].name();//todo getStringRes
		}
		return name;
	}

	public class CurrentApkMetaData implements Serializable
	{

		@SerializedName("packageName")
		private String  packageName;
		@SerializedName("versionName")
		private String  versionName;
		@SerializedName("flavor")
		private String  flavor;
		@SerializedName("buildType")
		private String  buildType;
		@SerializedName("versionCode")
		private Integer versionCode;

		public String getPackageName()
		{
			return packageName;
		}

		public String getVersionName()
		{
			return versionName;
		}

		public String getFlavor()
		{
			return flavor;
		}

		public String getBuildType()
		{
			return buildType;
		}

		public Integer getVersionCode()
		{
			return versionCode;
		}
	}
}