package com.application.chetna_priya.weather_forecast.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.application.chetna_priya.weather_forecast.app.data.WeatherContract;
import com.bumptech.glide.Glide;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    final String EXTRA_TEXT = "weather_info";
    final String TAG = DetailFragment.class.getSimpleName();
    public static final String DETAIL_TRANSITION_ANIMATION = "postpone_detail_transition_animation";

    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MIN_TEMP = 3;
    public static final int COL_WEATHER_MAX_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;


    private ShareActionProvider mShareActionProvider;
    private String mForecastStr;
    private String hashTagSunshineApp = " #SunshineApp";
    private static final int DETAIL_WEATHER_LOADER = 0;
    private TextView mDateView;
   // private TextView mDayView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mWeatherDesc;
    private TextView mHumidityView;
    private TextView mWindSpeedView;
    private TextView mPressureView;
    private ImageView mWeatherIcon;
    private Uri mWeatherUri;


    private static final String DATA_URI = "data_uri";

    public DetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public static DetailFragment newInstance(Uri uri, boolean postponeAnim) {
        DetailFragment f = new DetailFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(DATA_URI, uri);
        args.putBoolean(DETAIL_TRANSITION_ANIMATION, postponeAnim);
        f.setArguments(args);

        return f;
    }

    public Uri getSelectedUri()
    {
        return getArguments().getParcelable(DATA_URI);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_WEATHER_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_start, container, false);
        mWeatherUri = getSelectedUri();
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
       // mDayView = (TextView)rootView.findViewById(R.id.detail_date_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mWeatherDesc = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.tv_humidity);
        mWindSpeedView = (TextView) rootView.findViewById(R.id.tv_wind);
        mPressureView = (TextView) rootView.findViewById(R.id.tv_pressure);
        mWeatherIcon = (ImageView) rootView.findViewById(R.id.detail_icon);
        Log.d(TAG, "IN on create view of detail acitivity fragment");
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

    }

    private Intent getShareIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + hashTagSunshineApp);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mWeatherUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mWeatherUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_WEATHER_LOADER, null, this);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle weather) {
        Log.d(TAG, "URI RECEIVED: " + mWeatherUri);
        if(mWeatherUri != null)
        return new CursorLoader(getActivity(),
                mWeatherUri,
                DETAIL_COLUMNS,
                null,
                null,
                null);
        ViewParent vp = getView().getParent();
        if ( vp instanceof CardView ) {
            ((View)vp).setVisibility(View.INVISIBLE);
        }
            return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            if(getArguments().getBoolean(DETAIL_TRANSITION_ANIMATION, false))
                getActivity().supportStartPostponedEnterTransition();
            ViewParent vp = getView().getParent();
            if (vp instanceof CardView) {
                ((View) vp).setVisibility(View.VISIBLE);
            }
            cursor.moveToFirst();
            Log.d(TAG, "ROWS: " + cursor.getCount() + " COLUMNS: " + cursor.getColumnCount());
            Long date = cursor.getLong(COL_WEATHER_DATE);
            Double max_temp = cursor.getDouble(COL_WEATHER_MAX_TEMP);
            Double min_temp = cursor.getDouble(COL_WEATHER_MIN_TEMP);
            String description = cursor.getString(COL_WEATHER_DESC);
            Float humidity = cursor.getFloat(COL_WEATHER_HUMIDITY);
            Float pressure = cursor.getFloat(COL_WEATHER_PRESSURE);

            String dayName = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
            // String dateStr = dayName.toUpperCase()+"\n"+dateText.toUpperCase();
            mDateView.setText(dayName + ", " + dateText);
            //mDayView.setText(dayName);

            String high = Utility.formatTemperature(getActivity(), max_temp);
            String low = Utility.formatTemperature(getActivity(), min_temp);
            mHighTempView.setText(high);
            mLowTempView.setText(low);

            mWeatherDesc.setText(description);

            mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));
            float windSpeedStr = cursor.getFloat(COL_WEATHER_WIND_SPEED);
            float windDirStr = cursor.getFloat(COL_WEATHER_DEGREES);
            mWindSpeedView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));
            mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

            //    mWeatherIcon.setImageResource(Utility.getArtResourceForWeatherCondition(cursor.getInt(COL_WEATHER_CONDITION_ID)));
            Glide.with(this).load(Utility.getArtUrlForWeatherCondition(getActivity(), (cursor.getInt(COL_WEATHER_CONDITION_ID))))
                    .error(Utility.getArtResourceForWeatherCondition(cursor.getInt(COL_WEATHER_CONDITION_ID)))
                    .into(mWeatherIcon);
            Log.d(TAG, "DETAIL URL: " + Utility.getArtUrlForWeatherCondition(getActivity(), (cursor.getInt(COL_WEATHER_CONDITION_ID))));

            mWeatherIcon.setContentDescription(description);
            mForecastStr = String.format("%s - %s - %s/%s", dateText, description, high, low);


        }
        if(mShareActionProvider != null)
        {
            mShareActionProvider.setShareIntent(getShareIntent());
        }

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        // We need to start the enter transition after the data has loaded
        if (activity instanceof DetailActivity) {
            activity.supportStartPostponedEnterTransition();

            if ( null != toolbarView ) {
                activity.setSupportActionBar(toolbarView);

                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            if ( null != toolbarView ) {
                Menu menu = toolbarView.getMenu();
                if ( null != menu ) menu.clear();
                toolbarView.inflateMenu(R.menu.detailfragment);
                finishCreatingMenu(toolbarView.getMenu());
            }
        }
    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(getShareIntent());
    }




    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
