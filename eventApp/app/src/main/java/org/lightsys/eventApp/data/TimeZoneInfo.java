package org.lightsys.eventApp.data;

import java.util.TimeZone;

/**
 * Created by Littlesnowman88 on 05/31/2018
 *
 * Object for getting an event's locations
 */

public class TimeZoneInfo {

    /* returns an array of string time zones, based on the android/java TimeZone libraries */
    public static String[] getAllTimeZones() {
        return TimeZone.getAvailableIDs();
    }
}
