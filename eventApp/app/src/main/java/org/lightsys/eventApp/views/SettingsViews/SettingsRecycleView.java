package org.lightsys.eventApp.views.SettingsViews;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.LocationInfo;
import org.lightsys.eventApp.data.TimeZoneInfo;
import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.tools.SettingsAdapters.ColorContrastHelper;
import org.lightsys.eventApp.tools.SettingsAdapters.EventLocationAdapter;
import org.lightsys.eventApp.tools.SettingsAdapters.TimeZoneAdapter;


public class SettingsRecycleView extends AppCompatActivity implements EventLocationAdapter.EventLocationAdapterOnClickHandler, TimeZoneAdapter.TimeZoneAdapterOnClickHandler {

    private RecyclerView recyclerView;
    private LocalDB db;
    private EventLocationAdapter eventAdapter;
    private TimeZoneAdapter zoneAdapter;
    private String[] eventLocations, allTimeZones;
    private String selectedStringTimeZone;
    private Intent recyclerview_to_fragment;
    private int color, black_or_white;
    private static final int BLACK = Color.parseColor("#000000");
    private static final int WHITE = Color.parseColor("#ffffff");


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = this.getApplicationContext();
        setContentView(R.layout.time_zone_settings_layout);
        recyclerView = findViewById(R.id.timezone_settings_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        db = new LocalDB(context);
        Intent fragment_to_here = getIntent();
        recyclerview_to_fragment = new Intent(context, SettingsFragment.class);
        String adapterToUse = fragment_to_here.getStringExtra("adapter");

        /* set up an action bar for returning to Main Activity */
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        try {
            color = Color.parseColor(db.getThemeColor("themeColor"));
            black_or_white = ColorContrastHelper.determineBlackOrWhite(color);
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
            setToolBarTextColor(actionBar);
        } catch (Exception e) {
            color = Color.parseColor("#6080C0");
            black_or_white = ColorContrastHelper.determineBlackOrWhite(color);
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
            setToolBarTextColor(actionBar);
        }

        if (adapterToUse.equals("EventLocationAdapter")) {
            eventAdapter = new EventLocationAdapter(this);
            eventLocations = LocationInfo.getEventLocations(db);
            eventAdapter.setLocationData(eventLocations);
            recyclerView.setAdapter(eventAdapter);
        } else if (adapterToUse.equals("TimeZoneAdapter")) {
            zoneAdapter = new TimeZoneAdapter(this);
            allTimeZones = TimeZoneInfo.getAllTimeZones();
            zoneAdapter.setTimeData(allTimeZones);
            recyclerView.setAdapter(zoneAdapter);

        }

    }

    /**
     * converts the given hex_color into grayscale and determines whether toolBar text color should be black or white
     * Created by Littlesnowman88 on 22 June 2018
     * @param action_bar, the action bar having its text color set
     * Postcondition: text color is set to either black or white, whatever will show better
     */
    private void setToolBarTextColor(ActionBar action_bar) {
        String title = action_bar.getTitle().toString();
        SpannableStringBuilder color_setter = new SpannableStringBuilder(title);
        if (black_or_white == BLACK) {
            ForegroundColorSpan color_span = new ForegroundColorSpan(BLACK);
            color_setter.setSpan(color_span, 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            action_bar.setTitle(color_setter);
            action_bar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black);
        } else {
            ForegroundColorSpan color_span = new ForegroundColorSpan(WHITE);
            color_setter.setSpan(color_span, 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            action_bar.setTitle(color_setter);
            action_bar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
    }

    /* overrides EventLocationAdapterOnClickHandler's and TimeZoneAdapterOnClickHandler's onClick(String) */
    @Override
    public void onClick(String recycler_view_item) {
        selectedStringTimeZone = recycler_view_item.trim();
        recyclerview_to_fragment.putExtra("selected_item", selectedStringTimeZone);
        setResult(Activity.RESULT_OK, recyclerview_to_fragment);
        finish();
    }

    /* used for the back-arrow button that returns to the Settings Activity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED, recyclerview_to_fragment);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
