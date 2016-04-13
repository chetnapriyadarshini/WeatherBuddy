package com.application.chetna_priya.weather_forecast.app;

import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.application.chetna_priya.weather_forecast.app.sync.SunshineSyncAdapter;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    final String TAG = SettingsActivityFragment.class.getSimpleName();

    public SettingsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummarytoValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummarytoValue(findPreference(getString(R.string.pref_unit_key)));
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
        return rootView;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String summary = newValue.toString();
        if(preference instanceof ListPreference)
        {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(summary);
            if(prefIndex >= 0)
            {
                Log.d(TAG,"Set summary!!!!!!!!!!!!!!! "+prefIndex);
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
            else

                Log.d(TAG," NOTTTTTTTTT Set summary!!!!!!!!!!!!!!! "+prefIndex);
        }
        else
        {
            if(preference instanceof EditTextPreference) {
                Log.d(TAG, "Call SYNC IMMIDIATLY from SETTINGS ACTIVITY");
                SunshineSyncAdapter.syncImmediately(getActivity());
            }
            preference.setSummary(summary);
        }
        return true;
    }
}
