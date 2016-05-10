package com.application.chetna_priya.weather_forecast.app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.RemoteViews;

import com.application.chetna_priya.weather_forecast.app.DetailActivity;
import com.application.chetna_priya.weather_forecast.app.MainActivity;
import com.application.chetna_priya.weather_forecast.app.R;
import com.application.chetna_priya.weather_forecast.app.data.WeatherContract;
import com.application.chetna_priya.weather_forecast.app.sync.SunshineSyncAdapter;

/**
 * Created by chetna_priya on 5/4/2016.
 */
public class DetailWidgetProvider extends AppWidgetProvider {

    private static final String CLICK_ACTION = "com.application.chetna_priya.weather_forecast.app.widget.CLICK_ACTION";
    private static HandlerThread sWorkerThread;
    private static Handler sWorkerQueue;
    private static WeatherDataProviderObserver sDataObserver;

    public DetailWidgetProvider()
    {
        sWorkerThread = new HandlerThread("WeatherWidgetProvider-Worker");
        sWorkerThread.start();
        sWorkerQueue = new Handler(sWorkerThread.getLooper());
    }

    @Override
    public void onEnabled(Context context) {
        final ContentResolver r= context.getContentResolver();
        if(sDataObserver == null)
        {
            sDataObserver = new WeatherDataProviderObserver(AppWidgetManager.getInstance(context),
                    new ComponentName(context, DetailWidgetProvider.class),
                    sWorkerQueue);
            r.registerContentObserver(WeatherContract.WeatherEntry.CONTENT_URI, true, sDataObserver);
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews layout = buildLayout(context, appWidgetIds[i]);
            appWidgetManager.updateAppWidget(appWidgetIds[i], layout);
            // Create an Intent to launch ExampleActivity
            /*
            Intent intent = new Intent(context, DetailActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_detail);
            views.setOnClickPendingIntent(R.id.widget_linear_layout, pendingIntent);*/


        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private RemoteViews buildLayout(Context context, int appWidgetId) {
        final Intent intent = new Intent(context, DetailWidgetRemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_detail);
        rv.setRemoteAdapter(R.id.weather_list, intent);
      //  rv.setEmptyView(R.id.weather_list, R.id.widget_empty_view);

        final Intent onClickIntent = new Intent(context, DetailActivity.class);
        final PendingIntent onClickPendingIntent = PendingIntent.getActivity(context, 0, onClickIntent,
                0);
        rv.setPendingIntentTemplate(R.id.weather_list, onClickPendingIntent);
        return rv;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(SunshineSyncAdapter.ACTION_DATA_UPDATED)
              )
        {
            context.startService(new Intent(context, UpdateWidgetService.class));
        }
        super.onReceive(context, intent);
    }
}

class WeatherDataProviderObserver extends ContentObserver {
    private AppWidgetManager mAppWidgetManager;
    private ComponentName mComponentName;
    WeatherDataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
        super(h);
        mAppWidgetManager = mgr;
        mComponentName = cn;
    }
    @Override
    public void onChange(boolean selfChange) {
        // The data has changed, so notify the widget that the collection view needs to be updated.
        // In response, the factory's onDataSetChanged() will be called which will requery the
        // cursor for the new data.
        mAppWidgetManager.notifyAppWidgetViewDataChanged(
                mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.weather_list);
    }
}
