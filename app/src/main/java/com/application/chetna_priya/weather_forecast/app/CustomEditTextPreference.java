package com.application.chetna_priya.weather_forecast.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;

/**
 * Created by chetna_priya on 4/18/2016.
 */
public class CustomEditTextPreference extends EditTextPreference {

    private static final String LOG_TAG = CustomEditTextPreference.class.getSimpleName();
    int mLocationLength;

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EditPreferenceTextView,0,0);
        try
        {
            mLocationLength = a.getInteger(R.styleable.EditPreferenceTextView_min_edit_location_length,Constant.DEFAULT_LOC_LENGTH);
            Log.d(LOG_TAG, "LENGTH "+mLocationLength);
        }finally {
            a.recycle();
        }
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if(resultCode == ConnectionResult.SUCCESS)
        {
            setWidgetLayoutResource(R.layout.widget_current_location);
        }
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        View currentLocationPicker = view.findViewById(R.id.location_picker);
        currentLocationPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity context = (SettingsActivity)getContext();
                try {
                    context.startActivityForResult(new PlacePicker.IntentBuilder().build(context), SettingsActivity.PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                //  Snackbar.make(v, "Everything fine", Snackbar.LENGTH_LONG).show();
            }
        });
        return view;
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final EditText editText = getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(getDialog() instanceof AlertDialog)
                {
                    if(editText.getText().length() < mLocationLength)
                        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    else
                        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
    }
}
