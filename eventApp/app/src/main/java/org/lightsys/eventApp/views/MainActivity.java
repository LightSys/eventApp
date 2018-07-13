package org.lightsys.eventApp.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.Info;
import org.lightsys.eventApp.tools.AutoUpdater;
import org.lightsys.eventApp.tools.DataConnection;
import org.lightsys.eventApp.tools.LocalDB;
import org.lightsys.eventApp.tools.NavigationAdapter;
import org.lightsys.eventApp.tools.RefreshPressedHelper;
import org.lightsys.eventApp.tools.ScannedEventsAdapter;
import org.lightsys.eventApp.tools.qr.launchQRScanner;
import org.lightsys.eventApp.views.SettingsViews.SettingsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.TimeZone;
import java.util.zip.Inflater;

import static android.content.ContentValues.TAG;

/**
 * Created by otter57 on 3/29/17.
 * Modified by Littlesnowman88 on 4 June 2018.
 *
 * creates theme, navigation menu, and options menu
 * base activity for app fragment views
 *
 */

public class MainActivity extends AppCompatActivity implements ScannedEventsAdapter.ScannedEventsAdapterOnClickHandler{
    static private final String QR_DATA_EXTRA = "qr_data";
    static private final int QR_RESULT = 1;
    private static final String RELOAD_PAGE = "reload_page";
    private static final int BLACK = Color.parseColor("#000000");
    private static final int WHITE = Color.parseColor("#ffffff");

    private Fragment fragment;
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private Context context;
    private Activity activity;
    private Intent main_to_settings, updateIntent;
    private LocalDB db;
    private AlertDialog alert;
    private Toolbar toolbar;
    private View previousNavView;
    private ListView navigationList;
    private RecyclerView scannedEventsView;
    private ScannedEventsAdapter scannedEventsAdapter;
    private ArrayList<String[]> scannedEvents;
    private int color, black_or_white;
    ActionBarDrawerToggle toggle;

    private boolean successfulConnection = true;

    private RefreshPressedHelper refresh_pressed_helper;

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

        /*set up auto updater*/
        updateIntent = new Intent(getBaseContext(), AutoUpdater.class);
        startService(updateIntent);
        refreshHandler.postDelayed(refreshRunnable, 1000);

        /*set up toolbar*/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*set up scanned events recycler view*/
        String scan_qr = getResources().getString(R.string.scan_new_qr);
        if(db.getEvent(scan_qr) == null){
            db.addEvent(scan_qr,scan_qr);
        }
        scannedEvents = db.getAllEvents();
        scannedEventsView = findViewById(R.id.scanned_events_recyclerview);
        scannedEventsView.setVisibility(View.GONE);
        scannedEventsView.setEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        scannedEventsView.setLayoutManager(layoutManager);
        scannedEventsAdapter = new ScannedEventsAdapter(this,scannedEvents);
        scannedEventsView.setAdapter(scannedEventsAdapter);

        //set theme color
        color = Color.parseColor(db.getThemeColor("themeColor"));
        determineBlackOrWhite(color);

        /*set up drawer*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //if no data, import data
        gatherData(db.getGeneral("refresh_expire") == null);
        if(scannedEvents.size() == 1){
            handleNoScannedEvent();
        }
    }

    /**
     * Used by the drawer to refresh the toggle button
     * (on activity resume)
     */
    @Override
    public void onPostCreate(Bundle savedInstanceState){
        refresh_pressed_helper = RefreshPressedHelper.getInstance();
        super.onPostCreate(savedInstanceState);
    }

