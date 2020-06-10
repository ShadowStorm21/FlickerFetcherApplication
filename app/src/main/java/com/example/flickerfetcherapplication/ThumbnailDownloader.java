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
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThumbnailDownloader<Token> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Map<Token,String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());      // create a synchronized hashmap since we dealing with different handlers ( Multithreading)
    private Handler mHandler;
    private Handler mResponseHandler;
    private Bitmap bitmap;
    private Listener mListener;

    public ThumbnailDownloader(Handler handler) {
        super(TAG);
        mResponseHandler = handler;    // set a handler to respond to the main handler
    }

    public void queueThumbnail(Token token,String url)
    {
        Log.i(TAG,"got url from "+url);
        requestMap.put(token,url);  // put the imageview and url in the hashmap, token here is a generic type
        mHandler.obtainMessage(MESSAGE_DOWNLOAD,token).sendToTarget(); // let a handler to send the message the message queue ( inbox)
    }
    @SuppressLint("HandlerLeak")  // here we must suppress the warning since we are dealing with different thread and this may cause some leaking problems but i initialized my handler inside the loop prepared so the chances of leaking are almost none
    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler()
        {

            @Override
            public void handleMessage(@NonNull Message msg) {

                if(msg.what == MESSAGE_DOWNLOAD) // check if the message we sent has the same download code
                {
                    @SuppressWarnings("unchecked")
                    Token token = (Token) msg.obj; // here we have to convert the object to token and token is generic type therefore we have to suppress the warning unchecked
                    handleRequest(token); // call the method to handle the request
                }

            }
        };
    }

    private void handleRequest(final Token token) {  // a method the handle the request

        try {
            final String url = requestMap.get(token); // get the url from the map using the token as the key
            if(url == null)
            {
                return;
            }
            else
            {

                byte[] bitmapBytes = new FlickerFetcher().getBytes(url);  // get the bitmap bytes using the getByte method in flickerfetcher class
                if(bitmapBytes == null)
                {
                    return;
                }
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);  // decode the byte array to bitmap
                Log.i(TAG,"Bitmap Created!");
                mResponseHandler.post(new Runnable() {  // make another handler to set the changes
                    @Override
                    public void run() {

                        if(requestMap.get(token) != url) // check the the url in the map is the same the one we got
                        {
                            return;
                        }
                        else
                        {

                            requestMap.remove(token); // remove the token from the map
                            mListener.onThumbnailDownloaded(token,bitmap); // let the interface to handle the changes

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
        void onThumbnailDownloaded(Token token,Bitmap thumbnail);        // method to handle the bitmap and the imageview (token)
    }

    public void setListener(Listener<Token> tokenListener) // set the interface
    {
        mListener = tokenListener;
    }


    public void clearQueue() // method to clear the queue
    {
        requestMap.clear(); // clear the map
        mHandler.removeMessages(MESSAGE_DOWNLOAD); // remove the message
    }









}
