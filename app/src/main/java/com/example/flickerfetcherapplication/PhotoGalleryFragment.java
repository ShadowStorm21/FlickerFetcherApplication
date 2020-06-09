package com.example.flickerfetcherapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

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
        new FetchItemsTask().execute();
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
            return new FlickerFetcher().fetchItems();

        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> galleryItems) {
            Items = galleryItems;
            setupAdapter();
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailDownloader.clearQueue();


    }

}