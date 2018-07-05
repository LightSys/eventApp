package org.lightsys.eventApp.tools;

import java.util.Date;
import java.util.TimeZone;

public final class TimeAdjuster {

    /**
     * addTimes two integer formatted times, returning the time in integer format
     * NOTE: THE RETURNED TIME CAN BE < 0 OR > 2359.
     * To get a "wrapped around time", use adjustTime(time, offset)
     * Created by: Littlesnowman88 on 15 June 2018
     *
     * @param time,   an integer-formatted time (hhmm). Can be positive or negative.
     * @param offset, an integer-formatted time offset (hhmm). Can be positive or negative.
     * @return time + offset, with properly added minutes and hours.
     */
    public static int addTimes(int time, int offset) {
        int timeSign = determineSign(time);
        int offsetSign = determineSign(offset);
        String timeHrs = padZeroes(Integer.toString(time).replace("-", "")).substring(0, 2);
        String timeMins = padZeroes(Integer.toString(time).replace("-", "")).substring(2, 4);
        String offsetHrs = padZeroes(Integer.toString(offset).replace("-", "")).substring(0, 2);
        String offsetMins = padZeroes(Integer.toString(offset).replace("-", "")).substring(2, 4);
        int adjustedMins = (((Integer.valueOf(timeMins) * timeSign) + (Integer.valueOf(offsetMins) * offsetSign)));
        int carry = 0;
        if (adjustedMins < 0) {
            carry = -1;
        } else if (adjustedMins > 59) {
            carry = 1;
        }
        adjustedMins = adjustedMins % 60;
        if (adjustedMins < 0) {
            adjustedMins = 60 + adjustedMins;
        }
        int adjustedHrs = (((Integer.valueOf(timeHrs) * timeSign)) + (Integer.valueOf(offsetHrs) * offsetSign)) + carry;
        return (adjustedHrs * 100) + adjustedMins;
    }

    /**
     * adjustTime adjusts an event start time by a given number of hours
     * IMPORTANT: adjustTime "wraps around" so that things stay in hhmm format between 0 and 2359
     * Created by Littlesnowman88 on 13 June 2018
     *
     * @param startTime, an integer-formatted time (hhmm); offset, the number of hours to offset by.
     * @return the adjusted start time (hhmm) with four digits and its original sign ( + or -)
     */
    public static int adjustTime(int startTime, int offset) {
        int startTimeSign = determineSign(startTime);
        int offsetSign = determineSign(offset);
        String startHrs = padZeroes(Integer.toString(startTime).replace("-", "")).substring(0, 2);
        String startMins = padZeroes(Integer.toString(startTime).replace("-", "")).substring(2, 4);
        String offsetHrs = padZeroes(Integer.toString(offset).replace("-", "")).substring(0, 2);
        String offsetMins = padZeroes(Integer.toString(offset).replace("-", "")).substring(2, 4);
        int adjustedMins = (((Integer.valueOf(startMins) * startTimeSign) + (Integer.valueOf(offsetMins) * offsetSign)));
        int carry = 0;
        if (adjustedMins < 0) {
            carry = -1;
        } else if (adjustedMins > 59) {
            carry = 1;
        }
        adjustedMins = adjustedMins % 60;
        int adjustedHrs = (((Integer.valueOf(startHrs) * startTimeSign)) + (Integer.valueOf(offsetHrs) * offsetSign)) + carry;
        adjustedHrs = adjustedHrs % 24;
        if (adjustedHrs < 0) {
            adjustedHrs = 24 + adjustedHrs;
        }
        return (adjustedHrs * 100) + adjustedMins;
    }

    /**
     * determine sign returns the sign of a given integer
     *
     * @param number, an integer
     * @return sign, -1 if number is negative, 1 if positive
     */
    private static int determineSign(int number) {
        if (number < 0) {
            return -1;
        }
        return 1;
    }

    /**
     * padZeroes adds leading zeroes to a given small string
     * Created by Littlesnowman88 on 13 June 2018
     * Precondition: hhmm has
     * If hhmm.length() > 4, nothing happens and hhmm is returned as-is.
     *
     * @param hhmm, a String representation of an integer-formatted time.
     * @return a fixed-size, four-character version of hhmm, or else hhmm if hhmm has 4 or more characters in it.
     */
    private static String padZeroes(String hhmm) {
        String hhmm_copy = hhmm;
        if (hhmm_copy.contains("-")) {
            hhmm_copy = hhmm.substring(1);
        }
        for (int i = hhmm_copy.length(); i < 4; i++) {
            hhmm_copy = "0" + hhmm_copy;
        }
        return hhmm_copy;
    }

