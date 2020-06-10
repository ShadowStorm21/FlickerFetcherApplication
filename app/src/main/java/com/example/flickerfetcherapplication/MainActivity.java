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
    }  // returns new Photo Fragment
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createFragment(); // call the method on create
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG,"I got intent from "+intent);

        PhotoGalleryFragment fragment = (PhotoGalleryFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer); // get the photo fragment

        if(intent.ACTION_SEARCH.equals(intent.getAction())) // check if the intent action is a search action
        {
            String query = intent.getStringExtra(SearchManager.QUERY); // get the user searched query
            Log.i(TAG,"Received a query from "+query);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(FlickerFetcher.PREF_SEARCH_QUERY,query).commit(); // if you use apply method, thumbnails will be downloaded in the background
            // put the query in shared preferences
            Toast.makeText(this, "Search results : "+FlickerFetcher.count, Toast.LENGTH_SHORT).show();

        }
        fragment.updateItems(); // update items if not

    }
}