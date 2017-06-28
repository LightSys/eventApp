package org.lightsys.sbcat.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class RefreshAdapter extends ArrayAdapter {

    private int color;
    private String[] data;
    LocalDB db;

    public RefreshAdapter(Context context, int resource, String [] data) {
        super(context, resource, data);
        this.data = data;
        db = new LocalDB(getContext());
        this.color = Color.parseColor(db.getThemeColor("themeColor"));
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View mView = super.getView(position, v, parent);
        TextView textView = (TextView) mView.findViewById(R.id.headerText);
        int time = Integer.parseInt(data[position]);

        if (time == -1){
            textView.setText("Never");
        }else if (time <60) {
            textView.append(" minute");
        } else {
            time = time/60;
            textView.setText(time + " hour");
        }
        if (time != 1 && time>0) {
            textView.append("s");
        }

        return mView;
    }
}