    /**
     * timeToMinutes takes an hhmm formatted length and converts it to an integer number of minutes
     * Created by: Littlesnowman88 on 18 June 2018
     * @param timeLength, an hhmm-formatted time (as an integer)
     * Precondition: timeLength must be >= 0
     * @return the time as an integer number of minutes
     * EXCEPTIONS: Runtime exception thrown if timeLength < 0.
     */
    public static int timeToMinutes(int timeLength) {
        if (timeLength >= 0) {
            int hours = (timeLength / 100) * 60; //integer division, then * 60 to get hours
            int minutes = timeLength % 100; //leftover minutes once hours are saved.
            return hours + minutes;
        } else {
            throw new RuntimeException("ERROR: in timeToMinutes, timeLength was < 0. Length of an activity cannot be < 0.");
        }
    }

    /**
     * minutesToTime takes an integer number of minutes and converts it to an hhmm formatted time.
     * Created by: Littlesnowman88 on 18 June 2018
     * @param timeLength, an integer number of minutes
     * Precondition: timeLength must be >= 0
     * @return the length of time as an hhmm-formatted time.
     * EXCEPTIONS: Runtime exception thrown if timeLength < 0.
     */
    public static int minutesToTime(int timeLength) {
        if (timeLength >= 0) {
            int hours = (timeLength / 60) * 100; //integer division, then * 100 for time formatting
            int minutes = timeLength % 60; //getting levtover minutes once hours are saved.
            return hours + minutes;
        } else {
            throw new RuntimeException("ERROR: in timeToMinutes, timeLength was < 0. Length of an activity cannot be < 0.");
        }
    }

    /** getTimeZoneDifference takes two time zones & a date, and returns the difference (hours) between the zones
     *  Created by: Littlesnowman88
     *  Date Created: 13 June 2018
     *  @param base_zone, a TimeZone object
     *  @param alternate_zone, a TimeZone object
     *  @param: date, a String date formatted MM/dd/yyyy for determining if daylight Savings needs to be applied.
     *  @return the difference (hours) between the two timezone objects.
     *      //NOTE: if timezone 2 is ahead, the result will be positive (for a higher utc offset)
     *              if the timezone 2 is behind, the result will be negative (for a lower utc offset)
     */
    public static int getTimeZoneDifference(TimeZone base_zone, TimeZone alternate_zone, String date) {
        int milli_offset = determineMilliOffset(base_zone, alternate_zone, date);

        int ONE_HOUR = 1000 * 60 * 60; //1 second -> 1 minute -> 1 hour
        return (milli_offset / ONE_HOUR) * 100; //the difference, put into hhmm format
    }

    /**
     * determineMilliOffset takes two time zones & a date, and returns the difference (milliseconds) between the zones
     *      IMPORTANT: determineMilliOffset checks if the two zones are in Daylight Savings time adjusts accordingly
     * @param base_zone, the time zone at the event location
     * @param alternate_zone, the time zone of the user at remote or custom location
     * @param date, the date at which the times are being compared.
     * @return the difference between the two zones, in milliseconds (with Daylight Savings Time accounted for)
     */
    /**IMPORTANT NOTE FROM LITTLESNOWMAN88:
     * If an event takes place during a daylight savings change, whether because the event is originally
     *      scheduled over a daylight savings change or because a time zone adjustment caused the event
     *      to overlap with a daylight savings change, the event end time WILL BE WRONG. I am relying
     *      on user kindness for now because of time constraints.
     * This algorithm has another bug. The algorithm in LocalDB's getAdjustedDailySchedules
     *      gives this algorithm each of its days. If base_zone's day at a given time is different
     *      from alternate_zone's day (for example, comparing Denver to Tokyo), the daylight
     *      savings check could report an INCORRECT time offset because it can use only one day.
     *      Again, I am accepting this bug because of time constraints and because of the circumstances
     *      required to create the bug.
     */
    private static int determineMilliOffset(TimeZone base_zone, TimeZone alternate_zone, String date) {
        int milli_offset;
        try {
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MM/dd/yyyy");
            int alternate_offset = alternate_zone.getRawOffset();
            int base_offset = base_zone.getRawOffset();
            Date theDate = formatter.parse(date);
            if (alternate_zone.inDaylightTime(theDate)) {
                alternate_offset += alternate_zone.getDSTSavings();
            }
            if (base_zone.inDaylightTime(theDate)) {
                base_offset += base_zone.getDSTSavings();
            }
            milli_offset = alternate_offset - base_offset;
        }catch (Exception e) {
            e.printStackTrace();
            milli_offset = alternate_zone.getRawOffset() - base_zone.getRawOffset();
        }
        return milli_offset;
    }


}
