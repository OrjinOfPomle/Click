package com.fireflyglobe.kmamo.click;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class MyLocationUpdateReceiver extends BroadcastReceiver{
    private static final String TAG = "MyLocationUpdateRecvr";
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("uIdL");
    GeoFire geoFire = new GeoFire(ref);
    String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();//userID with Location info


    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = context.getSharedPreferences("shared preferences",MODE_PRIVATE);
        boolean noBackgroundUpdates= !(pref.getBoolean("backgroundloc", false));
        Log.d(TAG, "onReceive: BackgroundUpdates? ----- " + noBackgroundUpdates);
        if(noBackgroundUpdates){   // will be false if background updates are allowed
            if(LocationResult.hasResult(intent)) {
                if (isLocationEnabled(context)) {
                    LocationResult locationResult = LocationResult.extractResult(intent);
                    for (Location location : locationResult.getLocations()) {
                        boolean setting = pref.getBoolean("locSetting", true);

                        Log.d(TAG, "onReceive: locSetting ------- " + setting);
                        if(setting){
                            //ref holds the location where the data will be stored
                            geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            Log.d(TAG, "information was added to the database");

                            int count = pref.getInt("backgroundCount", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            Log.d(TAG, "updating in the background ---------- " + count);
                                // geolocation stores lat in 0 and
                                // store long in 1 inside of the database
                            if(count % 5 == 0 || count < 2){
                                Log.d(TAG, "updating in the background ----- " + count);
                                Toast.makeText(context, "CLICK: location is updating in the background", Toast.LENGTH_LONG ).show();
                            }
                            //broadcast reciever will sometime run once more after being deleted resulting in location still being
                            //public

                            count++;
                            editor.putInt("backgroundCount", count).apply();
                            if(count > 1000000){
                                editor.putInt("backgroundCount", 0).apply();
                            }
                        }
                    }
                }
            }
        }
    }
    private static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

}
