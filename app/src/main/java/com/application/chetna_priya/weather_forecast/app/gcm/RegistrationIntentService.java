package com.application.chetna_priya.weather_forecast.app.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.application.chetna_priya.weather_forecast.app.MainActivity;
import com.application.chetna_priya.weather_forecast.app.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

/**
 * Created by chetna_priya on 4/20/2016.
 */
public class RegistrationIntentService extends IntentService{

    public static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try{
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
                       // ensure that they are processed sequentially.
            synchronized (TAG)
            {
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                InstanceID instanceID = InstanceID.getInstance(this);

                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                sendRegistrationToServer(token);

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.

                sharedPreferences.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, true).apply();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            sharedPreferences.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    private void sendRegistrationToServer(String token) {
        Log.i(TAG, "GCM Registration Token: "+token);
        //Server Api Key
        //AIzaSyB8G9KXpBx88xRz3Si8Q5Rq2fKDA3WsxTY
        //Sender Id
        //461379039394
    }
}
