package org.lightsys.sbcat.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
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
import android.widget.ImageView;

import org.lightsys.sbcat.R;
import org.lightsys.sbcat.data.Info;
import org.lightsys.sbcat.tools.AutoUpdater;
import org.lightsys.sbcat.tools.DataConnection;
import org.lightsys.sbcat.tools.LocalDB;
import org.lightsys.sbcat.tools.qr.myTest;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static private final String QR_DATA_EXTRA = "qr_data";
    static private final int QR_RESULT = 1;
    private static final String RELOAD_PAGE = "reload_page";


    private Fragment fragment;
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private String dataURL;
    private Context context;
    private Activity activity;
    private LocalDB db;
    private AlertDialog alert;
    private Toolbar toolbar;

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

        /*set up auto updater*/
        Intent updateIntent = new Intent(getBaseContext(), AutoUpdater.class);
        startService(updateIntent);
        refreshHandler.postDelayed(refreshRunnable, 1000);

        /*set up toolbar*/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;
        activity = this;

        /*set up drawer*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //if no data, import data
        db = new LocalDB(this);
        gatherData(db.getGeneral("year"));

    }

    private void createNavigationMenu(ArrayList<Info> menu){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;

        //Navigation Header Color
        int colors [] = {Color.parseColor(db.getThemeColor("themeDark")), Color.parseColor(db.getThemeColor("themeMedium")), Color.parseColor(db.getThemeColor("themeColor"))};
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,colors);
        View header = navigationView.getHeaderView(0);
        header.setBackground(gd);

        //Navigation Header Image
        ImageView image = (ImageView) header.findViewById(R.id.imageView);
        String logo = db.getGeneral("logo");
        if (logo.equals("null")) {
            image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sbcat_transparent));
        }else{
            byte[] decodedBytes = Base64.decode(logo, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            image.setImageBitmap(decodedBitmap);
        }

        //Navigation menu items
        Menu navMenu = navigationView.getMenu();
        navMenu.clear();
        Resources r = getResources();
        for (int m=0; m < menu.size();m++){
            int drawableId = r.getIdentifier(menu.get(m).getBody(),"drawable","org.lightsys.sbcat");
            navMenu.add(0, 0, Menu.NONE, menu.get(m).getHeader()).setIcon(drawableId);
        }
        navigationView.setNavigationItemSelectedListener(this);

        //Title bar Color
        toolbar.setBackgroundColor(Color.parseColor(db.getThemeColor("themeColor")));

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor(db.getThemeColor("themeDark")));
        }
    }

    public void gatherData(String year){

        if (year == null) {
            while (ActivityCompat.checkSelfPermission(this, "android.permission.CAMERA") != 0) {
                requestCameraPermission();
            }
            Intent QR = new Intent(MainActivity.this, myTest.class);
            startActivityForResult(QR, QR_RESULT);
        }else{
            createNavigationMenu(db.getNavigationTitles());
            fragment = new WelcomeView();
            fragmentManager.beginTransaction().replace(R.id.contentFrame,fragment,"WelcomeView")
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
        if (id == R.id.action_refresh) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String title = (String) item.getTitle();
        Log.d(TAG, "onNavigationItemSelected: " + title);

        switch(title) {
            case "Notifications":
                fragment = new WelcomeView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "WelcomeView")
                        .commit();
                break;
            case "Contacts":
                fragment = new ContactsView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "ContactView")
                        .commit();
                break;
            case "Schedule":
                fragment = new ScheduleView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "ScheduleView")
                        .commit();
                break;
            case "Housing":
                fragment = new HousingView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "HousingView")
                    .commit();
                break;
            case "Prayer Partners":
                fragment = new PrayerPartnerView();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "PrayerPartnerView")
                    .commit();
                break;
            case "Refresh":
                new DataConnection(context, activity, "refresh", db.getGeneral("url"), true).execute("");
                break;
            case "Rescan":
                gatherData(null);
                break;
            default:
                Bundle bundle = new Bundle();
                bundle.putString("page", title);

                fragment = new InformationalPageView();
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "InformationalPageView")
                        .commit();
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);

        return id != R.id.nav_refresh;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_RESULT && resultCode == RESULT_OK && data != null) {
            dataURL = data.getStringExtra(QR_DATA_EXTRA);

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
        fragment = new WelcomeView();
        fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment, "WelcomeView")
                .commit();
    }

    // handler for received Intents for the RELOAD_PAGE  event
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent

            if (intent.getStringExtra("action").equals("retry")){
                RetryConnection();
            } else if (intent.getStringExtra("action").equals("new")) {
                createNavigationMenu(db.getNavigationTitles());
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