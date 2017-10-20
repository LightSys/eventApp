package org.lightsys.eventApp.tools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
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
public class AutoUpdater extends Service {

    //time constants in milliseconds
    private static final int ONE_SECOND     = 1000;
    private static final int ONE_MINUTE     = ONE_SECOND * 60;
    private static final int NEVER          = -1;

    private final LocalDB db; //local database
    private int   notificationID = 0; //ID of an update notification

    private int      updateMillis = NEVER; //number of milliseconds between updates
    private Calendar prevDate     = Calendar.getInstance();


    //custom timer that ticks every minute
    //used to constantly check to see if it's time to check for updates
    private final Handler timerHandler  = new Handler();

    public AutoUpdater() {
        db = new LocalDB(this);
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {

                db.close();
                Calendar currentDate = Calendar.getInstance();

                //set refresh frequency
                updateMillis = (db.getGeneral("refresh") != null ? ONE_MINUTE*Integer.parseInt(db.getGeneral("refresh")) : ONE_MINUTE*15);
                //difference between the previous time and the current time
                Log.d("AutoUpdater", "run: " + updateMillis + db.getGeneral("refresh"));
                long elapsedTime = currentDate.getTimeInMillis() - prevDate.getTimeInMillis();

                //check to see if the time elapsed is greater than the update period
                if (elapsedTime > updateMillis && updateMillis > 0) {
                    Log.d("auto-update", "run: " + elapsedTime);
                    getUpdates();
                    prevDate = Calendar.getInstance();
                }

                //resets timer continuously
                timerHandler.postDelayed(this, ONE_MINUTE);
            }
        };
        timerHandler.postDelayed(timerRunnable, ONE_SECOND);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //keeps service running after app is shut down

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private  void getUpdates()
    {
        //updates each account
           new DataConnection(this.getBaseContext(),null, "auto_update",db.getGeneral("notifications_url"),false).execute("");

        //list of new notifications
        ArrayList<Info> notifications = db.getNewNotifications();

        //send notifications
        for (Info item : notifications) {
            notificationID = item.getId();
            sendNotification(item.getHeader(), item.getBody());
        }
    }

    private void sendNotification(String title, String subject){

        Context context = this;
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder nBuild;
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

        n = nBuild.build();
        notificationManager.notify(notificationID, n);
    }
}
