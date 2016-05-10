package com.application.chetna_priya.weather_forecast.app.muzei;

import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.application.chetna_priya.weather_forecast.app.MainActivity;
import com.application.chetna_priya.weather_forecast.app.Utility;
import com.application.chetna_priya.weather_forecast.app.data.WeatherContract;
import com.application.chetna_priya.weather_forecast.app.sync.SunshineSyncAdapter;
import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

/**
 * Created by chetna_priya on 5/5/2016.
 */
public class WeatherMuzeiArtSource extends RemoteMuzeiArtSource {

    private static final String[] WEATHER_PROJECTION = {WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC};

    public WeatherMuzeiArtSource() {
        super("WeatherMuzeiArtSource");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
            boolean dataUpdated = intent != null && SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction());
            if(dataUpdated && isEnabled())
                onUpdate(UPDATE_REASON_OTHER);

        }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        onUpdate(reason);
    }

    @Override
    protected void onUpdate(int reason) {
        String locationSetting = Utility.getPreferredLocation(this);
        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, System.currentTimeMillis());

        Cursor cursor = getContentResolver().query(weatherUri,
                WEATHER_PROJECTION,
                null,
                null,
                null);

        if(cursor.moveToFirst())
        {
            int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            String desc = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));

            String imageUrl = Utility.getImageUrlForWeatherCondition(weatherId);
            if(imageUrl != null)
            {
                publishArtwork(new Artwork.Builder()
                .imageUri(Uri.parse(imageUrl))
                .title(desc)
                .byline(locationSetting)
                .viewIntent(new Intent(this, MainActivity.class))
                .build());
            }

            cursor.close();
        }

    }
}
