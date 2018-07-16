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
import android.view.MenuItem;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.LocationInfo;
import org.lightsys.eventApp.data.TimeZoneInfo;
import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.tools.ColorContrastHelper;
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
            actionBar = ColorContrastHelper.setToolBarTextColor(actionBar, black_or_white);
        } catch (Exception e) {
            color = Color.parseColor("#6080C0");
            black_or_white = ColorContrastHelper.determineBlackOrWhite(color);
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
            actionBar = ColorContrastHelper.setToolBarTextColor(actionBar, black_or_white);
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
