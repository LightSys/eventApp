package org.lightsys.eventApp.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.OneTimeWorkRequest;
//import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Constraints;
import androidx.work.Data;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.Info;
import org.lightsys.eventApp.tools.CompletionInterface;
import org.lightsys.eventApp.tools.DataConnection;
import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.tools.NavigationAdapter;
import org.lightsys.eventApp.tools.ScannedEventsAdapter;
import org.lightsys.eventApp.tools.ColorContrastHelper;
import org.lightsys.eventApp.tools.RecyclerViewDivider;
import org.lightsys.eventApp.tools.AutoUpdateWorker;
import org.lightsys.eventApp.tools.qr.launchQRScanner;
import org.lightsys.eventApp.views.SettingsViews.SettingsActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Created by otter57 on 3/29/17.
 * Modified by Littlesnowman88 on 4 June 2018.
 *
 * creates theme, navigation menu, and options menu
 * base activity for app fragment views
 *
 */

public class MainActivity extends AppCompatActivity implements ScannedEventsAdapter.ScannedEventsAdapterOnClickHandler, CompletionInterface {
    static private final String QR_DATA_EXTRA = "qr_data";
    static private final int QR_RESULT = 1;
    private static final String RELOAD_PAGE = "reload_page";
    private static final int BLACK = Color.parseColor("#000000");
    private static final int WHITE = Color.parseColor("#ffffff");

    private Fragment fragment;
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private Context context;
    private Activity activity;
    private Intent main_to_settings; //, updateIntent;
    private LocalDB db;
    private AlertDialog alert;
    private Toolbar toolbar;
    private View previousNavView;
    private ListView navigationList;
    private RecyclerView scannedEventsView;
    private ScannedEventsAdapter scannedEventsAdapter;
    private ArrayList<String[]> scannedEvents;
    private int color, black_or_white;
    private OneTimeWorkRequest autoUpdateWork;
    private PeriodicWorkRequest backgroundUpdateWork;
    ActionBarDrawerToggle toggle;
    private ProgressDialog spinner;
    public static String version;

    private boolean successfulConnection = true;

    //stuff to automatically refresh the current fragment
    private final android.os.Handler refreshHandler = new android.os.Handler();
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        db = new LocalDB(this);
        context = this;
        activity = this;
        main_to_settings = new Intent(MainActivity.this, SettingsActivity.class);

        //deprecated but still used in Android Oreo, as of 23 July 2018 at least. -Littlesnowman88
        spinner = new ProgressDialog(this, R.style.MySpinnerStyle);

        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (Exception e) {
            // ignore
        }

        /*set up auto updater*/
        //updateIntent = new Intent(MainActivity.this, AutoUpdater.class);
        //refreshHandler.postDelayed(refreshRunnable, 1000);

        /* Start auto update */
        startUpdater(false);

        /*set up toolbar*/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*set up scanned events recycler view*/
        String scan_qr = getResources().getString(R.string.scan_new_qr);
        if(db.getEventName(scan_qr).equals("")){
            db.addEvent(scan_qr,scan_qr, getString(R.string.scan_qr_logo));
        }
        scannedEvents = db.getAllEvents();
        scannedEventsView = (RecyclerView) findViewById(R.id.scanned_events_recyclerview);
        scannedEventsView.addItemDecoration(new RecyclerViewDivider(this));
        scannedEventsView.setVisibility(View.GONE);
        scannedEventsView.setEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        scannedEventsView.setLayoutManager(layoutManager);
        scannedEventsAdapter = new ScannedEventsAdapter(this,scannedEvents, context);
        scannedEventsView.setAdapter(scannedEventsAdapter);

        //set theme color
        color = Color.parseColor(db.getThemeColor("themeColor"));
        black_or_white = ColorContrastHelper.determineBlackOrWhite(color);

