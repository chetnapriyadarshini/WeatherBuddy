package com.application.chetna_priya.weather_forecast.app.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by chetna_priya on 4/20/2016.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        Intent intent= new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
