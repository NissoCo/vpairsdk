package com.walabot.home.ble.device;

import com.walabot.home.ble.R;

import java.util.Locale;

public enum Units
{
	METERS(R.string.meter_suffix)
			{
				@Override
				public double fromMeters(double meters)
				{
					return meters;
				}

				@Override
				public double fromInches(double inches)
				{
					return inches * 0.0254;
				}
			},
	INCHES(R.string.inches)
			{
				@Override
				public double fromMeters(double meters)
				{
					return meters / 0.0254;
				}

				@Override
				public double fromInches(double inches)
				{
					return inches;
				}
			},
	FEET(R.string.feet)
			{
		        @Override
	        	public double fromMeters(double meters) { return meters * 3.281; }

		        @Override
	         	public double fromInches(double inches) {return inches / 12; }
	};
	public static final int    MEASURE_SYSTEM_IMPERIAL             = 1;
	public static final int    MEASURE_SYSTEM_METRIC               = 2;
	Units(int textResId)
	{
		_textResId = textResId;
	}

	public static int getMeasureSystemByLocale()
	{
		String countryCode = Locale.getDefault().getCountry();
		switch (countryCode)
		{
			case "US":
			case "GB":
			case "LR":
			case "MM":
				return MEASURE_SYSTEM_IMPERIAL;
			default:
				return MEASURE_SYSTEM_METRIC;
		}
	}

	public abstract double fromMeters(double meters);

	public abstract double fromInches(double inches);

	public int _textResId;

	public static Units getUnitFromOrdinal(int ordinal)
	{
		Units[] units = Units.values();
		if (ordinal >= 0 && ordinal < units.length)
		{
			return units[ordinal];
		}
		else
		{
			return null;
		}
	}
}
