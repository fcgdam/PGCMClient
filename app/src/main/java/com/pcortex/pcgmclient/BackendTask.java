package com.pcortex.pcgmclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by fdam on 10-03-2015.
 */
public class BackendTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = BackendTask.class.getSimpleName();

    private Context mContext;

    private String deviceId;
    private String registrationId;

    private AppAdapter mAdapter;
    private int mIcon;
    private String mTitle;
    private String mDesc;

    private Map<String,String> serverResponse;

    public BackendTask ( Context context , AppAdapter appAdapter) {
        mContext = context;
        mAdapter = appAdapter;
        // To hold up Json key/values response from server.
        serverResponse = new HashMap<String,String>();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Log.i(LOG_TAG, "onPreExecute");

        AppSettings appSettings = new AppSettings(mContext);

        deviceId       = appSettings.getDeviceId();
        registrationId = appSettings.getRegistrationId();

    }

    @Override
    protected Void doInBackground(String... params) {
        JSONObject message = new JSONObject();

        String     baseURL   = getBackEndURL();
        String     operation = params[0];

        Boolean    isDone = false;
        StatusLine statusLine;
        int        statusCode;
        String     result ="";
        String     strMsg ="";

        try {
            message.put("Operation", operation);
            message.put("DeviceId" , deviceId);
            if ( operation.contentEquals("Register") || operation.contentEquals("ReRegister")) {
                message.put("RegistrationID", registrationId);
            }
        } catch ( Exception e) {
            e.printStackTrace();
        }

        Log.i ( LOG_TAG , "Json request: " + message.toString() );

        try {
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(baseURL);

//            BasicNameValuePair param = new BasicNameValuePair("message", message.toString() );
//            List<BasicNameValuePair> paramslist = new ArrayList<BasicNameValuePair>();
//
//            paramslist.add(param);
//
//            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramslist);
//            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            //entity.setContentEncoding(HTTP.ISO_8859_1);

            StringEntity entity = new StringEntity( message.toString());


            httpPost.setEntity(entity);
            httpPost.setHeader("User-Agent", "AndroidPGCM/1.0");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            //Execute the request

            HttpResponse httpResponse = httpClient.execute(httpPost);

            statusLine = httpResponse.getStatusLine();
            statusCode = statusLine.getStatusCode();

            InputStream inputStream = httpResponse.getEntity().getContent();

            Log.i(LOG_TAG , "HTTP Response Code: " + Integer.toString(statusCode));

            if(inputStream != null){
                switch ( statusCode) {
                    case HttpStatus.SC_OK:
                        result = getStringFromInputStream(inputStream);
                        Log.i(LOG_TAG, "Backend Server response: " + result);
                        isDone = true;
                        strMsg = "Data received ok.";
                        break;
                    case HttpStatus.SC_BAD_GATEWAY:
                        strMsg = "Unable to connect.";

                        break;
                    case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                        strMsg = "Remote Internal Server Error.";
                        break;
                    default:
                        strMsg = "Something failed! Code: " + Integer.toString(statusCode);
                        break;
                }
            }
            else{
                strMsg = "Failed to get result...";
            }

        } catch (UnsupportedEncodingException uex ) {
            uex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Process the JSon response.
        if ( isDone ) {
            // Returns true if the response is valid JSON
            boolean success = decodeResponse(result);

            mIcon = R.drawable.ic_info;
            if ( success ) {
                String rCode = serverResponse.get("rcode");


                mTitle = serverResponse.get("title");
                mDesc  = serverResponse.get("desc");

                if ( (rCode == null) || (mTitle == null) || (mDesc==null) ) {
                    mTitle = "Backend Server Response";
                    mDesc  = "Response OK, but not decoded.";

                }
            } else {
                mIcon = R.drawable.ic_error;
                mTitle = "BackEnd Server Response";
                mDesc  = "Got no valid JSON answer.";
            }

        } else {
            // You can also include some extra data.
            mIcon = R.drawable.ic_error;
            mTitle = "Error on BackEnd Server";
            mDesc  = "Error connecting to Back End Server " + strMsg ;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        // Update the UI
        mAdapter.add( mIcon , mTitle , mDesc);

    }

    private String getBackEndURL() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        String server = prefs.getString("pref_server", "" );
        String port   = prefs.getString("pref_port", "");
        String baseURL= prefs.getString("pref_baseurl", "");

        String URL = "http://" + server;

        if ( !port.contentEquals(""))
            URL = URL + ":" + port;

        URL = URL + baseURL;

        Log.i (LOG_TAG , "Backend URL: " + URL);

        return URL;

    }

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder  sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    /*
     * Convert the HTTP Response to a Json object and from there fill up the class variables
     */
    private boolean decodeResponse(String json) {
        JSONObject msg;

        try {
            msg = new JSONObject( json );

            Iterator iter = msg.keys();
            while ( iter.hasNext() ) {
                String key   = (String)iter.next();
                String value = msg.getString(key);
                serverResponse.put( key , value );
            }

        } catch ( Exception e ) {
            Log.i(LOG_TAG , "Http response to Json failed: " + e.toString());
            return false;

        }

        return true;

    }

}
