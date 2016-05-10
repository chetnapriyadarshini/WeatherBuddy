package com.application.chetna_priya.weather_forecast.app;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.application.chetna_priya.weather_forecast.app.data.WeatherContract;
import com.application.chetna_priya.weather_forecast.app.gcm.RegistrationIntentService;
import com.application.chetna_priya.weather_forecast.app.sync.SunshineSyncAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    final int PLAY_SERVICES_RESOLUTION_REQUEST = 0;
    static String mLocation;
    final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static boolean mTwoPane = false;
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        mLocation = Utility.getPreferredLocation(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar == null)
            Log.d(LOG_TAG, "NULLLLLLLLLLLLLLLL");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if(findViewById(R.id.weather_detail_container)!= null)
        {
            Log.d(LOG_TAG, "THIS IS A TWO PANE LAYOUT");
            mTwoPane = true;
            //this is a tablet and we operate in two pane mode adding the detail fragment for right pane
            if(savedInstanceState == null)
            {
                Log.d(LOG_TAG, "SAVED INSTANCE NULL INFLATE DETAIL FRAGMENT");
                DetailFragment detailFragment = DetailFragment.newInstance
                        (WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation,System.currentTimeMillis()),false);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, detailFragment)
                        .commit();
            }
        }
        else {
            Log.d(LOG_TAG, "THIS IS NOT A TWO PANE LAYOUT");
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        ff.setUseTodayLayout(!mTwoPane);

        SunshineSyncAdapter.initializeSyncAdapter(this);

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if(status!=ConnectionResult.SUCCESS){
            //  tvStatus.setText("Google Play Services are not available");
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, PLAY_SERVICES_RESOLUTION_REQUEST);
            dialog.show();
        }else{
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
            if(!sentToken)
            {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {

            case R.id.action_settings:
                Intent settingsIntent = new Intent(this,SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String location = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            Log.d(LOG_TAG,"LOCATION CHANGED RESTART THE LOADER");
            if ( null != ff ) {
                ff.onLocationChanged();
            }
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }/*else {
            ff.getLoaderManager().initLoader(ForecastFragment.WEATHER_LOADER_ID, null, ff);
        }*/

    }


    /*private boolean googlePlayServicesAvailaible() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {
            if(apiAvailability.isUserResolvableError(resultCode))
            {
                apiAvailability.getErrorDialog(this,resultCode,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }*/

    @Override
    public void onItemSelected(ForecastAdapter.WeatherInfoHolder viewHolder, Uri dateUri) {
        if(mTwoPane)
        {
            DetailFragment detailFragment = DetailFragment.newInstance(dateUri, false);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment)
                    .commit();
        }else{
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.setData(dateUri);
            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this,  new Pair<View, String>(viewHolder.weatherIcon, getString(R.string.detail_icon_transition_name)));
            ActivityCompat.startActivity(this,detailIntent, activityOptions.toBundle());
        }
    }
}
