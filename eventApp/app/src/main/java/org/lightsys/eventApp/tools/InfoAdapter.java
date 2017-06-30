package org.lightsys.eventApp.tools;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.lightsys.eventApp.R;

import java.util.List;
import java.util.Map;

/**
 * Created by otter57 on 4/5/17.
 *
 * adapter for informational page
 */

public class InfoAdapter extends SimpleAdapter {

    private final List<? extends Map<String, String>> data;
    private final int color;

    public InfoAdapter(Context context, List<? extends Map<String, String>> data, String[] from, int[] to, int color) {
        super(context, data, R.layout.info_list_item, from, to);
        this.data = data;
        this.color = color;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View mView = super.getView(position, v, parent);

        TextView header = mView.findViewById(R.id.headerText);
        if (data.get(position).get("header") != null) {
            header.setVisibility(View.VISIBLE);
            header.setTextColor(color);
        } else {
            header.setVisibility(View.GONE);
        }


        return mView;
    }
}