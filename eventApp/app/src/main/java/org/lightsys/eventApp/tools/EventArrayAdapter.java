package org.lightsys.eventApp.tools;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.lightsys.eventApp.R;

import java.util.ArrayList;
import java.util.HashMap;

public class EventArrayAdapter extends ArrayAdapter<HashMap<String, String>> {

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
            if (itemType == 0) {    //Notification
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.notification_item, null);
            } else {                //Event
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.notification_item, null);
                //Change color gradient of event box
                int color = Color.parseColor(item.get("color"));
                int colors[] = { color, color, 0xe4e4e5,0xe4e4e5, 0xe4e4e5 };

                //gradient background to show event types
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,colors);
                gd.setCornerRadius(0f);
                gd.setShape(GradientDrawable.RECTANGLE);
                convertView.setBackground(gd);

                ImageView calendarImage = convertView.findViewById(R.id.calendarImage);
                calendarImage.setImageResource(R.drawable.ic_schedule);

            }
        }

        TextView title = convertView.findViewById(R.id.titleText);
        TextView date = convertView.findViewById(R.id.dateText);
        TextView notes = convertView.findViewById(R.id.noteText);
        title.setText(item.get("title"));
        notes.setText(item.get("content"));
        date.setText(item.get("date"));

        return convertView;
    }

}
