package com.application.chetna_priya.weather_forecast.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.application.chetna_priya.weather_forecast.app.data.WeatherContract;
import com.application.chetna_priya.weather_forecast.app.sync.SunshineSyncAdapter;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,
    SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String LOG_TAG = SettingsActivityFragment.class.getSimpleName();
    final String TAG = SettingsActivityFragment.class.getSimpleName();
    private ImageView attributions;

    public SettingsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummarytoValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummarytoValue(findPreference(getString(R.string.pref_unit_key)));
        bindPreferenceSummarytoValue(findPreference(getString(R.string.pref_icon_key)));

    }


    private void bindPreferenceSummarytoValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        attributions = (ImageView) rootView.findViewById(R.id.img_attributions);
        attributions.setVisibility(View.GONE);
        return rootView;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object summary) {
        setPreferenceSummary(preference, summary);
        return true;
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
        String key = preference.getKey();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
            if(key.equals(getString(R.string.pref_icon_key)))
            {
                /*
                Clear disk cache and memory if the user changes his preferred icon type
                to enable reload of images
                 */
                AsyncTask clearMemoryTask = new AsyncTask() {
                    @Override
                    protected Boolean doInBackground(Object[] params) {
                        try{

                            Glide.get(getActivity()).clearDiskCache();
                            Log.d(TAG, "Cleared Glide cache and memory images will be reloaded now");
                        }catch(Exception e)
                        {
                            Log.d(TAG, "Exception while clearing glide cache: "+e);
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        Glide.get(getActivity()).clearMemory();
                        getActivity().getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
                        super.onPostExecute(o);
                    }
                };
                clearMemoryTask.execute();
            }
        } else if (key.equals(getString(R.string.pref_location_key))) {
            @SunshineSyncAdapter.LocationStatus int status = Utility.getLocationStatus(getActivity());
            switch (status) {
                case SunshineSyncAdapter.LOCATION_STATUS_OK:
                    preference.setSummary(stringValue);
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN:
                    preference.setSummary(getString(R.string.pref_location_unknown_description, value.toString()));
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_LOCATION_INVALID:
                    preference.setSummary(getString(R.string.pref_location_error_description, value.toString()));
                    break;
                default:
                    // Note --- if the server is down we still assume the value
                    // is valid
                    preference.setSummary(stringValue);
            }
            Log.d(LOG_TAG, "Setting the location summary here: "+preference.getSummary());
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SettingsActivity.PLACE_PICKER_REQUEST)
        {
            if(resultCode == SettingsActivity.RESULT_OK)
            {
                attributions.setVisibility(View.VISIBLE);

                Place place = PlacePicker.getPlace(data, getActivity());
                String address = place.getAddress().toString();
                LatLng latLng = place.getLatLng();
                if(TextUtils.isEmpty(address))
                {
                    address = String.format("(%.2f, %.2f)",latLng.latitude, latLng.longitude);
                }
                Log.d(LOG_TAG, "ADDRESS SELECTED: "+address+" NAME: "+place.getName());
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.pref_location_key), address);
                editor.putFloat(getString(R.string.pref_location_longitude), (float) latLng.longitude);
                editor.putFloat(getString(R.string.pref_location_latitude), (float) latLng.latitude);
                editor.commit();
                Preference preference = findPreference(getString(R.string.pref_location_key));
                setPreferenceSummary(preference, address);
                Utility.resetLocationStatus(getActivity());
                Log.d(LOG_TAG, "Location Summary: "+preference.getSummary());
                SunshineSyncAdapter.syncImmediately(getActivity());

            }else
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ( key.equals(getString(R.string.pref_location_key)) ) {
            // we've changed the location
            // first clear locationStatus
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(getString(R.string.pref_location_latitude));
            editor.remove(getString(R.string.pref_location_longitude));
            editor.commit();
            Utility.resetLocationStatus(getActivity());
            SunshineSyncAdapter.syncImmediately(getActivity());
        } else if ( key.equals(getString(R.string.pref_unit_key)) ) {
            // units have changed. update lists of weather entries accordingly
            getActivity().getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        } else if ( key.equals(getString(R.string.pref_location_status_key)) ) {
            // our location status has changed.  Update the summary accordingly
            Preference locationPreference = findPreference(getString(R.string.pref_location_key));
            bindPreferenceSummarytoValue(locationPreference);
        }
    }
}
