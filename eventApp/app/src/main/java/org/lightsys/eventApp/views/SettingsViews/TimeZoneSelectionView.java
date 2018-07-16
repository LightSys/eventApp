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
import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.tools.ColorContrastHelper;
import org.lightsys.eventApp.tools.SettingsAdapters.ZoneSelectionAdapter;


public class TimeZoneSelectionView extends AppCompatActivity implements ZoneSelectionAdapter.ZoneSelectionAdapterOnClickHandler {

    private RecyclerView recyclerView;
    private LocalDB db;
    private ZoneSelectionAdapter zoneSelectionAdapter;
    private String[] zones;
    private String selectedStringTimeZone;
    private Intent recyclerview_to_recyclerview;
    private int color, black_or_white;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = this.getApplicationContext();
        setContentView(R.layout.zone_selection_layout);
        recyclerView = findViewById(R.id.zone_selection_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        db = new LocalDB(context);
        recyclerview_to_recyclerview = new Intent(context, SettingsRecycleView.class);
        
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

        zoneSelectionAdapter = new ZoneSelectionAdapter(this);
        zones = getIntent().getExtras().getStringArray("selected_continent");
        zoneSelectionAdapter.setZoneData(zones);
        recyclerView.setAdapter(zoneSelectionAdapter);
        
    }

    /* overrides TimeZoneAdapter's on click handler */
    @Override
    public void onClick(String recycler_view_item) {
        selectedStringTimeZone = recycler_view_item.trim();
        recyclerview_to_recyclerview.putExtra("selected_item", selectedStringTimeZone);
        setResult(Activity.RESULT_OK, recyclerview_to_recyclerview);
        finish();
    }

    /* used for the back-arrow button that returns to the Settings Activity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED, recyclerview_to_recyclerview);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
