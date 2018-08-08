package org.lightsys.eventApp.tools;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import org.lightsys.eventApp.R;

/**
 * created by tfmoo on 7 August 2018
 * Based on Littlesnowman88's EventLocationAdapter.java, as well as
 *          Udacity's 2016 The Android Open Source Project,
 *          android studio project S03.02-Solution-RecyclerViewClickHandling
 *          Apache License 2.0
 * This is responsible for the list of event locations presented to the user when a new event is loaded
 */
public class LocationSelectionAdapter
        extends RecyclerView.Adapter<LocationSelectionAdapter.LocationSelectionAdapterViewHolder>
{

    private String[] locationData;
    private int active_location_index = 0;

    private final LocationSelectionAdapterOnClickHandler clickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface LocationSelectionAdapterOnClickHandler {
        void onClick(String event_location);
    }

    /** Constructor **/
    public LocationSelectionAdapter(LocationSelectionAdapterOnClickHandler clikHandlr){
        clickHandler = clikHandlr;

    }

    /**
     * A viewholder to handle clicks that happen inside of the recycler view.
     */
    public class LocationSelectionAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView locationTextView;
        public final RadioButton radioButton;

        public LocationSelectionAdapterViewHolder(View view){
            super(view);
            locationTextView = (TextView) view.findViewById(R.id.location_name);
            radioButton = (RadioButton) view.findViewById(R.id.location_radio_button);
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    active_location_index = getAdapterPosition();
                    String event_location = locationData[active_location_index];
                    clickHandler.onClick(event_location);
                    notifyDataSetChanged();
                }
            };
            locationTextView.setOnClickListener(clickListener);
            radioButton.setOnClickListener(clickListener);
            view.setOnClickListener(clickListener);
        }
    }

    /** called when each ViewHolder in the RecycleView is created.
     * @param viewGroup: the single view group that contains all the list views
     * @param viewType: an integer indicating different types of items.
     * @return: a LocationSelectionAdapterViewHolder that contains the Views for each event location
     */
    @Override
    public LocationSelectionAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int ListItemId = R.layout.radio_button_location_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(ListItemId, viewGroup, shouldAttachToParentImmediately);
        return new LocationSelectionAdapterViewHolder(view);
    }

    /** called by RecyclerView to set event_location at specified pos
     * @param: LocationSelectionAdapterViewHolder lsavh: ViewHolder that holds event_location at pos
     * @param: int pos, the position of the event_location within lsavh being bound.
     */
    @Override
    public void onBindViewHolder(LocationSelectionAdapterViewHolder lsavh, int pos) {
        lsavh.radioButton.setChecked(pos == active_location_index);
        String event_location = locationData[pos];
        lsavh.locationTextView.setText(event_location);
    }

    @Override
    public int getItemCount() {
        if (null == locationData) return 0;
        return locationData.length;
    }

    public void setLocationData(String[] locations) {
        locationData = locations;
        notifyDataSetChanged(); //a method inside of Recycler View.
    }
}
