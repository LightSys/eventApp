package org.lightsys.eventApp.tools;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class EventArrayAdapter extends ArrayAdapter<HashMap<String, String>> {

    public static final int TYPE_NOTIFICATION = 0;
    public static final int TYPE_EVENT = 1;

    private ArrayList<HashMap<String, String>> events;

    public EventArrayAdapter(Context context, ArrayList<HashMap<String, String>> events) {
        super(context, 0, events);
        this.events = events;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        String type = events.get(position).get("type");
        if (type == "notification") {
            return 0;
        } else {    //Event
            return 1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HashMap<String, String> item = events.get(position);
        int itemType = getItemViewType(position);

        if (convertView == null) {

        }

        return convertView;
    }
}
