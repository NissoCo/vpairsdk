package com.walabot.home.ble.config;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WalabotConfig implements Serializable
{
	public final static int    FALLING_SENSITIVITY_DEFAULT    = 2;

	@SerializedName("xMin")
	private double                     _xMin;
	@SerializedName("xMax")
	private double                     _xMax;
	@SerializedName("yMin")
	private double                     _yMin;
	@SerializedName("yMax")
	private double                     _yMax;
	@SerializedName("zMin")
	private double                     _zMin;
	@SerializedName("zMax")
	private double                     _zMax;
	@SerializedName("fallingSensitivity")
	private Integer                 _fallingSensitivity;
	@SerializedName("fallingTrigger")
	private int                     _fallingTrigger;
	@SerializedName("thresholdRegion")
	private ThresholdRegion            _thresholdRegion;
	@SerializedName("trackerSubRegions")
	private List<AppTrackingSubRegion> _trackerSubRegions;

	public WalabotConfig()
	{

	}

	public WalabotConfig(double xMin, double xMax, double yMin, double yMax, double zMin, double zMax, int fallingSensitivity, int fallingTrigger)
	{
		this._xMin = xMin;
		this._xMax = xMax;
		this._yMin = yMin;
		this._yMax = yMax;
		this._zMin = zMin;
		this._zMax = zMax;
		this._fallingSensitivity = fallingSensitivity;
		this._fallingTrigger = fallingTrigger;
	}

	public static WalabotConfig from(WalabotConfig walabotConfig)
	{
		WalabotConfig config = new WalabotConfig();
		config.setxMin(walabotConfig._xMin);
		config.setxMax(walabotConfig._xMax);
		config.setyMin(walabotConfig._yMin);
		config.setyMax(walabotConfig._yMax);
		config.setzMin(walabotConfig._zMin);
		config.setzMax(walabotConfig._zMax);
		config.setThresholdRegion(walabotConfig._thresholdRegion != null ?
				ThresholdRegion.from(walabotConfig._thresholdRegion) : null);
		config.setTrackerSubRegions(walabotConfig._trackerSubRegions != null ?
				(new ArrayList<>(walabotConfig._trackerSubRegions)) : new ArrayList<>());
		config._fallingSensitivity = walabotConfig._fallingSensitivity;
		config._fallingTrigger= walabotConfig._fallingTrigger;
		return config;
	}

	public double getxMax()
	{
		return _xMax;
	}

	public double getxMin()
	{
		return _xMin;
	}

	public double getyMax()
	{
		return _yMax;
	}

	public double getyMin()
	{
		return _yMin;
	}

	public double getzMax()
	{
		return _zMax;
	}

	public double getzMin()
	{
		return _zMin;
	}

	public void setxMin(double xMin)
	{
		this._xMin = xMin;
	}

	public void setxMax(double xMax)
	{
		this._xMax = xMax;
	}

	public void setyMin(double yMin)
	{
		this._yMin = yMin;
	}

	public void setyMax(double yMax)
	{
		this._yMax = yMax;
	}

	public void setzMin(double zMin)
	{
		this._zMin = zMin;
	}

	public void setzMax(double zMax)
	{
		this._zMax = zMax;
	}

	public ThresholdRegion getThresholdRegion()
	{
		return _thresholdRegion;
	}

	public void setThresholdRegion(ThresholdRegion thresholdRegion)
	{
		this._thresholdRegion = thresholdRegion;
	}

	public void setTrackerSubRegions(List<AppTrackingSubRegion> trackerSubRegions)
	{
		_trackerSubRegions = trackerSubRegions;
	}

	@Nullable
	public List<AppTrackingSubRegion> getTrackerSubRegions()
	{
		return _trackerSubRegions;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		WalabotConfig that = (WalabotConfig) o;
		return Double.compare(that._xMin, _xMin) == 0 &&
				Double.compare(that._xMax, _xMax) == 0 &&
				Double.compare(that._yMin, _yMin) == 0 &&
				Double.compare(that._yMax, _yMax) == 0 &&
				Double.compare(that._zMin, _zMin) == 0 &&
				Double.compare(that._zMax, _zMax) == 0 &&
				equals(_thresholdRegion, that._thresholdRegion) &&
				equals(_trackerSubRegions, that._trackerSubRegions);
	}

	public boolean equals(Object a, Object b)
	{
		if (a == null && b == null)
		{
			return true;
		}
		else if (a == null || b == null)
		{
			return false;
		}
		else return a.equals(b);
	}

//	@Override
//	public int hashCode()
//	{
//		return Objects.hash(_xMin, _xMax, _yMin, _yMax, _zMin, _zMax, _thresholdRegion, _trackerSubRegions);
//	}

	public static class ThresholdRegion implements Serializable
	{
		@SerializedName("xMin")
		private double _xMin;
		@SerializedName("xMax")
		private double _xMax;
		@SerializedName("yMin")
		private double _yMin;
		@SerializedName("yMax")
		private double _yMax;

		public ThresholdRegion(double xMin, double xMax, double yMin, double yMax)
		{
			this._xMin = xMin;
			this._xMax = xMax;
			this._yMin = yMin;
			this._yMax = yMax;
		}

		public static ThresholdRegion from(ThresholdRegion thresholdRegion)
		{
			return new ThresholdRegion(thresholdRegion._xMin,
					thresholdRegion._xMax,thresholdRegion._yMin,thresholdRegion._yMax);
		}

		public double getYMin()
		{
			return _yMin;
		}

		public double getYMax()
		{
			return _yMax;
		}

		public double getXMin()
		{
			return _xMin;
		}

		public double getXMax()
		{
			return _xMax;
		}

		public static ThresholdRegion createDefault()
		{
			return new ThresholdRegion(0, 0, 0, 0);
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			ThresholdRegion that = (ThresholdRegion) o;
			return Double.compare(that._xMin, _xMin) == 0 && Double.compare(that._xMax, _xMax) == 0 && Double.compare(that._yMin, _yMin) == 0 && Double.compare(that._yMax, _yMax) == 0;
		}
	}


	//We needed a distinct class because the sdk class is not serializable by gson unless the
	// backend model expect _ prefix on fields, due to sdk own gson dependency
	public static class AppTrackingSubRegion implements Serializable
	{
		@SerializedName("xMin")
		private double  _xMin;
		@SerializedName("xMax")
		private double  _xMax;
		@SerializedName("yMin")
		private double  _yMin;
		@SerializedName("yMax")
		private double  _yMax;
		@SerializedName("isPresenceDetection")
		private boolean _isPresenceDetection;
		@SerializedName("isFallingDetection")
		private boolean _isFallingDetection;
		@SerializedName("enterDuration")
		private double  _enterDuration;
		@SerializedName("exitDuration")
		private double  _exitDuration;

		public AppTrackingSubRegion(double xMin, double xMax, double yMin, double yMax, boolean isPresenceDetection, boolean isFallingDetection, double enterDuration, double exitDuration)
		{
			_xMin = xMin;
			_xMax = xMax;
			_yMin = yMin;
			_yMax = yMax;
			_isFallingDetection = isFallingDetection;
			_isPresenceDetection = isPresenceDetection;
			_enterDuration = enterDuration;
			_exitDuration = exitDuration;
		}

		public double getxMin()
		{
			return _xMin;
		}

		public void setxMin(double xMin)
		{
			_xMin = xMin;
		}

		public double getxMax()
		{
			return _xMax;
		}

		public void setxMax(double xMax)
		{
			_xMax = xMax;
		}

		public double getyMin()
		{
			return _yMin;
		}

		public void setyMin(double yMin)
		{
			_yMin = yMin;
		}

		public double getyMax()
		{
			return _yMax;
		}

		public void setyMax(double yMax)
		{
			_yMax = yMax;
		}

		public boolean isPresenceDetection()
		{
			return _isPresenceDetection;
		}

		public void setPresenceDetection(boolean presenceDetection)
		{
			_isPresenceDetection = presenceDetection;
		}

		public boolean isFallingDetection()
		{
			return _isFallingDetection;
		}

		public void setFallingDetection(boolean fallingDetection)
		{
			_isFallingDetection = fallingDetection;
		}

		public double getEnterDuration()
		{
			return _enterDuration;
		}

		public void setEnterDuration(double enterDuration)
		{
			_enterDuration = enterDuration;
		}

		public double getExitDuration()
		{
			return _exitDuration;
		}

		public void setExitDuration(double exitDuration)
		{
			_exitDuration = exitDuration;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			AppTrackingSubRegion that = (AppTrackingSubRegion) o;
			return Double.compare(that._xMin, _xMin) == 0 &&
					Double.compare(that._xMax, _xMax) == 0 &&
					Double.compare(that._yMin, _yMin) == 0 &&
					Double.compare(that._yMax, _yMax) == 0 &&
					Boolean.compare(that._isPresenceDetection, _isPresenceDetection) == 0 &&
					Boolean.compare(that._isFallingDetection, _isFallingDetection) == 0 &&
					Double.compare(that._enterDuration, _enterDuration) == 0 &&
					Double.compare(that._exitDuration, _exitDuration) == 0;
		}

//		@Override
//		public int hashCode()
//		{
//			return Objects.hash(_xMin, _xMax, _yMin, _yMax, _isPresenceDetection, _isFallingDetection, _enterDuration, _exitDuration);
//		}
	}
}
