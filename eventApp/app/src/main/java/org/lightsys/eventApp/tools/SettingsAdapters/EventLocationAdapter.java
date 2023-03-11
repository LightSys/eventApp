package org.lightsys.eventApp.tools.SettingsAdapters;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
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
 * responsible for the list of event locations to the Settings's time zone recycle view
 */

public class EventLocationAdapter
        extends RecyclerView.Adapter<EventLocationAdapter.EventLocationAdapterViewHolder>
{

    private String[] locationData;

    private final EventLocationAdapterOnClickHandler clickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface EventLocationAdapterOnClickHandler {
        void onClick(String event_location);
    }

    /** Constructor **/
    public EventLocationAdapter(EventLocationAdapterOnClickHandler clikHandlr) {
        clickHandler = clikHandlr;
    }


    /**
     * A viewholder to handle clicks that happen inside of the recycler view; passes items clicked up to the EventLocationAdapter's onClick.
     */
    public class EventLocationAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView locationTextView;

        public EventLocationAdapterViewHolder(View view) {
            super(view);
            locationTextView = (TextView) view.findViewById(R.id.event_location_data);
            view.setOnClickListener(this);
        }

        /**
         * Called by the recycler view's child views at click
         *
         * @param: View v, a View that was clicked (an event location)
         */
        @Override
        public void onClick(View v) {
            String event_location = locationData[getAdapterPosition()];
            clickHandler.onClick(event_location);
        }
    }


    /** called when each ViewHolder in the RecycleView is created.
     * @param viewGroup: the single view group that contains all the list views
     * @param viewType: an integer indicating different types of items.
     * @return: an EventLocationAdapterViewHolder that contains the Views for each event location
     */
    @Override
    public EventLocationAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int ListItemId = R.layout.event_location_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(ListItemId, viewGroup, shouldAttachToParentImmediately);
        return new EventLocationAdapterViewHolder(view);
    }

    /** called by RecyclerView to set event_location at specified pos
     * @param: EventLocationAdapterViewHolder elavh: ViewHolder that holds event_location at pos
     * @param: int pos, the position of the event_location within elavh being bound.
     */
    @Override
    public void onBindViewHolder(EventLocationAdapterViewHolder elavh, int pos) {
        String event_location = locationData[pos];
        elavh.locationTextView.setText(event_location);
    }

    @Override
    public int getItemCount() {
        if (null == locationData) return 0;
        return locationData.length;
    }

    public String getFirstItem() {
        if (locationData.length == 0) {return null; }
        else { return locationData[0]; }
    }

    public void setLocationData(String[] locations) {
        locationData = locations;
        notifyDataSetChanged(); //a method inside of Recycler View.
    }
}
