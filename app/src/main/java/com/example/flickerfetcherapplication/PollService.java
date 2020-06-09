package com.example.flickerfetcherapplication;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class PollService extends IntentService {

    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000 * 60 * 5 ; // 5 min
    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        boolean isNetworkAvailable = connectivityManager.getBackgroundDataSetting() && connectivityManager.getActiveNetworkInfo() != null;

        if(!isNetworkAvailable)
            return;

        String lastResult_id = PreferenceManager.getDefaultSharedPreferences(this).getString(FlickerFetcher.PREF_LAST_RESULT_ID,null);
        String query = PreferenceManager.getDefaultSharedPreferences(this).getString(FlickerFetcher.PREF_SEARCH_QUERY,null);
        ArrayList<GalleryItem> items;

        if(query != null)
        {
            items = new FlickerFetcher().search(query);
        }
        else
        {
            items = new FlickerFetcher().fetchItems();
        }

        if(items.size() == 0)
            return;

        String result_id = items.get(0).getID();

        if(!result_id.equals(lastResult_id))
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
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
            notificationManager.notify(0,notification);

            Log.i(TAG,"I got new Result "+result_id);
        }
        else
        {
            Log.i(TAG,"I got old Result "+result_id);
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(FlickerFetcher.PREF_LAST_RESULT_ID,result_id).commit();

    }

    public static void setAlarmService(Context context,boolean isOn)
    {
        Intent intent = new Intent(context,PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context,0,intent,0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        if(isOn)
        {
            alarmManager.setRepeating(AlarmManager.RTC,System.currentTimeMillis(),POLL_INTERVAL,pendingIntent);

        }
        else
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static boolean isAlarmSet(Context context)
    {
        Intent intent = new Intent(context,PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context,0,intent,PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }
}
