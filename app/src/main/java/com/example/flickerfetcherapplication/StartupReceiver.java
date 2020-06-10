package com.example.flickerfetcherapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG,"Received Broadcast : "+intent.getAction());

        boolean isOn = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PollService.PERF_IS_ALARM_ON,false);
        PollService.setAlarmService(context,isOn); // set polling service to on when the device turns on using broadcast

    }
}
