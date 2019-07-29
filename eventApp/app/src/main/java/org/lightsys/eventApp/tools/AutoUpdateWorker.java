package org.lightsys.eventApp.tools;

import androidx.concurrent.futures.ResolvableFuture;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.Info;
import org.lightsys.eventApp.views.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Greg Beeley
 *
 * February 23-26, 2019
 *
 * AutoUpdateWorker: automatic update background processing using Android's new WorkManager
 * tool, which really simplifies a lot of things instead of having to worry about alarm and boot up
 * notifications and Android possibly blocking those during Doze.  This is a clean interface and
 * works with all modern versions of Android.
 */

public class AutoUpdateWorker extends ListenableWorker implements CompletionInterface, SharedPreferences.OnSharedPreferenceChangeListener {

    // This is a "Future".  It's similar to a Promise in JavaScript, and provides the ability for
    // the creator of the Future to notify other users of (listeners to) the Future, so those
    // listeners can find out when the Future has been marked with Success or Failure (the
    // "Result").  It's also possible to pass data through the Future -- for that use a Payload
    // instead of a Result for the <generic>.  In our case, we use this to notify WorkManager that
    // our auto update operation is complete.  Note that ResolvableFuture is a subclass of
    // ListenableFuture.
    private ResolvableFuture<ListenableWorker.Result> future;

    //time constants in milliseconds
    private static final int ONE_SECOND     = 1000;
    private static final int ONE_MINUTE     = ONE_SECOND * 60;
    private static final int ONE_HOUR       = ONE_MINUTE * 60;
    private static final int NEVER          = -1;

    private LocalDB db; //local database

    private static int      updateMillis = NEVER; //number of milliseconds between updates
    private static long elapsedTime;
    private static Calendar prevDate     = Calendar.getInstance();

    private Context app_context;
    private Context our_context;

    //accessing shared preferences (refresh rate) set by the settings activity
    private SharedPreferences sharedPreferences;

    //a flag to record if the refresh button in MainActivity has been pressed.
    private boolean refresh_pressed = false;

    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;
    private WifiManager wifiManager = null;
    private WifiManager.WifiLock wifiLock = null;

    //
    // Methods
    //

