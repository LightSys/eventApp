package org.lightsys.eventApp.views;


import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.MapInfo;
import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.tools.MapExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Nate Gamble on 7/25/2019.
 *
 * Displays maps with points of interest, helps user find points of interest (like restrooms, dining areas, etc.)
 * Some code copied from:
 *      https://androidclarified.com/fusedlocationproviderclient-current-location-example/
 *      https://androidclarified.com/display-current-location-google-map-fusedlocationproviderclient/
 */

public class MapView extends Fragment {

    private ArrayList<MapInfo> maps;
    private LocalDB db;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private List<String> map_names;
    private List<String> map_options;
    private HashMap<String, List<String>> map_list_detail;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private static final int LOCATION_REQUEST_CODE = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_view, container, false);
        getActivity().setTitle("Maps");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If location services are turned off, request location services
            ActivityCompat.requestPermissions(getActivity(), new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        fetchLastLocation();

        db = new LocalDB(getContext());
        maps = db.getMaps();

        map_options = new ArrayList<>();
        map_options.add("Help me find...");
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If location services is turned off
            map_options.add("Show full map");
        } else {
            map_options.add("Show my location");
        }

        map_list_detail = new HashMap<>();
        for (int i = 0; i < maps.size(); i++) {
            // Add all maps to map_list_detail
            map_list_detail.put(maps.get(i).getName(), map_options);
    }

        // Set on click listeners for each textView
        // "help me find..." pops up recyclerView? with POI options
        // "show my location" and "show full map" show image with POIs on it (maybe condense into one option?
        // don't show "show my location" if location services are off (replace with "show full map")

        expandableListView = (ExpandableListView) v.findViewById(R.id.expandableListView);
        map_names = new ArrayList<>(map_list_detail.keySet());
        expandableListAdapter = new MapExpandableListAdapter(getContext(), map_names, map_list_detail);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getContext(),
                        map_names.get(groupPosition) + " List Expanded.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getContext(),
                        map_names.get(groupPosition) + " List Collapsed.",
                        Toast.LENGTH_SHORT).show();

            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Toast.makeText(
                        getContext(),
                        map_names.get(groupPosition)
                                + " -> "
                                + map_list_detail.get(
                                map_names.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT
                ).show();
                return false;
            }
        });


//             PrayerPartner Example
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






        return v;
    }


    private void fetchLastLocation(){
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = location;
                        Toast.makeText(getContext(), currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No Location recorded", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLastLocation();
                } else {
                    Toast.makeText(getContext(),"Location permission missing",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
