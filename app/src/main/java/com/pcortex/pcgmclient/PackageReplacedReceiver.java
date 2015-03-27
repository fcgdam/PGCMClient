package com.pcortex.pcgmclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by fdam on 13-03-2015.
 */
public class PackageReplacedReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            // Explicitly specify that GcmIntentService will handle the intent.
            ComponentName comp = new ComponentName(context.getPackageName(),
                    GcmUpgradePackageIntentService.class.getName());

            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, intent.setComponent(comp));

        }
    }
}
