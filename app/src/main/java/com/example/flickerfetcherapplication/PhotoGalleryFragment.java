package com.example.flickerfetcherapplication;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";
    private GridView mGridView;
    private ArrayList<GalleryItem> Items;
    private ThumbnailDownloader<ImageView> thumbnailDownloader;


    public PhotoGalleryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        updateItems();
        thumbnailDownloader = new ThumbnailDownloader<>(new Handler());
        thumbnailDownloader.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if(isVisible())
                {

                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        thumbnailDownloader.start();
        thumbnailDownloader.getLooper();
        Log.i(TAG,"Background thread started..");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mGridView = view.findViewById(R.id.gridView);
        setupAdapter();
        return view;
    }


    private class GalleryAdapter extends ArrayAdapter<GalleryItem>
    {
        public GalleryAdapter(@NonNull Context context, int resource, List<GalleryItem> galleryItemList) {
            super(context, resource,galleryItemList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView == null)
            {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item,parent,false);
            }
            ImageView imageView = convertView.findViewById(R.id.img);
            GalleryItem item = getItem(position);


            if(item.getUrl() != null)
                //this can be used for caching images
            //Glide.with(getActivity()).load(Uri.parse(item.getUrl())).centerCrop().diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.ic_baseline_autorenew_24).into(imageView);
            thumbnailDownloader.queueThumbnail(imageView,item.getUrl());
            return convertView;
        }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>>
    {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... voids) {

            if(getActivity() == null)
                return new ArrayList<GalleryItem>();

            String query = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(FlickerFetcher.PREF_SEARCH_QUERY,null);

            if(query != null)
            {
                return new FlickerFetcher().search(query);
            }
            else
            {
               return new FlickerFetcher().fetchItems();
            }


        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> galleryItems) {
            Items = galleryItems;
            setupAdapter();
        }
    }

    public void updateItems()
    {
        new FetchItemsTask().execute();
    }




    private void setupAdapter()
    {
        if(getActivity() == null || mGridView == null)
            return;
        if(Items != null)
        {
            mGridView.setAdapter(new GalleryAdapter(getActivity(),R.layout.gallery_item,Items));
        }
        else
        {
            mGridView.setAdapter(null);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = getActivity().getComponentName();
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(componentName);
        searchView.setSearchableInfo(searchableInfo);


    }
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.Toggle_Polling);
        if(PollService.isAlarmSet(getActivity())) {
            menuItem.setTitle("Stop Polling");
            menuItem.setIcon(R.drawable.ic_baseline_sync_disabled_24);
        }
        else
        {
            menuItem.setTitle("Start Polling");
            menuItem.setIcon(R.drawable.ic_baseline_sync_24);
        }

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {

            case R.id.app_bar_search:
                getActivity().onSearchRequested();

                return true;

            case R.id.item_clear:
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(FlickerFetcher.PREF_SEARCH_QUERY,null).commit(); // if you use apply method, thumbnails will be downloaded in the background
                updateItems();
                return true;

            case R.id.Toggle_Polling:
                boolean shouldStartAlarm = !PollService.isAlarmSet(getActivity());
                PollService.setAlarmService(getActivity(),shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;

        }
        return false;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailDownloader.clearQueue();


    }


}