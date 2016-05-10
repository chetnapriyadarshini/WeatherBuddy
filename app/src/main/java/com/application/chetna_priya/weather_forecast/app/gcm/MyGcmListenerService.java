package com.application.chetna_priya.weather_forecast.app.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.application.chetna_priya.weather_forecast.app.MainActivity;
import com.application.chetna_priya.weather_forecast.app.R;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chetna_priya on 4/20/2016.
 */
public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static final String EXTRA_DATA = "data";
    private static final String EXTRA_WEATHER = "weather";
    private static final String EXTRA_LOCATION = "location";
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        if(!data.isEmpty())
        {
            String senderId = getString(R.string.gcm_defaultSenderId);
            if (senderId.length() == 0) {
                Toast.makeText(this, "SenderID string needs to be set", Toast.LENGTH_LONG).show();
                }
            if((senderId).equals(from))
            {
                try{
                    Log.i(TAG, "Received: " + data.toString());
                  //  JSONObject jsonObject = new JSONObject(data.);
                    String weather = data.getString(EXTRA_WEATHER);
                    String location = data.getString(EXTRA_LOCATION);
                    String alert = String.format(getString(R.string.gcm_weather_alert), weather, location);
                    sendNotification(alert);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendNotification(String message) {
        NotificationManager notificationManager
                = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.art_storm);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.art_clear)
                .setLargeIcon(largeIcon)
                .setContentTitle(getString(R.string.weather_alert))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

    }
}
