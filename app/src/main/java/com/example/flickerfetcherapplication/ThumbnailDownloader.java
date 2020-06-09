package com.example.flickerfetcherapplication;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThumbnailDownloader<Token> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Map<Token,String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    private Handler mHandler;
    private Handler mResponseHandler;
    private Bitmap bitmap;
    private Listener mListener;

    public ThumbnailDownloader(Handler handler) {
        super(TAG);
        mResponseHandler = handler;
    }

    public void queueThumbnail(Token token,String url)
    {
        Log.i(TAG,"got url from "+url);
        requestMap.put(token,url);
        mHandler.obtainMessage(MESSAGE_DOWNLOAD,token).sendToTarget();
    }
    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler()
        {

            @Override
            public void handleMessage(@NonNull Message msg) {

                if(msg.what == MESSAGE_DOWNLOAD)
                {
                    @SuppressWarnings("unchecked")
                    Token token = (Token) msg.obj;
                    handleRequest(token);
                }

            }
        };
    }

    private void handleRequest(final Token token) {

        try {
            final String url = requestMap.get(token);
            if(url == null)
            {
                return;
            }
            else
            {

                byte[] bitmapBytes = new FlickerFetcher().getBytes(url);
                if(bitmapBytes == null)
                {
                    return;
                }
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
                Log.i(TAG,"Bitmap Created!");
                mResponseHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(requestMap.get(token) != url)
                        {
                            return;
                        }
                        else
                        {

                            requestMap.remove(token);
                            mListener.onThumbnailDownloaded(token,bitmap);

                        }
                    }
                });


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface Listener<Token>
    {
        void onThumbnailDownloaded(Token token,Bitmap thumbnail);
    }

    public void setListener(Listener<Token> tokenListener)
    {
        mListener = tokenListener;
    }


    public void clearQueue()
    {
        requestMap.clear();
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
    }









}
