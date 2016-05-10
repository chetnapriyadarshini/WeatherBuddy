package com.application.chetna_priya.weather_forecast.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.application.chetna_priya.weather_forecast.app.data.WeatherContract;
import com.application.chetna_priya.weather_forecast.app.data.WeatherContract.*;
import com.application.chetna_priya.weather_forecast.app.sync.SunshineSyncAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener, ForecastAdapter.ForecastAdapterOnClickHandler {
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
    static final int COL_WEATHER_MIN_TEMP = 3;
    static final int COL_WEATHER_MAX_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    private static Uri selectedDateUri;
    private final String POSITION = "position";

    Callback mCallback;
    private int mPos = RecyclerView.NO_POSITION;
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private boolean mUseTodayLayout;
    private boolean shouldPostponeEnterTransition = false;


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
        if(shouldPostponeEnterTransition)
            getActivity().supportPostponeEnterTransition();
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDestroy() {
        mCallback = null;
        if(null != mRecyclerView)
            mRecyclerView.clearOnScrollListeners();
        super.onDetach();
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

    @Override
    public void onPause() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_location_status_key)))
            updateEmptyView();
    }

    @Override
    public void onClick(Long date, ForecastAdapter.WeatherInfoHolder viewHolder) {
        mPos = viewHolder.getLayoutPosition();
        String locationSetting = Utility.getPreferredLocation(getActivity());
        selectedDateUri = WeatherEntry.buildWeatherLocationWithDate(locationSetting, date);
        if (mCallback != null) {
            mCallback.onItemSelected(viewHolder,selectedDateUri);
        }
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(ForecastAdapter.WeatherInfoHolder viewHolder, Uri dateUri);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPos != ListView.INVALID_POSITION){
            outState.putInt(POSITION,mPos);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ForecastFragment);
        shouldPostponeEnterTransition = typedArray.getBoolean(0,false);
        if(shouldPostponeEnterTransition)
            getActivity().supportPostponeEnterTransition();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        getActivity().supportPostponeEnterTransition();
        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);
        mEmptyView = (TextView)rootView.findViewById(R.id.recyclerView_empty_view);
        mWeatherAdapter = new ForecastAdapter(getActivity(), this, mEmptyView);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView_forecast);
       // mRecyclerView.setEmptyView(mEmptyView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mWeatherAdapter);
        if(savedInstanceState!=null && savedInstanceState.containsKey(POSITION)){
            Log.d(TAG, "RECEIVED BUNDLE");
            mPos = savedInstanceState.getInt(POSITION);
        }else
        Log.d(TAG, "BUNDLE NULL");
        mWeatherAdapter.setUseTodayLayout(mUseTodayLayout);

        final View parallaxView = rootView.findViewById(R.id.parallax_bar);
        if(null != parallaxView)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int max = parallaxView.getHeight();
                        if(dy > 0)
                            parallaxView.setTranslationY(Math.max(-max, parallaxView.getTranslationY() - dy/2));
                        else
                            parallaxView.setTranslationY(Math.min(0, parallaxView.getTranslationY() - dy/2));

                    }

                });
            }
        }
        return rootView;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mWeatherAdapter != null) {
            mWeatherAdapter.setUseTodayLayout(mUseTodayLayout);
        }
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
                {
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
       // updateWeather();
        getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
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
        Log.d(TAG, "Rows: " + cursor.getCount() + " Scroll to position: " + mPos);
        if(cursor.getCount() == 0)
            getActivity().supportStartPostponedEnterTransition();
        else mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (shouldPostponeEnterTransition)
                    getActivity().supportStartPostponedEnterTransition();
                return true;
            }
        });
        mWeatherAdapter.swapCursor(cursor);
        if (mPos != ListView.INVALID_POSITION) {
         //   mRecyclerView.setSelection(mPos);
            mRecyclerView.smoothScrollToPosition(mPos);
        }
        updateEmptyView();
    }

    private void updateEmptyView()
    {
        if(mWeatherAdapter.getItemCount() == 0)
        {
            int message = R.string.no_weather_info_available;
            @SunshineSyncAdapter.LocationStatus int locationStatus = Utility.getLocationStatus(getActivity());
                switch(locationStatus)
                {
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.empty_forecast_list_server_down;
                        break;

                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.empty_forecast_list_server_error;
                        break;

                    case SunshineSyncAdapter.LOCATION_STATUS_LOCATION_INVALID:
                        message = R.string.empty_forecast_list_unknown_location;
                        break;

                    default:
                        if(!Utility.isNetworkAvailable(getActivity()))
                            message = R.string.no_internet;
                        break;
                }
            mEmptyView.setText(message);
            Log.d(TAG, "STATUSSSSS: "+getString(message));
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mWeatherAdapter.swapCursor(null);
    }

}
