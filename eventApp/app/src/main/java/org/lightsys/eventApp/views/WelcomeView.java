package org.lightsys.eventApp.views;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.lightsys.eventApp.data.Info;
import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.R;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by otter57 on 3/29/17.
 *
 * Welcomes user to code-a-thon
 */

public class WelcomeView extends Fragment {

    private ArrayList<Info> events = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.welcome, container, false);
        getActivity().setTitle("Welcome");

        LocalDB db = new LocalDB(getActivity());

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
        String welcomeMessage = db.getGeneral("welcome_message")!=null?db.getGeneral("welcome_message"):"Welcome!";
        ((TextView) v.findViewById(R.id.welcomeHeader)).setText(welcomeMessage);

        ArrayList<HashMap<String,String>> itemList = generateListItems();

        // display donor name, fund name, date, and amount for all gifts
        String[] from = {"title", "content", "date"};
        int[] to = {R.id.titleText, R.id.noteText, R.id.dateText};
        final SimpleAdapter adapter = new SimpleAdapter(getActivity(), itemList, R.layout.notification_item, from, to );

        eventList.setAdapter(adapter);

        return v;
    }

    /**
     * Formats the gift information into a HashMap ArrayList.
     *
     * @return a HashMap array with gift information, to be used in a SimpleAdapter
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
    private String minutesFormat(String minutes){
        return minutes.length() == 1? "0"+minutes:minutes;
    }

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
