package org.lightsys.eventApp.data;

import org.lightsys.eventApp.tools.LocalDB;

/**
 * Created by Littlesnowman88 on 05/31/2018
 *
 * Object for getting an event's locations
 */

public final class LocationInfo {

    /* returns an array of string event locations
     * TODO: change this method to access multiple event locations from the JSON file.
     */
    public static String[] getEventLocations(LocalDB database) {
        return new String[] {database.getGeneral("time_zone")};
    }
}
