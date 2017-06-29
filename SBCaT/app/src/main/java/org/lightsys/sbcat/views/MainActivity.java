package org.lightsys.sbcat.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
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

import org.lightsys.sbcat.R;
import org.lightsys.sbcat.data.Info;
import org.lightsys.sbcat.tools.AutoUpdater;
import org.lightsys.sbcat.tools.DataConnection;
import org.lightsys.sbcat.tools.LocalDB;
import org.lightsys.sbcat.tools.NavigationAdapter;
import org.lightsys.sbcat.tools.RefreshAdapter;
import org.lightsys.sbcat.tools.qr.myTest;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity {
    static private final String QR_DATA_EXTRA = "qr_data";
    static private final int QR_RESULT = 1;
    private static final String RELOAD_PAGE = "reload_page";


    private Fragment fragment;
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private Context context;
    private Activity activity;
    private LocalDB db;
    private AlertDialog alert;
    private Toolbar toolbar;
    private ListView refreshList;
    private View previousNavView;
    private String [] refreshCategories;
    private LinearLayout navigationView;
    private ListView navigationList;
    private DrawerLayout drawer;
    private int color;
    private int refreshItem = -1;

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

        /*set up auto updater*/
        Intent updateIntent = new Intent(getBaseContext(), AutoUpdater.class);
        startService(updateIntent);
        refreshHandler.postDelayed(refreshRunnable, 1000);

        /*set up toolbar*/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*set up drawer*/
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //if no data, import data
        gatherData(db.getGeneral("year"));
    }

    private void createNavigationMenu(){
        //navigationView = (LinearLayout) findViewById(R.id.nav_view);
        //assert navigationView != null;
        ArrayList<Info> menu = db.getNavigationTitles();

        navigationList = (ListView) findViewById(R.id.nav_list);

        //set up refresh menu
        refreshList = (ListView) findViewById(R.id.refresh_list);
        refreshCategories = getResources().getStringArray(R.array.refresh_options);
        refreshList.setAdapter(new RefreshAdapter(context, refreshCategories));
        if (refreshItem == -1) {
            int index = -1;
            for (int n = 0; n<refreshCategories.length;n++){
                if (refreshCategories[n].equals(db.getGeneral("refresh"))){
                    index = n;
                }
            }
            if (index == -1){
                index = 1;
                db.updateRefreshRate(refreshList.getItemAtPosition(index).toString());
            }
            refreshList.setItemChecked(index, true);
            refreshItem = index;
        }else{
            refreshList.setItemChecked(refreshItem, true);
        }

        //set theme color
        color = Color.parseColor(db.getThemeColor("themeColor"));

        //Navigation Header Color
        int colors [] = {Color.parseColor(db.getThemeColor("themeDark")), Color.parseColor(db.getThemeColor("themeMedium")), color};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,colors);
        LinearLayout header = (LinearLayout)findViewById(R.id.nav_header);
        header.setBackground(gd);


        //Navigation Header Image
        ImageView image = (ImageView) findViewById(R.id.imageView);
        String logo = db.getGeneral("logo");
        if (logo == null ) {
            image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sbcat_transparent));
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

    private ArrayList<HashMap<String, String>> generateListItems(ArrayList<Info> menu) {
        ArrayList<HashMap<String, String>> aList = new ArrayList<>();
        for (Info m : menu) {
            HashMap<String, String> hm = new HashMap<>();

            hm.put("text", m.getHeader());
            hm.put("icon", Integer.toString(getResources().getIdentifier(m.getBody(),"drawable","org.lightsys.sbcat")));

            aList.add(hm);
        }
        return aList;
    }

    public void gatherData(String year){
        Log.d(TAG, "gatherData:  "+ year);

        if (year == null) {
            while (ActivityCompat.checkSelfPermission(this, "android.permission.CAMERA") != 0) {
                requestCameraPermission();
            }
            Intent QR = new Intent(MainActivity.this, myTest.class);
            startActivityForResult(QR, QR_RESULT);
        }else{
            Log.d(TAG, "gatherData: gather data");
            createNavigationMenu();
            navigationList.setItemChecked(0, true);
            fragment = new WelcomeView();
            fragmentManager.beginTransaction().replace(R.id.contentFrame,fragment)
                    .commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    /**
     * Used by the drawer to refresh the toggle button
     * (on activity resume)
     */
    @Override
    public void onPostCreate(Bundle savedInstanceState){

        super.onPostCreate(savedInstanceState);
    }

    private void requestCameraPermission() {
        Log.w("Barcode-reader", "Camera permission is not granted. Requesting permission");
        final String[] permissions = new String[]{"android.permission.CAMERA"};
        if(!ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.CAMERA")) {
            ActivityCompat.requestPermissions(this, permissions, 2);
        } else {
            new View.OnClickListener() {
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, 2);
                }
            };
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate((R.menu.main), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_refresh:
                new DataConnection(context, activity, "refresh", db.getGeneral("url"), true).execute("");
                break;
            case R.id.action_refresh_dropdown:
                if (refreshList.getVisibility()==View.VISIBLE){
                    closeRefresh();
                }else{
                    openRefresh();
                    refreshList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            //highlight new selection and set refresh rate
                            refreshList.setItemChecked(i, true);
                            refreshItem = i;
                            db.updateRefreshRate(refreshCategories[i]);
                            closeRefresh();
                        }
                    });
                }
                break;
            case R.id.action_rescan:
                gatherData(null);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void closeRefresh(){
        refreshList.setVisibility(View.GONE);
        findViewById(R.id.refresh_list_header).setVisibility(View.GONE);
        findViewById(R.id.refresh_list_border).setVisibility(View.GONE);
    }

    private void openRefresh(){
        refreshList.setVisibility(View.VISIBLE);
        findViewById(R.id.refresh_list_header).setVisibility(View.VISIBLE);
        findViewById(R.id.refresh_list_border).setVisibility(View.VISIBLE);
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

    private void onNavigationItemSelected(View view) {
        // Handle navigation view item clicks here.

        if (previousNavView != null){
            ((TextView)previousNavView.findViewById(R.id.nav_item)).setTextColor(ContextCompat.getColor(context, R.color.darkGray));
            ((ImageView)previousNavView.findViewById(R.id.iconView)).setColorFilter(ContextCompat.getColor(context, R.color.darkGray));
        } else{
            ((TextView)navigationList.getChildAt(0).findViewById(R.id.nav_item)).setTextColor(ContextCompat.getColor(context, R.color.darkGray));
            ((ImageView)navigationList.getChildAt(0).findViewById(R.id.iconView)).setColorFilter(ContextCompat.getColor(context, R.color.darkGray));
        }
        ((TextView) view.findViewById(R.id.nav_item)).setTextColor(color);
        ((ImageView) view.findViewById(R.id.iconView)).setColorFilter(color);
        previousNavView = view;

        String title = ((TextView)view.findViewById(R.id.nav_item)).getText().toString();
        switch(title) {
            case "Notifications":
                fragment = new WelcomeView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment)
                        .commit();
                break;
            case "Contacts":
                fragment = new ContactsView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment)
                        .commit();
                break;
            case "Schedule":
                fragment = new ScheduleView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment)
                        .commit();
                break;
            case "Housing":
                fragment = new HousingView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment)
                    .commit();
                break;
            case "Prayer Partners":
                fragment = new PrayerPartnerView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment)
                    .commit();
                break;
            default:
                Bundle bundle = new Bundle();
                bundle.putString("page", title);

                fragment = new InformationalPageView();
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment)
                        .commit();
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_RESULT && resultCode == RESULT_OK && data != null) {
            String dataURL = data.getStringExtra(QR_DATA_EXTRA);

            new DataConnection(context, activity, "new", dataURL, true).execute("");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(RELOAD_PAGE));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigationList ==null){
            createNavigationMenu();
        }
        navigationList.setItemChecked(0, true);
        fragment = new WelcomeView();
        fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "WelcomeView")
                .commit();
    }

    // handler for received Intents for the RELOAD_PAGE  event
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent

            //set Refresh dropdown select
            if (refreshList.getCheckedItemPosition() == -1) {
                int child = 0;
                for (int i = 0; i < refreshCategories.length; i++) {
                    if (refreshCategories[i].equals(db.getGeneral("refresh"))) {
                        child = i;
                    }
                }
                refreshList.setItemChecked(child, true);

            }

            //based on broadcast message received perform correct action
            if (intent.getStringExtra("action").equals("retry")){
                RetryConnection();
            } else if (intent.getStringExtra("action").equals("new")) {
                createNavigationMenu();
                fragment = new WelcomeView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "WelcomeView")
                        .commit();

            }else {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
                final FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
                fragTransaction.detach(currentFragment);
                fragTransaction.attach(currentFragment);
                fragTransaction.commit();
            }
        }
    };

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
                        new DataConnection(context, activity, "refresh", db.getGeneral("url"), true).execute("");
                    }
                })
                .setPositiveButton(R.string.rescan_qr_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gatherData(null);
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

    @Override
    protected void onStop() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onStop();
    }
}