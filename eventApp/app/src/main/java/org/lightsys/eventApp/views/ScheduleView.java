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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import org.lightsys.eventApp.data.ContactInfo;
import org.lightsys.eventApp.data.ScheduleInfo;
import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.R;

import java.util.ArrayList;

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
    private int width, height, textSizeHeader, paddingLg, padding, divider, textSizeContent, iconSize;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.schedule_layout, container, false);
        getActivity().setTitle("Schedule");
        context = this.getContext();
        mainLayout = v.findViewById(R.id.main_layout);
        ScrollH = v.findViewById(R.id.HeaderScroll);
        ScrollB = v.findViewById(R.id.bodyScroll);
        float density = (getResources().getDisplayMetrics().density)/2;

        db = new LocalDB(getContext());
        ArrayList<String> days = db.getDays();
        textSizeHeader = 30;
        textSizeContent = 14;
        paddingLg = Math.round(20*density);
        padding = Math.round(5*density);
        width = Math.round(100*density);
        height = Math.round(75*density);
        divider = Math.round(1*density);
        iconSize = Math.round(60*density);

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
        LinearLayout dayLayout = v.findViewById(R.id.day_layout);
        LinearLayout Box = v.findViewById(R.id.topLeftBox);

        //create Time header
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(width + padding + paddingLg, height + (2 * paddingLg));
        TextView header = new TextView(context);
        header.setText(" ");
        header.setTypeface(null, Typeface.BOLD);
        header.setTextSize(textSizeHeader);
        header.setGravity(Gravity.CENTER_HORIZONTAL);
        header.setLayoutParams(headerParams);

        View divider_h = new View(context);
        divider_h.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
        divider_h.setLayoutParams(new ViewGroup.LayoutParams(divider, ViewGroup.LayoutParams.MATCH_PARENT));

        Box.addView(header,0);
        Box.addView(divider_h,1);


        for (String d:days) {
            headerParams = new LinearLayout.LayoutParams(4*width+padding + paddingLg, width+(2*paddingLg));
            header = new TextView(context);
            header.setText(d);
            header.setTypeface(null, Typeface.BOLD);
            header.setTextSize(textSizeHeader);
            header.setGravity(Gravity.CENTER);
            header.setLayoutParams(headerParams);

            divider_h = new View(context);
            divider_h.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
            divider_h.setLayoutParams(new ViewGroup.LayoutParams(divider, ViewGroup.LayoutParams.MATCH_PARENT));

            dayLayout.addView(header);
            dayLayout.addView(divider_h);
        }
    }

    private void CreateTimeCol(ArrayList<Integer> times, View v){
        LinearLayout timeLayout = v.findViewById(R.id.time);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(width+paddingLg+padding, height+(2*paddingLg));

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
            time.setPadding(paddingLg, paddingLg, padding, paddingLg);
            time.setLayoutParams(timeParams);
            time.setTextSize(textSizeContent);

            View divider_v = new View(context);
            divider_v.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
            divider_v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,divider));

            timeLayout.addView(time);
            timeLayout.addView(divider_v);
        }
    }

    //create day column for schedule
    private void CreateColumn(ArrayList<ScheduleInfo> schedule){

        //create Column containing event info
        LinearLayout columnLayout = new LinearLayout(context);
        columnLayout.setLayoutParams(new LinearLayout.LayoutParams(width*4+padding + paddingLg, LinearLayout.LayoutParams.MATCH_PARENT));
        columnLayout.setOrientation(LinearLayout.VERTICAL);


        for (ScheduleInfo sch : schedule){

            LinearLayout iconsLayout = (LinearLayout) View.inflate(context, R.layout.schedule_event_item, null);
            TextView event = iconsLayout.findViewById(R.id.eventText);

            double heightCol = (height + (2.00* paddingLg))/15.00;
            int widthCol = 4*width;

            int color = Color.parseColor("#d6d4d4");

            if (sch != null) {
                heightCol = heightCol * sch.getTimeLength() + divider*(Math.ceil(sch.getTimeLength()/15)-1);

                event.setText(sch.getDesc());

                if (sch.getLocationName() != null){
                    ContactInfo contactInfo = db.getContactByName(sch.getLocationName());
                    if(contactInfo.getAddress() != null) {
                        ImageButton car = (ImageButton) View.inflate(context, R.layout.contact_button, null);
                        car.setLayoutParams(new ViewGroup.LayoutParams(iconSize,(int) Math.round(iconSize*1.2)));
                        car.setImageResource(R.drawable.ic_car);
                        car.setPadding(0, paddingLg/2, padding, paddingLg/2);
                        iconsLayout.addView(car);
                        widthCol -= iconSize;

                        car.setOnClickListener(new OnCarClicked(contactInfo.getAddress()));
                    }
                    if (contactInfo.getPhone() != null){
                        ImageButton phone = (ImageButton) View.inflate(context, R.layout.contact_button, null);
                        phone.setLayoutParams(new ViewGroup.LayoutParams(iconSize,(int) Math.round(iconSize*1.2)));
                        phone.setImageResource(R.drawable.ic_call_phone);
                        phone.setPadding(0, paddingLg/2, padding, paddingLg/2);
                        iconsLayout.addView(phone);
                        widthCol -= iconSize;

                        phone.setOnClickListener(new OnPhoneClicked(contactInfo.getPhone()));
                    }
                    event.setLayoutParams(new LinearLayout.LayoutParams(widthCol + padding + paddingLg, ViewGroup.LayoutParams.MATCH_PARENT));
                }

                color = Color.parseColor(db.getThemeColor(sch.getCategory()));

            }else{
                heightCol = heightCol*15 + 2* paddingLg;
            }

            int colors[] = { color , 0xfffffff,0xfffffff,0xfffffff,0xfffffff,0xfffffff, 0xfffffff };


            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,colors);
            gd.setCornerRadius(0f);
            gd.setShape(GradientDrawable.RECTANGLE);
            iconsLayout.setBackground(gd);

            //create cell of column containing event info
            iconsLayout.setLayoutParams(new FrameLayout.LayoutParams(width*4 + paddingLg + padding, (int)Math.round(heightCol)));

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


        View divider_h = new View(context);
        divider_h.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
        divider_h.setLayoutParams(new ViewGroup.LayoutParams(divider, ViewGroup.LayoutParams.MATCH_PARENT));

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


