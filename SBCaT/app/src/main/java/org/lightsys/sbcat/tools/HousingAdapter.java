package org.lightsys.sbcat.tools;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.lightsys.sbcat.R;

import java.util.List;
import java.util.Map;

/**
 * Created by otter57 on 4/5/17.
 *
 * adapter for housing list
 */

public class HousingAdapter extends SimpleAdapter {

    private final List<? extends Map<String, String>> data;
    private int color;

    public HousingAdapter(Context context, List<? extends Map<String, String>> data,
                          int resource, String[] from, int[] to, int color) {
        super(context, data, resource, from, to);
        this.data = data;
        this.color = color;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View mView = super.getView(position, v, parent);

        TextView driver = (TextView) mView.findViewById(R.id.driver);
        if (data.get(position).get("driver") != null) {
            driver.setVisibility(View.VISIBLE);
            driver.setBackgroundColor(color);

        } else {
            driver.setVisibility(View.GONE);
        }
        return mView;
    }
}