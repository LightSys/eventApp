package org.lightsys.eventApp.tools;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.app.Service;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by gbeeley on 2/28/18.
 */

public class WakeupAlarmReceiver extends BroadcastReceiver {

    private AutoUpdater asService;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction() != null) {
            Log.d("WakeupAlarmReceiver", "action: " + intent.getAction());
        } else {
            Log.d("WakeupAlarmReceiver", "no action");
        }
        // Ensure the auto updater service is running
        try {
            Intent pingAutoUpdater = new Intent(ctx, AutoUpdater.class);
            pingAutoUpdater.putExtra("checkOnce", "true");
            ctx.startService(pingAutoUpdater);
        } catch (Exception e) {
            Log.d("WakeupAlarmReceiver", "exception: " + e.getMessage());
        }
    }
}
