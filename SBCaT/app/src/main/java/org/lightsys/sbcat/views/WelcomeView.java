package org.lightsys.sbcat.views;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.lightsys.sbcat.R;
import org.lightsys.sbcat.data.Info;
import org.lightsys.sbcat.tools.LocalDB;

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
    private LocalDB db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.welcome, container, false);
        getActivity().setTitle("Welcome");

        db = new LocalDB(getActivity());

        //set up notifications
        ListView eventList = (ListView) v.findViewById(R.id.eventList);
        events = db.getNotifications();

        //set up notifications header if notifications are available
        TextView header = (TextView)v.findViewById(R.id.notificationsHeader);
        if (events.size() == 0){
            header.setVisibility(View.GONE);
        }else{
            header.setVisibility(View.VISIBLE);
            header.setBackgroundColor(Color.parseColor(db.getThemeColor("themeDark")));
        }

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
        ArrayList<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();
        SimpleDateFormat formatter = new SimpleDateFormat("mm/dd/yyyy hh:mm:ss", Locale.US);

        for(Info event : events){
            HashMap<String,String> hm = new HashMap<String,String>();
            hm.put("title", event.getHeader());
            hm.put("content", event.getBody());
            String date = null;
            try{
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(formatter.parse(event.getDate()));
                date = new SimpleDateFormat("EEEE", Locale.US).format(calendar.get(Calendar.DAY_OF_WEEK)) + " " +calendar.get(Calendar.HOUR_OF_DAY) + ":" +calendar.get(Calendar.MINUTE);
            }catch(Exception e){
                e.printStackTrace();
            }

            hm.put("date", date);
            aList.add(hm);
        }
        return aList;
    }
}

