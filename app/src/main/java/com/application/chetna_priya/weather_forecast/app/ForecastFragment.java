package com.application.chetna_priya.weather_forecast.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.application.chetna_priya.weather_forecast.app.data.WeatherContract;
import com.application.chetna_priya.weather_forecast.app.data.WeatherContract.*;
import com.application.chetna_priya.weather_forecast.app.sync.SunshineSyncAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.widget.ListView;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    ForecastAdapter mWeatherAdapter;
    final static int WEATHER_LOADER_ID = 0;
    private static final String[] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_MAX_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_COORD_LAT,
            LocationEntry.COLUMN_COORD_LONG
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    private static Uri selectedDateUri;
    private final String POSITION = "position";

    Callback mCallback;
    private int mPos = 0;
    private ListView mListview;
    private boolean mUseTodayLayout = false;


    public ForecastFragment() {
    }

    private final  String TAG = ForecastFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "ON ACTIVITY CREATED CALLED IN FORECAST FRAGMENT");
        getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) getActivity();
        }catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnItemSelectedListener");
        }
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPos != ListView.INVALID_POSITION){
            outState.putInt(POSITION,mPos);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);
        mWeatherAdapter = new ForecastAdapter(getActivity(),null,0);
        setUseTodayLayout(mUseTodayLayout);
        mListview = (ListView)rootView.findViewById(R.id.listview_forecast);
        mListview.setAdapter(mWeatherAdapter);
        if(savedInstanceState!=null && savedInstanceState.containsKey(POSITION)){
            Log.d(TAG, "RECEIVED BUNDLE");
            mPos = savedInstanceState.getInt(POSITION);
        }else
        Log.d(TAG, "BUNDLE NULL");
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Long date = cursor.getLong(COL_WEATHER_DATE);
                    mPos = position;
                    selectedDateUri = WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE));
                    if (mCallback != null) {
                        mCallback.onItemSelected(selectedDateUri);
                    }
                    //      getActivity().onItemSelected(selectedDateUri);
                    /*
                    Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                            .setData(WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE)));*/
                    //startActivity(detailIntent);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
/*
            case R.id.action_refresh:
                updateWeather();
                return true;
*/

            case R.id.action_view_on_map:
               // if(googlePlayServicesAvailaible())
                {
                   /* SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    String postalCode = sharedPreferences.getString(getString(R.string.pref_location_key)
                            , (getString(R.string.pref_default_location)));
                    Uri geolocation = Uri.parse("geo:0,0?").buildUpon()
                            .appendQueryParameter("q",postalCode).build();*/
                    if(mWeatherAdapter != null)
                    {
                        Cursor cursor = mWeatherAdapter.getCursor();
                        cursor.moveToPosition(0);
                        String pos_lat = cursor.getString(COL_COORD_LAT);
                        String pos_long = cursor.getString(COL_COORD_LONG);

                        Uri geolocation = Uri.parse("geo:"+pos_lat+","+pos_long);
                        Log.d(TAG, "LAT FOR MAP: "+pos_lat+"\n LONG FOR MAP: "+pos_long);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
                        mapIntent.setData(geolocation);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if(mapIntent.resolveActivity(getActivity().getPackageManager())!= null)
                        {
                            startActivity(mapIntent);
                        }
                    }

                }

        }
        return false;
    }

    public void onLocationChanged()
    {
        updateWeather();
        getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
    }

    public void updateWeather()
    {
        String location = Utility.getPreferredLocation(getActivity());
       /* FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity());
        fetchWeatherTask.execute(location);*//*
        Intent alarmIntent = new Intent(getActivity(),FetchWeatherService.AlarmReceiver.class);
        alarmIntent.putExtra(FetchWeatherService.LOCATION_KEY, location);
        //getActivity().startService(serviceIntent);
        Log.d(TAG, "Update Weather called, pending intent should get fired");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+5,pendingIntent);*/
        Log.d(TAG, "SHOULD SYNC NOWWWWWWWWWWW");
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    public void setUseTodayLayout(boolean useTodayLayout)
    {
        mUseTodayLayout = useTodayLayout;
        if(mWeatherAdapter != null)
             mWeatherAdapter.setUseTodayLayout(mUseTodayLayout);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherEntry.COLUMN_DATE+" ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG,"Rows: "+cursor.getCount()+" Scroll to position: "+mPos);
        mWeatherAdapter.swapCursor(cursor);
        if(mPos != ListView.INVALID_POSITION)
             mListview.setSelection(mPos);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mWeatherAdapter.swapCursor(null);
    }

}
