package org.lightsys.eventApp.views;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.lightsys.eventApp.data.ContactInfo;
import org.lightsys.eventApp.data.Info;
import org.lightsys.eventApp.data.LocationInfo;
import org.lightsys.eventApp.data.ScheduleInfo;
import org.lightsys.eventApp.tools.EventArrayAdapter;
import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by otter57 on 3/29/17.
 * Welcomes user to event
 */

public class WelcomeView extends Fragment {

    private ArrayList<Info> events = new ArrayList<>();
    private ArrayList<ScheduleInfo> schedule = new ArrayList<>();

    /* accessing shared preferences set by the settings activity */
    private SharedPreferences sharedPreferences;
    private String[] eventLocations;
    private TimeZone selectedTimeZone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.welcome, container, false);
        getActivity().setTitle("Welcome");

        LocalDB db = new LocalDB(getActivity());


        //set up notifications
        ListView eventList = v.findViewById(R.id.eventList);
        //TODO: Problem with notifications not quite working (works on Code-a-thon event, not on Dev Testing)
        events = db.getNotifications();


        //set up welcome message
        String welcomeMessage = db.getGeneral("welcome_message")!=null?db.getGeneral("welcome_message"):getString(R.string.default_welcome);
        ((TextView) v.findViewById(R.id.welcomeHeader)).setText(welcomeMessage);

        ArrayList<HashMap<String,String>> itemList = generateListItems();

        //set up notifications header if notifications or recent/upcoming events are available
        TextView header = v.findViewById(R.id.notificationsHeader);
        if (itemList.size() == 0){
            header.setVisibility(View.GONE);
        }else{
            header.setVisibility(View.VISIBLE);
            header.setBackgroundColor(Color.parseColor(db.getThemeColor("themeDark")));
        }

        // display title, content, and date posted for notification
        final EventArrayAdapter adapter = new EventArrayAdapter(getActivity(), itemList);

        // Not showing Notifications in list
        Log.d("List", "ItemList: " + itemList);

        eventList.setAdapter(adapter);


        return v;
    }

    /**
     * Formats the notification item into a HashMap ArrayList.
     *
     * @return a HashMap array with notification and calendar information, to be used in a SimpleAdapter
     */
    private ArrayList<HashMap<String,String>> generateListItems(){
        ArrayList<HashMap<String,String>> aList = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);


        //Insert Calendar Events
        //set up recent calendar info
        LocalDB db = new LocalDB(getActivity());

        /* allow Schedule View to access shared preferences for time zone information */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        eventLocations = LocationInfo.getEventLocations(db);
        selectedTimeZone = TimeZone.getTimeZone(sharedPreferences.getString("time_zone", eventLocations[0] ));

        db.setSharedPreferences(sharedPreferences);
        try {
            schedule = db.getFullSchedule();
        } catch (IndexOutOfBoundsException e) {
            //Database is empty
            return aList;
        }

        //Variable Setup
        ScheduleInfo earlyEvent = new ScheduleInfo(0, 1, "None");
        earlyEvent.setDay("00/00/0000");
        ScheduleInfo lateEvent = new ScheduleInfo(2400, 1, "None");
        lateEvent.setDay("99/99/9999");
        ScheduleInfo currentEvent = earlyEvent;
        ScheduleInfo nextEvent = lateEvent;

        Calendar calendar = Calendar.getInstance();
        String now = formatter.format(calendar.getTime());
        String today = now.substring(0, 10);
        //Get tomorrow's date
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrow = formatter.format(calendar.getTime()).substring(0,10);
        //Get yesterday's date
        calendar.add(Calendar.DAY_OF_YEAR, -2);
        String yesterday = formatter.format(calendar.getTime()).substring(0,10);
        //Reset Calendar
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        Integer time = Integer.parseInt(now.substring(11,13) + now.substring(14,16));

        Log.d("Calendar", "Current datetime is: " + formatter.format(calendar.getTime()));

        if (schedule.size() != 0){

            //Set currentEvent to the latest starting event before right now
            //Try to set nextEvent to earliest starting event today after right now
            for (ScheduleInfo event : schedule) {

                //If (event is today and started before now) and the event starts after the currentEvent started
                if (((event.getDay().equals(today) && event.getTimeStart() <= time))
                        && event.getTimeStart() > currentEvent.getTimeStart()) {
                    currentEvent = event;
                } else if (event.getDay().equals(today) && event.getTimeStart() >= time && event.getTimeStart() < nextEvent.getTimeStart()) {
                    nextEvent = event;
                }
            }
            //If there were no previous events today, set currentEvent to the latest event yesterday
            if (currentEvent == earlyEvent) {
                for (ScheduleInfo event : schedule) {
                    if (event.getDay().equals(yesterday) && event.getTimeStart() > currentEvent.getTimeStart()) {
                        currentEvent = event;
                    }
                }
            }
            //If there are no more events today, set nextEvent to the earliest event tomorrow
            if (nextEvent == lateEvent) {
                for (ScheduleInfo event : schedule) {
                    if (event.getDay().equals(tomorrow) && event.getTimeStart() < nextEvent.getTimeStart()) {
                        nextEvent = event;
                    }
                }
            }
        }

        Log.d("Event", "Current event is: " + currentEvent.getDesc() + " starting at: " + currentEvent.getTimeStart() + " on " + currentEvent.getDay());
        Log.d("Event", "Next event is: " + nextEvent.getDesc() + " starting at: " + nextEvent.getTimeStart() + " on " + nextEvent.getDay());


        HashMap<String, String> hm = new HashMap<>();
        if (nextEvent != lateEvent) {
            hm.put("title", "\tUpcoming");
            String timeString;
            // Properly format time
            if (String.valueOf(nextEvent.getTimeStart()).length() == 3) {
                timeString = String.valueOf(nextEvent.getTimeStart()).substring(0,1) + ":" + String.valueOf(nextEvent.getTimeStart()).substring(1);
            } else {
                timeString = String.valueOf(nextEvent.getTimeStart()).substring(0,2) + ":" + String.valueOf(nextEvent.getTimeStart()).substring(2);
            }
            // Set date to either "Today" or "Tomorrow" - no other options
            if (nextEvent.getDay().equals(today)) {
                hm.put("date", timeString + "\t\tToday");
            } else {
                hm.put("date", timeString + "\t\tTomorrow");
            }
            //Put location in description if given
            if (!"null".equals(nextEvent.getLocationName())) {
                hm.put("content", nextEvent.getDesc() + "\nLocation: " + nextEvent.getLocationName());
            } else {
                hm.put("content", nextEvent.getDesc());
            }
            hm.put("type", "event");
            hm.put("color", db.getThemeColor(nextEvent.getCategory()));
            aList.add(hm);
        }
        if (currentEvent != earlyEvent) {
            hm = new HashMap<>();
            if (currentEvent.getTimeStart() <= time && time <= currentEvent.getTimeEnd() && currentEvent.getDay().equals(today)) {
                hm.put("title", "\tRight Now");
            } else {
                hm.put("title", "\tMost Recently");
            }
            //Format time correctly to display like H:MM where H < 10 or HH:MM where H >= 10
            String timeString;
            if (String.valueOf(nextEvent.getTimeStart()).length() == 3) {
                timeString = String.valueOf(currentEvent.getTimeStart()).substring(0,1) + ":" + String.valueOf(currentEvent.getTimeStart()).substring(1);
            } else {
                timeString = String.valueOf(currentEvent.getTimeStart()).substring(0,2) + ":" + String.valueOf(currentEvent.getTimeStart()).substring(2);
            }
            //Put "Today" if the event is happening/happened today, else put "Yesterday"
            if (currentEvent.getDay().equals(today)) {
                hm.put("date", timeString + "\t\tToday");
            } else {
                hm.put("date", timeString + "\t\tYesterday");
            }
            //Put the location in the content if there is a location
            if (!"null".equals(currentEvent.getLocationName())) {
                hm.put("content", currentEvent.getDesc() + "\nLocation: " + currentEvent.getLocationName());
            } else {
                hm.put("content", currentEvent.getDesc());
            }
            hm.put("type", "event");
            hm.put("color", db.getThemeColor(currentEvent.getCategory()));
            aList.add(hm);
        }

        for(Info event : events){
            hm = new HashMap<>();
            hm.put("title", event.getHeader());
            hm.put("content", event.getBody());
            String date = null;
            try{
                calendar.setTime(formatter.parse(event.getDate()));
                date = getDatePrintOut(calendar);
            }catch(Exception e){
                e.printStackTrace();
            }

            hm.put("date", date);
            hm.put("type", "notification");
            aList.add(hm);
        }
        return aList;
    }

    //formats the calendar date for display
    private String getDatePrintOut(Calendar cal){
        Calendar calNow = Calendar.getInstance();
        if (calNow.get(Calendar.DATE) == cal.get(Calendar.DATE)) {
            return cal.get(Calendar.HOUR_OF_DAY) + ":" + minutesFormat(Integer.toString(cal.get(Calendar.MINUTE)));
        } else if (calNow.getTimeInMillis()-cal.getTimeInMillis() <= 604800000 && calNow.get(Calendar.DAY_OF_WEEK) != cal.get(Calendar.DAY_OF_WEEK)) {
            return dayIntToString(cal.get(Calendar.DAY_OF_WEEK)) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + minutesFormat(Integer.toString(cal.get(Calendar.MINUTE)));
        }else {
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            return formatter.format(cal.getTime());
        }
    }
    //set minutes in date to ## format
    private String minutesFormat(String minutes){
        return minutes.length() == 1? "0"+minutes:minutes;
    }

    //converts day number to day string
    private String dayIntToString(int day){
        switch(day){
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
        }
        return " ";
    }

}

