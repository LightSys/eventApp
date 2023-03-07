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
 * responsible for the list of continents to the Settings's time zone recycle view
 */

public class ContinentSelectionAdapter extends RecyclerView.Adapter<ContinentSelectionAdapter.ContinentSelectionAdapterViewHolder> {

    private String[] continents;

    private final TimeZoneAdapterOnClickHandler clickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface TimeZoneAdapterOnClickHandler {
        void onClick(String time_zone);
    }

    /** Constructor **/
    public ContinentSelectionAdapter(TimeZoneAdapterOnClickHandler clikHandlr) {
        clickHandler = clikHandlr;
    }

    /**
     * A viewholder to handle clicks that happen inside of the recycler view; passes items clicked up to the ContinentSelectionAdapter's onClick.
     */
    protected class ContinentSelectionAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView continentTextView;

        public ContinentSelectionAdapterViewHolder(View view) {
            super(view);
            continentTextView = (TextView) view.findViewById(R.id.continent_data);
            view.setOnClickListener(this);
        }

        /**
         * Called by the recycler view's child views at click
         *
         * @param: View v, a View that was clicked (a continent name)
         */
        @Override
        public void onClick(View v) {
            String continent = continents[getAdapterPosition()];
            clickHandler.onClick(continent);
        }
    }


    /** called when each ViewHolder in the RecycleView is created.
     * @param viewGroup: the single view group that contains all the list views
     * @param viewType: an integer indicating different types of items.
     * @return: an ContinentSelectionAdapterViewHolder that contains the Views for each continent
     */
    @Override
    public ContinentSelectionAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int ListItemId = R.layout.continent_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(ListItemId, viewGroup, shouldAttachToParentImmediately);
        return new ContinentSelectionAdapterViewHolder(view);
    }

    /** called by RecyclerView to set continent at specified pos
     * @param: ContinentSelectionAdapterViewHolder chavh: ViewHolder that holds continent at pos
     * @param: int pos, the position of the continent within chavh being bound.
     */
    @Override
    public void onBindViewHolder(ContinentSelectionAdapterViewHolder chavh, int pos) {
        String continent = continents[pos];
        chavh.continentTextView.setText(continent);
    }


    @Override
    public int getItemCount() {
        if (null == continents) return 0;
        return continents.length;
    }

    public void setContinentData(String[] continent_data) {
        continents = continent_data;
        notifyDataSetChanged(); //a method inside of Recycler View.
    }
}
