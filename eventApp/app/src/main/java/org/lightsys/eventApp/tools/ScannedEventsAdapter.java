package org.lightsys.eventApp.tools;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lightsys.eventApp.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ScannedEventsAdapter extends RecyclerView.Adapter<ScannedEventsAdapter.ScannedEventsAdapterViewHolder>{

    private ArrayList<String[]> scannedEvents;

    private final ScannedEventsAdapterOnClickHandler clickHandler;

    private ScannedEventsAdapterViewHolder seaVH;

    private int numRecyclerViewItems;

    /**
     * The interface that receives onClick messages.
     */
    public interface ScannedEventsAdapterOnClickHandler {
        void onClick(String scanned_event);
    }

    /** Constructor **/
    public ScannedEventsAdapter(ScannedEventsAdapterOnClickHandler clikHandlr) {
        scannedEvents = new ArrayList<>();
        clickHandler = clikHandlr;
    }

    /**
     * A viewholder to handle clicks that happen inside of the recycler view; passes items clicked up to the TimeZoneAdapter's onClick.
     */
    protected class ScannedEventsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView eventsTextView;

        public ScannedEventsAdapterViewHolder(View view) {
            super(view);
            eventsTextView = (TextView) view.findViewById(R.id.scanned_event_name);
            view.setOnClickListener(this);

        }

        /**
         * Called by the recycler view's child views at click
         *
         * @param: View v, a View that was clicked (an time zone)
         */
        @Override
        public void onClick(View v) {
            String[] scanned_item = scannedEvents.get(getAdapterPosition());
            String scanned_url = scanned_item[1];
            clickHandler.onClick(scanned_url);
        }
    }

    /** called when each ViewHolder in the RecycleView is created.
     * @param viewGroup: the single view group that contains all the list views
     * @param viewType: an integer indicating different types of items.
     * @return: an TimeZoneAdapterViewHolder that contains the Views for each time zone
     */
    @Override
    public ScannedEventsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int ListItemId = R.layout.scanned_events_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(ListItemId, viewGroup, shouldAttachToParentImmediately);
        seaVH = new ScannedEventsAdapterViewHolder(view);
        return seaVH;
    }

    @Override
    public void onBindViewHolder(ScannedEventsAdapterViewHolder seavh, int pos) {
            String scanned_event = scannedEvents.get(pos)[0];
            seavh.eventsTextView.setText(scanned_event);
    }


    @Override
    public int getItemCount() {
        if (null == scannedEvents) return 0;
        return numRecyclerViewItems;
        //return scannedEvents.size();
    }

    public void setItemCount(int size) {
        if(size >= 1 && size <= 6) {
            numRecyclerViewItems = size;
        }
    }

    public void setAdapterData(ArrayList<String[]> new_data){
        scannedEvents.clear();
        for(String[] item: new_data){
            scannedEvents.add(item);
        }
    }
}
