package org.lightsys.eventApp.tools;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.lightsys.eventApp.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ScannedEventsAdapter extends RecyclerView.Adapter<ScannedEventsAdapter.ScannedEventsAdapterViewHolder>{

    private ArrayList<String[]> scannedEvents;
    private Context app_context;

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
    public ScannedEventsAdapter(ScannedEventsAdapterOnClickHandler clikHandlr,ArrayList list, Context context) {
        scannedEvents = list;
        numRecyclerViewItems = list.size();
        clickHandler = clikHandlr;
        app_context = context;
    }

    /**
     * A viewholder to handle clicks that happen inside of the recycler view; passes items clicked up to the ContinentSelectionAdapter's onClick.
     */
    protected class ScannedEventsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView eventsTextView;
        public final ImageView eventsImageView;

        public ScannedEventsAdapterViewHolder(View view) {
            super(view);
            eventsTextView = (TextView) view.findViewById(R.id.scanned_event_name);
            eventsImageView = (ImageView) view.findViewById(R.id.scanned_event_logo);
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
        public void bind(String name, String image){
            eventsTextView.setText(name);
            if (image != null && !image.equals("")) {
                byte[] decodedBytes = Base64.decode(image, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                eventsImageView.setImageBitmap(decodedBitmap);
                LinearLayout.LayoutParams expand = new LinearLayout.LayoutParams(125, LinearLayout.LayoutParams.MATCH_PARENT);
                eventsImageView.setLayoutParams(expand);
            } else {
                LinearLayout.LayoutParams minimize = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
                eventsImageView.setLayoutParams(minimize);
            }
        }
    }

    /** called when each ViewHolder in the RecycleView is created.
     * @param viewGroup: the single view group that contains all the list views
     * @param viewType: an integer indicating different types of items.
     * @return: an ContinentSelectionAdapterViewHolder that contains the Views for each time zone
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
        String scanned_logo = scannedEvents.get(pos)[2];
        seavh.bind(scanned_event, scanned_logo);
    }


    @Override
    public int getItemCount() {
        if (null == scannedEvents) return 0;
        return numRecyclerViewItems;
        //return scannedEvents.size();
    }
}
