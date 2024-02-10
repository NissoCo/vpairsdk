package com.example.vpairsdk_flutter.ble.device;


import com.example.vpairsdk_flutter.ble.R;

/**
 * Created by Arbel on 07/05/2019.
 */

public enum RoomType
{
	BATHROOM(R.string.type_bathroom, R.drawable.bathroom_small, R.drawable.bathroom_big),
	BEDROOM(R.string.type_bedroom, R.drawable.bedroom_small, R.drawable.bedroom_big),
	FAMILY_ROOM(R.string.type_family_room, R.drawable.family_room_small, R.drawable.family_room_big),
	HALL(R.string.type_hall, R.drawable.hall_small, R.drawable.hall_big),
	KITCHEN(R.string.type_kitchen, R.drawable.kitchen_small, R.drawable.kitchen_big),
	DINING_ROOM(R.string.type_dining_room, R.drawable.dining_room_small, R.drawable.dining_room_big),
	LIVING_ROOM(R.string.type_living_room, R.drawable.living_room_small, R.drawable.living_room_big),
	OTHER(R.string.other, R.drawable.other_small, R.drawable.other_big);

	int _stringRes;
	int _smallDrawableRes;
	int _bigDrawableRes;

	RoomType(int stringRes, int smallDrawableRes, int bigDrawableRes)
	{
		_stringRes = stringRes;
		_smallDrawableRes = smallDrawableRes;
		_bigDrawableRes = bigDrawableRes;
	}

	public int getStringRes()
	{
		return _stringRes;
	}

	public int getSmallDrawableRes()
	{
		return _smallDrawableRes;
	}

	public int getBigDrawableRes()
	{
		return _bigDrawableRes;
	}

	public static RoomType getRoomTypeAccordingToOrdinal(int roomTypeOrdinal)
	{
		RoomType[] roomTypeVals = RoomType.values();
		if(roomTypeOrdinal >= 0 && roomTypeOrdinal < roomTypeVals.length)
		{
			return roomTypeVals[roomTypeOrdinal];
		}
		return RoomType.OTHER;
	}
}

