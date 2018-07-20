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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.LocationInfo;
import org.lightsys.eventApp.data.TimeZoneInfo;
import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.tools.ColorContrastHelper;
import org.lightsys.eventApp.tools.SettingsAdapters.ContinentSelectionAdapter;
import org.lightsys.eventApp.tools.SettingsAdapters.EventLocationAdapter;


public class SettingsRecycleView extends AppCompatActivity implements EventLocationAdapter.EventLocationAdapterOnClickHandler, ContinentSelectionAdapter.TimeZoneAdapterOnClickHandler {

    private RecyclerView recyclerView;
    private LocalDB db;
    private EventLocationAdapter eventAdapter;
    private ContinentSelectionAdapter continentAdapter;
    private String[][] allTimeZones;
    private String[] eventLocations, continents;
    private String selectedContinent;
    private Intent recyclerview_to_fragment, recyclerview_to_reyclerview;
    private int color, black_or_white;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = this.getApplicationContext();
        setContentView(R.layout.time_zone_settings_layout);
        recyclerView = findViewById(R.id.timezone_settings_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        db = new LocalDB(context);

        recyclerview_to_fragment = new Intent(context, SettingsFragment.class);
        recyclerview_to_reyclerview = new Intent(context, TimeZoneSelectionView.class);
        Intent fragment_to_here = getIntent();
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
        } else if (adapterToUse.equals("ContinentSelectionAdapter")) {
            continentAdapter = new ContinentSelectionAdapter(this);
            allTimeZones = TimeZoneInfo.getAllTimeZones();
            continents = allTimeZones[0];
            continentAdapter.setContinentData(continents);
            recyclerView.setAdapter(continentAdapter);
        }
    }

    /* overrides EventLocationAdapterOnClickHandler's and ContinentSelectionAdapterOnClickHandler's onClick(String) */
    @Override
    public void onClick(String recycler_view_item) {
        selectedContinent = recycler_view_item.trim();
        Bundle zone_container = new Bundle();
        //find the location of the continent in allTimeZones
        for (int i=0; i<allTimeZones[0].length; i++) {
            if (selectedContinent.equals(allTimeZones[0][i])) {
                zone_container.putStringArray("selected_continent", allTimeZones[i+1]);
                recyclerview_to_reyclerview.putExtras(zone_container);
                startActivityForResult(recyclerview_to_reyclerview, 1);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String selected_zone = selectedContinent + "/"
                        + data.getStringExtra("selected_item");
                recyclerview_to_fragment.putExtra("selected_item", selected_zone);
                setResult(Activity.RESULT_OK, recyclerview_to_fragment);
                finish();
            }
        }
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
