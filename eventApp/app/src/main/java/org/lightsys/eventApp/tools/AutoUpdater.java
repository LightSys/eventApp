package org.lightsys.eventApp.tools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.lightsys.eventApp.views.MainActivity;
import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.Info;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author Judah Sistrunk
 * created on 5/25/2016.
 * copied from missionary app
 *
 * service class that automatically updates local database with server database
 */
public class AutoUpdater extends Service implements CompletionInterface {

    //time constants in milliseconds
    private static final int ONE_SECOND     = 1000;
    private static final int ONE_MINUTE     = ONE_SECOND * 60;
    private static final int NEVER          = -1;

    private final LocalDB db; //local database

    private int      updateMillis = NEVER; //number of milliseconds between updates
    private Calendar prevDate     = Calendar.getInstance();

    //custom timer that ticks every minute
    //used to constantly check to see if it's time to check for updates
    private final Handler timerHandler  = new Handler();

    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;
    private WifiManager wifiManager = null;
    private WifiManager.WifiLock wifiLock = null;

    public AutoUpdater() {
        db = new LocalDB(this);
        Runnable timerRunnable = new Runnable() {
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
        timerHandler.postDelayed(timerRunnable, ONE_SECOND);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("AutoUpdater", "onStartCommand()");

        if (intent != null) {
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


    // This gets called when the AsyncTask DataConnection completes.
    public void onCompletion()
    {
        //list of new notifications
        ArrayList<Info> notifications = db.getNewNotifications();

        //send notifications
        for (Info item : notifications) {
            sendNotification(item.getId(), item.getHeader(), item.getBody());
        }

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

        // Go check for updates
        try {
            checkForUpdates();
        } catch (Exception e) {
            pmCleanup();
            Log.d("checkForUpdatesPM", "update check failed: " + e.getMessage());
        }
    }


    private void checkForUpdates()
    {
        db.close();
        Calendar currentDate = Calendar.getInstance();

        //set refresh frequency
        updateMillis = (db.getGeneral("refresh") != null ? ONE_MINUTE * Integer.parseInt(db.getGeneral("refresh")) : ONE_MINUTE * 15);

        //difference between the previous time and the current time
        Log.d("AutoUpdater", "run: " + updateMillis + db.getGeneral("refresh"));
        long elapsedTime = currentDate.getTimeInMillis() - prevDate.getTimeInMillis();

        //check to see if the time elapsed is greater than the update period
        if (elapsedTime > updateMillis && updateMillis > 0) {
            Log.d("auto-update", "run: " + elapsedTime);
            getUpdates();
            prevDate = Calendar.getInstance();
        } else {
            pmCleanup();
        }
    }

    private  void getUpdates()
    {
        // updates each account
        new DataConnection(this.getBaseContext(), null, "auto_update", db.getGeneral("notifications_url"), false, this).execute("");
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

        nBuild = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(subject)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setSmallIcon(R.drawable.ic_sbcat_transparent_nowords)
                .setColor(Color.parseColor(db.getThemeColor("themeDark")))
                .setContentIntent(intent)
                .setPriority(1)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(subject));

        // Turn on the device and send the notification.
        if (powerManager != null) {
            screenWakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
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
}
