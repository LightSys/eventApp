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

import org.lightsys.eventApp.data.Info;
import org.lightsys.eventApp.data.LocationInfo;
import org.lightsys.eventApp.data.ScheduleInfo;
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

        /* allow Schedule View to access shared preferences for time zone information */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
//        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        eventLocations = LocationInfo.getEventLocations(db);
        selectedTimeZone = TimeZone.getTimeZone(sharedPreferences.getString("time_zone", eventLocations[0] ));

        //set up notifications
        ListView eventList = v.findViewById(R.id.eventList);
        events = db.getNotifications();

        //set up notifications header if notifications are available
        TextView header = v.findViewById(R.id.notificationsHeader);
        if (events.size() == 0){
            header.setVisibility(View.GONE);
        }else{
            header.setVisibility(View.VISIBLE);
            header.setBackgroundColor(Color.parseColor(db.getThemeColor("themeDark")));
        }

        //set up welcome message
        String welcomeMessage = db.getGeneral("welcome_message")!=null?db.getGeneral("welcome_message"):getString(R.string.default_welcome);
        ((TextView) v.findViewById(R.id.welcomeHeader)).setText(welcomeMessage);

        ArrayList<HashMap<String,String>> itemList = generateListItems();

        // display title, content, and date posted for notification
        String[] from = {"title", "content", "date"};
        int[] to = {R.id.titleText, R.id.noteText, R.id.dateText};
        final SimpleAdapter adapter = new SimpleAdapter(getActivity(), itemList, R.layout.notification_item, from, to );

        eventList.setAdapter(adapter);

        //set up recent calendar info
        final ListView calendarList = v.findViewById(R.id.calendarList);
        db.setSharedPreferences(sharedPreferences);
        schedule = db.getFullSchedule();


        //Variable Setup
        ScheduleInfo earlyEvent = new ScheduleInfo(0, 1, "None");
        earlyEvent.setDay("00/00/0000");
        ScheduleInfo lateEvent = new ScheduleInfo(2400, 1, "None");
        ScheduleInfo currentEvent = earlyEvent;
        ScheduleInfo nextEvent = lateEvent;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HHmm");
        String now = formatter.format(calendar.getTime());
        String today = now.substring(0, 10);
        Integer day = Integer.parseInt(now.substring(3, 5));
        day += 1;
        String tomorrow = now.substring(0,3) + day.toString() + now.substring(5, 10);
        Integer time = Integer.parseInt(now.substring(11));


        //set up Calendar header if calendar events are available
        TextView calendarHeader = v.findViewById(R.id.calendarHeader);
        calendarHeader.setVisibility(View.VISIBLE);
        calendarHeader.setBackgroundColor(Color.parseColor(db.getThemeColor("themeDark")));

        if (schedule.size() != 0){

            Log.d("Welcome", "Event day format: " + schedule.get(0).getDay());
            Log.d("Welcome", "Calendar day format: " + today);

            //Set currentEvent to the latest starting event before right now
            //Try to set nextEvent to earliest starting event today after right now
            for (ScheduleInfo event : schedule) {

                //If (event is today and started before now or if event's start date is after currentEvent's start date) and the event starts after the currentEvent started
                boolean eventBeforeCurrent = (Integer.parseInt(event.getDay().substring(6)) > Integer.parseInt(currentEvent.getDay().substring(6)) && Integer.parseInt(event.getDay().substring(3, 5)) > Integer.parseInt(currentEvent.getDay().substring(3, 5)) && Integer.parseInt(event.getDay().substring(0,2)) > Integer.parseInt(currentEvent.getDay().substring(0,2)));
                if (((event.getDay() == today && event.getTimeStart() < time)
                        || eventBeforeCurrent)
                        && event.getTimeStart() > currentEvent.getTimeStart()) {
                    currentEvent = event;
                } else if (event.getDay() == today && event.getTimeStart() > time && event.getTimeStart() < nextEvent.getTimeStart()) {
                    nextEvent = event;
                }
            }
            //If there are no more events today, set nextEvent to the earliest event tomorrow
            if (nextEvent == lateEvent) {
                for (ScheduleInfo event : schedule) {
                    if (event.getDay() == tomorrow && event.getTimeStart() < nextEvent.getTimeStart()) {
                        nextEvent = event;
                    }
                }
            }
        }



        //Set ArrayList<HashMap<String, String>> to title, content, date
        ArrayList<HashMap<String, String>> calendarItemList = new ArrayList<>();

        HashMap<String, String> hm = new HashMap<>();
        hm.put("title", "Most Recent Event");
        //If no previous events today
        if (currentEvent == earlyEvent) {
            currentEvent.setDesc("No previous or current events today");
            hm.put("content", currentEvent.getDesc());
        } else {
            hm.put("date", String.valueOf(currentEvent.getTimeStart()));
            hm.put("content", currentEvent.getDesc() + " at " + currentEvent.getLocationName());
        }
        calendarItemList.add(hm);

        hm = new HashMap<>();
        hm.put("title", "Next Event");
        //If no more events today or tomorrow
        if (nextEvent == lateEvent) {
            nextEvent.setDesc("No more events today or tomorrow!");
            hm.put("content", nextEvent.getDesc());
        } else {
            hm.put("date", String.valueOf(nextEvent.getTimeStart()));
            hm.put("content", nextEvent.getDesc() + " at " + nextEvent.getLocationName());
        }
        calendarItemList.add(hm);


        final SimpleAdapter calendarAdapter = new SimpleAdapter(getActivity(), calendarItemList, R.layout.notification_item, from, to );
        calendarList.setAdapter(calendarAdapter);



        //Clickable listeners to expand/collapse notification list and calendar list
        TextView notificationHeader = v.findViewById(R.id.notificationsHeader);
        final ListView notificationList = v.findViewById(R.id.eventList);

        notificationHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notificationList.getVisibility() == View.VISIBLE) {
                    notificationList.setVisibility(View.GONE);
                } else if (notificationList.getVisibility() == View.GONE) {
                    notificationList.setVisibility(View.VISIBLE);
                }
            }
        });

        calendarHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calendarList.getVisibility() == View.VISIBLE) {
                    calendarList.setVisibility(View.GONE);
                } else if (calendarList.getVisibility() == View.GONE) {
                    calendarList.setVisibility(View.VISIBLE);
                }
            }
        });





        return v;
    }

    /**
     * Formats the notification item into a HashMap ArrayList.
     *
     * @return a HashMap array with notification information, to be used in a SimpleAdapter
     */
    private ArrayList<HashMap<String,String>> generateListItems(){
        ArrayList<HashMap<String,String>> aList = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss", Locale.US);

        for(Info event : events){
            HashMap<String,String> hm = new HashMap<>();
            hm.put("title", event.getHeader());
            hm.put("content", event.getBody());
            String date = null;
            try{
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(formatter.parse(event.getDate()));
                date = getDatePrintOut(calendar);
            }catch(Exception e){
                e.printStackTrace();
            }

            hm.put("date", date);
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

