package com.application.chetna_priya.weather_forecast.app;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.application.chetna_priya.weather_forecast.app.data.WeatherContract;
import com.bumptech.glide.Glide;

/**
 * Created by chetna_priya on 2/23/2016.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.WeatherInfoHolder> {

    private  static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final String TAG = ForecastAdapter.class.getSimpleName();
    public boolean mUseTodayLayout = false;
    private Cursor mCursor;
    private Context mContext;
    private ForecastAdapterOnClickHandler mClickHandler;
    private View mEmptyView;


    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler clickHandler, View emptyView) {
        super();
        mContext =  context;
        mClickHandler = clickHandler;
        mEmptyView = emptyView;
    }

    @Override
    public WeatherInfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(getInflatedViewId(viewType), parent, false);
        return new WeatherInfoHolder(itemView);
    }

    public Cursor getCursor()
    {
        mCursor.moveToFirst();
        return mCursor;
    }

    public void swapCursor(Cursor cursor)
    {
        mCursor = cursor;
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(WeatherInfoHolder viewHolder, int position) {

        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        ViewCompat.setTransitionName(viewHolder.weatherIcon, generateTransitionName(position));
        if(getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY) {
            // viewHolder.weatherIcon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
            Glide.with(mContext).load(Utility.getArtUrlForWeatherCondition(mContext,weatherId))
                    .error(Utility.getArtResourceForWeatherCondition(weatherId))
                    .into(viewHolder.weatherIcon);
            Log.d(TAG, "URL TODAY: "+Utility.getArtUrlForWeatherCondition(mContext,weatherId));
        }
        else {
            // viewHolder.weatherIcon.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
            Glide.with(mContext).load(Utility.getArtUrlForWeatherCondition(mContext,weatherId))
                    .error(Utility.getIconResourceForWeatherCondition(weatherId))
                    .into(viewHolder.weatherIcon);
            Log.d(TAG, "URL : " + Utility.getArtUrlForWeatherCondition(mContext, weatherId));
        }

        Long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        viewHolder.dateView.setText(Utility.getFriendlyDayString(mContext, date));

        String forecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.forecastView.setText(forecast);
        viewHolder.weatherIcon.setContentDescription(forecast);

        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        // Log.d(TAG, "HIGH TEMP------------: "+high);
        viewHolder.highTempView.setText(Utility.formatTemperature(mContext,high));
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        //  Log.d(TAG, "LOW TEMP-------------: "+low);
        viewHolder.lowTempView.setText(Utility.formatTemperature(mContext,low));

    }

    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemCount() {
        if(mCursor != null)
            return mCursor.getCount();
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if(position ==0)
            Log.d(TAG, "Returning view type today");
        else
            Log.d(TAG, "Returning view type future day");

        return position == 0 && mUseTodayLayout ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public void setUseTodayLayout(boolean useTodayLayout)
    {
        mUseTodayLayout = useTodayLayout;
    }

    public int getInflatedViewId(int position)
    {
        int layoutId = R.layout.list_item_forecast;
        switch (position)
        {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
        }
        return layoutId;
    }

    private String generateTransitionName(int pos)
    {
        return mContext.getString(R.string.forecast_icon)+pos;
    }


    class WeatherInfoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView highTempView, lowTempView, forecastView, dateView;
        public final ImageView weatherIcon;

        public WeatherInfoHolder(View view)
        {
            super(view);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            forecastView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            weatherIcon = (ImageView) view.findViewById(R.id.list_item_icon);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            Cursor cursor = getCursor();
            cursor.moveToPosition(getLayoutPosition());
            if (cursor != null) {
                Long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
                mClickHandler.onClick(date, this);
            }
        }
    }

    public static interface ForecastAdapterOnClickHandler
    {
        void onClick(Long date, WeatherInfoHolder viewHolder);
    }
}