        /*set up drawer*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //if no events have been imported, load the "no event" state
        if(scannedEvents.size() == 1){ handleNoScannedEvent(); }
        else { gatherData(false); }
    }

    // Called when the DataConnection completes.
    @Override
    public void onCompletion() {
        db.unflagNewNotifications();
        hideSpinner();
    }

    // Show the spinner
    private void showSpinner() {
        if (spinner != null) {
            spinner.setMessage(this.getResources().getString(R.string.loading_data));
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    spinner.setIndeterminate(true);
                    spinner.setCancelable(false);
                    spinner.show();
                }
            });
        }
    }

    // Hide the spinner
    private void hideSpinner() {
        if (spinner != null)
            spinner.dismiss();
    }

    // This gets called when the auto update attempt has been run.
    final Observer<WorkInfo> workObserver = new Observer<WorkInfo>() {
        @Override
        public void onChanged(@Nullable final WorkInfo info) {
            if (info != null && info.getState().isFinished() && info.getState() == WorkInfo.State.SUCCEEDED) {
                hideSpinner();
                //Log.d("workObserver","worker " + autoUpdateWork.getId() + " finished(" + info.getState().toString() + ") - calling stopUpdater()");
                stopUpdater();
                //Log.d("workObserver","worker " + autoUpdateWork.getId() + " finished - calling startUpdater()");
                startUpdater(false);
            }
        }
    };

    // Start the auto update background task
    private void startUpdater(boolean refresh_now) {
        Constraints autoUpdateConstraints;

        /* new  auto update using WorkManager */
        autoUpdateConstraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(!refresh_now)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        autoUpdateWork = new OneTimeWorkRequest.Builder(AutoUpdateWorker.class)
                .setInitialDelay(refresh_now?0:1, TimeUnit.MINUTES)
                .setConstraints(autoUpdateConstraints)
                .setInputData(new Data.Builder().putBoolean("refresh_now", refresh_now).build())
                .build();

