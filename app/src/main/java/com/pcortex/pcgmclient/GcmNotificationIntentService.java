package com.pcortex.pcgmclient;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Iterator;
import java.util.Set;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GcmNotificationIntentService extends IntentService {
    static final String TAG = GcmNotificationIntentService.class.getSimpleName();

    static final int NOTIFICATION_ID = 1;
    NotificationManager mNotificationManager;

    Context     mContext;
    Handler     mHandler;

    public GcmNotificationIntentService() {

        super("GcmNotificationIntentService");
        mHandler    = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

            Log.d(TAG, "Entered Intent Service function...");

            String messageType = gcm.getMessageType(intent);

            if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                    sendNotification("Send error: " , extras.toString());
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                    sendNotification("Deleted messages on server: " , extras.toString());
                    // If it's a regular GCM message, do some work.
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    Log.d(TAG, "Received: " + extras.toString());

                    // Get intent notification data:
                    String title = getNotificationTitle( intent );
                    String desc  = getNotificationDesc ( intent );

                    // Post notification of received message.
                    sendNotification(title , desc );

                    // Update user interface to show the new arrived GCM notification
                    updateUI(title , desc);

                    dumpIntent(intent); // For debug purposes. Use adb logcat or Android Studio to view contents.
                }
            }
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            GcmBroadcastReceiver.completeWakefulIntent(intent);

        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String title , String desc ) {
        AppSettings appSettings = new AppSettings(getApplicationContext());
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        // This is the Intent that calls MainActivity when the notification is pressed.
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_gcm)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(title))
                        .setContentText(desc);

        if ( appSettings.getNotificationsEnabled() ) {
            mBuilder.setLights(Color.BLUE, 500, 500);
            mBuilder.setContentIntent(contentIntent);
            mBuilder.setStyle(new NotificationCompat.InboxStyle());
            mBuilder.setSound(Uri.parse(appSettings.getNotificationSound()));

            if ( appSettings.getVibrateEnabled()) {
                long[] pattern = {500, 500, 500, 500};
                mBuilder.setVibrate(pattern);
            }


        }
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void updateUI( String title , String desc ) {
        final String mTitle = title;
        final String mDesc  = desc ;

        mContext = getApplicationContext();

        mHandler.post( new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(mContext,"Notification received",Toast.LENGTH_LONG).show();

                //Log.d("sender", "Broadcasting message");
                Intent intent = new Intent("PC_GCM_NOTIFICATION");
                // You can also include some extra data.
                intent.putExtra("type"  , "info");
                intent.putExtra("title" , mTitle);
                intent.putExtra("desc"  , mDesc );
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            }
        });

    }

    private static void dumpIntent(Intent i){

        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            Log.d(TAG,"Dumping Intent start");
            while (it.hasNext()) {
                String key = it.next();
                Log.d(TAG,"[" + key + "=" + bundle.get(key)+"]");
            }
            Log.d(TAG,"Dumping Intent end");
        }
    }

    private String notificationToString( Intent i ) {
        Bundle bundle = i.getExtras();
        String message = "" ;

        if ( bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();

            while (it.hasNext()) {
                String key = it.next();
                message = message + "[" + key + "=" + bundle.get(key)+"]\n";
            }
        }

        return message;
    }

    private String getNotificationTitle ( Intent i ) {
        Bundle bundle = i.getExtras();
        String title = "GCM Notification";

        if (bundle != null) {
            title = bundle.getString("title");
            if ( title == null )
                title = "GCM Notification";
        }

        return title;
    }

    private String getNotificationDesc ( Intent i ) {
        Bundle bundle = i.getExtras();
        String title = "Received notification from GCM servers";

        if (bundle != null) {
            title = bundle.getString("desc");
            if ( title == null )
                title = "Received notification from GCM servers";
        }

        return title;
    }



}
