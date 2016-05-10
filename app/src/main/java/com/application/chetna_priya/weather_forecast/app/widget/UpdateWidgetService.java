package com.application.chetna_priya.weather_forecast.app.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.application.chetna_priya.weather_forecast.app.MainActivity;
import com.application.chetna_priya.weather_forecast.app.R;
import com.application.chetna_priya.weather_forecast.app.Utility;
import com.application.chetna_priya.weather_forecast.app.data.WeatherContract;

/**
 * Created by chetna_priya on 5/3/2016.
 */
public class UpdateWidgetService extends IntentService{

    private static final int REQUEST_LAUNCH_MAIN_ACTIVITY = 0;
    private static final String LOG_TAG = TodayWidgetProvider.class.getSimpleName();

    public UpdateWidgetService() {
        super("updatewidgetservice");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));

        String[] NOTIFY_WEATHER_PROJECTION = new String[] {
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
        };

        int INDEX_WEATHER_ID = 0;
        int INDEX_SHORT_DESC = 1;
        int INDEX_MAX_TEMP = 2;
        int INDEX_MIN_TEMP = 3;

        for(int appWidgetId : appWidgetIds){
            String description;
            double maxTemp, minTemp;
            int weatherArtResourceId = R.drawable.art_clear;

            String locationQuery = Utility.getPreferredLocation(this);

            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

            // we'll query our contentProvider, as always
            Cursor cursor = getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);
            if(!cursor.moveToFirst())
            {
                Log.d(LOG_TAG, "NO DATA FOUNDDDDDDDDDD");
            }
            description = cursor.getString(INDEX_SHORT_DESC);
            weatherArtResourceId = Utility.getArtResourceForWeatherCondition(cursor.getInt(INDEX_WEATHER_ID));
            maxTemp = cursor.getDouble(INDEX_MAX_TEMP);
            minTemp = cursor.getDouble(INDEX_MIN_TEMP);
            String formattedMaxTemp = Utility.formatTemperature(this, maxTemp);
            String formattedLowTemp = Utility.formatTemperature(this, minTemp);

            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);
            int layoutId;
            if (widgetWidth >= largeWidth) {
                layoutId = R.layout.widget_today_large;
            } else if (widgetWidth >= defaultWidth) {
                layoutId = R.layout.widget_today;
            } else {
                layoutId = R.layout.widget_today_small;
            }


            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
            views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, description);
            }
            views.setTextViewText(R.id.widget_high_temp, formattedMaxTemp);
            if(layoutId == R.layout.widget_today)
            {
                views.setTextViewText(R.id.widget_low_temp, formattedLowTemp);
            }else if(layoutId == R.layout.widget_today_large)
            {
                views.setTextViewText(R.id.widget_low_temp, formattedLowTemp);
                views.setTextViewText(R.id.widget_desc, description);
            }
            Intent mainIntent = new Intent(this,MainActivity.class);
            PendingIntent pendingIntent= PendingIntent.getActivity(this, REQUEST_LAUNCH_MAIN_ACTIVITY,
                    mainIntent,0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            cursor.close();
        }
    }


    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }
}
