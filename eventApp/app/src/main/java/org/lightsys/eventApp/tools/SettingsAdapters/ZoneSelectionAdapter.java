package org.lightsys.eventApp.tools.SettingsAdapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lightsys.eventApp.R;

/**
 * created by Littlesnowman88 and tfmoo on 16 July 2018
 * Based on Udacity's 2016 The Android Open Source Project,
 *          android studio project S03.02-Solution-RecyclerViewClickHandling
 *          Apache License 2.0
 * responsible for the list of time zones to the ImeZoneSelectionView activity
 */
public class ZoneSelectionAdapter extends RecyclerView.Adapter<ZoneSelectionAdapter.ZoneSelectionAdapterViewHolder> {

    private String[] zones;

    private final ZoneSelectionAdapterOnClickHandler clickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface ZoneSelectionAdapterOnClickHandler {
        void onClick(String time_zone);
    }

    /** Constructor **/
    public ZoneSelectionAdapter(ZoneSelectionAdapterOnClickHandler clikHandlr) {
        clickHandler = clikHandlr;
    }

    /**
     * A viewholder to handle clicks that happen inside of the recycler view; passes items clicked up to the ZoneSelectionAdapter's onClick.
     */
    protected class ZoneSelectionAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView timeTextView;

        public ZoneSelectionAdapterViewHolder(View view) {
            super(view);
            timeTextView = (TextView) view.findViewById(R.id.time_zone_data);
            view.setOnClickListener(this);
        }

        /**
         * Called by the recycler view's child views at click
         *
         * @param: View v, a View that was clicked (a time zone)
         */
        @Override
        public void onClick(View v) {
            String time_zone = zones[getAdapterPosition()];
            clickHandler.onClick(time_zone);
        }
    }


    /** called when each ViewHolder in the RecycleView is created.
     * @param viewGroup: the single view group that contains all the list views
     * @param viewType: an integer indicating different types of items.
     * @return: a ZoneSelectionAdapterViewHolder that contains the Views for each time zone
     */
    @Override
    public ZoneSelectionAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int ListItemId = R.layout.time_zone_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(ListItemId, viewGroup, shouldAttachToParentImmediately);
        return new ZoneSelectionAdapterViewHolder(view);
    }

    /** called by RecyclerView to set time_zone at specified pos
     * @param: ContinentSelectionAdapterViewHolder chavh: ViewHolder that holds time_zone at pos
     * @param: int pos, the position of the time_zone within zsavh being bound.
     */
    @Override
    public void onBindViewHolder(ZoneSelectionAdapterViewHolder zsavh, int pos) {
        String zone = zones[pos];
        if (zone != null) {
            zsavh.timeTextView.setText(zone);
        } else {
            zsavh.timeTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (null == zones) return 0;
        return zones.length;
    }

    public void setZoneData(String[] zone_data) {
        zones = zone_data;
        notifyDataSetChanged(); //a method inside of Recycler View.
    }
}
