package org.lightsys.eventApp.tools.SettingsAdapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lightsys.eventApp.R;

/**
 * created by Littlesnowman88 on 6 June 2018
 * Based on Udacity's 2016 The Android Open Source Project,
 *          android studio project S03.02-Solution-RecyclerViewClickHandling
 *          Apache License 2.0
 * responsible for a blank list of no items for an empty Recycler View
 *          (used in Settings Activity's initilizeTimeZoneSettings
 */

public class BlankAdapter  extends RecyclerView.Adapter<BlankAdapter.BlankAdapterViewHolder>{
    private final BlankAdapterOnClickHandler clickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface BlankAdapterOnClickHandler {
        void onClick(String event_location);
    }

    /** Constructor **/
    public BlankAdapter(BlankAdapterOnClickHandler clikHandlr) {
        clickHandler = clikHandlr;
    }


    /**
     * A viewholder to handle clicks that happen inside of the recycler view; passes items clicked up to the BlankAdapter's onClick.
     */
    public class BlankAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView blankTextView;

        public BlankAdapterViewHolder(View view) {
            super(view);
            blankTextView = (TextView) view.findViewById(R.id.null_item);
            view.setOnClickListener(this);
        }

        /**
         * Called by the recycler view's child views at click
         *
         * @param: View v, a View that was clicked (in this case, nothing)
         */
        @Override
        public void onClick(View v) {
            //do nothing because a blank recycler view is allowed no items.
        }
    }


    /** called when each ViewHolder in the RecycleView is created.
     * @param viewGroup: the single view group that contains all the item views
     * @param viewType: an integer indicating different types of items.
     * @return: an BlankAdapterViewHolder that contains no items
     */
    @Override
    public BlankAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int ListItemId = R.layout.settings_null_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(ListItemId, viewGroup, shouldAttachToParentImmediately);
        return new BlankAdapterViewHolder(view);
    }


    /** called by RecyclerView
     * @param: BlankAdapterViewHolder elavh: ViewHolder that holds no views
     * @param: int pos, the position of (null) items in bavh being bound
     */
    @Override
    public void onBindViewHolder(BlankAdapterViewHolder bavh, int pos) {
        //do nothing because blank adapters are not allowed to hold data
    }
    
    @Override public int getItemCount() {return 0;}





}
