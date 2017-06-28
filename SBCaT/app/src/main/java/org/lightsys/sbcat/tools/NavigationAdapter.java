package org.lightsys.sbcat.tools;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.lightsys.sbcat.R;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by otter57 on 4/5/17.
 *
 * adapter for housing list
 */

public class NavigationAdapter extends SimpleAdapter {

    private Context context;
    private int color;
    private ArrayList<HashMap<String, String>> data;

    public NavigationAdapter(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int [] to) {
        super(context, data, resource, from, to);
        this.context = context;
        this.data = data;
        LocalDB db = new LocalDB(context);
        this.color = Color.parseColor(db.getThemeColor("themeColor"));
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View mView = super.getView(position, v, parent);

        ImageView icon = ((ImageView) mView.findViewById(R.id.iconView));
        icon.setImageDrawable(ContextCompat.getDrawable(context,Integer.parseInt(data.get(position).get("icon"))));
        icon.setColorFilter(ContextCompat.getColor(context, R.color.darkGray));

        if (position==0){
            icon.setColorFilter(color);
            ((TextView) mView.findViewById(R.id.nav_item)).setTextColor(color);
        }

        return mView;
    }

}