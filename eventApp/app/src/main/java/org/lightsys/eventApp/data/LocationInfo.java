package org.lightsys.eventApp.data;

import org.lightsys.eventApp.tools.LocalDB;

/**
 * Created by Littlesnowman88 on 05/31/2018
 *IMPORTANT NOTE: THIS REPLACES THE JSON DATA UNTIL THE DATA CAN BE READ FROM A REAL JSON.
 * ONCE THIS DATA IS IMPLEMENTED IN A REAL JSON, THIS CLASS WILL BE OBSOLETE.
 * Object for getting an event's locations
 */

public final class LocationInfo {

    /* returns an array of string event locations
     */
    public static String[][] getEventLocations() {
        return new String[][] {
                {"Colorado Springs", "America/Denver"},
                {"Denver", "America/Denver"},
                {"Grand Rapids, MI", "America/Detroit"},
                {"Walmart", "America/Walmart"},
                {"Undecided", ""},
                {"", "America/Chicago"},
                {"", ""}
        };
    }
}
