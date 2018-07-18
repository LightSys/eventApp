package org.lightsys.eventApp.views;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.ContactInfo;
import org.lightsys.eventApp.data.LocationInfo;
import org.lightsys.eventApp.data.ScheduleInfo;
import org.lightsys.eventApp.tools.LocalDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by otter57 on 3/28/17.
 * Modified by Littlesnowman88 in summer 2018
 *
 * Displays event's schedules, taking time zones into account
 */

public class ScheduleView extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private LinearLayout mainLayout, Box;
    private View v;
    private HorizontalScrollView ScrollH, ScrollB;
    private LocalDB db;
    private Context context;
    private int width, height, textSizeHeader, paddingLg, padding, divider, textSizeContent, iconSize, initScrollX, extraW, extraH;
    private String today = "";
    private float screenWidth, screenHeight;
    private Calendar calNow;
    private ArrayList<Integer> heights;
    private ArrayList<Integer> times;

    /* accessing shared preferences set by the settings activity */
    private SharedPreferences sharedPreferences;
    private String[] eventLocations;
    private TimeZone selectedTimeZone;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.schedule_layout, container, false);
        getActivity().setTitle("Schedule");
        context = this.getContext();
        db = new LocalDB(getContext());

        mainLayout = v.findViewById(R.id.main_layout);
        ScrollH = v.findViewById(R.id.HeaderScroll);
        ScrollB = v.findViewById(R.id.bodyScroll);

        /* allow Schedule View to access shared preferences for time zone information */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        eventLocations = LocationInfo.getEventLocations(db);
        selectedTimeZone = TimeZone.getTimeZone(sharedPreferences.getString("time_zone", eventLocations[0] ));


        //sets constants for schedule display (density changes values based on screen)
        float density = (getResources().getDisplayMetrics().density)/2;
        textSizeHeader = 22;
        textSizeContent = 14;
        paddingLg = Math.round(20*density);
        padding = Math.round(5*density);
        width = Math.round(100*density);
        height = Math.round(75*density);
        divider = Math.round(1*density);
        iconSize = Math.round(60*density);
        //used to expand items if they do not fill screen
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels - getActionBarHeight();

        buildSchedule();
        return v;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("time_zone")) {
            selectedTimeZone = TimeZone.getTimeZone(sharedPreferences.getString("time_zone", eventLocations[0]));
            removeViews();
            buildSchedule();
        }
    }

    private void removeViews() {
        LinearLayout dayLayout = v.findViewById(R.id.day_layout);
        int dayLayoutNumChildren = dayLayout.getChildCount();
        for (int child=0; child < dayLayoutNumChildren; child++) {
            dayLayout.removeView(dayLayout.getChildAt(0));
        }
        int boxNumChildren = Box.getChildCount() - 1;
        for (int child=0; child < boxNumChildren; child++) { //DOES NOT DELETE ScrollH!!
            Box.removeView(Box.getChildAt(0));
        }
        int mainLayoutNumChildren = mainLayout.getChildCount();
        for (int child=0; child < mainLayoutNumChildren; child++) {
            mainLayout.removeView(mainLayout.getChildAt(0));
        }
        LinearLayout timeBar = v.findViewById(R.id.time);
        int timeNumChildren = timeBar.getChildCount();
        for (int child=0; child < timeNumChildren; child++) {
            timeBar.removeView(timeBar.getChildAt(0));
        }
    }

    //OnCreateView part II, also called by OnSharedPreferenceChangedListener
    private void buildSchedule() {
        //create the schedule for the event, and also get the days and time range (edges)
        db.setSharedPreferences(sharedPreferences);
        ArrayList<ScheduleInfo> schedule = db.getFullSchedule();
        ArrayList<String> days = db.getDays();
        times = db.getScheduleTimeRange();

        CreateHeader(days,v);

        //the earliest event start time and the latest event end time
        int startTime = times.get(0);
        int endTime = times.get(1);

        //insert other event start and end time into the times ArrayList
        int oneItemStart, oneItemEnd;
        //TODO: add event end into Schedule Info item to save on computation here?
        for (ScheduleInfo event : schedule) {
            oneItemStart = event.getTimeStart();
            oneItemEnd = event.getTimeEnd();
            if (!times.contains(oneItemStart))
                times.add(oneItemStart);
            if (!times.contains(oneItemEnd))
                times.add(oneItemEnd);
        }

        Collections.sort(times);

        heights = new ArrayList<>();
        CreateTimeCol(v);

        //creates schedule column for each day, filling in blank spots.
        ArrayList< ArrayList<ScheduleInfo> > scheduleByDay = getScheduleDays(days, schedule);
        ArrayList<ScheduleInfo> oneDay;
        int currentTime, numEvents;
        int scheduleByDaySize = scheduleByDay.size();
        for (int d = 0; d < scheduleByDaySize; d++) {
            oneDay = scheduleByDay.get(d);
            currentTime = startTime;
            numEvents = 0;
            //while not at the end of a day
            while (currentTime < endTime) {
                //if there are no more scheduled events left in the day, fill the ending blank space
                if (numEvents >= oneDay.size()) {
                    oneDay.add(numEvents, new ScheduleInfo(
                            currentTime,
                            minutesBetweenTimes(currentTime, endTime),
                            "schedule_blank"
                    ));
                    //else if the current time is not at an event start time (so a blank won't override an event)
                    //add a blank space between the current time and the next event's start time
                } else if (currentTime != oneDay.get(numEvents).getTimeStart()) {
                    oneDay.add(numEvents, new ScheduleInfo(
                            currentTime,
                            minutesBetweenTimes(currentTime, oneDay.get(numEvents).getTimeStart()),
                            "schedule_blank"
                    ));
                }
                //put the current time at the current event's end time
                currentTime = oneDay.get(numEvents).getTimeEnd();
                //increment the number of events
                numEvents++;
            }

            CreateColumn(oneDay, today.equals(oneDay.get(0).getDay()));
        }

        //synchronize scroll header and scroll body
        ScrollB.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                int scrollX = view.getScrollX();
                int scrollY = view.getScrollY();

                ScrollH.scrollTo(scrollX, scrollY);
                return false;
            }
        });

        ScrollH.setOnTouchListener(new View.OnTouchListener(){

                                       @Override
                                       public boolean onTouch(View view, MotionEvent event) {

                                           int scrollX = view.getScrollX();
                                           int scrollY = view.getScrollY();

                                           ScrollB.scrollTo(scrollX, scrollY);
                                           return false;
                                       }
                                   }
        );

        //set Scroll to current day
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                ScrollB.setScrollX(initScrollX);
                ScrollH.setScrollX(initScrollX);
            }
        }, 250);
    }

    @Override
    public void onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    //gets the number of minutes between two clock times
    private int minutesBetweenTimes(int timeStart, int timeEnd){

        return ((timeEnd- ((int)Math.floor(timeEnd/100))*100)%60 + ((int)Math.floor(timeEnd/100))*60) - ((timeStart-((int)Math.floor(timeStart/100))*100)%60 + ((int)Math.floor(timeStart/100))*60);
    }

    //subtracted from total height for schedule display height minimum
    public int getActionBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            result = result + TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        return result;
    }

    private void CreateHeader(ArrayList<String> days, View v){
        Box = v.findViewById(R.id.topLeftBox);
        LinearLayout dayLayout = v.findViewById(R.id.day_layout);

        int scrollWidth = 0;

        //check if schedule width is too small for screen
        screenWidth = screenWidth - (width +padding+paddingLg) - days.size()*(4*width+padding + paddingLg);
        extraW = screenWidth>0 ? Math.round(screenWidth/(days.size()+1)):0;

        //create Time header
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(width + padding + paddingLg+extraW, width + (2 * paddingLg));
        TextView header = new TextView(context);
        header.setText("Event\nTime:");
        header.setTypeface(null, Typeface.BOLD);
        header.setTextSize(textSizeContent);
        header.setGravity(Gravity.CENTER);
        header.setLayoutParams(headerParams);

        //divider between each day header
        View dividerHorizontal = new View(context);
        dividerHorizontal.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
        dividerHorizontal.setLayoutParams(new ViewGroup.LayoutParams(divider, ViewGroup.LayoutParams.MATCH_PARENT));

        //divider between header and body
        View dividerVertical1 = new View(context);
        dividerVertical1.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
        dividerVertical1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,divider*5));

        //add time header and divider
        LinearLayout headerBox1 = new LinearLayout(context);
        headerBox1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerBox1.setOrientation(LinearLayout.VERTICAL);
        headerBox1.addView(header, 0);
        headerBox1.addView(dividerVertical1, 1);

        Box.addView(headerBox1,0);
        Box.addView(dividerHorizontal,1);

        //prep to convert date to day of week for schedule display
        Log.d("Selected_Time_Zone ", selectedTimeZone.toString());
        TimeZone calTimeZone = selectedTimeZone;
        Calendar cal = Calendar.getInstance(calTimeZone, Locale.getDefault());
        Log.d("Time_Zone_Verify", cal.getTimeZone().toString());
        calNow = Calendar.getInstance(TimeZone.getDefault());

        SimpleDateFormat inputFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        SimpleDateFormat dayOutputFormatter = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat dateOutputFormatter = new SimpleDateFormat("MMMM dd yyyy", Locale.getDefault());
        inputFormatter.setTimeZone(calTimeZone); dayOutputFormatter.setTimeZone(calTimeZone); dateOutputFormatter.setTimeZone(calTimeZone);

        //create headers for each day
        Date storedDate = null;
        for (String d:days) {
            try{
                storedDate = inputFormatter.parse(d);
                cal.setTime(storedDate);
            }catch(Exception e){
                e.printStackTrace();
            }

            //create day header
            headerParams = new LinearLayout.LayoutParams(4*width+padding + paddingLg +extraW, width+(2*paddingLg));
            header = new TextView(context);
            header.setText(
                    (dayOutputFormatter.format(storedDate) + "\n" +
                            dateOutputFormatter.format(storedDate))
            );
            header.setTypeface(null, Typeface.BOLD);
            header.setTextSize(textSizeHeader);
            header.setGravity(Gravity.CENTER);
            header.setLayoutParams(headerParams);

            //divider between header items
            dividerHorizontal = new View(context);
            dividerHorizontal.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
            dividerHorizontal.setLayoutParams(new ViewGroup.LayoutParams(divider, ViewGroup.LayoutParams.MATCH_PARENT));

            LinearLayout headerBox = new LinearLayout(context);
            headerBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            headerBox.setOrientation(LinearLayout.VERTICAL);

            //divider between header and body
            View dividerVertical = new View(context);
            dividerVertical.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
            dividerVertical.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,divider*5));

            //add header and divider
            headerBox.addView(header);
            headerBox.addView(dividerVertical);

            dayLayout.addView(dividerHorizontal);
            dayLayout.addView(headerBox);

            //TODO: this is where the today's color change will get set. Why no use isToday?
            //if day is today, highlight
            if (dateOutputFormatter.format(calNow.getTime()).equals(dateOutputFormatter.format(cal.getTime()))){
                today = d;
                initScrollX=scrollWidth;
                header.setTextColor(ContextCompat.getColor(context, R.color.color_blue));
                header.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_day_header));
                dividerVertical.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.color_blue));

            //if not today scroll past day
            }else{
                scrollWidth+=4*width+padding + paddingLg+divider +extraW;
            }
        }
    }

    private void CreateTimeCol(View v) {

        // compute the overall schedule height
        int schedHeight = width + (2*paddingLg) + divider*5;
        for(int i=0; i < times.size()-1; i++) {
            // We're using pow() as a transfer function so that longer time slots are longer,
            // but not proportionately longer, so we don't eat up too much screen real estate with
            // really long time slots.  With transferPower = 1.0, it is proportional.  With
            // it less than 1.0, compression happens > 15 min and expansion happens < 15 min.
            double transferPower = 0.5;
            int oneHeight = (int)Math.ceil(Math.pow(minutesBetweenTimes(times.get(i), times.get(i+1))/15.0, transferPower)*(height + (2*paddingLg)));
            heights.add(i, oneHeight);
            schedHeight += (oneHeight + divider);
        }

        // Adjust height as needed.
        extraH = ((int)screenHeight) - schedHeight;
        int extraHRemaining = extraH;
        if (extraH > 0 && times.size() > 1) {
            for(int i=0; i < times.size()-1; i++) {
                int oneExtra = extraH / (times.size()-1);
                if (i == times.size() - 2)
                    oneExtra = extraHRemaining;
                heights.set(i, heights.get(i) + oneExtra);
                extraHRemaining -= oneExtra;
            }
        }

        LinearLayout timeLayout = v.findViewById(R.id.time);

        //create time header for each time (exclude 1st 2 times which are start and end times)
        for (int i=0; i<times.size()-1;i++) {
            int t = times.get(i);
            int h = heights.get(i) - divider;

            TextView time = new TextView(context);
            time.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            String timeStr;
            //set time format to ####
            if (t<1000) {
                timeStr = "0" + Integer.toString(t);
            }else{
                timeStr = Integer.toString(t);
            }

            time.setText(timeStr);
            time.setPadding(paddingLg, paddingLg, padding, paddingLg);
            LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(width+paddingLg+padding+extraW, h);
            time.setLayoutParams(timeParams);
            time.setTextSize(textSizeContent);

            //divider between time column and schedule columns
            View divider_v = new View(context);
            divider_v.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
            divider_v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,divider));

            timeLayout.addView(time);
            timeLayout.addView(divider_v);
        }
    }

    /**
     * getScheduleDays splits up the Schedule into a 2-dimensional array, days x events per day
     * @param scheduleDays, the string MM/dd/yyyy names of the dates within the schedule
     * @param schedule, the schedule to be split up
     * @return: a 2 dimensional array, days x events per day
     */
    private ArrayList< ArrayList<ScheduleInfo> > getScheduleDays(ArrayList<String> scheduleDays, ArrayList<ScheduleInfo> schedule) {
        ArrayList< ArrayList<ScheduleInfo> > scheduleByDay = new ArrayList <>();
        ArrayList<ScheduleInfo> oneDay;
        String day;
        int activityIndex = 0;
        int scheduleSize = schedule.size();
        int scheduleDaysSize = scheduleDays.size();
        if ((scheduleDaysSize > 0) && (scheduleSize > 0))
        for (int d = 0; d < scheduleDaysSize; d++) {
            day = scheduleDays.get(d);
            oneDay = new ArrayList<>();
            //while still on day d, add activities into "oneDay"
            while (schedule.get(activityIndex).getDay().equals(day)) {
                oneDay.add(schedule.get(activityIndex));
                activityIndex++;
                if (activityIndex == scheduleSize) {break;}
            }
            scheduleByDay.add(d, oneDay);

        }
        return scheduleByDay;
    }


    //create day column for schedule
    private void CreateColumn(ArrayList<ScheduleInfo> schedule, boolean isToday){

        //create Column containing event info
        LinearLayout columnLayout = new LinearLayout(context);
        columnLayout.setLayoutParams(new LinearLayout.LayoutParams(width*4+padding + paddingLg+extraW, LinearLayout.LayoutParams.MATCH_PARENT));
        columnLayout.setOrientation(LinearLayout.VERTICAL);
        //TODO: this is where the today's color change will get set
        if (isToday){
            columnLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_day_right));
        }

        for (ScheduleInfo sch : schedule){

            RelativeLayout iconsLayout = (RelativeLayout) View.inflate(context, R.layout.schedule_event_item, null);
            TextView event = iconsLayout.findViewById(R.id.eventText);

            double heightCol = (extraH+height + (2.00* paddingLg))/15.00;
            int widthCol = 4*width + extraW;

            int color = Color.parseColor("#d6d4d4");

            if (sch != null) {
                int timesIndex = times.indexOf(sch.getTimeStart());
                heightCol = 0;
                while(times.get(timesIndex) < sch.getTimeEnd()) {
                    heightCol += heights.get(timesIndex);
                    timesIndex++;
                }
                heightCol -= divider;
                //heightCol = heightCol * sch.getTimeLength() + divider*(Math.round(sch.getTimeLength()/15)-1);
                //heightCol = sch.getTimeLength()%15!=0?heightCol + 0.5 *divider:heightCol;

                event.setText(sch.getDesc());

                if (sch.getLocationName() != null){
                    ContactInfo contactInfo = db.getContactByName(sch.getLocationName());

                    RelativeLayout.LayoutParams iconLPPhone = new RelativeLayout.LayoutParams(iconSize,(int) Math.round(iconSize*1.2));
                    iconLPPhone.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                    RelativeLayout.LayoutParams iconLPCar = new RelativeLayout.LayoutParams(iconSize,(int) Math.round(iconSize*1.2));

                    // if phone number is present, add phone icon to event
                    if (contactInfo.getPhone() != null){
                        ImageButton phone = (ImageButton) View.inflate(context, R.layout.contact_button, null);
                        phone.setLayoutParams(iconLPPhone);
                        phone.setImageResource(R.drawable.ic_call_phone);
                        phone.setPadding(0, paddingLg/2, padding, paddingLg/2);
                        phone.setId(R.id.phoneIcon);

                        iconsLayout.addView(phone);
                        widthCol -= iconSize;

                        phone.setOnClickListener(new OnPhoneClicked(contactInfo.getPhone()));
                        iconLPCar.addRule(RelativeLayout.LEFT_OF, R.id.phoneIcon);

                    }else{
                        iconLPCar.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    }
                    // if address is present, add car icon to event
                    if(contactInfo.getAddress() != null) {
                        ImageButton car = (ImageButton) View.inflate(context, R.layout.contact_button, null);
                        car.setLayoutParams(iconLPCar);
                        car.setImageResource(R.drawable.ic_car);
                        car.setPadding(0, paddingLg/2, padding, paddingLg/2);
                        iconsLayout.addView(car);
                        widthCol -= iconSize;

                        car.setOnClickListener(new OnCarClicked(contactInfo.getAddress()));
                    }
                    event.setLayoutParams(new RelativeLayout.LayoutParams(widthCol + padding + paddingLg, RelativeLayout.LayoutParams.MATCH_PARENT));
                }

                //add background color based on type of event
                color = Color.parseColor(db.getThemeColor(sch.getCategory()));

            }else{
                heightCol = heightCol*15 + 2* paddingLg;
            }


            //TODO: ALSO SET THE EVENT'S COLOR TO BRIGHT YELLOW

            //if column is current day, add red line at current time
            if (isToday){
                Calendar cal = Calendar.getInstance();
                String[]timeStr=new String[2];
                assert sch != null;
                String time = Integer.toString(sch.getTimeStart());
                // converts something like 3:45 -> 03:45. (technically 345->0345)
                while (time.length()<4){
                    time = "0" + time;
                }
                timeStr[0] = time.substring(0,2);   //hour
                timeStr[1] = time.substring(2,4);   //minute


                //adjust for time zone difference if device time zone is different from event time zone
                cal.setTimeZone(TimeZone.getTimeZone(db.getGeneral("time_zone")));
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(timeStr[1]));

                event.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_day_left));
                iconsLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                event.setTypeface(event.getTypeface(), Typeface.NORMAL);
            }

            //gradient background to show event types
            int colors[] = { color , 0xfffffff,0xfffffff,0xfffffff,0xfffffff,0xfffffff, 0xfffffff };
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,colors);
            gd.setCornerRadius(0f);
            gd.setShape(GradientDrawable.RECTANGLE);
            iconsLayout.setBackground(gd);

            //create cell of column containing event info
            iconsLayout.setLayoutParams(new FrameLayout.LayoutParams(width*4 + paddingLg + padding+extraW, (int)Math.round(heightCol)));

            //textView padding
            event.setPadding(paddingLg,paddingLg,padding,paddingLg);
            event.setTextSize(textSizeContent);

            //divider between cells
            View divider_v = new View(context);
            divider_v.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
            divider_v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,divider));

            columnLayout.addView(iconsLayout);
            columnLayout.addView(divider_v);
        }

        //divider between day columns
        View dividerDay = new View(context);
        dividerDay.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
        dividerDay.setLayoutParams(new ViewGroup.LayoutParams(divider, ViewGroup.LayoutParams.MATCH_PARENT));

        //add layout to your mainLayout
        mainLayout.addView(columnLayout);
        mainLayout.addView(dividerDay);
    }

    //asks user then opens maps
    private class OnCarClicked implements AdapterView.OnClickListener {
        final String address;

        public OnCarClicked(String address){
            this.address = address;
        }

        @Override
        public void onClick(View view){
            view.setBackgroundResource(R.drawable.button_pressed);
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setMessage("Go to maps?");

            alert.setCancelable(false)
                    .setPositiveButton(R.string.map_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse("google.navigation:q=" + address));
                            try {
                                startActivity(intent);
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(getActivity(), "No Map app Found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            alert.setNeutralButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }

    //asks user then opens phone
    private class OnPhoneClicked implements AdapterView.OnClickListener {
        final String phone;

        private OnPhoneClicked(String phone){
            this.phone = phone;
        }

        @Override
        public void onClick(View view){
            view.setBackgroundResource(R.drawable.button_pressed);
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setMessage("Go to phone?");

            alert.setNegativeButton(R.string.phone_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String phoneCall = "+" + phone.replaceAll("[^0-9.]", "");
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneCall, null));
                    try {
                        getActivity().startActivity(intent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), "no Phone app found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            alert.setNeutralButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }
}


