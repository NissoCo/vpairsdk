package com.walabot.home.ble.config;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.walabot.home.ble.LearningModeModel;

import java.io.Serializable;

public class AppConfig implements Serializable
{
	public static final int DEFAULT_VOLUME_LEVEL = 0;
	private static final int DEFAULT_LED_MODE     = 1;

	@SerializedName("silentMode")
	private Boolean _silentMode;
	@SerializedName("ledMode")
	private Integer _ledMode;
	@SerializedName("allowCompanion")
	private Boolean _allowCompanion;
	@SerializedName("otaEnabled")
	private Boolean _otaEnabled;
	@SerializedName("subscriptionExpired")
	private Boolean _subscriptionExpired;
	@SerializedName("disableTaskSwitching")
	private Boolean _disableTaskSwitching;
	@SerializedName("disableStatusBar")
	private Boolean _disableStatusBar;
	@SerializedName("disableDeviceAdmin")
	private Boolean _disableDeviceAdmin;
	@SerializedName("volume")
	private Integer _volume;
	@SerializedName("voipVolume")
	private Integer _voipVolume;
	@SerializedName("reportFallsToMqtt")
	private Boolean _reportFallsToMqtt;
	@SerializedName("reportPresenceToMqtt")
	private Boolean _reportPresenceToMqtt;
	@SerializedName("systemLang")
	@Nullable
	private String  _systemLang;
	@SerializedName("enableTestMode")
	private Boolean _enableTestMode;
	@SerializedName("learningModeStartTs")
	private Long    _learningModeStartTs;
	@SerializedName("learningModeEndTs")
	private Long    _learningModeEndTs;

	public AppConfig()
	{
		_ledMode = DEFAULT_LED_MODE;
		_silentMode = false;
	}

	public AppConfig(boolean ledMode, boolean silentMode, Integer volume)
	{
		_ledMode = ledMode ? 1 : 0;
		_silentMode = silentMode;
		_volume = volume;
	}

	public AppConfig(boolean ledMode, boolean deactivateTemporarily,
					 LearningModeModel learningModeModel, boolean testMode)
	{
		_ledMode = ledMode ? 1 : 0;
		_silentMode = deactivateTemporarily;
		_enableTestMode = testMode;
		_learningModeStartTs = learningModeModel.getStartTimestampMillis();
		_learningModeEndTs = learningModeModel.getEndTimestampMillis();
	}

	public AppConfig(boolean ledMode, boolean deactivateTemporarily, boolean testMode,
			Integer volume)
	{
		_ledMode = ledMode ? 1 : 0;
		_silentMode = deactivateTemporarily;
		_enableTestMode = testMode;
		if (volume != null)
		{
			_volume = volume;
		}
	}

	public boolean isLedMode()
	{
		return _ledMode != null ? _ledMode == 1 : true;//default 'on'
	}

	public boolean isSilentMode()
	{
		return _silentMode != null ? _silentMode : false;
	}

	public int getVolume()
	{
		return _volume != null ? _volume : DEFAULT_VOLUME_LEVEL;
	}

	public void setVolume(Integer volume)
	{
		_volume = volume;
	}

	public boolean getEnableTestMode()
	{
		return _enableTestMode != null ? _enableTestMode : false;
	}

	public long getLearningModeStartTs()
	{
		return _learningModeStartTs != null ? _learningModeStartTs : 0;
	}

	public long getLearningModeEndTs()
	{
		return _learningModeEndTs != null ? _learningModeEndTs : 0;
	}

	@Override
	public boolean equals(@Nullable Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AppConfig appConfig = (AppConfig) o;
		return _ledMode.equals(appConfig._ledMode) && _silentMode == appConfig._silentMode && _volume.equals(appConfig._volume)
				&& _enableTestMode == appConfig._enableTestMode && !hasLearningModeChanged(appConfig);
	}

	private boolean hasLearningModeChanged(AppConfig appConfig)
	{
		//it'd be null if the appconfig hasn't changed, so we keep it null so the config in the cloud won't be affected
		if (appConfig._learningModeStartTs != null && appConfig._learningModeEndTs != null)
		{
			if (_learningModeStartTs == 0 && _learningModeEndTs == 0)
			{
				return !(appConfig._learningModeStartTs == 0 && appConfig._learningModeEndTs == 0);
			}
			else
			{
				return appConfig._learningModeStartTs == 0 && appConfig._learningModeEndTs == 0;
			}
		}
		else
		{
			return false;
		}
	}

	public static AppConfig from(AppConfig appConfig)
	{
		AppConfig config = new AppConfig();
		config._silentMode = appConfig._silentMode;
		config._ledMode = appConfig._ledMode;
		config._allowCompanion = appConfig._allowCompanion;
		config._otaEnabled = appConfig._otaEnabled;
		config._subscriptionExpired = appConfig._subscriptionExpired;
		config._disableTaskSwitching = appConfig._disableTaskSwitching;
		config._disableStatusBar = appConfig._disableStatusBar;
		config._disableDeviceAdmin = appConfig._disableDeviceAdmin;
		config._volume = appConfig._volume;
		config._voipVolume = appConfig._voipVolume;
		config._reportFallsToMqtt = appConfig._reportFallsToMqtt;
		config._reportPresenceToMqtt = appConfig._reportPresenceToMqtt;
		config._enableTestMode = appConfig._enableTestMode;
		config._learningModeStartTs = appConfig._learningModeStartTs;
		config._learningModeEndTs = appConfig._learningModeEndTs;
		return config;
	}

}
