package com.example.farmacia.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.farmacia.util.AlarmScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                SharedPreferences prefs = context.getSharedPreferences("FarmacIAPrefs", Context.MODE_PRIVATE);
                int lastUserId = prefs.getInt("LAST_USER_ID", -1);
                if (lastUserId != -1) {
                    String userName = prefs.getString("USER_NAME_" + lastUserId, "Usuario");
                    // Delegate all scheduling logic to the centralized scheduler
                    AlarmScheduler.scheduleAllAlarmsForUser(context, lastUserId, userName);
                }
            });
        }
    }
}
