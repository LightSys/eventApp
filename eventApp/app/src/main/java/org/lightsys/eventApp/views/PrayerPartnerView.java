package org.lightsys.eventApp.views;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by otter57 on 3/29/17.
 *
 * Displays prayer partner groups
 */

public class PrayerPartnerView extends Fragment{

    private ArrayList<String> students;
    private LocalDB db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.listview, container, false);
        getActivity().setTitle("Prayer Partners");


        ListView listview = v.findViewById(R.id.infoList);
        db = new LocalDB(getContext());
        students = db.getPrayerPartners();

        ArrayList<HashMap<String, String>> itemList = generateListItems();

        // display group number and people in group
        String[] from = {"groupNum", "students"};
        int[] to = {R.id.groupNumberText, R.id.studentsText};
        final SimpleAdapter adapter = new SimpleAdapter(getActivity(), itemList, R.layout.prayer_partner_list_item, from, to){
            @Override
            public View getView(int position, View v, ViewGroup parent) {
                View mView = super.getView(position, v, parent);

                TextView groupNum = mView.findViewById(R.id.groupNumberText);
                groupNum.setTextColor(Color.parseColor(db.getThemeColor("themeMedium")));
                return mView;
            }
        };

        listview.setAdapter(adapter);
        listview.setSelector(android.R.color.transparent); //makes list items not visibly clickable

        return v;
    }

    //generates list of students and group numbers to be displayed
    private ArrayList<HashMap<String, String>> generateListItems() {
        ArrayList<HashMap<String, String>> aList = new ArrayList<>();

        for (int n=0; n<students.size();n++) {
            HashMap<String, String> hm = new HashMap<>();

            hm.put("groupNum", "Group " + Integer.toString(n+1));
            hm.put("students", students.get(n));

            aList.add(hm);
        }
        return aList;
    }
}