    //displays home page as selected in navigation menu
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        onNavigationItemSelected(navigationList.getChildAt(0));
        navigationList.setItemChecked(0, true);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register mMessageReceiver to receive messages.
        //if opened from notification - open notification screen
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(RELOAD_PAGE));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigationList == null){
            setupMenusAndTheme();
        }
    }

    //on return from QRScanner, import data
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_RESULT && resultCode == RESULT_OK && data != null) {
            final String dataURL = data.getStringExtra(QR_DATA_EXTRA);
            Runnable updateEventList = new Runnable() {
                @Override
                public void run() {
                    resetScannedEventsAdapter(dataURL);
                }
            };
            new DataConnection(context, activity, "new", dataURL, true, null,updateEventList).execute("");
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

    /**
     * converts the given hex_color into grayscale and determines whether toolbar items should be black or white
     * Created by Littlesnowman88 on 22 June 2018
     * @param theme_color, the integer color to be analyzed and contrasted against
     * Postcondition: black_or_white = #000000 if black, ffffff if white, whatever will show better given hex_color
     */
    private void determineBlackOrWhite(int theme_color) {
        int r = Color.red(theme_color); // 0 < r < 255
        int g = Color.green(theme_color); // 0 < g < 255
        int b = Color.blue(theme_color); // 0 < b < 255
        int average_intensity = (r + g + b) / 3;
        if (average_intensity >= 120) {black_or_white = BLACK; }
        else {black_or_white = WHITE; }
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
                refresh_pressed_helper.setRefreshPressed(this);
                final String current_url = db.getGeneral("url");
                Runnable refresh_ui = new Runnable() {
                    @Override
                    public void run() {
                        color = Color.parseColor(db.getThemeColor("themeColor"));
                        determineBlackOrWhite(color);
                        setupMenusAndTheme();
                    }
                };
                new DataConnection(context, activity, "refresh", current_url, true, null, refresh_ui).execute("");
                stopService(updateIntent); //"refresh" (restart) the auto updater
                startService(updateIntent);
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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        //Set welcome message
        String no_event_message = getString(R.string.no_event_welcome);
        db.addGeneral("welcome_message",no_event_message);
        gatherData(false);

        DataConnection.addNotificationTitle(null, db, this);
        DataConnection.setupAboutPage(db, this);

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
            Runnable updateList = new Runnable() {
                @Override
                public void run() {
                    resetScannedEventsAdapter(scanned_url);
                }
            };
            new DataConnection(context, activity, "new", scanned_url, true, null, updateList).execute("");
        }
    }

    private void resetScannedEventsAdapter(String scanned_url){
        String[] name_and_url = {getValidEventName(), scanned_url};
        addScannedEvent(name_and_url);
        scannedEventsAdapter = new ScannedEventsAdapter(this,scannedEvents);
        scannedEventsView.setAdapter(scannedEventsAdapter);
        //setupMenusAndTheme() is called here because it needs to happen AFTER a new DataConnection is created,
        //   and new data connections are created after a scanned event is clicked AND after the QR is received.
        //   DataConnection calls this after it has built the database, so the themes will now load properly.
        color = Color.parseColor(db.getThemeColor("themeColor"));
        determineBlackOrWhite(color);
        setupMenusAndTheme();
    }

    //Returns "No Name" if invalid
    private String getValidEventName(){
        String name = null;
        String event_name = db.getGeneral("event_name");
        if (event_name != null){ name = event_name.trim();}
        if (name == null || name.equals("")) { name = getString(R.string.no_event_name); }
        return name;
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

    public void addScannedEvent(String[] name_and_url) {
        if(!hasNameAndUrl(name_and_url)) {
            scannedEvents.add(0,name_and_url);
            if(scannedEvents.size() > 6) {
                db.replaceEvent(scannedEvents.get(5)[1], name_and_url[1], name_and_url[0]);
                scannedEvents.remove(5);
            }
            else {
                db.addEvent(name_and_url[1], name_and_url[0]);
            }
        }
        else {
            int event_position = findIndexOfUrl(name_and_url);
            scannedEvents.remove(event_position);
            scannedEvents.add(0,name_and_url);
            db.replaceEvent(name_and_url[1],name_and_url[1],name_and_url[0]);
        }
    }

    private int findIndexOfUrl(String[] name_and_url){
        int size = scannedEvents.size();
        for(int i = 0; i < size; i++){
            if(scannedEvents.get(i)[1].equals(name_and_url[1])) {
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
    private void RetryConnection(){
        if (alert !=null){
            alert.cancel();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setCancelable(false)
                .setMessage("Error: data not imported")
                .setNegativeButton(R.string.retry_connection_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: " + db.getGeneral("url"));
                        new DataConnection(context, activity, "refresh", db.getGeneral("url"), true, null,null).execute("");
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
                RetryConnection();
            } else if (intent.getStringExtra("action").equals("auto_update_error")) {
                //sets refresh error icon
                successfulConnection = false;
            } else if (intent.getStringExtra("action").equals("new")) {
                //if new, sets up menus and theme and sends user to welcome page
                setupMenusAndTheme();
                fragment = new WelcomeView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "WELCOME")
                        .commit();
            }else if (intent.getStringExtra("action").equals("expired")){
                //if event has expired
                Toast.makeText(context, "Event has expired, please scan a QR for a new event", Toast.LENGTH_SHORT).show();
                successfulConnection = false;
            }else {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
                final FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
                fragTransaction.detach(currentFragment);
                fragTransaction.attach(currentFragment);
                fragTransaction.commit();
            }
        }
    };
}