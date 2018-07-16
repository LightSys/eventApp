package org.lightsys.eventApp.data;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * Created by Littlesnowman88 on 05/31/2018
 *
 * Object for getting an event's locations
 */

final public class TimeZoneInfo {

    /* returns an array of string time zones, based on the android/java TimeZone libraries */
    public static String[][] getAllTimeZones() {
        ArrayList<ArrayList<String>> parsed_time_zones = new ArrayList<>();
        String[] all_time_zones = TimeZone.getAvailableIDs();
        int all_time_zone_size = all_time_zones.length;
        String[] time_zone;
        ArrayList<String> other = new ArrayList<>();
        other.add("Other");
        ArrayList<String> first_continent = new ArrayList<>();
        time_zone = all_time_zones[0].split("/", 2);

        if(first_continent.size() == 1){
            other.add(time_zone[0]);
            first_continent.add("Other");
        }
        else {
            first_continent.add(time_zone[0]);
            first_continent.add(time_zone[1]);
            parsed_time_zones.add(first_continent);
            parsed_time_zones.add(other);
        }

        for(int i = 1; i < all_time_zone_size; i++){ //TODO: GO BACK TO A FOR EACH
            time_zone = all_time_zones[i].split("/", 2);
            if(time_zone.length == 1){
                other.add(all_time_zones[i]);
            }
            else{
                parsed_time_zones = addTimeZone(parsed_time_zones, time_zone);
            }
        }

        int ptz_size = parsed_time_zones.size();
        String[][] organized_time_zones = new String[ptz_size + 1][];
        String[] continent_names = new String[ptz_size];
        String[] continent_zones;

        for(int i = 0; i < ptz_size; i++){
            ArrayList<String> time_zones = parsed_time_zones.get(i);
            continent_names[i] = time_zones.remove(0);
            continent_zones = time_zones.toArray(new String[ptz_size-1]);
            organized_time_zones[i+1] = continent_zones;
        }
        organized_time_zones[0] = continent_names;
        return organized_time_zones;
    }

    private static ArrayList<ArrayList<String>> addTimeZone(ArrayList<ArrayList<String>> parsed_time_zones, String[] time_zone){
        ArrayList<String> new_continent = new ArrayList<>();
        for(ArrayList<String> continent: parsed_time_zones){
            if(time_zone[0].equals(continent.get(0))){
                continent.add(time_zone[1]);
                return parsed_time_zones;
            }
        }
        new_continent.add(time_zone[0]);
        new_continent.add(time_zone[1]);
        parsed_time_zones.add(new_continent);
        return parsed_time_zones;
    }
}
