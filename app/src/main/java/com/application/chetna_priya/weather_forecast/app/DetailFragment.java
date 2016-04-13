package com.application.chetna_priya.weather_forecast.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.application.chetna_priya.weather_forecast.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    final String EXTRA_TEXT = "weather_info";
    final String TAG = DetailFragment.class.getSimpleName();

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
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
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
    private TextView mDayView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mWeatherDesc;
    private TextView mHumidityView;
    private TextView mWindSpeedView;
    private TextView mPressureView;
    private ImageView mWeatherIcon;
    private Uri mWeatherUri;

    MyCustomVew myView;


    private static final String DATA_URI = "data_uri";

    public DetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public static DetailFragment newInstance(Uri uri) {
        DetailFragment f = new DetailFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(DATA_URI,uri);
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
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mWeatherUri = getSelectedUri();
        mDateView = (TextView) rootView.findViewById(R.id.tv_date);
        mDayView = (TextView)rootView.findViewById(R.id.tv_day);
        mHighTempView = (TextView) rootView.findViewById(R.id.tv_max_temp);
        mLowTempView = (TextView) rootView.findViewById(R.id.tv_min_temp);
        mWeatherDesc = (TextView) rootView.findViewById(R.id.tv_weather_desc);
        mHumidityView = (TextView) rootView.findViewById(R.id.tv_humidity);
        mWindSpeedView = (TextView) rootView.findViewById(R.id.tv_wind);
        mPressureView = (TextView) rootView.findViewById(R.id.tv_pressure);
        mWeatherIcon = (ImageView) rootView.findViewById(R.id.img_weather_icon);
        Log.d(TAG, "IN on create view of detail acitivity fragment");

        myView = new MyCustomVew(getActivity());
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
        else
            return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        Log.d(TAG, "ROWS: "+cursor.getCount()+" COLUMNS: "+cursor.getColumnCount());
        Long date = cursor.getLong(COL_WEATHER_DATE);
        Double max_temp = cursor.getDouble(COL_WEATHER_MAX_TEMP);
        Double min_temp = cursor.getDouble(COL_WEATHER_MIN_TEMP);
        String description = cursor.getString(COL_WEATHER_DESC);
        Float humidity = cursor.getFloat(COL_WEATHER_HUMIDITY);
        Float pressure = cursor.getFloat(COL_WEATHER_PRESSURE);

        String dayName = Utility.getDayName(getActivity(), date);
        String dateText = Utility.getFormattedMonthDay(getActivity(),date);
       // String dateStr = dayName.toUpperCase()+"\n"+dateText.toUpperCase();
        mDateView.setText(dateText);
        mDayView.setText(dayName);

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

        mWeatherIcon.setImageResource(Utility.getArtResourceForWeatherCondition(cursor.getInt(COL_WEATHER_CONDITION_ID)));

        mWeatherIcon.setContentDescription(description);
        mForecastStr = String.format("%s - %s - %s/%s", dateText, description, high, low);



        if(mShareActionProvider != null)
        {
            mShareActionProvider.setShareIntent(getShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public static class MyCustomVew extends View
    {
        Paint mCirclePaint;
        private float centerX, centerY;
        private String TAG = MyCustomVew.class.getSimpleName();
        private float extRadius;
        String[] texts = {"N","E","S","W"};
        float[]offset = new float[4];
        private float internalRad,textOffset;
        private Paint mTextPaint;
        private float mTextHeight = 20, mTextWidth = 20;

        public MyCustomVew(Context context) {
            super(context);
            initializeDrawable();
        }

        private void initializeDrawable() {
            setWillNotDraw(false);
            mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCirclePaint.setColor(getResources().getColor(R.color.black));
            mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTextPaint.setColor(getResources().getColor(R.color.black));
            if (mTextHeight == 0) {
                mTextHeight = mTextPaint.getTextSize();
            } else {
                mTextPaint.setTextSize(mTextHeight);
            }

        }

        public MyCustomVew(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initializeDrawable();
        }

        public MyCustomVew(Context context, AttributeSet attrs) {
            super(context, attrs);
            initializeDrawable();
        }


        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            float xpad = (float) getPaddingLeft() + getPaddingRight();
            float ypad = (float) getPaddingTop() + getPaddingBottom();
            w = getWidth();
            h = getHeight();
            float ww = (float)w - xpad; float hh = (float)h - ypad;
            extRadius = Math.min(ww, hh) / 2;
            internalRad = extRadius -3;
            float[] offsetArr = {0,internalRad,0,-internalRad};
            offset = offsetArr;
            centerX = w/2;
            centerY = h/2;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Log.d(TAG, "ON DRAWWWWWWWWWWWWWWWWW");
            canvas.drawCircle(centerX, centerY, extRadius, mCirclePaint);
            mCirclePaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(centerX, centerY, internalRad, mCirclePaint);
            Path path = new Path();
            path.addCircle(centerX,centerY,internalRad, Path.Direction.CW);
            for(int i=0; i<texts.length;i++)
                canvas.drawTextOnPath(texts[i],path,offset[i],0,mTextPaint);
        }
    }
}
