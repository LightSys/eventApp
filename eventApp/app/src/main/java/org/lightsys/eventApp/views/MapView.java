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

    // TODO: finish class implementation
    // "help me find..." pops up recyclerView? with POI options
    // Change DataConnection, LocalDB and MapInfo to allow passing in of map image
    // "show my location" and "show full map" show image with POIs on it (maybe condense into one option?
    // Refresh options after location services allowed ("show full map" still displays until page refreshed)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_view, container, false);
        getActivity().setTitle("Maps");
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If location services are turned off, request location services
            ActivityCompat.requestPermissions(getActivity(), new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        db = new LocalDB(getContext());
        maps = db.getMaps();

        map_options = new ArrayList<>();
        map_options.add("Help me find...");
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If location services is turned off
            map_options.add("Show full map");
        } else {
            map_options.add("Show my location");
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
            fetchLastLocation();
        }

        map_list_detail = new HashMap<>();
        for (int i = 0; i < maps.size(); i++) {
            // Add all maps to map_list_detail
            map_list_detail.put(maps.get(i).getName(), map_options);
    }



        expandableListView = (ExpandableListView) v.findViewById(R.id.expandableListView);
        map_names = new ArrayList<>(map_list_detail.keySet());
        expandableListAdapter = new MapExpandableListAdapter(getContext(), map_names, map_list_detail);
        expandableListView.setAdapter(expandableListAdapter);
        // Checks if Map names have been expanded or collapsed. Probably not necessary
//        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
//            @Override
//            public void onGroupExpand(int groupPosition) {
//                Toast.makeText(getContext(),
//                        map_names.get(groupPosition) + " List Expanded.",
//                        Toast.LENGTH_SHORT).show();
//            }
//        });

//        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
//
//            @Override
//            public void onGroupCollapse(int groupPosition) {
//                Toast.makeText(getContext(),
//                        map_names.get(groupPosition) + " List Collapsed.",
//                        Toast.LENGTH_SHORT).show();
//
//            }
//        });

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
                // If "Help me find..." was clicked
                if (map_list_detail.get(map_names.get(groupPosition)).get(childPosition) == map_options.get(0)) {
                    // Show options for all POI names on map
                }
                // If "Show my location" was clicked
                else if (map_list_detail.get(map_names.get(groupPosition)).get(childPosition) == "Show my location") {
                    // Show full map with POIs and user location
                    displayMap(true, map_names.get(groupPosition));
                }
                // If "Show full map" was clicked
                else {
                    // Show full map with POIs in correct locations
                    displayMap(false, map_names.get(groupPosition));
                }
                return false;
            }
        });

        return v;
    }


    private void fetchLastLocation(){
        // Fetches user's last location (pretty much synonymous with current location)
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = location;
                        Toast.makeText(getContext(), currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    } else {
                        // Needed in case device is new and no locations recorded yet
                        //      or location services turned off for device (not just app)
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
                if (grantResult.length == 1 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLastLocation();
                } else {
                    Log.d("Permissions", "Location permissions denied");
                    // TODO: Toasts should display if permissions denied, but don't show
                    // getActivity() or getContext() should work, but neither do.
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), "android.permission.CAMERA")) {
                        Toast.makeText(getActivity(), R.string.disabled_location_permissions, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.denied_location_permissions, Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    // Displays the chosen map on screen with POIs as markers and user's location if permitted
    private void displayMap(boolean locationGranted, String mapName) {
        // TODO: Implement function
        // Show map (maybe new View, definitely new layout xml)
        // Populate map with POIs (use relative scaling with Lat and Long)
            // Divide map/screen size by difference in map Lat/Long to get how many pixels correspond to measurement in Lat/Long
            // Use ratio to place each POI on map
        if (locationGranted) {
            // Show user location on map (if user within map Lat/Long)
        }
    }

}