        Executor executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();

            }
        };
        class WorkManagerRunnable implements Runnable {
            OneTimeWorkRequest myAutoUpdateWork;
            WorkManagerRunnable(OneTimeWorkRequest autoUpdateWork) { myAutoUpdateWork = autoUpdateWork; }
            public void run() {
                WorkManager.getInstance().enqueue(autoUpdateWork);
                WorkManager.getInstance().getWorkInfoByIdLiveData(autoUpdateWork.getId())
                        .observeForever(workObserver);
            }
        }
        WorkManagerRunnable wmr = new WorkManagerRunnable(autoUpdateWork);
        executor.execute(wmr);

        Log.d("startUpdater","worker " + autoUpdateWork.getId() + " started");
    }

    // Stop the auto update background task
    private void stopUpdater() {
        //Stop OneTimeWorkRequest
        UUID uuid = autoUpdateWork.getId();
        //Log.d("stopUpdater","worker " + uuid + " stopping...");
        WorkManager.getInstance().getWorkInfoByIdLiveData(uuid)
                .removeObserver(workObserver);
        WorkManager.getInstance().cancelWorkById(uuid);
        //Log.d("stopUpdater","worker " + uuid + " stopped");

        //Stop PeriodicWorkRequest
        if (backgroundUpdateWork != null) {
            uuid = backgroundUpdateWork.getId();
            WorkManager.getInstance().getWorkInfoByIdLiveData(uuid)
                    .removeObserver(workObserver);
            WorkManager.getInstance().cancelWorkById(uuid);
        }
    }

    /**
     * Used by the drawer to refresh the toggle button
     * (on activity resume)
     */

    //displays home page as selected in navigation menu
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        onNavigationItemSelected(navigationList.getChildAt(0));
        navigationList.setItemChecked(0, true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //startService(updateIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register mMessageReceiver to receive messages.
        //if opened from notification - open notification screen
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(RELOAD_PAGE));
        if (backgroundUpdateWork != null) {
            WorkManager.getInstance().getWorkInfoByIdLiveData(backgroundUpdateWork.getId())
                    .removeObserver(workObserver);
            WorkManager.getInstance().cancelWorkById(backgroundUpdateWork.getId());
        }
        Log.d("Stop", "MainActivity Started");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigationList == null){
            setupMenusAndTheme();
        }
        Log.d("Stop", "MainActivity Resumed");
    }

    //on return from QRScanner, import data
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_RESULT && resultCode == RESULT_OK && data != null) {
            final String dataURL = data.getStringExtra(QR_DATA_EXTRA);
            new DataConnection(context, activity, "new", dataURL, this).execute("");
        }
    }


    //modified by Littlesnowman88 on 22 June 2018
    //now corrects menu icons for best color (black or white)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //set menu item colors
        toolbar.setTitleTextColor(black_or_white);
        toggle.getDrawerArrowDrawable().setColor(black_or_white);
        MenuItem qr = menu.findItem(R.id.action_rescan);
        MenuItem refresh = menu.findItem(R.id.action_refresh);
        MenuItem settings = menu.findItem(R.id.open_settings_gear);
        if (black_or_white == WHITE) {
            qr.setIcon(R.drawable.ic_event_list);
            refresh.setIcon(R.drawable.ic_refresh);
            settings.setIcon(R.drawable.ic_settings_24dp);
        }
        else {
            qr.setIcon(R.drawable.ic_event_list_black);
            refresh.setIcon(R.drawable.ic_refresh_black);
            settings.setIcon(R.drawable.ic_settings_black_24dp);
        }
        if (!successfulConnection) {
            if (black_or_white == WHITE) {
                menu.getItem(1).setIcon(R.drawable.ic_refresh_error);
            } else {
                menu.getItem(1).setIcon(R.drawable.ic_refresh_error_black);
            }
        }
        invalidateOptionsMenu();

        return super.onCreateOptionsMenu(menu);
    }

    //options menu (refresh, refresh frequency, rescan QR)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_rescan:
                toggleVisibility();
                break;
            case R.id.action_refresh:
                showSpinner();
                //new DataConnection(context, activity, "refresh", db.getGeneral("notifications_url"), false, null).execute("");
                //Log.d("onOptionsItemSelected:refresh","calling stopUpdater()");
                stopUpdater();
                //new DataConnection(context, activity, "refresh", db.getGeneral("url"), false, null).execute("");
                //Log.d("onOptionsItemSelected:refresh","calling startUpdater()");
                startUpdater(true);
                //stopService(updateIntent); //"refresh" (restart) the auto updater
                //updateIntent.removeExtra("refreshed_pressed");
                //updateIntent.putExtra("refresh_pressed", true);
                //startService(updateIntent);
                break;
            case R.id.open_settings_gear:
                startActivity(main_to_settings);
                onPause();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Stop", "MainActivity Destroyed");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        // Create PeriodicWorkRequest for when MainActivity is killed
        Log.d("Stop", "Creating PeriodicWorkRequest...");

        Constraints backgroundUpdateConstraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        backgroundUpdateWork = new PeriodicWorkRequest.Builder(AutoUpdateWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(backgroundUpdateConstraints)
                .setInputData(new Data.Builder().putBoolean("refresh_now", false).build())
                .build();
        WorkManager.getInstance().enqueue(backgroundUpdateWork);
        WorkManager.getInstance().getWorkInfoByIdLiveData(backgroundUpdateWork.getId())
                .observeForever(workObserver);

        super.onStop();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 2 && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gatherData(true);
        } else {
            if (! ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.CAMERA")) {
                Toast.makeText(context, R.string.disabled_camera_permissions, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, R.string.denied_camera_permissions, Toast.LENGTH_LONG).show();
            }
        }
    }

    //Handles what happens when no events have been scanned. It enables the settings, about page, notifications page, and welcome message.
    public void handleNoScannedEvent(){
        //set a dummy url
        db.addGeneral("url", "No_Event");
        db.addGeneral("old_url", "No_Event");
        db.addGeneral("notifications_url", "No_Event");
        int[] no_version = {0,0};
        int[] start_version = {-2,-2};
        if (Arrays.equals(no_version, db.getJSONVersionNum())) db.addJSONVersionNum(start_version);
        else db.replaceJSONVersionNum(start_version);

        //Set welcome message
        String no_event_message = getString(R.string.no_event_welcome);
        db.addGeneral("welcome_message",no_event_message);
        gatherData(false);

        DataConnection.addNotificationTitle(null, db, getResources());
        DataConnection.setupAboutPage(db, getResources());

        //Set refresh rate
        db.addGeneral("refresh_rate",getString(R.string.refresh_val_never));

        //Set time zone
        db.addGeneral("time_zone", TimeZone.getDefault().getID());
        db.addGeneral("remote_viewing","0");
        db.addGeneral("custom_time_zone","0");

        setupMenusAndTheme();
    }

    //Overriding the scanned events recycler view
    @Override
    public void onClick(final String scanned_url) {
        toggleVisibility();
        if(scanned_url.equals(getResources().getString(R.string.scan_new_qr))){
            gatherData(true);
        }
        else {
            String action;
            if (scanned_url.equals(db.getGeneral("url"))) {action = "refresh";}
            else {action = "new";}
            new DataConnection(context, activity, action, scanned_url, this).execute("");
        }
    }

    //Overriding the scanned events recycler view
    @Override
    public boolean onLongClick(final String scanned_url) {
        if(scanned_url.equals(getResources().getString(R.string.scan_new_qr))){
            return true;
        }
        else {
            AlertDialog prompt_event_remove = promptEventRemove(scanned_url);
            prompt_event_remove.show();
        }
        return true;
    }

    //Prompts the user if they would like to delete an event upon a long click
    private AlertDialog promptEventRemove(final String scanned_url){
        AlertDialog dialog_box = new AlertDialog.Builder(this)
                .setMessage(R.string.alert_message)
                .setPositiveButton(R.string.alert_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String most_recent_url = scannedEvents.get(0)[1];
                        resetScannedEventsAdapter("remove",scanned_url);
                        String new_most_recent_url = scannedEvents.get(0)[1];
                        if(new_most_recent_url.equals(getString(R.string.scan_new_qr)) || !new_most_recent_url.equals(most_recent_url)){
                            db.clear();
                            db.deleteNotifications();
                            handleNoScannedEvent();
                        }
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
            return dialog_box;
    }

    private void resetScannedEventsAdapter(String add_or_remove, String scanned_url){
        if(add_or_remove.equals("add")){
            String[] name_url_image = {getValidEventName(scanned_url), scanned_url, getValidEventLogo(scanned_url)};
            addScannedEvent(name_url_image);

            //setupMenusAndTheme() is called here because it needs to happen AFTER a new DataConnection is created,
            //   and new data connections are created after a scanned event is clicked AND after the QR is received.
            //   DataConnection calls this after it has built the database, so the themes will now load properly.
            setupMenusAndTheme();
        }
        else if(add_or_remove.equals("remove")){
            db.removeEvent(scanned_url);
            int event_position = findIndexOfURL(scanned_url);
            scannedEvents.remove(event_position);
        }
        scannedEventsAdapter = new ScannedEventsAdapter(this,scannedEvents, context);
        scannedEventsView.setAdapter(scannedEventsAdapter);
    }

    //If invalid name, returns either the welcome message or "no name". If no connection, returns "Connection failed."
    private String getValidEventName(String scanned_url){
        String name = null;
        if (successfulConnection) {
            String event_name = db.getGeneral("event_name");
            if (event_name != null) {
                name = event_name.trim();
            }
            if (name == null || name.equals("")) {
                //for backwards compatibility with old JSONs
                if (db.getGeneral("event_name") != null) {
                    name = db.getGeneral("event_name");
                } else if (db.getGeneral("welcome_message") != null) {
                    name = db.getGeneral("welcome_message");
                } else {
                    name = getString(R.string.no_event_name);
                }
            }
        } else {
            String no_connection = getString(R.string.no_connection);
            name = db.getEventName(scanned_url);
            if (name.equals("")) {
                name = no_connection;
            } else if (! name.startsWith(no_connection)) {
                name = no_connection + ":\n" + name;
            }
        }
        return name;
    }

    private String getValidEventLogo(String scanned_url){
        if (successfulConnection) { return db.getGeneral("logo"); }
        //else
        String logo = db.getEventLogo(scanned_url);
        if (logo == null || logo.equals("")) {
            logo = getString(R.string.red_X_logo);
        }
        return logo;
    }

    //toggles the visibility of the scanned events recycler view
    private void toggleVisibility() {
        if(scannedEventsView.getVisibility()==View.VISIBLE){
            scannedEventsView.setEnabled(false);
            scannedEventsView.setVisibility(View.GONE);
        }
        else{
            scannedEventsView.setVisibility(View.VISIBLE);
            scannedEventsView.setEnabled(true);
        }
    }

    //if scanned events dropdown is open and click occurs outside dropdown, dropdown closes
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View content;
            int[] contentLocation = new int[2];
            scannedEventsView.getLocationOnScreen(contentLocation);
            Rect scannedEventsRect = new Rect(contentLocation[0],
                    contentLocation[1],
                    contentLocation[0] + scannedEventsView.getWidth(),
                    contentLocation[1] + scannedEventsView.getHeight());

            content = findViewById(R.id.toolbar);
            contentLocation = new int[2];
            content.getLocationOnScreen(contentLocation);
            Rect toolBarRect = new Rect(contentLocation[0],
                    contentLocation[1],
                    contentLocation[0] + content.getWidth(),
                    contentLocation[1] + content.getHeight());

            if (!(scannedEventsRect.contains((int) event.getX(), (int) event.getY())) && !(toolBarRect.contains((int) event.getX(), (int) event.getY())) && scannedEventsView.getVisibility() == View.VISIBLE) {
                toggleVisibility();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void addScannedEvent(String[] name_url_image) {
        if(!hasNameAndUrl(name_url_image)) {
            scannedEvents.add(0,name_url_image);
            if(scannedEvents.size() > 6) {
                db.replaceEvent(scannedEvents.get(5)[1], name_url_image[1], name_url_image[0], name_url_image[2]);
                scannedEvents.remove(5);
            }
            else {
                db.addEvent(name_url_image[1], name_url_image[0], name_url_image[2]);
            }
        }
        else {
            int event_position = findIndexOfURL(name_url_image[1]);
            scannedEvents.remove(event_position);
            scannedEvents.add(0,name_url_image);
            db.replaceEvent(name_url_image[1],name_url_image[1],name_url_image[0], name_url_image[2]);
        }
    }

    private int findIndexOfURL(String url){
        int size = scannedEvents.size();
        for(int i = 0; i < size; i++){
            if(scannedEvents.get(i)[1].equals(url)) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasNameAndUrl(String[] scanned_event){
        for(String[] item: scannedEvents){
            if(scanned_event[1].equals(item[1])){
                return true;
            }
        }
        return false;
    }

    //launches QR scanner
    public void gatherData(boolean launchScanner){
        if (launchScanner) {
            if (ActivityCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
            } else {
                Intent QR = new Intent(MainActivity.this, launchQRScanner.class);
                startActivityForResult(QR, QR_RESULT);
            }
        }else{
            setupMenusAndTheme();
            navigationList.setItemChecked(0, true);
            fragment = new WelcomeView();
            fragmentManager.beginTransaction().replace(R.id.contentFrame,fragment, "WELCOME")
                    .commit();
        }
    }

    //if app does not have camera permission, ask user for permission
    //modified by Littlesnowman88 11 July 2018
    private void requestCameraPermission() {
        Log.w("Barcode-reader", "Camera permission is not granted. Requesting permission");
        final String[] permissions = new String[]{"android.permission.CAMERA"};
        ActivityCompat.requestPermissions(this, permissions, 2);
    }

    //navigation, theme, refresh menu setup
    private void setupMenusAndTheme(){
        ArrayList<Info> menu = db.getNavigationTitles();

        navigationList = (ListView) findViewById(R.id.nav_list);

        color = Color.parseColor(db.getThemeColor("themeColor"));
        black_or_white = ColorContrastHelper.determineBlackOrWhite(color);

        //Navigation Header Color
        int colors [] = {Color.parseColor(db.getThemeColor("theme1"))==0? Color.parseColor(db.getThemeColor("theme1")): Color.parseColor(db.getThemeColor("themeDark")),
                Color.parseColor(db.getThemeColor("theme2"))==0? Color.parseColor(db.getThemeColor("theme2")): Color.parseColor(db.getThemeColor("themeMedium")),
                Color.parseColor(db.getThemeColor("theme3"))==0? Color.parseColor(db.getThemeColor("theme3")): color
        };

        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,colors);
        LinearLayout header = (LinearLayout)findViewById(R.id.nav_header);
        header.setBackground(gd);


        //Navigation Header Image
        ImageView image = (ImageView) findViewById(R.id.imageView);
        String logo = db.getGeneral("logo");
        if (logo == null ) {
            image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_eventapp_text));
        }else{
            byte[] decodedBytes = Base64.decode(logo, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            image.setImageBitmap(decodedBitmap);
        }

        //Navigation menu items
        ArrayList<HashMap<String, String>> itemList = generateListItems(menu);

        String[] from = {"text"};
        int[] to = {R.id.nav_item};
        NavigationAdapter navAdapter = new NavigationAdapter(this, itemList, from, to);
        navigationList.setAdapter(navAdapter);
        navigationList.setOnItemClickListener(new DrawerItemClickListener());
        navigationList.setItemChecked(0, true);

        //Title bar Color
        toolbar.setBackgroundColor(color);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor(db.getThemeColor("themeDark")));
        }

    }

    //generate items for navigation menu
    private ArrayList<HashMap<String, String>> generateListItems(ArrayList<Info> menu) {
        ArrayList<HashMap<String, String>> aList = new ArrayList<>();
        for (Info m : menu) {
            HashMap<String, String> hm = new HashMap<>();

            hm.put("text", m.getHeader());
            hm.put("icon", Integer.toString(getResources().getIdentifier(m.getBody(),"drawable","org.lightsys.eventApp")));

            aList.add(hm);
        }
        return aList;
    }

    //If error, gives user options to retry, rescan, or cancel
    private void RetryConnection(final String scanned_url){
        if (alert !=null){
            alert.cancel();
        }
        final CompletionInterface callback = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setCancelable(false)
                .setMessage("Error: data not imported")
                .setNegativeButton(R.string.retry_connection_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: " + scanned_url);
                        new DataConnection(context, activity, "refresh", scanned_url, callback).execute("");
                    }
                })
                .setPositiveButton(R.string.rescan_qr_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gatherData(true);
                    }
                })
                .setNeutralButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);
        alert = builder.create();
        alert.show();
    }

    /**
     * The listener for the drawer menu. waits for a drawer item to be clicked.
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            onNavigationItemSelected(view);
        }
    }

    //sends user to appropriate page
    //modified by Littlesnowman88 to recognize custom-titled navigation items
    private void onNavigationItemSelected(View view) {
        try {
            if (previousNavView != null) {
                ((TextView) previousNavView.findViewById(R.id.nav_item)).setTextColor(ContextCompat.getColor(context, R.color.darkGray));
                ((ImageView) previousNavView.findViewById(R.id.iconView)).setColorFilter(ContextCompat.getColor(context, R.color.darkGray));
            } else {
                ((TextView) navigationList.getChildAt(0).findViewById(R.id.nav_item)).setTextColor(ContextCompat.getColor(context, R.color.darkGray));
                ((ImageView) navigationList.getChildAt(0).findViewById(R.id.iconView)).setColorFilter(ContextCompat.getColor(context, R.color.darkGray));
            }
            ((TextView) view.findViewById(R.id.nav_item)).setTextColor(color);
            ((ImageView) view.findViewById(R.id.iconView)).setColorFilter(color);
            previousNavView = view;

            String title = ((TextView) view.findViewById(R.id.nav_item)).getText().toString();
            String nav_id = "";
            ArrayList<Info> titles_table = db.getNavigationTitles();
            ArrayList<String> titles = new ArrayList<>();

            for (Info info_item : titles_table) {
                titles.add(info_item.getName());
                if (title.equals(info_item.getHeader())) {
                    nav_id = info_item.getName();
                }
            }

            switch (nav_id) {
                case "Notifications":
                    fragment = new WelcomeView();
                    fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "WELCOME")
                            .commit();
                    break;
                case "Contacts":
                    fragment = new ContactsView();
                    fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "CONTACTS")
                            .commit();
                    break;
                case "Schedule":
                    fragment = new ScheduleView();
                    fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "SCHEDULE")
                            .commit();
                    break;
                case "Maps":
                    fragment = new MapView();
                    fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "MAPS").commit();
                    break;
                case "Housing":
                    fragment = new HousingView();
                    fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "HOUSING")
                            .commit();
                    break;
                case "Prayer Partners":
                    fragment = new PrayerPartnerView();
                    fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "PRAYER_PARTNERS")
                            .commit();
                    break;
                case "About":
                    Bundle about_bundle = new Bundle();
                    about_bundle.putString("page", title);

                    fragment = new AboutPageView();
                    fragment.setArguments(about_bundle);
                    fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "ABOUT")
                            .commit();
                    break;
                default:
                    Bundle bundle = new Bundle();
                    bundle.putString("page", title);

                    fragment = new InformationalPageView();
                    fragment.setArguments(bundle);
                    fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "INFO")
                            .commit();
                    break;
            }

        } catch (Exception e) {
            // ignore
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null)
            drawer.closeDrawer(GravityCompat.START);

    }

    // handler for received Intents for the RELOAD_PAGE  event
    //depending on where DataConnection was called from and what the result was, performs different actions
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent

            /*based on broadcast message received perform correct action*/
            if (intent.getStringExtra("action").equals("retry")){
                //sets refresh error icon and allows user to retry
                successfulConnection = false;
                String received_url = intent.getStringExtra("received_url");
                if (! intent.getBooleanExtra("action_refresh", false)) {
                    resetScannedEventsAdapter("add", received_url);
                }
                RetryConnection(received_url);
            } else if (intent.getStringExtra("action").equals("auto_update_error")) {
                //sets refresh error icon
                successfulConnection = false;
            } else if (intent.getStringExtra("action").equals("new")) {
                successfulConnection = true;
                //if new, sets up menus and theme and sends user to welcome page
                resetScannedEventsAdapter("add", db.getGeneral("url"));
                fragment = new WelcomeView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "WELCOME")
                        .commit();
                //sets refresh icon
            }else if (intent.getStringExtra("action").equals("expired")){
                //if event has expired
                Toast.makeText(context, "Event has expired, please scan a QR for a new event", Toast.LENGTH_SHORT).show();
                successfulConnection = false;
            }else {
                successfulConnection = true;
                try {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
                    //Check that currentFragment is not a ScheduleView - Don't want to update schedule when user is looking at it
                    if (currentFragment != null && !(currentFragment instanceof ScheduleView)) {

                        resetScannedEventsAdapter("add", db.getGeneral("url"));
                        final FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
                        fragTransaction.detach(currentFragment);
                        fragTransaction.attach(currentFragment);
                        fragTransaction.commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                if (intent.getBooleanExtra("update_schedule", false) && fragment instanceof ScheduleView){
                        fragment = new ScheduleView();
                        fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "SCHEDULE")
                                .commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}