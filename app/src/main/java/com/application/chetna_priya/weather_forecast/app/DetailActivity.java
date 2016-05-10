package com.application.chetna_priya.weather_forecast.app;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);/*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);*/
        if(savedInstanceState == null)
        {
            Uri uri = getIntent().getData();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, DetailFragment.newInstance(uri,true))
                    .commit();
        }
        supportPostponeEnterTransition();

    }
}
