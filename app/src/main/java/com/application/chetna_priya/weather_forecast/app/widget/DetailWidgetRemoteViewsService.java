package com.application.chetna_priya.weather_forecast.app.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.application.chetna_priya.weather_forecast.app.DetailActivity;
import com.application.chetna_priya.weather_forecast.app.ForecastFragment;
import com.application.chetna_priya.weather_forecast.app.MainActivity;
import com.application.chetna_priya.weather_forecast.app.R;
import com.application.chetna_priya.weather_forecast.app.Utility;
import com.application.chetna_priya.weather_forecast.app.data.WeatherContract;

/**
 * Created by chetna_priya on 5/4/2016.
 */
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    private static final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewFactory(this.getApplicationContext(), intent);
    }

    private class ListRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

        private Context mContext;
        private Cursor mCursor;
        private int mAppWidgetId;

        public ListRemoteViewFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            // Since we reload the cursor in onDataSetChanged() which gets called immediately after
            // onCreate(), we do nothing here.

        }

        @Override
        public void onDataSetChanged() {
            // Refresh the cursor
            if (mCursor != null) {
                mCursor.close();
            }
            mCursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.CONTENT_URI, null, null,
                    null, null);
        }

        @Override
        public void onDestroy() {
            if (mCursor != null) {
                mCursor.close();
            }
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {

            final Intent fillInIntent = new Intent();
            String locationSetting = Utility.getPreferredLocation(mContext);

            final int itemId =  R.layout.weather_detail_list_item;
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);

            if(mCursor.moveToPosition(position)) {
                Log.d(LOG_TAG, "INFLATE VIEWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
                Long date = mCursor.getLong(mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE));
                Log.d(LOG_TAG, "DATE: "+date+" String: "+Utility.getFriendlyDayString(mContext, date));
                rv.setTextViewText(R.id.list_item_date_textview, Utility.getFriendlyDayString(mContext, date));
                int weatherId = mCursor.getInt(mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
                String forecast = mCursor.getString(mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));

                Log.d(LOG_TAG, "Forecast: "+forecast);
                rv.setTextViewText(R.id.list_item_forecast_textview, forecast);
                rv.setImageViewResource(R.id.list_item_icon, Utility.getArtResourceForWeatherCondition(weatherId));

                double high = mCursor.getDouble(mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
                // Log.d(TAG, "HIGH TEMP------------: "+high);
                Log.d(LOG_TAG, "high temp: "+high);
                rv.setTextViewText(R.id.list_item_high_textview, Utility.formatTemperature(mContext, high));

                double low = mCursor.getDouble(mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
                //  Log.d(TAG, "LOW TEMP-------------: "+low);
                rv.setTextViewText(R.id.list_item_low_textview, Utility.formatTemperature(mContext, low));

                Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, date);
                fillInIntent.setData(weatherForLocationUri);
            }
            rv.setOnClickFillInIntent(R.id.item_weather, fillInIntent);
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
