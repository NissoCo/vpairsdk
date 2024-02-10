package com.example.vpairsdk_flutter.ble;

/**
 * Created by Arbel on 15/01/2019.
 */

public class LearningModeModel
{
	long _startTimestampMillis;
	long _endTimestampMillis;

	public LearningModeModel(long startTimestampMillis, long endTimestampMillis)
	{
		_startTimestampMillis = startTimestampMillis;
		_endTimestampMillis = endTimestampMillis;
	}

	public long getEndTimestampMillis()
	{
		return _endTimestampMillis;
	}

	public long getStartTimestampMillis()
	{
		return _startTimestampMillis;
	}

	public boolean isLearningModeActive()
	{
		long now = System.currentTimeMillis();
		return _startTimestampMillis > 0 && _startTimestampMillis < _endTimestampMillis && (now > _startTimestampMillis && now < _endTimestampMillis);
	}
}