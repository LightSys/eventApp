package org.lightsys.eventApp.tools;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.Info;
import org.lightsys.eventApp.views.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Judah Sistrunk
 * created on 5/25/2016.
 * copied from missionary app
 * service class that automatically updates local database with server database
 *
 * Modified by Littlesnowman88 8 June 2018
 *  SharedPreferences updates.
 *  Update frequency while device is asleep.
 */
public class AutoUpdater extends Service implements CompletionInterface, SharedPreferences.OnSharedPreferenceChangeListener {

    //time constants in milliseconds
    private static final int ONE_SECOND     = 1000;
    private static final int ONE_MINUTE     = ONE_SECOND * 60;
    private static final int ONE_HOUR       = ONE_MINUTE * 60;
    private static final int NEVER          = -1;

    private LocalDB db; //local database

    private int      updateMillis = NEVER; //number of milliseconds between updates
    private long elapsedTime;
    private Calendar prevDate     = Calendar.getInstance();

    //custom timer that ticks every minute
    //used to constantly check to see if it's time to check for updates
    private Handler timerHandler  = new Handler();

    //accessing shared preferences (refresh rate) set by the settings activity
    private SharedPreferences sharedPreferences;

    //a flag to record if the refresh button in MainActivity has been pressed.
    private boolean refresh_pressed = false;

    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;
    private WifiManager wifiManager = null;
    private WifiManager.WifiLock wifiLock = null;

    public AutoUpdater() { }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            // Make sure the alarm is running
            try {
                checkAlarm();
            } catch (Exception e) {
                Log.d("AutoUpdater", "checkAlarm exception: " + e.getMessage());
            }

            // check for updates.  wrap in try/catch in the event something
            // goes wrong, so we can keep the service from crashing entirely.
            try {
                checkForUpdates();
            } catch (Exception e) {
                Log.d("AutoUpdater", "update check failed: " + e.getMessage());
            }

