package com.application.chetna_priya.weather_forecast.app.sync;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

import com.application.chetna_priya.weather_forecast.app.Constant;
import com.application.chetna_priya.weather_forecast.app.MainActivity;
import com.application.chetna_priya.weather_forecast.app.R;
import com.application.chetna_priya.weather_forecast.app.Utility;
import com.application.chetna_priya.weather_forecast.app.data.WeatherContract;
import com.application.chetna_priya.weather_forecast.app.muzei.WeatherMuzeiArtSource;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the weather, in milliseconds.
// 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;
    public static final String ACTION_DATA_UPDATED = "com.application.chetna_priya.weather_forecast.app.ACTION_DATA_UPDATED";

    @IntDef({LOCATION_STATUS_OK, LOCATION_STATUS_SERVER_DOWN, LOCATION_STATUS_SERVER_INVALID,
            LOCATION_STATUS_UNKNOWN, LOCATION_STATUS_LOCATION_INVALID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LocationStatus{}


    public static final int LOCATION_STATUS_OK = 0;
    public static final int LOCATION_STATUS_SERVER_DOWN = 1;
    public static final int LOCATION_STATUS_SERVER_INVALID = 2;
    public static final int LOCATION_STATUS_UNKNOWN = 3;
    public static final int LOCATION_STATUS_LOCATION_INVALID = 4;

    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public void setLocationStatus(@LocationStatus int locationStatus)
    {
        Context context = getContext();
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(context.getResources().getString(R.string.pref_location_status_key), locationStatus);
        prefs.commit();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called RUNNNNNNNNNNNNNNNNNNNNNNNNNN");
        fetchWeatherFromWeb(Utility.getPreferredLocation(getContext()));
        if(Utility.isNotificationsOn(getContext())) {
            Log.d(LOG_TAG,"Notifications on proceed to notify");
            notifyWeather();
        }else
            Log.d(LOG_TAG, "Notifications off, do not notify");
    }

    private void fetchWeatherFromWeb(String loc) {
        // If there's no zip code, there's nothing to look up.  Verify size of params.

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int numDays = 14;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "APPID";
            final String LAT_PARAM = "lat";
            final String LONG_PARAM = "lon";


            Uri.Builder builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon();
            if(Utility.isLocationLatLongAvailable(getContext()))
            {
                float locLatitude =  Utility.getLocationLatitude(getContext());
                float locLongitude = Utility.getLocationLongitude(getContext());

                builtUri.appendQueryParameter(LAT_PARAM, String.valueOf(locLatitude))
                        .appendQueryParameter(LONG_PARAM, String.valueOf(locLongitude));
            }else {
                builtUri.appendQueryParameter(QUERY_PARAM, loc);
            }

            builtUri.appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM, Constant.WEATHER_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.d(LOG_TAG, String.valueOf(builtUri));
            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                Log.d(LOG_TAG, "11111 ");
                setLocationStatus(LOCATION_STATUS_SERVER_DOWN);
                return;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            Log.d(LOG_TAG, "2222 " );
            setLocationStatus(LOCATION_STATUS_SERVER_DOWN);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                    Log.d(LOG_TAG, "3333 " );
                    setLocationStatus(LOCATION_STATUS_UNKNOWN);
                }
            }
        }

        try {
            getWeatherDataFromJson(forecastJsonStr, loc);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            Log.d(LOG_TAG, "44444 " );
            setLocationStatus(LOCATION_STATUS_SERVER_INVALID);
            e.printStackTrace();
        }
    }

    private void notifyWeather() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = prefs.getLong(lastNotificationKey, 0);

        if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
            // Last sync was more than 1 day ago, let's send a notification with the weather.
            String locationQuery = Utility.getPreferredLocation(context);

            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

            // we'll query our contentProvider, as always
            Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

            if (cursor.moveToFirst()) {
                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                double high = cursor.getDouble(INDEX_MAX_TEMP);
                double low = cursor.getDouble(INDEX_MIN_TEMP);
                String desc = cursor.getString(INDEX_SHORT_DESC);

                int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
                String title = context.getString(R.string.app_name);
                int largeIconWidth = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        ? context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
                        : context.getResources().getDimensionPixelSize(R.dimen.notification_large_icon_default);

                int largeIconHeight = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        ? context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
                        : context.getResources().getDimensionPixelSize(R.dimen.notification_large_icon_default);

                Bitmap largeIcon;

                try {
                    largeIcon = Glide.with(context).
                            load(Utility.getArtUrlForWeatherCondition(context,weatherId))
                            .asBitmap()
                            .error(iconId)
                            .into(largeIconWidth, largeIconHeight)
                            .get();
                } catch (InterruptedException e) {
                    largeIcon = BitmapFactory.decodeResource(context.getResources(),iconId);
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    largeIcon = BitmapFactory.decodeResource(context.getResources(),iconId);
                    e.printStackTrace();
                }

                // Define the text of the forecast.
                String contentText = String.format(context.getString(R.string.format_notification),
                        desc,
                        Utility.formatTemperature(context, high),
                        Utility.formatTemperature(context, low));

                //build your notification here.
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(context)
                        .setSmallIcon(iconId)
                        .setLargeIcon(largeIcon)
                        .setContentTitle(title)
                        .setContentText(contentText)
                        .setAutoCancel(true);

                TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);

                Intent resultIntent = new Intent(context, MainActivity.class);

                taskStackBuilder.addParentStack(MainActivity.class);
                taskStackBuilder.addNextIntent(resultIntent);
                PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
                notificationBuilder.setContentIntent(pendingIntent);
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(WEATHER_NOTIFICATION_ID,notificationBuilder.build());


                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.commit();
            }
        }

    }

    private void getWeatherDataFromJson(String forecastJsonStr,
                                        String locationSetting)
            throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        // Location coordinate
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_LIST = "list";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // All temperatures are children of the "temp" object.
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";
        final String OWM_RESPONSE_CODE = "cod";

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            if(forecastJson.has(OWM_RESPONSE_CODE))
            {
                int error_code = forecastJson.getInt(OWM_RESPONSE_CODE);
                switch (error_code) {
                    case HttpURLConnection.HTTP_OK:
                        break;

                    case HttpURLConnection.HTTP_NOT_FOUND:
                        Log.d(LOG_TAG, "Setting location unknownnnnnnnnnnnn");
                        setLocationStatus(LOCATION_STATUS_LOCATION_INVALID);
                        return;

                    default:
                        setLocationStatus(LOCATION_STATUS_SERVER_DOWN);
                        return;
                }
            }
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            for(int i = 0; i < weatherArray.length(); i++) {
                // These are the values that will be collected.
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                // Description is in a child array called "weather", which is 1 element long.
                // That element also contains a weather code.
                JSONObject weatherObject =
                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);
                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                cVVector.add(weatherValues);
            }

            // add to database
            if ( cVVector.size() > 0 ) {
                // Student: call bulkInsert to add the weatherEntries to the database here

                ContentValues[] cVArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cVArray);
                int insertedRows = getContext().getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI,cVArray);
                Log.d(LOG_TAG, "INSERTED -- "+insertedRows+" -- rows");

                Calendar calendar = Calendar.getInstance();
                long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
                long date_yesterday = calendar.getTimeInMillis()-DAY_IN_MILLIS;
                String condition = WeatherContract.WeatherEntry.COLUMN_DATE+" <= ?";
                int rowsDeleted = getContext().getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
                        condition, new String[]{Long.toString(dayTime.setJulianDay(julianStartDay-1))});
                Log.d(LOG_TAG, "OLD ROWS DELETED: "+rowsDeleted);
            }
            setLocationStatus(LOCATION_STATUS_OK);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        Intent updatedWidgetIntent = new Intent(ACTION_DATA_UPDATED);
        getContext().sendBroadcast(updatedWidgetIntent);
        getContext().startService(updatedWidgetIntent.setClass(getContext(), WeatherMuzeiArtSource.class));
    }

    public long addLocation(String locationSetting, String cityName, double lat, double lon) {
        // Students: First, check if the location with this city name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI
        Cursor cursor = null;
        long id = -1;
        try
        {
            final String sCityNameSelection =
                    WeatherContract.LocationEntry.COLUMN_CITY_NAME + " = ? ";

            String[] selectionArgs = new String[]{cityName};
            cursor  = getContext().getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
                    new String[]{WeatherContract.LocationEntry._ID},
                    sCityNameSelection,
                    selectionArgs,
                    null);
            if(cursor.moveToFirst()) {
                int colIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
                id = cursor.getLong(colIndex);
                Log.d(LOG_TAG, "Found "+cityName+" with id "+id);
            }
            else{

                Log.d(LOG_TAG,"Inserted city "+cityName+" lat is "+lat+" lon is "+lon);
                ContentValues values = new ContentValues();
                values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,locationSetting);
                values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME,cityName);
                values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT,lat);
                values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);
                Uri insertedUri = getContext().getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, values);
                id = ContentUris.parseId(insertedUri);
                Log.d(LOG_TAG,"Inserted city "+cityName+" id is "+id);
            }

        }catch(Exception e)
        {
            e.printStackTrace();

        }finally {
            if(cursor != null)
                cursor.close();
        }
        return id;
    }


    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), "sunshine.example.com");

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
