package org.lightsys.eventApp.tools;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lightsys.eventApp.R;

import java.util.ArrayList;

public class ScannedEventsAdapter extends RecyclerView.Adapter<ScannedEventsAdapter.ScannedEventsAdapterViewHolder>{

    private volatile static ScannedEventsAdapter uniqueAdapterInstance;

    private ArrayList<String[]> scannedEvents;

    private final ScannedEventsAdapterOnClickHandler clickHandler;

    private Resources system_resources;

    private ScannedEventsAdapterViewHolder seaVH;

    /**
     * The interface that receives onClick messages.
     */
    public interface ScannedEventsAdapterOnClickHandler {
        void onClick(String scanned_event);
    }

    /** unique instance enforcer (singleton pattern) **/
    public static ScannedEventsAdapter getInstance(ScannedEventsAdapterOnClickHandler clikHandlr, Resources resources) {
        if (uniqueAdapterInstance == null) {
            synchronized (ScannedEventsAdapter.class) {
                if (uniqueAdapterInstance == null) {
                    uniqueAdapterInstance = new ScannedEventsAdapter(clikHandlr, resources);
                }
            }
        }
        return uniqueAdapterInstance;
    }

    /** Constructor **/
    private ScannedEventsAdapter(ScannedEventsAdapterOnClickHandler clikHandlr, Resources resources) {
        clickHandler = clikHandlr;
        scannedEvents = new ArrayList<>();
        system_resources = resources;
        String scan_qr = system_resources.getString(R.string.scan_new_qr);
        String[] scanQR = {scan_qr,scan_qr};
        scannedEvents.add(scanQR);

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
            if(!scanned_url.equals(system_resources.getString(R.string.scan_new_qr))) {
                addScannedEvent(scanned_item);
            }
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
        return scannedEvents.size();
    }

    public void addScannedEvent(String[] name_and_url) {
        if(!hasNameAndUrl(name_and_url)) {
            scannedEvents.add(0,name_and_url);
            if(scannedEvents.size() > 6) {
                scannedEvents.remove(5);
            }
            onBindViewHolder(seaVH,0);
            //notifyDataSetChanged(); //buggy; fixes only upon app minimize and opening back up
        }
        else {
            int event_position = scannedEvents.indexOf(name_and_url);
            //Data Connection should add the event in.
        }

    }

    private boolean hasNameAndUrl(String[] scanned_event){
        for(String[] item: scannedEvents){
            if(scanned_event[1].equals(item[1])){
                return true;
            }
        }
        return false;
    }

    public static ScannedEventsAdapter getInstance() {
        return uniqueAdapterInstance;
    }
}
