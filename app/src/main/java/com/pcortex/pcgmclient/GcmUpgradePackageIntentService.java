package com.pcortex.pcgmclient;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
/**
 * Created by fdam on 13-03-2015.
 */
public class GcmUpgradePackageIntentService extends IntentService {
    private static final String LOG_TAG = GcmUpgradePackageIntentService.class.getSimpleName();

    public GcmUpgradePackageIntentService() {
        super("GcmUpgradePackageIntentService");
    }
    @Override
    public void onHandleIntent(Intent intent) {
        Context mContext = getApplicationContext();
        AppSettings appSettings = new AppSettings(mContext);
        Log.i (LOG_TAG , " GCM Upgrade Package Intent Service called...");
        try {
            // Remove the stored GCM registration ID
            appSettings.clearRegistrationId();

            // Register again on GCM servers.
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

            String regId = gcm.register(appSettings.getGCMProjectId());

            // You should send the registration ID to your server over HTTP,
            // so it can use GCM/HTTP or CCS to send messages to your app.
            // The request to your server should be authenticated if your app
            // is using accounts.

            //sendRegistrationIdToBackend(regId);

            // store the regId locally somewhere (e.g. SharedPreferences)
            appSettings.storeRegistrationId(regId);

            // Release the wake lock provided by the WakefulBroadcastReceiver.
            PackageReplacedReceiver.completeWakefulIntent(intent);

        } catch (Exception e ) {
            Log.i(LOG_TAG, " Failed to re-register in GCM after package replacement.");
            e.printStackTrace();
        }
    }

}
