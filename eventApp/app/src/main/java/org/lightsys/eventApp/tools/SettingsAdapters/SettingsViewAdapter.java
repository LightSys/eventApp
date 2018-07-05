package org.lightsys.eventApp.tools.SettingsAdapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lightsys.eventApp.R;
/**
 * created by Littlesnowman88 on 7 June 2018
 * Based on Udacity's 2016 The Android Open Source Project,
 *          android studio project S03.02-Solution-RecyclerViewClickHandling
 *          Apache License 2.0
 * responsible for the settingsView root RecyclerView
 *          (used in Settings Activity's onCreate)
 */

public class SettingsViewAdapter  extends RecyclerView.Adapter<SettingsViewAdapter.SettingsViewAdapterViewHolder>{
    private final SettingsViewAdapterOnClickHandler clickHandler;

    private String[] settings_options;

    /**
     * The interface that receives onClick messages.
     */
    public interface SettingsViewAdapterOnClickHandler {
        void onClick(String event_location);
    }

    /** Constructor **/
    public SettingsViewAdapter(SettingsViewAdapterOnClickHandler clikHandlr) {
        clickHandler = clikHandlr;
    }


    /**
     * A viewholder to handle clicks that happen inside of the recycler view; passes items clicked up to the SettingsViewAdapter's onClick.
     */
    public class SettingsViewAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView settingsViewTextView;

        public SettingsViewAdapterViewHolder(View view) {
            super(view);
            settingsViewTextView = (TextView) view.findViewById(R.id.settings_option);
            view.setOnClickListener(this);
        }

        /**
         * Called by the recycler view's child views at click
         *
         * @param: View v, a View that was clicked
         */
        @Override
        public void onClick(View v) {
            String chosen_setting = settings_options[getAdapterPosition()];
            clickHandler.onClick(chosen_setting);
        }
    }


    /** called when each ViewHolder in the RecycleView is created.
     * @param viewGroup: the single view group that contains all the list views
     * @param viewType: an integer indicating different types of items.
     * @return: an SettingsViewAdapterViewHolder that contains the Views for each event location
     */
    @Override
    public SettingsViewAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int ListItemId = R.layout.settings_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(ListItemId, viewGroup, shouldAttachToParentImmediately);
        return new SettingsViewAdapterViewHolder(view);
    }


    /** called by RecyclerView to set a setting at specified pos
     * @param: SettingsViewAdapterViewHolder svavh: ViewHolder that holds event_location at pos
     * @param: int pos, the position of the event_location within svavh being bound.
     */
    @Override
    public void onBindViewHolder(SettingsViewAdapterViewHolder svavh, int pos) {
        String setting_option = settings_options[pos];
        svavh.settingsViewTextView.setText(setting_option + "\n");
    }

    @Override public int getItemCount() {
        if (null == settings_options) return 0;
        return settings_options.length;
    }

    public void setSettingsOptions(String[] options) {
        settings_options = options;
        notifyDataSetChanged(); //a method inside of Recycler View
    }
}