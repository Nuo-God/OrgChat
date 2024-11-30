package com.dream.orgchat.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class NotificationService extends Service {
  private static final String TAG = "AutoStartService";

  @Override
  public void onCreate() {
    super.onCreate();
    // startForeground(1, getNotification());
    registerReceiver(new BootReceiver(), new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // Your service logic here
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  public static class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
        Log.d(TAG, "Boot completed, starting service");
        Intent serviceIntent = new Intent(context, NotificationService.class);
        context.startService(serviceIntent);
      }
    }
  }
}