            //resets timer continuously
            timerHandler.postDelayed(this, ONE_MINUTE);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        db = new LocalDB(this);
    }

    @Override
    public void onDestroy() {
        timerHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("AutoUpdater", "onStartCommand()");

        timerHandler.removeCallbacksAndMessages(null);
        timerHandler.postDelayed(timerRunnable, ONE_SECOND);

        if (intent != null) {
            if (intent.getBooleanExtra("refresh_pressed", false)) {
                processRefreshPressed();
            }
            String once = intent.getStringExtra("checkOnce");
            if (once != null && once.equals("true")) {
                Log.d("AutoUpdater", "checking updates via onStartCommand()");
                checkForUpdatesPM();
            }
        }

        //keeps service running after app is shut down
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    //Modified by Littlesnowman88 on 16 July 2018 so new notifications don't get passed into the for loop.
    // This gets called when the AsyncTask DataConnection completes.
    public void onCompletion()
    {
        //list of new notifications
        ArrayList<Info> notifications = db.getNewNotifications();

        //send notifications
        for (Info item : notifications) {
            createNotificationChannel();
            sendNotification(item.getId(), item.getHeader(), item.getBody());
        }
        db.unflagNewNotifications();

        // Release any wake lock now that we're done.
        pmCleanup();
    }

    private void pmCleanup()
    {
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

    private void checkAlarm()
    {
        // Our alarm handler reference
        Intent wakeAlarmHandler = new Intent(getApplicationContext(), WakeupAlarmReceiver.class);

        // Already exists? (don't reset it if so)
        PendingIntent isAlarm = PendingIntent.getBroadcast(
                getApplicationContext(),
                0,
                wakeAlarmHandler,
                PendingIntent.FLAG_NO_CREATE
        );

        if (isAlarm == null) {
            // Does not exist -- create a new one.
            PendingIntent wakeAlarm = PendingIntent.getBroadcast(
                    getApplicationContext(),
                    0,
                    wakeAlarmHandler,
                    PendingIntent.FLAG_CANCEL_CURRENT
            );

            // Set a 15 minute interval for the alarm.
            AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarms.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    SystemClock.elapsedRealtime() + (1000 * 60 * 5), // AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    1000 * 60 * 5, // AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    wakeAlarm
            );
        }
    }


    public void checkForUpdatesPM() {
        acquirePowerLock();
        acquireWifiLock();
        // Go check for updates
        try {
            checkForUpdates();
        } catch (Exception e) {
            pmCleanup();
            Log.d("checkForUpdatesPM", "update check failed: " + e.getMessage());
        }
    }

    private void acquirePowerLock() {
        // Acquire the power manager wake lock.
        if (wakeLock == null || !wakeLock.isHeld()) {
            powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            try {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "org.lightsys.eventApp.tools.AutoUpdater");
            } catch (Exception e) {
                pmCleanup();
                return;
            }
            wakeLock.acquire(5000 /* milliseconds */);
        }
        return;
    }

    private void acquireWifiLock() {
        // Acquire the wifi manager lock.
        if (wifiLock == null || !wifiLock.isHeld()) {
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            try {
                wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "org.lightsys.eventApp.tools.AutoUpdater");
            } catch (Exception e) {
                pmCleanup();
                return;
            }
        }
        return;
    }



    /**
     * Modified by Littlesnowman88 on 8 June 2018
     * Now, tries to access the refresh_rate from the shared preferences,
     *      defaults to the event's default refresh rate if shared preferences
     *      are not specified in the Settings Activity.
     *      Also, now takes device sleeping/awake into account.
     */
    private void checkForUpdates()
    {
        db.close();

        Calendar currentDate = Calendar.getInstance();
        elapsedTime = currentDate.getTimeInMillis() - prevDate.getTimeInMillis();

        //check to see if the time elapsed is greater than the update period
        // also check to see if the expiration date has passed. -Littlesnowman88
        if ((elapsedTime > updateMillis && updateMillis > 0) && (eventIsNow())){
            Log.d("checkForUpdates","prev: " + prevDate.getTimeInMillis() + ", elapsed: " + elapsedTime + ", updateMillis: " + updateMillis);
            getUpdates();
            prevDate = Calendar.getInstance();
        } else {
            pmCleanup();
        }
        setRefreshFrequency(); //needs to be after any changes to elapsedTime and prevDate because of auto.
    }

    /** Created by Littlesnowman88 */
    private boolean eventIsNow() {
        SimpleDateFormat expirationDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Date last_day;
        try {
            last_day = expirationDate.parse(db.getGeneral("refresh_expire"));
            if ( !(new Date().after(last_day))) {
                return true;
            } else {
                return false;}
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void getUpdates() {
        //new DataConnection(this.getBaseContext(), null, "auto_update", db.getGeneral("notifications_url"), false, this).execute("");
        new DataConnection(this.getBaseContext(), null, "auto_update", db.getGeneral("url"), this).execute("");

        Intent auto_to_main = new Intent(this, MainActivity.class);
        auto_to_main.putExtra("update_schedule", true);
        sendBroadcast(auto_to_main);
    }

    private void processRefreshPressed() {
        elapsedTime = 0;
        refresh_pressed = true;
        String db_refresh_rate;
        //catch allows for old JSON compatibility
        try { db_refresh_rate = db.getGeneral("refresh_rate").trim(); }
        catch (Exception e) {
            db_refresh_rate = db.getGeneral("refresh");
            //in Testing/Demo QR code, the refresh rate is -1. Otherwise this "if" statement isn't a problem.
            if (db_refresh_rate==null || db_refresh_rate.equals("-1")) {db_refresh_rate = "never"; }
        }

        String refresh_setting = sharedPreferences.getString("refresh_rate", db_refresh_rate);
        if (refresh_setting.equals("auto")) {
            setAutoRefresh();
        }
        refresh_pressed = false;
    }

    /** checks the system preferences (or defaults to the database's general refresh rate)
     *  and sets the notifications refresh rate. Takes device's sleep state into account.
     *  "auto" takes wifi and cellular connection into account, too.
     * @author: Littlesnowman88
     */
    private void setRefreshFrequency() {
        String db_refresh_rate;
        //catch allows for old JSON compatibility
        try { db_refresh_rate = db.getGeneral("refresh_rate").trim(); }
        catch (Exception e) {
            db_refresh_rate = db.getGeneral("refresh");
            //in Testing/Demo QR code, the refresh rate is -1. Otherwise this "if" statement isn't a problem.
            if (db_refresh_rate==null || db_refresh_rate.equals("-1")) {db_refresh_rate = "never"; }
        }
        String refresh_setting = sharedPreferences.getString("refresh_rate", db_refresh_rate);
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
                if (! isAwake()) { refresh_setting = "5"; } //then continue onto default.
            default:
                    updateMillis = Integer.parseInt(refresh_setting) * ONE_MINUTE; // refresh_setting, converted into milliseconds
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
            /*} else if (elapsedTime < (4 * ONE_HOUR)) {
                updateMillis = chooseMaxTime(30);
            } else {
                updateMillis = ONE_HOUR;*/
            }
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
            if ( (wifiStatus != null) && (wifiStatus.getNetworkId() == -1) ) { //no access point connection
                return false;
            }
            return true; //else, there is an access point connection. yay!
        } else return false; //in this case, wifi is disabled.
    }

    /** API-dependent power manager functions related to device wakefulness **/
    private boolean isAwake() {
        Boolean awake;
        acquirePowerLock();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) { //if api > 20
            awake = powerManager.isInteractive();
        } else {
            awake = powerManager.isScreenOn();
        }
        return awake;
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

    private void sendNotification(int notificationID, String title, String subject){

        Context context = this;
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder nBuild;
        PowerManager.WakeLock screenWakeLock = null;
        Notification n;

        // Build the notification to be sent
        // BigTextStyle allows notification to be expanded if text is more than one line
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        nBuild = new NotificationCompat.Builder(context, context.getResources().getString(R.string.notification_id))
                .setContentTitle(title)
                .setContentText(subject)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setSmallIcon(R.drawable.ic_sbcat_transparent_nowords)
                .setColor(Color.parseColor(db.getThemeColor("themeDark")))
                .setContentIntent(intent)
                .setPriority(1)
                .setChannelId(getString(R.string.channel_id))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(subject));

        // Turn on the device and send the notification.
        if (powerManager != null) {
            screenWakeLock = powerManager.newWakeLock(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
                    "org.lightsys.eventApp.tools.AutoUpdater"
            );
            screenWakeLock.acquire(500);
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String CHANNEL_ID = getString(R.string.channel_id);
            String name = getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
