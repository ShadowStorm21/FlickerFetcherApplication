package com.example.flickerfetcherapplication;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Received Results :"+ getResultCode());   // get the result code from the broadcast

        if(getResultCode() != Activity.RESULT_OK)  // check if the activity result is the same
        {
            return;  // a foreground activity cancel the broadcast

        }
        else
        {
            int requestCode = intent.getIntExtra("REQUEST_CODE",0);
            Notification notification = intent.getParcelableExtra("NOTIFICATION");
                // get the request code and notification, this will fire up the notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(requestCode,notification);
        }
    }
}
