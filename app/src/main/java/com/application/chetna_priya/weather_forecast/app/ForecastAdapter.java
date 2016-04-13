package com.application.chetna_priya.weather_forecast.app;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by chetna_priya on 2/23/2016.
 */
public class ForecastAdapter extends CursorAdapter {

    private  static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    public boolean mUseTodayLayout = false;


    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

        /**
         * Prepare the weather high/lows for presentation.
         */
       /* private String formatHighLows(double high, double low) {
            boolean isMetric = Utility.isMetric(mContext);
            String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
            return highLowStr;
        }
*/
        /*
            This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
            string.
         */
    /*    private String convertCursorRowToUXFormat(Cursor cursor) {
            // get row indices for our cursor
            String highAndLow = formatHighLows(
                    cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                    cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

            return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                    " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                    " - " + highAndLow;
        }
*/


    @Override
    public int getItemViewType(int position) {
        return position == 0 && mUseTodayLayout ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public void setUseTodayLayout(boolean useTodayLayout)
    {
        mUseTodayLayout = useTodayLayout;
    }


    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    /*
                Remember that these views are reused as needed.
             */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
          //  View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
            int viewType = getItemViewType(cursor.getPosition());
            int layoutId = -1;
            switch (viewType)
            {
                case VIEW_TYPE_TODAY:
                    layoutId = R.layout.list_item_forecast_today;
                    break;
                case VIEW_TYPE_FUTURE_DAY:
                    layoutId = R.layout.list_item_forecast;
                    break;
            }
            View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
            view.setTag(new WeatherInfoHolder(view));
            return view;
        }

        /*
            This is where we fill-in the views with the contents of the cursor.
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            WeatherInfoHolder viewHolder = (WeatherInfoHolder) view.getTag();

            int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
            if(getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY)
                viewHolder.weatherIcon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
            else
                viewHolder.weatherIcon.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));

            Long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
            viewHolder.dateView.setText(Utility.getFriendlyDayString(context,date));

            String forecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
            viewHolder.forecastView.setText(forecast);
            viewHolder.weatherIcon.setContentDescription(forecast);

            double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
            viewHolder.highTempView.setText(Utility.formatTemperature(context,high));
            double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
            viewHolder.lowTempView.setText(Utility.formatTemperature(context,low));


        }

    static class WeatherInfoHolder {

        public final TextView highTempView, lowTempView, forecastView, dateView;
        public final ImageView weatherIcon;

        public WeatherInfoHolder(View view)
        {
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            forecastView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            weatherIcon = (ImageView) view.findViewById(R.id.list_item_icon);
        }

    }
}
