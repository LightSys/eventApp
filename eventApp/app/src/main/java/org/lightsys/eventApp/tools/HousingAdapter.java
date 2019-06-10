package org.lightsys.eventApp.tools;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.lightsys.eventApp.R;

import java.util.List;
import java.util.Map;

/**
 * Created by otter57 on 4/5/17.
 *
 * adapter for housing list
 */

public class HousingAdapter extends SimpleAdapter {

    private final List<? extends Map<String, String>> data;
    private final int color;

    public HousingAdapter(Context context, List<? extends Map<String, String>> data, String[] from, int[] to, int color) {
        super(context, data, R.layout.housing_list_item, from, to);
        this.data = data;
        this.color = color;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View mView = super.getView(position, v, parent);

        //if driver is listed, makes bar visible and displays driver text
        TextView driver = mView.findViewById(R.id.driver);
        Log.d("Housing", "" + data.get(position).get("driver"));
        if (data.get(position).get("driver") != null && !data.get(position).get("driver").equals("Driver: No Assigned Driver")) {
            driver.setVisibility(View.VISIBLE);
            driver.setBackgroundColor(color);
        } else {
            driver.setVisibility(View.GONE);
        }
        return mView;
    }
}