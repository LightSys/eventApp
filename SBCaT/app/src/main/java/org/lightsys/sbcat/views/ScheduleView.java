package org.lightsys.sbcat.views;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import android.widget.TextView;
import android.widget.Toast;

import org.lightsys.sbcat.R;
import org.lightsys.sbcat.data.ContactInfo;
import org.lightsys.sbcat.data.ScheduleInfo;
import org.lightsys.sbcat.tools.LocalDB;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by otter57 on 3/28/17.
 *
 * Displays events schedules
 */

public class ScheduleView extends Fragment {

    private LinearLayout mainLayout;
    private HorizontalScrollView ScrollH, ScrollB;
    private LocalDB db;
    private Context context;
    private float density;
    private int width, height, textSize, paddingLg, padding, divider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.schedule_layout, container, false);
        getActivity().setTitle("Schedule");
        context = this.getContext();
        mainLayout = (LinearLayout) v.findViewById(R.id.main_layout);
        ScrollH = (HorizontalScrollView)v.findViewById(R.id.HeaderScroll);
        ScrollB = (HorizontalScrollView)v.findViewById(R.id.bodyScroll);
        density = getResources().getDisplayMetrics().density;

        db = new LocalDB(getContext());
        ArrayList<String> days = db.getDays();
        textSize = Math.round(15 * density);// + 0.5f);
        paddingLg = Math.round(10 * density);//) + 0.5f);
        padding = Math.round(5/2 * density);// + 0.5f);
        width = Math.round(50 * density);// + 0.5f);
        height = Math.round(75/2 * density);// + 0.5f);
        divider = Math.round(1/2*density);

        CreateHeader(days,v);

        //create time column
        ArrayList<Integer> times = db.getScheduleTimeRange();
        int startTime = times.get(0);

        for (int time = times.get(0); time < times.get(1); time+=15) {


            if ((time%100) == 60){
                time+=40;
            }
            times.add(time);
        }

        CreateTimeCol(times,v);

        for (String d : days) {
            ArrayList<ScheduleInfo> schedule = db.getScheduleByDay(d);

            int timeLengthMin = minutesBetweenTimes(times.get(0), times.get(1));
            int currentTime = 0;
            int i = 0;
            while(timeLengthMin > currentTime) {
                if (i >= schedule.size()) {
                    schedule.add(i, new ScheduleInfo(timeLengthMin - currentTime, "schedule_blank"));
                    currentTime = timeLengthMin;
                }else if (currentTime != minutesBetweenTimes(startTime, schedule.get(i).getTimeStart())) {
                    schedule.add(i, new ScheduleInfo(minutesBetweenTimes(startTime,schedule.get(i).getTimeStart())-currentTime, "schedule_blank"));
                    currentTime = minutesBetweenTimes(startTime,schedule.get(i+1).getTimeStart());
                } else {
                    currentTime += schedule.get(i).getTimeLength();
                }
                i++;
            }
            CreateColumn(schedule);

        }


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

        return v;
    }

    private int minutesBetweenTimes(int timeStart, int timeEnd){

        return ((timeEnd- ((int)Math.floor(timeEnd/100))*100)%60 + ((int)Math.floor(timeEnd/100))*60) - ((timeStart-((int)Math.floor(timeStart/100))*100)%60 + ((int)Math.floor(timeStart/100))*60);
    }

    private void CreateHeader(ArrayList<String> days, View v){
        LinearLayout dayLayout = (LinearLayout) v.findViewById(R.id.day_layout);
        LinearLayout Box = (LinearLayout) v.findViewById(R.id.topLeftBox);

        //create Time header
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(100, 75);
        TextView header = new TextView(context);
        header.setText(" ");
        header.setTypeface(null, Typeface.BOLD);
        header.setTextSize(textSize);
        header.setGravity(Gravity.CENTER_HORIZONTAL);
        header.setLayoutParams(headerParams);

        View divider_h = new View(context);
        divider_h.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
        divider_h.setLayoutParams(new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));

        Box.addView(header,0);
        Box.addView(divider_h,1);


        for (String d:days) {
            headerParams = new LinearLayout.LayoutParams(400, 100);
            header = new TextView(context);
            header.setText(d);
            header.setTypeface(null, Typeface.BOLD);
            header.setTextSize(textSize);
            header.setGravity(Gravity.CENTER);
            header.setLayoutParams(headerParams);

            divider_h = new View(context);
            divider_h.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
            divider_h.setLayoutParams(new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));

            dayLayout.addView(header);
            dayLayout.addView(divider_h);
        }
    }

    private void CreateTimeCol(ArrayList<Integer> times, View v){
        LinearLayout timeLayout = (LinearLayout) v.findViewById(R.id.time);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(100, 75);

        for (int i=2; i<times.size();i++) {
            int t = times.get(i);

            TextView time = new TextView(context);
            time.setGravity(Gravity.CENTER);
            String timeStr;
            if (t<1000) {
                timeStr = "0" + Integer.toString(t);
            }else{
                timeStr = Integer.toString(t);
            }
            time.setText(timeStr);
            //time.setPadding(paddingLg, paddingLg, padding, paddingLg);
            time.setLayoutParams(timeParams);

            View divider_v = new View(context);
            divider_v.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
            divider_v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1));

            timeLayout.addView(time);
            timeLayout.addView(divider_v);
        }
    }

    //create day column for schedule
    private void CreateColumn(ArrayList<ScheduleInfo> schedule){

        //create Column containing event info
        LinearLayout columnLayout = (LinearLayout) View.inflate(context, R.layout.schedule_column_layout, null);

        for (ScheduleInfo sch : schedule){

            LinearLayout iconsLayout = (LinearLayout) View.inflate(context, R.layout.schedule_event_item, null);
            TextView event = (TextView) iconsLayout.findViewById(R.id.eventText);


            iconsLayout.measure(0, 0);
            int heightCol = 75/15;
            int widthCol = 400;

            int color = Color.parseColor("#d6d4d4");

            if (sch != null) {
                heightCol = heightCol * sch.getTimeLength() + Math.round((sch.getTimeLength()/15)-1);
                event.setText(sch.getDesc());

                if (sch.getLocationName() != null){
                    ContactInfo contactInfo = db.getContactByName(sch.getLocationName());
                    if(contactInfo.getAddress() != null) {
                        ImageButton car = (ImageButton) View.inflate(context, R.layout.contact_button, null);
                        car.setImageResource(R.drawable.ic_car);
                        car.setPadding(0, 10, 5, 10);
                        iconsLayout.addView(car);
                        widthCol -= 60+5;

                        car.setOnClickListener(new OnCarClicked(contactInfo.getAddress()));
                    }
                    if (contactInfo.getPhone() != null){
                        ImageButton phone = (ImageButton) View.inflate(context, R.layout.contact_button, null);
                        phone.setImageResource(R.drawable.ic_call_phone);
                        phone.setPadding(0, 10, 5, 10);
                        iconsLayout.addView(phone);
                        widthCol -= 60+5;

                        phone.setOnClickListener(new OnPhoneClicked(contactInfo.getPhone()));
                    }
                    event.setLayoutParams(new LinearLayout.LayoutParams(widthCol, ViewGroup.LayoutParams.MATCH_PARENT));
                }

                color = Color.parseColor(db.getThemeColor(sch.getCategory()));

            }else{
                heightCol = heightCol*15;
            }

            int colors[] = { color , 0xfffffff,0xfffffff,0xfffffff,0xfffffff,0xfffffff, 0xfffffff };


            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,colors);
            gd.setCornerRadius(0f);
            gd.setShape(GradientDrawable.RECTANGLE);
            iconsLayout.setBackground(gd);

            //create cell of column containing event info
            iconsLayout.setLayoutParams(new FrameLayout.LayoutParams(400, heightCol));

            //textView padding
            event.setPadding(20,20,5,20);

            //divider between cells
            View divider_v = new View(context);
            divider_v.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
            divider_v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1));

            columnLayout.addView(iconsLayout);
            columnLayout.addView(divider_v);
        }


        View divider_h = new View(context);
        divider_h.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
        divider_h.setLayoutParams(new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));

        //add layout to your mainLayout
        mainLayout.addView(columnLayout);
        mainLayout.addView(divider_h);

    }

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

    private class OnPhoneClicked implements AdapterView.OnClickListener {
        final String phone;

        public OnPhoneClicked(String phone){
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


