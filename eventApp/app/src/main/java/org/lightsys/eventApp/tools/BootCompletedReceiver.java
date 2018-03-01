package org.lightsys.eventApp.tools;

import android.content.BroadcastReceiver;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.util.Log;


/**
 * Created by gbeeley on 2/28/18.
 *
 * Copyright (c) 2018 LightSys Technology Services, Inc.
 *
 * BootCompletedReceiver: automatically start AutoUpdater when the device boots
 * up, otherwise notifications will stop being received.
 *
 * per AndroidManifest.xml, this is invoked only when BOOT_COMPLETED is received.
 */

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction() != null) {
            Log.d("BootCompletedReceiver", "action: " + intent.getAction());
        } else {
            Log.d("BootCompletedReceiver", "no action");
        }
        // Start the AutoUpdater service
        try {
            Intent startAutoUpdater = new Intent(ctx, AutoUpdater.class);
            ctx.startService(startAutoUpdater);
        } catch (Exception e) {
            Log.d("BootCompletedReceiver", "exception: " + e.getMessage());
        }
    }
}
