package org.lightsys.eventApp.views;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import org.lightsys.eventApp.data.ContactInfo;
import org.lightsys.eventApp.data.ScheduleInfo;
import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.R;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by otter57 on 3/28/17.
 *
 * Displays event's schedules
 */

public class ScheduleView extends Fragment {

    private LinearLayout mainLayout;
    private HorizontalScrollView ScrollH, ScrollB;
    private LocalDB db;
    private Context context;
    private int width, height, textSizeHeader, paddingLg, padding, divider, textSizeContent, iconSize, initScrollX, extraW, extraH;
    private String today = "";
    private float density, screenWidth, screenHeight;
    private Calendar calNow;
    private int schedHeight;
    private ArrayList<Integer> heights;
    private ArrayList<Integer> times;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.schedule_layout, container, false);
        getActivity().setTitle("Schedule");
        context = this.getContext();
        mainLayout = v.findViewById(R.id.main_layout);
        ScrollH = v.findViewById(R.id.HeaderScroll);
        ScrollB = v.findViewById(R.id.bodyScroll);

        //sets constants for schedule display (density changes values based on screen)
        density = (getResources().getDisplayMetrics().density)/2;
        db = new LocalDB(getContext());
        ArrayList<String> days = db.getDays();
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

        CreateHeader(days,v);

        // Get the event times
        heights = new ArrayList<Integer>();
        times = db.getScheduleTimeRange();
        int startTime = times.get(0);
        int endTime = times.get(1);
        for (String d : days) {
            ArrayList<ScheduleInfo> one_day = db.getScheduleByDay(d);
            for (ScheduleInfo one_item : one_day) {
                int oneItemStart = one_item.getTimeStart();
                int oneItemEnd = one_item.getTimeEnd();
                if (!times.contains(oneItemStart))
                    times.add(oneItemStart);
                if (!times.contains(oneItemEnd))
                    times.add(oneItemEnd);
            }
        }
        Collections.sort(times);

            //create time column

        /*for (int time = startTime; time < endTime; time+=15) {

            if ((time%100) == 60){
                time+=40;
            }
            times.add(time);
            if (time+15>times.get(1)){
                time +=15;
                if ((time%100) == 60){
                    time+=40;
                }
                times.set(1,time);
            }
        }*/

        CreateTimeCol(v);

        //creates schedule column for each day, filling in blank spots.
        for (String d : days) {
            ArrayList<ScheduleInfo> schedule = db.getScheduleByDay(d);

            int currentTime = startTime;
            int i = 0;
            while (currentTime < endTime) {
                if (i >= schedule.size()){
                    schedule.add(i, new ScheduleInfo(
                            currentTime,
                            minutesBetweenTimes(currentTime, endTime),
                            "schedule_blank"
                    ));
                } else if (currentTime != schedule.get(i).getTimeStart()) {
                    schedule.add(i, new ScheduleInfo(
                            currentTime,
                            minutesBetweenTimes(currentTime, schedule.get(i).getTimeStart()),
                            "schedule_blank"
                    ));
                }
                currentTime = schedule.get(i).getTimeEnd();
                i++;
            }

            /*int timeLengthMin = minutesBetweenTimes(times.get(0), times.get(1));
            int currentTime = 0;
            int i = 0;
            while(timeLengthMin > currentTime) {
                if (i >= schedule.size()) {
                    schedule.add(i, new ScheduleInfo(
                            timeLengthMin - currentTime,
                            "schedule_blank"
                    ));
                    currentTime = timeLengthMin;
                }else if (currentTime != minutesBetweenTimes(startTime, schedule.get(i).getTimeStart())) {
                    schedule.add(i, new ScheduleInfo(
                            startTime,
                            minutesBetweenTimes(startTime,schedule.get(i).getTimeStart())-currentTime,
                            "schedule_blank"
                    ));
                    currentTime = minutesBetweenTimes(startTime,schedule.get(i+1).getTimeStart());
                } else {
                    currentTime += schedule.get(i).getTimeLength();
                }
                i++;
            }*/
            CreateColumn(schedule, today.equals(d));
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

        });

        //set Scroll to current day
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                ScrollB.setScrollX(initScrollX);
                ScrollH.setScrollX(initScrollX);
            }
        }, 250);

        return v;
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
        LinearLayout dayLayout = v.findViewById(R.id.day_layout);
        LinearLayout Box = v.findViewById(R.id.topLeftBox);

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
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Calendar cal = Calendar.getInstance();
        calNow = Calendar.getInstance();
        calNow.setTimeZone(TimeZone.getDefault());
        cal.setTimeZone(TimeZone.getTimeZone(db.getGeneral("time_zone")));

        //create headers for each day
        for (String d:days) {
            try{
                cal.setTime(formatter.parse(d));
            }catch(Exception e){
                e.printStackTrace();
            }

            //create day header
            headerParams = new LinearLayout.LayoutParams(4*width+padding + paddingLg +extraW, width+(2*paddingLg));
            header = new TextView(context);
            //header.setText(dayIntToString(cal.get(Calendar.DAY_OF_WEEK)));
            header.setText(
                    cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + "\n" +
                            DateFormat.getDateInstance(DateFormat.MEDIUM).format(cal.getTime())
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

            //if day is today, highlight
            if (formatter.format(calNow.getTime()).equals(formatter.format(cal.getTime()))){
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
        schedHeight = width + (2*paddingLg) + divider*5;
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


        //check if schedule height is too small for screen
        //screenHeight = (int)Math.ceil(screenHeight - (width + (2 * paddingLg) + divider*5 + divider*(times.size()-2) + (height+(2*paddingLg))*(times.size()-2)));
        //extraH = screenHeight>0? (int)Math.ceil(screenHeight/(times.size()-2)):0;

        LinearLayout timeLayout = v.findViewById(R.id.time);
        //LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(width+paddingLg+padding+extraW, height+(2*paddingLg)+extraH);

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

    //create day column for schedule
    private void CreateColumn(ArrayList<ScheduleInfo> schedule, boolean isToday){

        //create Column containing event info
        LinearLayout columnLayout = new LinearLayout(context);
        columnLayout.setLayoutParams(new LinearLayout.LayoutParams(width*4+padding + paddingLg+extraW, LinearLayout.LayoutParams.MATCH_PARENT));
        columnLayout.setOrientation(LinearLayout.VERTICAL);

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

            //if column is current day, add red line at current time
            if (isToday){
                Calendar cal = Calendar.getInstance();
                String[]timeStr=new String[2];
                assert sch != null;
                String time = Integer.toString(sch.getTimeStart());
                while (time.length()<4){
                    time = "0" + time;
                }
                timeStr[0] = time.substring(0,2);
                timeStr[1] = time.substring(2,4);

                //adjust for time zone difference if device time zone is different from event time zone
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(timeStr[1]));
                cal.setTimeZone(TimeZone.getTimeZone(db.getGeneral("time_zone")));

                long timeDif = calNow.getTimeInMillis()-cal.getTimeInMillis();

                /*if (timeDif>=0 && timeDif<=sch.getTimeLength()*60000){

                    event.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_item));
                    iconsLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.current_event));
                    event.setTypeface(event.getTypeface(), Typeface.BOLD);

                    View redLine = new View(context);
                    RelativeLayout.LayoutParams redLineLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 4);

                    //math to figure out where red line should be located
                    int redYLoc = (int)((timeDif/60000)*(Math.round(heightCol))/(sch.getTimeLength()));

                    redLineLP.setMargins(Math.round(5*density), redYLoc, Math.round(5*density), 0);
                    redLine.setLayoutParams(redLineLP);
                    redLine.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                    iconsLayout.addView(redLine);

                }else{*/
                    event.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_day_left));
                    iconsLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
                    event.setTypeface(event.getTypeface(), Typeface.NORMAL);
                /*}*/

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


