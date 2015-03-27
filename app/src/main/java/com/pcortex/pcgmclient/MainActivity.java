// Application icons are from www.icons4android.com

package com.pcortex.pcgmclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    Context      mContext;
    AppFunctions appFunctions ;
    AppSettings  appSettings;

    ListView     list;
    AppAdapter   mAdapter;
    String[]     msgTitles;
    String[]     msgDescriptions;
    Integer[]    images = {R.drawable.ic_info , R.drawable.ic_info};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG , "Starting App: onCreate...");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext     = getApplicationContext();

        // Let's fill the view with initial Data...
        Resources res   = getResources();
        msgTitles       = res.getStringArray(R.array.titles);
        msgDescriptions = res.getStringArray(R.array.descriptions);

        list     = (ListView) findViewById(R.id.listView);
        // Our custom adapter that is associated to our ListView UI Object.
        mAdapter = new AppAdapter(mContext , images , msgTitles, msgDescriptions);
        list.setAdapter(mAdapter);

        appFunctions = new AppFunctions(mContext , mAdapter);
        appSettings  = new AppSettings(mContext);

        // For updating the UI from data that is "published" from background services
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("PC_GCM_NOTIFICATION"));

        // Let's get or generate a device id.
        String deviceId = appSettings.getDeviceId();
        if ( deviceId.contentEquals("") ) {
            deviceId = UUID.randomUUID().toString();
            appSettings.storeDeviceId(deviceId);
            Log.i(LOG_TAG, "Device id generated: " + deviceId);
        } else {
            Log.i(LOG_TAG, "Device id found: " + deviceId );
        }

        // Let's register this device in GCM servers.
        // This will also call our backend server if enabled.
        if (!appFunctions.registerDevice() )
            mAdapter.add( R.drawable.ic_error,"GCM Error", appFunctions.getError());

    }

    @Override
    protected void onPause() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    // This gets called when other activities or BroadcastReceivers want to update the UI.
    // Its main purpose is to update the UI from data received from the GCM notification
    // that comes from the GcmIntentService.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String title   = intent.getStringExtra("title");
            String desc    = intent.getStringExtra("desc");
            String msgType = intent.getStringExtra("type");
            int iconId     = R.drawable.ic_error;

            if ( msgType.contentEquals("info"))    iconId = R.drawable.ic_info;
            if ( msgType.contentEquals("unknown")) iconId = R.drawable.ic_action;

            mAdapter.add( iconId , title , desc );  // Add to the adapter that will refresh the list view UI.

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this , SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        appFunctions.checkPlayServices();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }


    // Send an upstream message.
    public void onClick(final View view) {

        if ( view == findViewById(R.id.send)) {
            Boolean status = appFunctions.getServerData(); // Just for fun. Get server data.
            if ( status )
                mAdapter.add( R.drawable.ic_info , "Backend Request", "Getting data...");
            else
                mAdapter.add( R.drawable.ic_error, "Backend Request", "Server is disabled in settings");

        }
        if ( view == findViewById(R.id.clear)) {
            mAdapter.clear();
        }

    }

}
