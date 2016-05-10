package com.application.chetna_priya.weather_forecast.app;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

public class SettingsActivity extends AppCompatActivity{


    public static final int PLACE_PICKER_REQUEST = 1;
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private SettingsActivityFragment settingsActivityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        FragmentManager fragmentManager = getFragmentManager();
        settingsActivityFragment = (SettingsActivityFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_settings);
        if(settingsActivityFragment == null)
        {
            settingsActivityFragment = new SettingsActivityFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_settings,settingsActivityFragment);
            fragmentTransaction.commit();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PLACE_PICKER_REQUEST)
        {
            if(resultCode == RESULT_OK)
            {
                settingsActivityFragment.onActivityResult(requestCode, resultCode, data);

            }else
                super.onActivityResult(requestCode, resultCode, data);
        }
    }


  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

  */
}
