package org.lightsys.eventApp.tools.SettingsAdapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lightsys.eventApp.R;

/**
 * created by Littlesnowman88 on 31 May 2018
 * Based on Udacity's 2016 The Android Open Source Project,
 *          android studio project S03.02-Solution-RecyclerViewClickHandling
 *          Apache License 2.0
 * responsible for the list of time zones to the Settings's time zone recycle view
 */

public class TimeZoneAdapter extends RecyclerView.Adapter<TimeZoneAdapter.TimeZoneAdapterViewHolder> {

    private String[] timeData;

    private final TimeZoneAdapterOnClickHandler clickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface TimeZoneAdapterOnClickHandler {
        void onClick(String time_zone);
    }

    /** Constructor **/
    public TimeZoneAdapter(TimeZoneAdapterOnClickHandler clikHandlr) {
        clickHandler = clikHandlr;
    }

    /**
     * A viewholder to handle clicks that happen inside of the recycler view; passes items clicked up to the TimeZoneAdapter's onClick.
     */
    protected class TimeZoneAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView timeTextView;

        public TimeZoneAdapterViewHolder(View view) {
            super(view);
            timeTextView = (TextView) view.findViewById(R.id.time_zone_data);
            view.setOnClickListener(this);
        }

        /**
         * Called by the recycler view's child views at click
         *
         * @param: View v, a View that was clicked (an time zone)
         */
        @Override
        public void onClick(View v) {
            String time_zone = timeData[getAdapterPosition()];
            clickHandler.onClick(time_zone);
        }
    }


    /** called when each ViewHolder in the RecycleView is created.
     * @param viewGroup: the single view group that contains all the list views
     * @param viewType: an integer indicating different types of items.
     * @return: an TimeZoneAdapterViewHolder that contains the Views for each time zone
     */
    @Override
    public TimeZoneAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int ListItemId = R.layout.time_zone_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(ListItemId, viewGroup, shouldAttachToParentImmediately);
        return new TimeZoneAdapterViewHolder(view);
    }

    /** called by RecyclerView to set time_zone at specified pos
     * @param: TimeZoneAdapterViewHolder elavh: ViewHolder that holds time_zone at pos
     * @param: int pos, the position of the time_zone within tzavh being bound.
     */
    @Override
    public void onBindViewHolder(TimeZoneAdapterViewHolder tzavh, int pos) {
        String time_zone = timeData[pos];
        tzavh.timeTextView.setText(time_zone + "\n");
    }


    @Override
    public int getItemCount() {
        if (null == timeData) return 0;
        return timeData.length;
    }

    public void setTimeData(String[] times) {
        timeData = times;
        notifyDataSetChanged(); //a method inside of Recycler View.
    }
}
