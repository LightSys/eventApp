package org.lightsys.sbcat.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.lightsys.sbcat.R;
import org.lightsys.sbcat.data.HousingInfo;
import org.lightsys.sbcat.tools.HousingAdapter;
import org.lightsys.sbcat.tools.LocalDB;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by otter57 on 3/28/17.
 *
 * Displays housing information, address, students, and drivers
 */

public class HousingView extends Fragment {

    private ArrayList<HousingInfo> housing;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.listview, container, false);
        getActivity().setTitle("Housing");


        ListView listview = (ListView)v.findViewById(R.id.infoList);
        LocalDB db = new LocalDB(getContext());
        housing = db.getHousing();

        ArrayList<HashMap<String, String>> itemList = generateListItems();
        Log.d(TAG, "onCreateView: " + housing.size());

        // display donor name, fund name, date, and amount for all gifts
        String[] from = {"host_info", "students", "driver"};
        int[] to = {R.id.infoText, R.id.studentsText, R.id.driver};
        final HousingAdapter adapter = new HousingAdapter(getActivity(), itemList, R.layout.housing_list_item, from, to, Color.parseColor(db.getThemeColor("themeDark")));

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new OnHostClicked());
        listview.setSelector(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed, null));


        return v;
    }

    private ArrayList<HashMap<String, String>> generateListItems() {
        ArrayList<HashMap<String, String>> aList = new ArrayList<>();
        String oldDriver = null;
        for (HousingInfo h : housing) {
            HashMap<String, String> hm = new HashMap<>();

            String info = h.getName();
            info += (h.getAddress()==null)? "":"\n"+ h.getAddress();
            info += (h.getPhone()==null)? "":"\n"+ h.getPhone();

            hm.put("host_info", info);
            hm.put("students", h.getStudents());

            if (!h.getDriver().equals(oldDriver)) {
                oldDriver = h.getDriver();
                hm.put("driver", "Driver: "+h.getDriver());
            } else {
                hm.put("driver", null);
            }
            aList.add(hm);
        }
        return aList;
    }

    private class OnHostClicked implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            final String address = housing.get(position).getAddress();
            final String phone = housing.get(position).getPhone();
            String title = "Go to maps or phone?";
            title = (address == null)?"Go to phone?":title;
            title = (phone == null)?"Go to maps?":title;

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
                    .setCancelable(false)
                    .setMessage(title);
                    if (address != null) {
                        builder.setPositiveButton(R.string.map_button, new DialogInterface.OnClickListener() {
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
                    }
            if (phone != null) {

                builder.setNegativeButton(R.string.phone_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phoneCall = "+" + phone.replaceAll("[^0-9.]", "");
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneCall, null));
                        try {
                            startActivity(intent);
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity(), "no Phone app found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
                    builder.setNeutralButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            }

        }
    }

