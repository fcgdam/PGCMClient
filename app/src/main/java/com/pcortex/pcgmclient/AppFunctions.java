package com.pcortex.pcgmclient;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by fdam on 13-03-2015.
 */
public class AppFunctions {
    private static final String LOG_TAG = AppFunctions.class.getSimpleName();
    private static final int    PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private Context mContext;
    private String  regId;
    private String  mError;
    private AppSettings appSettings;
    private GoogleCloudMessaging gcm;
    private AppAdapter mAdapter;

    public AppFunctions( Context context , AppAdapter appAdapter ) {
        mContext    = context;
        mAdapter    = appAdapter;
        appSettings = new AppSettings( context );
    }

    public String getError() {
        return mError;
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                mError = GooglePlayServicesUtil.getErrorString(resultCode);     // .getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
                Log.i ( LOG_TAG , " Google Play Error: " + mError );
            } else {
                Log.i(LOG_TAG, "This device is not supported.");

            }
            return false;
        }

        return true;
    }

    public boolean registerDevice() {

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm   = GoogleCloudMessaging.getInstance(mContext);
            regId = appSettings.getRegistrationId();

            // If the registration id is empty we have to register at the google servers.
            if (regId.isEmpty()) {
                registerInBackground();
            } else {
                // We have the registration ID, but we send it again to the backend.
                sendRegistrationIdToBackend();
            }
            return true;
        } else {
            Log.i(LOG_TAG, "No valid Google Play Services APK found.");
        }
        return false;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    public void registerInBackground() {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";

                // First let's see if we have the Project ID to register.
                String GCMProjectId = appSettings.getGCMProjectId();

                if (!GCMProjectId.contentEquals("")) {

                    try {
                        if (gcm == null) {
                            gcm = GoogleCloudMessaging.getInstance(mContext);
                        }
                        regId = gcm.register(GCMProjectId);

                        Log.i(LOG_TAG, "Device registered, registration ID=" + regId);
                        // You should send the registration ID to your server over HTTP, so it
                        // can use GCM/HTTP or CCS to send messages to your app.
                        sendRegistrationIdToBackend();

                        appSettings.storeRegistrationId(regId);
                    } catch (IOException ex) {
                        msg = "Error :" + ex.getMessage();
                        // If there is an error, don't just keep trying to register.
                        // Require the user to click a button again, or perform
                        // exponential back-off.
                    }
                    return msg;
                } else {
                    Log.i(LOG_TAG , "No GCM Project ID defined...");
                    return "";
                }
            }

        }.execute(null, null, null);
    }


    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    public void sendRegistrationIdToBackend() {
        if ( appSettings.getIsBackEndEnabled(mContext)) {
            BackendTask beTsk = new BackendTask(mContext , mAdapter);

            beTsk.execute("Register");
        }
    }

    public boolean getServerData() {
        if ( appSettings.getIsBackEndEnabled(mContext)) {

            BackendTask beTsk = new BackendTask(mContext , mAdapter);

            beTsk.execute("Ping");

            return true;
        } else {
            return false;

        }

    }

}
