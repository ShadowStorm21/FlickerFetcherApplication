package com.example.flickerfetcherapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createFragment();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG,"I got intent from "+intent);

        PhotoGalleryFragment fragment = (PhotoGalleryFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if(intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(TAG,"Received a query from "+query);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(FlickerFetcher.PREF_SEARCH_QUERY,query).commit(); // if you use apply method, thumbnails will be downloaded in the background
            Toast.makeText(this, "Search results : "+FlickerFetcher.count, Toast.LENGTH_SHORT).show();

        }
        fragment.updateItems();

    }
}