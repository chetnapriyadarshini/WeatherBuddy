package com.application.chetna_priya.weather_forecast.app;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.application.chetna_priya.weather_forecast.app.sync.SunshineSyncAdapter;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Created by chetna_priya on 2/23/2016.
 */
public class Utility
{
    private static final String LOG_TAG = Utility.class.getSimpleName();
    private static final float DEFAULT_LATLONG = 0f;

    public static boolean isNotificationsOn(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_notification_key), true);
    }

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d(LOG_TAG, "Returning PREFEREDDDD Locationnnnnnn");
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_default_location));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_unit_key),
                context.getString(R.string.pref_unit_metric))
                .equals(context.getString(R.string.pref_unit_metric));
    }

    public static boolean isLocationLatLongAvailable(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return  preferences.contains(context.getString(R.string.pref_location_latitude))
                && preferences.contains(context.getString(R.string.pref_location_longitude));
    }

    public static float getLocationLatitude(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat(context.getString(R.string.pref_location_latitude), DEFAULT_LATLONG);
    }

    public static float getLocationLongitude(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat(context.getString(R.string.pref_location_longitude), DEFAULT_LATLONG);
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }


    /*
    * Helper method to provide the correct image according to the weather condition id returned
    * by the OpenWeatherMap call.
    *
    * @param weatherId from OpenWeatherMap API response
    * @return A string URL to an appropriate image or null if no mapping is found
    */
    public static String getImageUrlForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return "http://upload.wikimedia.org/wikipedia/commons/2/28/Thunderstorm_in_Annemasse,_France.jpg";
        } else if (weatherId >= 300 && weatherId <= 321) {
            return "http://upload.wikimedia.org/wikipedia/commons/a/a0/Rain_on_leaf_504605006.jpg";
        } else if (weatherId >= 500 && weatherId <= 504) {
            return "http://upload.wikimedia.org/wikipedia/commons/6/6c/Rain-on-Thassos.jpg";
        } else if (weatherId == 511) {
            return "http://upload.wikimedia.org/wikipedia/commons/b/b8/Fresh_snow.JPG";
        } else if (weatherId >= 520 && weatherId <= 531) {
            return "http://upload.wikimedia.org/wikipedia/commons/6/6c/Rain-on-Thassos.jpg";
        } else if (weatherId >= 600 && weatherId <= 622) {
            return "http://upload.wikimedia.org/wikipedia/commons/b/b8/Fresh_snow.JPG";
        } else if (weatherId >= 701 && weatherId <= 761) {
            return "http://upload.wikimedia.org/wikipedia/commons/e/e6/Westminster_fog_-_London_-_UK.jpg";
        } else if (weatherId == 761 || weatherId == 781) {
            return "http://upload.wikimedia.org/wikipedia/commons/d/dc/Raised_dust_ahead_of_a_severe_thunderstorm_1.jpg";
        } else if (weatherId == 800) {
            return "http://upload.wikimedia.org/wikipedia/commons/7/7e/A_few_trees_and_the_sun_(6009964513).jpg";
        } else if (weatherId == 801) {
            return "http://upload.wikimedia.org/wikipedia/commons/e/e7/Cloudy_Blue_Sky_(5031259890).jpg";
        } else if (weatherId >= 802 && weatherId <= 804) {
            return "http://upload.wikimedia.org/wikipedia/commons/5/54/Cloudy_hills_in_Elis,_Greece_2.jpg";
        }
        return null;
    }

    public static String formatTemperature(Context context, double temperature) {
        boolean isMetric = isMetric(context);
        double temp;
        if ( !isMetric ) {
            temp = (9*temperature/5)+32;
        } else {
            temp = temperature;
        }
        return context.getString(R.string.format_temprature, temp);
    }

    public static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateInMillis)));
        } else if ( julianDay < currentJulianDay + 7 ) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @SuppressWarnings("ResourceType")
    static public @SunshineSyncAdapter.LocationStatus
    int getLocationStatus(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(context.getString(R.string.pref_location_status_key), SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN);
    }

    public static void resetLocationStatus(Context context)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(context.getResources().getString(R.string.pref_location_status_key),
                SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN);
        prefs.commit();
    }


    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis ) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Helper method to provide the art urls according to the weather condition id returned
     * by the OpenWeatherMap call.
     *
     * @param context Context to use for retrieving the URL format
     * @param weatherId from OpenWeatherMap API response
     * @return url for the corresponding weather artwork. null if no relation is found.
     */
    public static String getArtUrlForWeatherCondition(Context context, int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String path =  prefs.getString(context.getString(R.string.pref_icon_key),
                context.getString(R.string.pref_defualt_icon_type));
        String art_path = "art_";


        if (weatherId >= 200 && weatherId <= 232) {
            /*return context.getString(R.string.format_art_url,path+"storm");*/
            art_path = art_path.concat("storm");
        }
        else if (weatherId >= 300 && weatherId <= 321) {
            /*return context.getString(R.string.format_art_url,path+"light_rain");*/
            art_path = art_path.concat("light_rain");
        } else if (weatherId >= 500 && weatherId <= 504) {
            /*return context.getString(R.string.format_art_url,path+"rain");*/
            art_path = art_path.concat("rain");
        } else if (weatherId == 511) {
            /*return context.getString(R.string.format_art_url,path+"snow");*/
            art_path = art_path.concat("snow");
        } else if (weatherId >= 520 && weatherId <= 531) {
            /*return context.getString(R.string.format_art_url,path+"rain");*/
            art_path = art_path.concat("rain");
        } else if (weatherId >= 600 && weatherId <= 622) {
            /*return context.getString(R.string.format_art_url,path+"snow");*/
            art_path = art_path.concat("snow");
        } else if (weatherId >= 701 && weatherId <= 761) {
            /*return context.getString(R.string.format_art_url,path+"fog");*/
            art_path = art_path.concat("fog");
        } else if (weatherId == 761 || weatherId == 781) {
            /*return context.getString(R.string.format_art_url,path+"storm");*/
            art_path = art_path.concat("storm");
        } else if (weatherId == 800) {
            /*return context.getString(R.string.format_art_url,path+"clear");*/
            art_path = art_path.concat("clear");
        } else if (weatherId == 801) {
            /*return context.getString(R.string.format_art_url,path+"light_clouds");*/
            art_path = art_path.concat("light_clouds");
        } else if (weatherId >= 802 && weatherId <= 804) {
            /*return context.getString(R.string.format_art_url,path+"clouds");*/
            art_path = art_path.concat("clouds");
        }
        art_path = art_path.concat(".png");

        Uri buildUri = Uri.parse(context.getString(R.string.format_art_url)).buildUpon()
                .appendPath(path)
                .appendPath(art_path).build();
        Log.d(LOG_TAG, "URL: "+buildUri);
        return String.valueOf(buildUri);
    }


    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }
}
