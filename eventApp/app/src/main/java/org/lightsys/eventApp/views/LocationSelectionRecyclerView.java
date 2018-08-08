package org.lightsys.eventApp.views;

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
import android.view.View;
import android.widget.Button;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.tools.ColorContrastHelper;
import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.tools.LocationSelectionAdapter;

public class LocationSelectionRecyclerView extends AppCompatActivity implements LocationSelectionAdapter.LocationSelectionAdapterOnClickHandler
{
    private RecyclerView recyclerView;
    private Button confirm_button, cancel_button;
    private LocalDB db;
    private LocationSelectionAdapter locationAdapter;
    private String[] eventLocations;
    private Intent main_to_here, recyclerview_to_main;
    private int color, black_or_white;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = this.getApplicationContext();
        setContentView(R.layout.location_selection_layout);
        confirm_button = (Button) findViewById(R.id.confirm_button);
        cancel_button = (Button) findViewById(R.id.cancel_button);
        recyclerView = findViewById(R.id.location_selection_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        db = new LocalDB(context);

        recyclerview_to_main = new Intent(context, MainActivity.class);
        main_to_here = getIntent();

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

        locationAdapter = new LocationSelectionAdapter(this);
        eventLocations = db.getAllEventLocations();

        int eventLocations_size = eventLocations.length;

        //This adds a 'Remote Location' option to the recyclerview
        //In the future, this could be made toggleable.
        String[] location_choices = new String[eventLocations_size + 1];
        System.arraycopy(eventLocations, 0, location_choices, 0, eventLocations_size);
        location_choices[eventLocations_size] = getString(R.string.remote_location);

        locationAdapter.setLocationData(location_choices);
        recyclerView.setAdapter(locationAdapter);
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recyclerview_to_main.getStringExtra("event_location") == null)
                {
                    recyclerview_to_main.putExtra("event_location", eventLocations[0]);
                }
                setResult(Activity.RESULT_OK, recyclerview_to_main);
                finish();
            }
        });
        cancel_button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 finish();
             }
         });
    }

    /* Overrides LocationSelectionAdapter's onClick */
    @Override
    public void onClick(String recycler_view_item) {
        recyclerview_to_main.putExtra("event_location", recycler_view_item);
    }

    /* used for the back-arrow button that returns to MainActivity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED, recyclerview_to_main);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
