package org.lightsys.eventApp.tools;

import android.content.Context;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.lightsys.eventApp.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by otter57 on 4/5/17.
 *
 * adapter for housing list
 */

public class NavigationAdapter extends SimpleAdapter {

    private final Context context;
    private final int color;
    private final ArrayList<HashMap<String, String>> data;

    public NavigationAdapter(Context context, ArrayList<HashMap<String, String>> data, String[] from, int [] to) {
        super(context, data, R.layout.drawer_list_item, from, to);
        this.context = context;
        this.data = data;
        LocalDB db = new LocalDB(context);
        this.color = Color.parseColor(db.getThemeColor("themeColor"));
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View mView = super.getView(position, v, parent);

        //set specified icon for navigation menu
        ImageView icon = mView.findViewById(R.id.iconView);
        icon.setImageDrawable(ContextCompat.getDrawable(context,Integer.parseInt(data.get(position).get("icon"))));
        icon.setColorFilter(ContextCompat.getColor(context, R.color.darkGray));

        //sets Notification tab as preselected
        if (position==0){
            icon.setColorFilter(color);
            ((TextView) mView.findViewById(R.id.nav_item)).setTextColor(color);
        }

        return mView;
    }

}