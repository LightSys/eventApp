package org.lightsys.eventApp.views.SettingsViews;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.LocationInfo;
import org.lightsys.eventApp.data.TimeZoneInfo;
import org.lightsys.eventApp.tools.LocalDB;

import java.util.TimeZone;

/** Created by Littlesnowman88 21 June 2018**/
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefEditor;
    private LocalDB db;
    private Intent fragment_to_recycleview;
    private ListPreference refresh_pref;
    private CheckBoxPreference event_zone_button, my_remote_zone_button, custom_zone_button;
    private PreferenceCategory time_zone_preferences;
    private String selectedStringTimeZone, selectedAdapter;
    private String[] eventLocations;
    private int saved_time_zone_button, current_time_zone_button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.app_preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        prefEditor = sharedPreferences.edit();

        db = new LocalDB(this.getContext());
        eventLocations = db.getAllEventLocations();
        selectedAdapter = "none";
        initializeIntent();

        refresh_pref = (ListPreference) findPreference(getResources().getString(R.string.refresh_rate_setting));
        event_zone_button = (CheckBoxPreference) findPreference(getResources().getString(R.string.on_site_pref));
        my_remote_zone_button = (CheckBoxPreference) findPreference(getResources().getString(R.string.my_location_pref));
        custom_zone_button = (CheckBoxPreference) findPreference(getResources().getString(R.string.custom_zone_pref));
        time_zone_preferences = (PreferenceCategory) findPreference("time_prefs_category");
        hideIllegalTimeZoneButtons();
        initializeTimeZoneButtons();

        event_zone_button.setOnPreferenceClickListener(this);

        initializePreferenceSummaries();

    }

    private void initializeIntent() {
        fragment_to_recycleview = new Intent(getContext(), SettingsRecycleView.class);
        fragment_to_recycleview.putExtra("adapter", selectedAdapter);
    }

    private void initializePreferenceSummaries() {
        setPreferenceSummary(refresh_pref, sharedPreferences.getString(refresh_pref.getKey(), ""));
        String selected_time_setting = sharedPreferences.getString("selected_time_setting", "on-site");
        switch (selected_time_setting) {
            case "on-site":
                String pref_summary;
                if (eventLocations[0].equals("")) {
                    if (db.getLocationTimeZone(eventLocations[0]).equals("")) {
                        pref_summary = getContext().getResources().getString(R.string.blank_time_zone)
                        + "\n" + TimeZone.getDefault().getID(); //if event name is blank and time zone is blank, show just this
                    } else {
                        pref_summary = sharedPreferences.getString("time_zone", db.getLocationTimeZone(eventLocations[0])); //show only the valid time zone
                    }
                } else {
                    //show the location and the time zone
                    pref_summary = eventLocations[0] + "\n" + sharedPreferences.getString("time_zone", db.getLocationTimeZone(eventLocations[0]));
                }
                event_zone_button.setSummaryOn(pref_summary);
                my_remote_zone_button.setSummaryOff("");
                custom_zone_button.setSummaryOff("");
                break;
            case "my location":
                event_zone_button.setSummaryOff("");
                my_remote_zone_button.setSummaryOn(sharedPreferences.getString("time_zone", TimeZone.getDefault().getID()));
                custom_zone_button.setSummaryOff("");
                break;
            case "custom_zone":
                event_zone_button.setSummaryOff("");
                my_remote_zone_button.setSummaryOff("");
                custom_zone_button.setSummaryOn(sharedPreferences.getString("time_zone", ""));
                break;
        }
    }

    private void setPreferenceSummary(Preference preference, String summary) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(summary);
            if (prefIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference selectedBox = (CheckBoxPreference) preference;
            event_zone_button.setSummaryOff("");
            my_remote_zone_button.setSummaryOff("");
            custom_zone_button.setSummaryOff("");
            selectedBox.setSummaryOn(summary);
        }
    }

    private void hideIllegalTimeZoneButtons() {
        int remote_zone_allowed = Integer.parseInt(
                db.getGeneral("remote_viewing")!=null?db.getGeneral("remote_viewing"):"0");
        int custom_zone_allowed = Integer.parseInt(
                db.getGeneral("custom_time_zone")!=null?db.getGeneral("custom_time_zone"):"0");
        if (remote_zone_allowed == 0) {
            my_remote_zone_button.setVisible(false);
            if (my_remote_zone_button.isChecked()) {
                my_remote_zone_button.setEnabled(false);
                prefEditor.putString("selected_time_setting", "on-site");
                event_zone_button.setChecked(true);
                selectedStringTimeZone = db.getLocationTimeZone(eventLocations[0]);
                if (selectedStringTimeZone.equals("")) {
                    selectedStringTimeZone = TimeZone.getDefault().getID();
                }
                prefEditor.putString("time_zone", selectedStringTimeZone).apply();
            }
        } else {
            my_remote_zone_button.setVisible(true);
            my_remote_zone_button.setEnabled(true);
            my_remote_zone_button.setOnPreferenceClickListener(this);
        }
        if (custom_zone_allowed == 0) {
            custom_zone_button.setVisible(false);
            if (custom_zone_button.isChecked()) {
                custom_zone_button.setEnabled(false);
                prefEditor.putString("selected_time_setting", "on-site");
                event_zone_button.setChecked(true);
                selectedStringTimeZone = db.getLocationTimeZone(eventLocations[0]);
                if (selectedStringTimeZone.equals("")) {
                    selectedStringTimeZone = TimeZone.getDefault().getID();
                }
                prefEditor.putString("time_zone", selectedStringTimeZone).apply();
            }
        } else {
            custom_zone_button.setVisible(true);
            custom_zone_button.setEnabled(true);
            custom_zone_button.setOnPreferenceClickListener(this);
        }
    }

    private void initializeTimeZoneButtons() {
        /* default to first event location, or previously selected location/time zone if applicable. */
        selectedStringTimeZone = sharedPreferences.getString("time_zone", db.getLocationTimeZone(eventLocations[0]));
        if (selectedStringTimeZone.equals("")) selectedStringTimeZone = TimeZone.getDefault().getID();
        String selected_time_setting = sharedPreferences.getString("selected_time_setting", "on-site");
        switch (selected_time_setting) {
            case "on-site":
                setOnSiteButton();
                break;
            case "my location":
                setMyLocationButton();
                break;
            case "custom_zone":
                setCustomZoneButton();
                break;
        }
    }

    private void setOnSiteButton() {
        my_remote_zone_button.setChecked(false);
        custom_zone_button.setChecked(false);
        event_zone_button.setChecked(true);
        current_time_zone_button = 0;
        saved_time_zone_button = 0;
    }

    private void setMyLocationButton() {
        event_zone_button.setChecked(false);
        custom_zone_button.setChecked(false);
        my_remote_zone_button.setChecked(true);
        current_time_zone_button = 1;
        saved_time_zone_button = 1;
    }

    private void setCustomZoneButton() {
        event_zone_button.setChecked(false);
        my_remote_zone_button.setChecked(false);
        custom_zone_button.setChecked(true);
        current_time_zone_button = 2;
        saved_time_zone_button = 2;
    }

    //if the on-site button is clicked, display the calendar in the chosen location's time zone.
    public void onEventZoneClicked() {
        /*take note of the button press and prep the adapter */
        current_time_zone_button = 0;

        int itemCount = eventLocations.length;
        fragment_to_recycleview.removeExtra("adapter");
        fragment_to_recycleview.putExtra("adapter", "EventLocationAdapter");
        if (itemCount > 1) {
            waitToCheckButton();
            //start event location recycler view activity, passing along a string with event location adapter
            startActivityForResult(fragment_to_recycleview, 1);
        } else if (itemCount==1) {
            event_zone_button.setChecked(true);
            my_remote_zone_button.setChecked(false);
            custom_zone_button.setChecked(false);
            selectedStringTimeZone = db.getLocationTimeZone(eventLocations[0]);
            if (selectedStringTimeZone.equals("")) selectedStringTimeZone = TimeZone.getDefault().getID();
            selectedAdapter = "EventLocationAdapter";
            updateTimeZonePreferences();
            String pref_summary = selectedStringTimeZone;
            if (!eventLocations[0].equals("")) pref_summary = eventLocations[0] + "\n" + pref_summary;
            setPreferenceSummary(event_zone_button, pref_summary);
        } else {
            selectedStringTimeZone = TimeZone.getDefault().getID();
        }
    }

    //if the my_remote_time_zone button is clicked, display the calendar in the device's time zone
    public void onMyRemoteZoneClicked() {
        event_zone_button.setChecked(false);
        custom_zone_button.setChecked(false);
        my_remote_zone_button.setChecked(true);
        current_time_zone_button = 1;
        saved_time_zone_button = 1;

        selectedStringTimeZone = TimeZone.getDefault().getID();
        selectedAdapter = "";
        updateTimeZonePreferences();
        setPreferenceSummary(my_remote_zone_button, selectedStringTimeZone);
    }

    //if the on-site button is clicked, display the calendar in the chosen location's time zone.
    public void onCustomZoneClicked() {
        /*take note of the button press and prep the adapter */
        current_time_zone_button = 2;
        fragment_to_recycleview.removeExtra("adapter");
        fragment_to_recycleview.putExtra("adapter", "ContinentSelectionAdapter");
        waitToCheckButton();
        //start time zone recycler view activity, passing along a string with time zone adapter
        startActivityForResult(fragment_to_recycleview, 1);
    }

    //keep the previous radio button selected until a legitimate location/time zone is actually selected.
    public void waitToCheckButton() {
        switch (saved_time_zone_button) {
            case 0:  //event_zone_button
            {
                custom_zone_button.setChecked(false);
                my_remote_zone_button.setChecked(false);
                event_zone_button.setChecked(true);
                break;
            }
            case 1:  //remote_location_button
            {
                event_zone_button.setChecked(false);
                custom_zone_button.setChecked(false);
                my_remote_zone_button.setChecked(true);
                break;
            }
            case 2:  //custom_time_button
            {
                event_zone_button.setChecked(false);
                my_remote_zone_button.setChecked(false);
                custom_zone_button.setChecked(true);
                break;
            }
        }
    }

    private void updateSavedTimeZoneButton() {
        saved_time_zone_button = current_time_zone_button;
    }

    /* saves the user's selection into shared preferences */
    private void updateTimeZonePreferences() {
        prefEditor.putString("time_zone", selectedStringTimeZone);
        //LocalDB needs these next settings for timezone adjustment behavior and the schedule fragment
        switch (current_time_zone_button) {
            case 0:
                prefEditor.putString("selected_time_setting", "on-site"); break;
            case 1:
                prefEditor.putString("selected_time_setting", "my location"); break;
            case 2:
                prefEditor.putString("selected_time_setting", "custom_zone"); break;
        }
        prefEditor.apply();
        db.replaceTimeZone(selectedStringTimeZone);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String sentAdapter = fragment_to_recycleview.getStringExtra("adapter");
                if (! selectedAdapter.equals(sentAdapter) ){
                    selectedAdapter = sentAdapter;
                    switch (sentAdapter) {
                        case "EventLocationAdapter":
                            event_zone_button.setChecked(true);
                            my_remote_zone_button.setChecked(false);
                            custom_zone_button.setChecked(false);
                            break;
                        case "":
                            event_zone_button.setChecked(false);
                            my_remote_zone_button.setChecked(true);
                            custom_zone_button.setChecked(false);
                            selectedStringTimeZone = data.getStringExtra("selected_item");
                            updateTimeZonePreferences();
                            break;
                        case "ContinentSelectionAdapter":
                            event_zone_button.setChecked(false);
                            my_remote_zone_button.setChecked(false);
                            custom_zone_button.setChecked(true);
                            selectedStringTimeZone = data.getStringExtra("selected_item");
                            updateTimeZonePreferences();
                            break;
                    }
                    updateSavedTimeZoneButton();
                }

                String pref_summary = selectedStringTimeZone;
                if (selectedAdapter.equals("EventLocationAdapter")) {
                    String returned_item = data.getStringExtra("event_location");
                    pref_summary = db.getLocationTimeZone(returned_item);
                    if (pref_summary.equals("")) {
                        pref_summary = getContext().getResources().getString(R.string.blank_time_zone);
                        selectedStringTimeZone = TimeZone.getDefault().getID();
                    } else {
                        selectedStringTimeZone = pref_summary;
                    }
                    updateTimeZonePreferences();

                    if (!returned_item.equals(""))
                        pref_summary = returned_item + "\n" + pref_summary;
                }

                int num_boxes = time_zone_preferences.getPreferenceCount();
                CheckBoxPreference current_box;
                for (int b=0; b < num_boxes; b++) {
                    current_box = (CheckBoxPreference) time_zone_preferences.getPreference(b);
                    if (current_box.isEnabled() && current_box.isChecked()) {
                        setPreferenceSummary(current_box, pref_summary);
                    }
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Preference pref = findPreference(s);
        if (pref != null) {
            if (pref instanceof ListPreference) {
                String value = sharedPreferences.getString(pref.getKey(), "");
                setPreferenceSummary(pref, value);
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == findPreference(getResources().getString(R.string.on_site_pref))) {
            onEventZoneClicked();
        } else if ( my_remote_zone_button.isEnabled() &&
                preference == findPreference(getResources().getString(R.string.my_location_pref))) {
            onMyRemoteZoneClicked();
        } else if ( custom_zone_button.isEnabled() &&
                preference == findPreference(getResources().getString(R.string.custom_zone_pref))) {
            onCustomZoneClicked();
        }
        return true;
    }

}