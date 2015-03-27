package com.pcortex.pcgmclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by fdam on 12-03-2015.
 */
public class AppSettings {
    private static final String TAG = AppSettings.class.getSimpleName();
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private Context mContext;

    SharedPreferences prefs;

    public AppSettings ( Context context ) {
        mContext = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }
    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public String getRegistrationId() {

        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        //Log.i( TAG , "Stored registration id: " + registrationId );
        return registrationId;
    }

    public String getGCMProjectId() {

        String projectId = prefs.getString("pref_gcmkey", "");
        if (projectId.isEmpty()) {
            Log.i(TAG, "GCM Project key not found/defined");
            return "";
        }
        //Log.i( TAG , "Project ID found: " + projectId );
        return projectId;
    }

    public String getDeviceId() {

        String deviceId = prefs.getString("pref_deviceid", "");
        if (deviceId.isEmpty()) {
            Log.i(TAG, "GCM Project key not found/defined");
            return "";
        }
        //Log.i( TAG , "Device ID found: " + deviceId );
        return deviceId;
    }

    public boolean getIsBackEndEnabled(Context context) {
        return prefs.getBoolean("pref_backendenabled" , false );
    }

    public void storeDeviceId( String devId) {

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("pref_deviceid", devId);

        editor.commit();
    }
    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regId registration ID
     */
    public void storeRegistrationId(String regId) {

        int appVersion = getAppVersion();
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    public void clearRegistrationId() {

        int appVersion = getAppVersion();
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, "");
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public int getAppVersion() {
        try {
            PackageInfo packageInfo = mContext.getPackageManager()
                    .getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public boolean getNotificationsEnabled() {
        return prefs.getBoolean("notifications_new_message" , false );
    }

    public boolean getVibrateEnabled () {
        return prefs.getBoolean("notifications_new_message_vibrate", false );
    }

    public String getNotificationSound () {
        return prefs.getString("notifications_new_message_ringtone" , "content://settings/system/notification_sound");
    }

//    /**
//     * @return Application's {@code SharedPreferences}.
//     */
//    private SharedPreferences getGcmPreferences(Context mContext) {
//        // This sample app persists the registration ID in shared preferences, but
//        // how you store the regID in your app is up to you.
//        return getSharedPreferences(MainActivity.class.getSimpleName(),
//                Context.MODE_PRIVATE);
//    }

}
