package org.lightsys.sbcat.views;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.lightsys.sbcat.R;
import org.lightsys.sbcat.data.Info;
import org.lightsys.sbcat.tools.InfoAdapter;
import org.lightsys.sbcat.tools.LocalDB;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by otter57 on 3/29/17.
 *
 * Displays Informational Page
 */

public class InformationalPageView extends Fragment {

    private ArrayList<Info> HQInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.listview_no_line, container, false);
        String page = getArguments().getString("page");
        getActivity().setTitle(page);

        LocalDB db = new LocalDB(getContext());
        HQInfo = db.getHQ(page);

        ListView infoListView = (ListView) v.findViewById(R.id.infoList);


        ArrayList<HashMap<String, String>> itemList = generateListItems();
        // display schedule
        String[] from = {"header", "text"};
        int[] to = {R.id.headerText, R.id.text};
        InfoAdapter adapter = new InfoAdapter(getActivity(), itemList, from, to, Color.parseColor(db.getThemeColor("themeMedium")) );
        infoListView.setAdapter(adapter);

        infoListView.setSelector(android.R.color.transparent);

        return v;
    }

    private ArrayList<HashMap<String, String>> generateListItems() {
        ArrayList<HashMap<String, String>> aList = new ArrayList<>();
        String oldHeader = null;


        for (Info hq : HQInfo) {
            HashMap<String, String> hm = new HashMap<>();

            if (!hq.getHeader().equals(oldHeader)) {
                oldHeader = hq.getHeader();
                hm.put("header", oldHeader);
            } else {
                hm.put("header", null);
            }
            hm.put("text", hq.getBody().replace("~", getResources().getString(R.string.bullet_custom)));

            aList.add(hm);
        }
        return aList;
    }
}

