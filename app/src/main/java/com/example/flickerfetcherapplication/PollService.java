package com.example.flickerfetcherapplication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.security.PublicKey;
import java.util.ArrayList;

public class PollService extends IntentService {

    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000 * 60 * 3 ; // set the polling rate to 3 min
    public static final String PERF_IS_ALARM_ON = "isAlarmOn";
    public static final String ACTION_SHOW_NOTIFICATION = "com.example.flickerfetcherapplication.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "com.example.flickerfetcherapplication.PRIVATE";      // signature key for the broadcast
    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        boolean isNetworkAvailable = connectivityManager.getBackgroundDataSetting() && connectivityManager.getActiveNetworkInfo() != null;   // check if the network and background data setting  is available

        if(!isNetworkAvailable) // if not available just return
            return;

        String lastResult_id = PreferenceManager.getDefaultSharedPreferences(this).getString(FlickerFetcher.PREF_LAST_RESULT_ID,null); // get the last result id from the shared preferences
        String query = PreferenceManager.getDefaultSharedPreferences(this).getString(FlickerFetcher.PREF_SEARCH_QUERY,null); // get the search query from the shared preferences
        ArrayList<GalleryItem> items;

        if(query != null) // get if we have a search query
        {
            items = new FlickerFetcher().search(query); // return searched items
        }
        else
        {
            items = new FlickerFetcher().fetchItems(); // return new items
        }

        if(items.size() == 0)
            return;

        String result_id = items.get(0).getID(); // get the result id from the array list

        if(!result_id.equals(lastResult_id)) // check if the result id not equals to the last result id
        {
            Resources resources = getResources();
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),0);
            String CHANNEL_ID="Channel 1";
            NotificationChannel notificationChannel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel(CHANNEL_ID,"My Channel 1", NotificationManager.IMPORTANCE_LOW);
            }
            Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID).
                    setTicker(resources.getString(R.string.newpic)).setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.newpic)).setContentText("You have a new Picture!")
                    .setContentIntent(pendingIntent).setAutoCancel(true).build();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            showBackgroundNotification(0,notification);
                                                                                        // send a notification that there is a new results
            Log.i(TAG,"I got new Result "+result_id);

        }
        else
        {
            Log.i(TAG,"I got old Result "+result_id);
            // results are the same, do not do anything
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(FlickerFetcher.PREF_LAST_RESULT_ID,result_id).commit(); // save the last result id in the shared preferences

    }

    public static void setAlarmService(Context context,boolean isOn)  // a method to set the alarm to service to notify the user
    {
        Intent intent = new Intent(context,PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context,0,intent,0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        if(isOn) // check if the alarm is on
        {
            alarmManager.setRepeating(AlarmManager.RTC,System.currentTimeMillis(),POLL_INTERVAL,pendingIntent);  // set alarm to repeat each 3 min

        }
        else
        {
            alarmManager.cancel(pendingIntent); // cancel the alarm
            pendingIntent.cancel(); // cancel the pending intent
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PERF_IS_ALARM_ON,isOn).commit(); // save the PERF alarm in the shard preference
    }

    public static boolean isAlarmSet(Context context) // method to check if the alarm is set or not
    {
        Intent intent = new Intent(context,PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context,0,intent,PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }

    public void showBackgroundNotification(int requestCode, Notification notification)  // method to show the notification when Photo fragment is not visible to the user
    {
        Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
        intent.putExtra("REQUEST_CODE",requestCode);
        intent.putExtra("NOTIFICATION",notification);
        sendOrderedBroadcast(intent,PERM_PRIVATE,null,null, Activity.RESULT_OK,null,null);     // Send the action with a broadcast

    }
}
