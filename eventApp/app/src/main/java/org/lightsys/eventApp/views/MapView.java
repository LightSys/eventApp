package org.lightsys.eventApp.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.MapInfo;
import org.lightsys.eventApp.tools.LocalDB;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Nate Gamble on 7/25/2019.
 *
 * Displays maps with points of interest, helps user find points of interest (like restrooms, dining areas, etc.)
 */

public class MapView extends Fragment {

    private ArrayList<MapInfo> maps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.listview, container, false);
        getActivity().setTitle("Maps");


        ListView listview = v.findViewById(R.id.infoList);
        LocalDB db = new LocalDB(getContext());
        maps = db.getMaps();

        Log.d("Maps", "maps: " + maps);
        for (int i = 0; i < maps.size(); i++) {
            Log.d("Maps", "maps[" + i + "]: " + maps.get(i));
        }

//        ArrayList<HashMap<String, String>> itemList = generateListItems();
//
//        // display host name (address and phone), students, and driver
//        String[] from = {"host_info", "students", "driver"};
//        int[] to = {R.id.infoText, R.id.studentsText, R.id.driver};
//        final MapAdapter adapter = new MapAdapter(getActivity(), itemList, from, to, Color.parseColor(db.getThemeColor("themeDark")));
//
//        listview.setAdapter(adapter);
//        listview.setOnItemClickListener(new OnHostClicked());
//        listview.setSelector(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed, null));



        return v;
    }

}
