package org.lightsys.eventApp.views;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.MapInfo;
import org.lightsys.eventApp.tools.LocalDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * Created by Nate Gamble on 7/25/2019.
 *
 * Displays maps with points of interest, helps user find points of interest (like restrooms, dining areas, etc.)
 */

public class MapView extends Fragment {

    private ArrayList<MapInfo> maps;
    private LocalDB db;
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> map_names;
    HashMap<String, List<String>> map_list_detail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.listview, container, false);
        getActivity().setTitle("Maps");

        db = new LocalDB(getContext());
        maps = db.getMaps();

        List<String> map_details = new ArrayList<>();
        map_details.add("Help me find...");
        map_details.add("Show my location");
        map_details.add("Show full map");

        Log.d("Maps", "maps: " + maps);
        for (int i = 0; i < maps.size(); i++) {
            Log.d("Maps", "maps[" + i + "]: " + maps.get(i));

            // Create map_list_view for each map
            // Set on click to expand
            // Set textViews within map_list_view to be "help me find..." and "show my location" or "show full map"
            // Set on click listeners for each textView
            // "help me find..." pops up recyclerView? with POI options
            // "show my location" and "show full map" show image with POIs on it (maybe condense into one option?
            // don't show "show my location" if location services are off (replace with "show full map")




            // PrayerPartner Example
//            final SimpleAdapter adapter = new SimpleAdapter(getActivity(), itemList, R.layout.prayer_partner_list_item, from, to){
//                @Override
//                public View getView(int position, View v, ViewGroup parent) {
//                    View mView = super.getView(position, v, parent);
//
//                    TextView groupNum = mView.findViewById(R.id.groupNumberText);
//                    groupNum.setTextColor(Color.parseColor(db.getThemeColor("themeMedium")));
//                    return mView;
//                }
//            };
        }






        return v;
    }

}
