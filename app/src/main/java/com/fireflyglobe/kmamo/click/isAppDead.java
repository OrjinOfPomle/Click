package com.fireflyglobe.kmamo.click;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


import com.google.android.gms.location.FusedLocationProviderClient;

import androidx.annotation.Nullable;

public class isAppDead extends Service{
    private FusedLocationProviderClient mFusedLocationClient;
    PendingIntent pendingIntent;
    private static final String TAG = "MainActivity/isAppDead";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
      /*  mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        final int locationUpdateRC = 0;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        Intent intent = new Intent(this, MyLocationUpdateReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, locationUpdateRC, intent, flags);
        mFusedLocationClient.removeLocationUpdates(pendingIntent);
        */Log.d(TAG, "onTaskRemoved: removed location updates");
    }
}