    public AutoUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        app_context = getApplicationContext();
        our_context = context;
    }

    // Called when Android's WorkManager tells us to begin the auto update process.
    @Override @NonNull
    public ListenableFuture<ListenableWorker.Result> startWork() {
        future = ResolvableFuture.create();

        Log.d("AutoUpdateWorker","startWork()");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app_context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        db = new LocalDB(our_context);

        // Need to do a refresh immediately?
        refresh_pressed = getInputData().getBoolean("refresh_now", false);
        if (refresh_pressed) {
            processRefreshPressed();
        }

        checkForUpdates();

        return future;
    }

    // Called when Android has decided to cancel our Worker.
    @Override
    public void onStopped() {
        if (sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    // Called when the DataConnection completes.
    @Override
    public void onCompletion() {
        //list of new notifications
        ArrayList<Info> notifications = db.getNewNotifications();

        //send notifications
        try {
            for (Info item : notifications) {
                createNotificationChannel();
                sendNotification(item.getId(), item.getHeader(), item.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.unflagNewNotifications();

        // Let the listeners (i.e., WorkManager) know that we're done.
        future.set(ListenableWorker.Result.success());

        // Release any wake lock now that we're done.
        pmCleanup();
    }

    /** updates the refresh rate and checks for updates with the new refresh rate set.
     * created by Littlesnowman88 on 20 June 2018.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("refresh_rate")) {
            setRefreshFrequency();
            checkForUpdates();
        }
    }

    // Release any wake and wifi locks.
    private void pmCleanup() {
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
        wifiLock = null;
        wifiManager = null;
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        wakeLock = null;
        powerManager = null;
    }

    private void acquirePowerLock() {
        // Acquire the power manager wake lock.
        if (wakeLock == null || !wakeLock.isHeld()) {
            powerManager = (PowerManager) app_context.getSystemService(Context.POWER_SERVICE);
            try {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "org.lightsys.eventApp.tools.AutoUpdater:wakelock");
            } catch (Exception e) {
                pmCleanup();
                return;
            }
            wakeLock.acquire(5000 /* milliseconds */);
        }
    }

    private void acquireWifiLock() {
        // Acquire the wifi manager lock.
        if (wifiLock == null || !wifiLock.isHeld()) {
            wifiManager = (WifiManager) app_context.getSystemService(Context.WIFI_SERVICE);
            try {
                wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "org.lightsys.eventApp.tools.AutoUpdater:wifilock");
            } catch (Exception e) {
                pmCleanup();
            }
        }
    }
    
    private void checkForUpdates() {
        db.close();

        Calendar currentDate = Calendar.getInstance();
        elapsedTime = currentDate.getTimeInMillis() - prevDate.getTimeInMillis();

        //check to see if the time elapsed is greater than the update period
        // also check to see if the expiration date has passed. -Littlesnowman88
        if ((refresh_pressed || (elapsedTime > updateMillis && updateMillis > 0)) && (eventIsNow())){
            Log.d("checkForUpdates","prev: " + prevDate.getTimeInMillis() + ", elapsed: " + elapsedTime + ", updateMillis: " + updateMillis);
            getUpdates();
            prevDate = Calendar.getInstance();
            setRefreshFrequency(); //needs to be after any changes to elapsedTime and prevDate because of auto.
        } else {
            pmCleanup();
            setRefreshFrequency(); //needs to be after any changes to elapsedTime and prevDate because of auto.

            // Let the listeners (i.e., WorkManager) know that we're done.
            future.set(ListenableWorker.Result.success());
        }
    }

    /** Created by Littlesnowman88 */
    private boolean eventIsNow() {
        SimpleDateFormat expirationDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Date last_day;
        try {
            last_day = expirationDate.parse(db.getGeneral("refresh_expire"));
            return !(new Date().after(last_day));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void getUpdates() {
        // Begin fetching updates.  We'll get a completion notification when it's done.
        new DataConnection(our_context, null, (refresh_pressed?"refresh":"auto_update"), db.getGeneral("url"), this).execute("");

        Intent auto_to_main = new Intent(our_context, MainActivity.class);
        auto_to_main.putExtra("update_schedule", true);
        our_context.sendBroadcast(auto_to_main);
    }

    private void processRefreshPressed() {
        elapsedTime = 0;
        String db_refresh_rate;

        //catch allows for old JSON compatibility
        try { db_refresh_rate = db.getGeneral("refresh_rate").trim(); }
        catch (Exception e) {
            db_refresh_rate = db.getGeneral("refresh");
            //in Testing/Demo QR code, the refresh rate is -1. Otherwise this "if" statement isn't a problem.
            if (db_refresh_rate==null || db_refresh_rate.equals("-1")) {db_refresh_rate = "never"; }
        }

        String refresh_setting = sharedPreferences.getString("refresh_rate", db_refresh_rate);
        if (refresh_setting == null || refresh_setting.equals("auto")) {
            setAutoRefresh();
        }
    }

    /** checks the system preferences (or defaults to the database's general refresh rate)
     *  and sets the notifications refresh rate. Takes device's sleep state into account.
     *  "auto" takes wifi and cellular connection into account, too.
     * @author: Littlesnowman88
     */
    private void setRefreshFrequency() {
        String db_refresh_rate;
        //catch allows for old JSON compatibility
        try {
            db_refresh_rate = db.getGeneral("refresh_rate").trim();
        } catch (Exception e) {
            db_refresh_rate = db.getGeneral("refresh");
            //in Testing/Demo QR code, the refresh rate is -1. Otherwise this "if" statement isn't a problem.
            if (db_refresh_rate==null || db_refresh_rate.equals("-1")) {db_refresh_rate = "never"; }
        }

        String refresh_setting = sharedPreferences.getString("refresh_rate", db_refresh_rate);
        if (refresh_setting == null) {
            updateMillis = 5 * ONE_MINUTE;
        } else {
            switch (refresh_setting) {
                case "never":
                    updateMillis = NEVER;
                    break;
                case "auto":
                    setAutoRefresh();
                    break;
                case "default":
                    if (db_refresh_rate.equals("never")) {
                        updateMillis = NEVER;
                    } else if (db_refresh_rate.equals("auto")) {
                        setAutoRefresh();
                    } else {
                        try {
                            updateMillis = Integer.parseInt(db_refresh_rate) * ONE_MINUTE;
                        } catch (NumberFormatException e) {
                            setAutoRefresh();
                        }
                    }
                    break;
                case "1":
                    if (!isAwake()) {
                        refresh_setting = "5";
                    } //then continue onto default.
                default:
                    updateMillis = Integer.parseInt(refresh_setting) * ONE_MINUTE; // refresh_setting, converted into milliseconds
            }
        }
        refresh_pressed = false;
    }

    /** runs through an algorithm to determine the required refresh rate for auto.
     *  If on wifi:
     *      check for updates every five minutes if sleeping, or every 1 minute if awake.
     *  If on Cellular:
     *      check every 5 minutes or JSON refresh rate # mins if update or refresh in past hour
     *      increase to next increment of 10, 15, 30, or 60 minutes for every additional passed hour, otherwise.
     *      GRB: with new minimalist get.php approach on server, set max increment to 15 min, not 60 min.
     * @author: Littlesnowman88
     */
    private void setAutoRefresh() {
        if (deviceHasWifi()) {
            if (isAwake()) {
                updateMillis = ONE_MINUTE;
            } else {
                updateMillis = 5 * ONE_MINUTE;
            }
        } else { //if connected via cellular network
            //if refresh has been pressed or if an update has happened in the last hour.
            if ((elapsedTime < (ONE_HOUR)) || refresh_pressed) {
                updateMillis = chooseMaxTime(5);
            } else if (elapsedTime < (2 * ONE_HOUR)) {
                updateMillis = chooseMaxTime(10);
            } else /*if (elapsedTime < (3 * ONE_HOUR)) */ {
                updateMillis = chooseMaxTime(15);
            } /* else if (elapsedTime < (4 * ONE_HOUR)) {
                updateMillis = chooseMaxTime(30);
            } else {
                updateMillis = ONE_HOUR;
            }*/
        }
    }

    //helper function of setAutoRefresh to protect against parsing an integer from an invalid string.
    //Created by Littlesnowman88
    private int chooseMaxTime(int time) {
        try {
            String db_refresh_rate;
            //catch allows for old JSON compatibility
            try { db_refresh_rate = db.getGeneral("refresh_rate").trim(); }
            catch (Exception e) {
                db_refresh_rate = db.getGeneral("refresh");
                //in Testing/Demo QR code, the refresh rate is -1. Otherwise this "if" statement isn't a problem.
                if (db_refresh_rate.equals("-1")) {db_refresh_rate = "never"; }
            }
            return Math.max(time, Integer.parseInt(db_refresh_rate)) * ONE_MINUTE;
        } catch (Exception e) {
            //if JSON default_rate is never or auto, choose the passed in value.
            return time * ONE_MINUTE;
        }
    }

    /** Created by Littlesnowman88
     * Based on revolutionary and Peter Mortensen on Stack Overflow
     * https://stackoverflow.com/questions/3841317/how-do-i-see-if-wi-fi-is-connected-on-android
     * @return: true if wifi is connected, false if wifi is disabled or not connected.
     */
    private boolean deviceHasWifi() {
        acquireWifiLock();
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiStatus = wifiManager.getConnectionInfo();

            // Is there an access point connection?
            return !( (wifiStatus != null) && (wifiStatus.getNetworkId() == -1) );
        } else {
            return false; //in this case, wifi is disabled.
        }
    }

    /** API-dependent power manager functions related to device wakefulness **/
    private boolean isAwake() {
        boolean awake;
        acquirePowerLock();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) { //if api > 20
            awake = powerManager.isInteractive();
        } else {
            awake = powerManager.isScreenOn();
        }
        return awake;
    }

    private void sendNotification(int notificationID, String title, String subject){

        NotificationManager notificationManager = (NotificationManager)
                app_context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder nBuild;
        PowerManager.WakeLock screenWakeLock = null;
        Notification n;

        // Build the notification to be sent
        // BigTextStyle allows notification to be expanded if text is more than one line
        Intent notificationIntent = new Intent(app_context, MainActivity.class);
        PendingIntent intent = PendingIntent.getActivity(our_context, 0, notificationIntent, 0);

        nBuild = new NotificationCompat.Builder(app_context, app_context.getString(R.string.channel_id))
                .setContentTitle(title)
                .setContentText(subject)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setSmallIcon(R.drawable.ic_sbcat_transparent_nowords)
                .setColor(Color.parseColor(db.getThemeColor("themeDark")))
                .setContentIntent(intent)
                .setPriority(1)
                .setChannelId(app_context.getString(R.string.channel_id))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(subject));

        // Turn on the device and send the notification.  SCREEN_BRIGHT_WAKE_LOCK
        // has been deprecated, but some wake lock level is required.  We'll keep it
        // for now.
        try {
            // Our standard set of flags:
            int pmFlags = /* how did this get in here: WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    |*/ PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE;

            // On older SDK's that don't support channels, we add the higher lock level.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                //pmFlags |= PowerManager.SCREEN_DIM_WAKE_LOCK;
                pmFlags |= PowerManager.PARTIAL_WAKE_LOCK;
            } else {
                //pmFlags |= PowerManager.SCREEN_DIM_WAKE_LOCK;
                pmFlags |= PowerManager.PARTIAL_WAKE_LOCK;
            }

            // Get the wake lock
            if (powerManager != null) {
                screenWakeLock = powerManager.newWakeLock(
                        pmFlags,
                        "org.lightsys.eventApp.tools.AutoUpdater:scrwakelock"
                );
                screenWakeLock.acquire(1500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        n = nBuild.build();
        try {
            notificationManager.notify(notificationID, n);
        } catch (Exception e) {
            // ignore
        }
        if (screenWakeLock != null && screenWakeLock.isHeld()) {
            screenWakeLock.release();
        }
    }

    // This is an Oreo-and-newer feature.  NOP if below sdk 26.
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Our notification channel options.
            String CHANNEL_ID = app_context.getString(R.string.channel_id);
            String name = app_context.getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            // Create the channel.
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationManager notificationManager = app_context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
