package com.fireflyglobe.kmamo.click;

import android.Manifest;
import android.app.AlertDialog;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;


import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.firebase.geofire.GeoFire;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.location.FusedLocationProviderClient;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.location.Location;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FacebookAuthProvider;

import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, PurchasesUpdatedListener, RecyclerViewAdapter.onNoteListener{


    LocationRequest mlocationRequest;
    Location mlastlocation;
    PendingIntent pendingIntent;
    BillingClient billingClient;
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mUserNames = new ArrayList<>();
    private ArrayList<String> mUserIds = new ArrayList<>();
    private ArrayList<String> mSuperOne = new ArrayList<>();
    private ArrayList<String> mSuperTwo = new ArrayList<>();
    private List<List<String>> GroupArraylist = new ArrayList<List<String>>();
    private ArrayList<ArrayList<String>> groupStatus = new ArrayList<ArrayList<String>>();
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<Integer> matching = new ArrayList<>();
    public static int globalCount = 0;


    GeoQuery geoQuery;
    private int screenWidth;
    private int fetchCount = 0;
    private static final String LOG_TAG = "MainActivity";
    private final static int basevalue = 25;
    private double radius = 25;
    GeoFire geoFire;
    private static final String TAG = "MainActivity";
    private int numberOfPeopleUpdatedToTheSystem = 0;
    RecyclerViewAdapter adapter;
    private boolean location_setting = true;




    // DatabaseReference is the class given to connect to firebase's database
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient mFusedLocationClient;
    Intent stickyService;
    private int Interval = 1000;
    private long fastestInterval = 500;
    private FirebaseAuth auth;
    private static final int RC_SIGN_IN = 1;
    private final static String code = "ca-app-pub-9187494759114498/5275816178"; // "ca-app-pub-9187494759114498/5275816178"
    private final static String vidCode = "ca-app-pub-9187494759114498/8351002235";  //test - "ca-app-pub-3940256099942544/8691691433"
    private InterstitialAd mAdView;
    private AdView mainBannerAd;
    private InterstitialAd mAdVids;


        List<AuthUI.IdpConfig> providers = Arrays.asList(
       // new AuthUI.IdpConfig.FacebookBuilder().build(),
        new AuthUI.IdpConfig.GoogleBuilder().build()
        );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);             // the activity isnt created until onCreate has finished
        for(String provider : AuthUI.SUPPORTED_PROVIDERS){
            Log.v(this.getClass().getName(), provider);
        }
        // checks if the user is logged in if not
        // ask them to sign in and if it is their first time add them the list of users
        AuthPlusUpdate();
        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "onCreate: this ran ----");
            addBasicInformation();
            hasUserInfoChanged();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences pref = getSharedPreferences("shared preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("backgroundCount", 0).apply();
        editor.putBoolean("backgroundloc", true).apply();// will be false if background updates are allowed



            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if(auth.getCurrentUser() != null) {
                    checkBilling();
                    SharedPreferences pref = getSharedPreferences("shared preferences", MODE_PRIVATE);
                    if (pref.getInt("radius", 0) > 160) {
                        String name = auth.getCurrentUser().getUid();
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("superUsers/" + name);
                        mDatabase.setValue("t");
                    }
                    }
                }
            };
            new Thread(runnable).start();


        location_setting = pref.getBoolean("locSetting",true);
        Log.d(TAG, "onCreate: about to load ad");
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                mAdView = new InterstitialAd(MainActivity.this);
                mAdView.setAdUnitId(code); //getString(R.string.interstitial_ad_unit_id)
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
                Log.d(TAG, "onCreate: isloading: " + mAdView.isLoading());

                mAdVids = new InterstitialAd(MainActivity.this);
                mAdVids.setAdUnitId(vidCode);
                adRequest = new AdRequest.Builder().build();
                mAdVids.loadAd(adRequest);

                mainBannerAd = findViewById(R.id.adView);
                adRequest = new AdRequest.Builder().build();
                mainBannerAd.loadAd(adRequest);

                mainBannerAd.setAdListener(new AdListener(){
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                    }

                    @Override
                    public void onAdFailedToLoad(int i) {
                        super.onAdFailedToLoad(i);
                    }

                    @Override
                    public void onAdLeftApplication() {
                        super.onAdLeftApplication();
                    }

                    @Override
                    public void onAdOpened() {
                        super.onAdOpened();
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                    }
                });

            }
        });


/*
        findViewById(R.id.logout).setOnClickListener(this);
        final Button groups = (Button) findViewById(R.id.add_info);
        groups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable()) {
                    Intent groupIntent = new Intent(MainActivity.this, groups.class);
                    startActivity(groupIntent);
                }else{
                    Toast.makeText(MainActivity.this,"Check location settings and/or internet connection ",Toast.LENGTH_LONG).show();
            }
            }
        });
*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId()){
            case R.id.edit_groups:
                if(isNetworkAvailable()) {
                    final Intent groupIntent = new Intent(MainActivity.this, groups.class);
                    Log.d(TAG, "onOptionsItemSelected: level 1 ");
                    if(mAdVids != null){
                        Log.d(TAG, "onOptionsItemSelected: level 2 ");
                        if(mAdVids.isLoaded()){
                            Log.d(TAG, "onOptionsItemSelected: level 3 ");
                            mAdVids.show();
                            Log.d(TAG, "onNoteClick: ad was shown");
                            mAdVids.setAdListener(new AdListener(){
                                @Override
                                public void onAdClosed() {
                                    startActivity(groupIntent);
                                    mAdVids.loadAd(new AdRequest.Builder().build());
                                }
                            });
                        }else{
                            Log.d(TAG, "onOptionsItemSelected: level 4 ");
                            startActivity(groupIntent);
                        }
                    }else{
                        Log.d(TAG, "onOptionsItemSelected: level 5 ");
                        startActivity(groupIntent);
                    }
                }else{
                    Toast.makeText(MainActivity.this,"Check location settings and/or internet connection ",Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.LocationSetting:
                SharedPreferences pref = getSharedPreferences("shared preferences",MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                if(location_setting){
                    editor.putBoolean("locSetting", false).apply();
                    location_setting = false;
                    Toast.makeText(MainActivity.this,"Location updates have been turned off",Toast.LENGTH_LONG).show();

                    if (auth.getCurrentUser() != null) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("uIdL");  //ref holds the location where the data will be stored
                        geoFire = new GeoFire(ref);
                        Log.d(TAG, "onOptionsItemSelected: mFusedLocationClient.remove was called");
                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        mFusedLocationClient.removeLocationUpdates(pendingIntent);
                        geoQuery.removeAllListeners();
                        geoFire.removeLocation(auth.getCurrentUser().getUid());
                        removeAllProfiles();
                        adapter.notifyDataSetChanged();
                    }
                }else{
                    editor.putBoolean("locSetting", true).apply();
                    location_setting = true;
                    fetchCount = 0;
                    updateLocation();
                    //Toast.makeText(MainActivity.this,"Location updates have been turned on",Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.upgrades:
                if(isNetworkAvailable()) {
                    Intent purchaseIntent = new Intent(MainActivity.this, purchases.class);
                    startActivity(purchaseIntent);
                }else{
                    Toast.makeText(MainActivity.this,"Check location settings and/or internet connection ",Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
           IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK){
                Log.e("AUTH", "AUTHENTICATED "+ Objects.requireNonNull(auth.getCurrentUser()).getDisplayName());
                if(adapter == null){
                    Log.d(TAG, "onActivityResult: adapter was null so we called initRecyclerView");
                    initRecyclerView();
                }
                if(geoQuery ==null){
                    Log.d(TAG, "onActivityResult: geoQuery was null so we called updateLocation");
                    updateLocation();
                }

                //-----------------------------------------------------------------------------------
                Thread uploadUserAccount = new Thread(){
                    @Override
                    public void run() {

                        final String uid = auth.getCurrentUser().getUid();
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("users/"+ uid);
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //dataSnapshot.getValue() doesnt download everything
                                if(dataSnapshot.getValue() == null){
                                    // this checks if the user is in users and add them if they are not
                                    //only when they log in
                                    mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
                                    Map<String, Object> userUpdate = new HashMap<String, Object>();
                                    userUpdate.put(uid, "true");
                                    mDatabase.updateChildren(userUpdate);
                                    // need to create a users account for the first time ----------------------------------------------------------------------------------------
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        mDatabase = FirebaseDatabase.getInstance().getReference().child("uInfo/"+ uid);
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //dataSnapshot.getValue() doesnt download everything
                                String pic = Objects.requireNonNull(auth.getCurrentUser().getPhotoUrl()).toString();
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                String facebookUserId = "";
                                for(UserInfo profile : user.getProviderData()) {
                                    // check if the provider id matches "facebook.com"
                                    if(FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                                        facebookUserId = profile.getUid();
                                    }
                                }
                                if(!facebookUserId.equals("")) {
                                    pic = "https://graph.facebook.com/" + facebookUserId + "/picture?type=large";
                                }

                                Log.d(TAG, "onDataChange: pic new---" + pic);
                                if (dataSnapshot.child("pic").getValue() == null || !(Objects.requireNonNull(dataSnapshot.child("pic").getValue()).toString().equals(pic))) {
                                    // this checks if the user is in users and add them if they are not
                                    //only when they log in
                                    Log.d(TAG, "onDataChange: pic from snapshot" + dataSnapshot.child("pic").getValue().toString());
                                    mDatabase = FirebaseDatabase.getInstance().getReference().child("uInfo/"+ uid +"/pic");
                                    mDatabase.setValue(pic);

                                }
                                String fullName = auth.getCurrentUser().getDisplayName();
                                if(dataSnapshot.child("name").getValue()== null || !(dataSnapshot.child("name")).getValue().toString().equals(fullName)){
                                    mDatabase = FirebaseDatabase.getInstance().getReference().child("uInfo/"+ uid +"/name");
                                    mDatabase.setValue(fullName);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        }

                    };
                uploadUserAccount.start();


               // ----------------------------------------------------------------------------------------------------------------------------------------

                //Toast.makeText(getApplicationContext(),"this is working",Toast.LENGTH_LONG);

            }
            else{
                //User not authenticated
                Log.d("Auth", "NOT AUTHENTICATED ERROR CODE:1");
                Toast.makeText(MainActivity.this,"NOT AUTHENTICATED",Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),"this is working",Toast.LENGTH_LONG);
                AuthPlusUpdate();
            }
        }
    }


    private void AuthPlusUpdate(){
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Log.d("AUTH", auth.getCurrentUser().getEmail());

            //used for giving profile.java this information
            initRecyclerView();

            SharedPreferences pref =  getSharedPreferences("shared preferences", MODE_PRIVATE);

            if(pref.getString("s1", null) == null){
                Log.d(TAG, "AuthPlusUpdate: there was no s1 variable in shared pref");
                getUserGroupsAndAddToSharedPref();
            }

        } else {
            // not signed in

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers).setLogo(R.mipmap.ic_launcher).
                            setTosAndPrivacyPolicyUrls("https://docs.google.com/document/d/14XE8nQ1rNUEVhYXsazFtnV9b_ceCrljYY5-G7cGYJ7w/edit?usp=sharing",
                                    "https://www.freeprivacypolicy.com/privacy/view/cc3393baedcc06ff61354cde76c89208")
                            .build(), //Arrays.asList(
                    // the stuff below is replaced by providers
                    //new AuthUI.IdpConfig.FacebookBuilder().build(),
                     //new AuthUI.IdpConfig.GoogleBuilder().build())).build(),
                    RC_SIGN_IN);

        }

    }


    private void initRecyclerView(){
         // source -------   https://www.youtube.com/watch?v=Vyqz_-sJGFk&t=286s
        Log.d(TAG, "initRecyclerView:  init recycler view.");
        RecyclerView recyclerView = findViewById(R.id.nearbyPpl);
        Log.d(TAG, "initRecyclerView: mnames = " + mUserNames);
        adapter = new RecyclerViewAdapter(matching,mImageUrls,mUserNames,mSuperOne,mSuperTwo,this, GroupArraylist, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Log.d(TAG, "initRecyclerView: finished");
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)

                        .setTitle("give location permission")

                        .setMessage("give permission message")

                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override

                            public void onClick(DialogInterface dialogInterface, int i) {

                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                            }

                        })

                        .create()

                        .show();

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

        }

    }

    @Override
    public void onClick(View v) {
        /*
        if(v.getId() == R.id.logout){
            // this button makes it so that the person is offline
            //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("uIdL");  //ref holds the location where the data will be stored
            //geoFire = new GeoFire(ref);
            //geoFire.removeLocation(auth.getCurrentUser().getUid()); // this might need to be need to be added in future
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("uIdL");  //ref holds the location where the data will be stored
            geoFire = new GeoFire(ref);
            if (auth.getCurrentUser() != null) {
                geoFire.removeLocation(auth.getCurrentUser().getUid());

            }

            // this used to allow signing out of the app
            AuthUI.getInstance().signOut(this). addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d("AUTH", "USER LOGGED OUT");
                    if(mFusedLocationClient != null) {
                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                    }


                    final SharedPreferences sharedPreferences =
                            getSharedPreferences("shared preferences", MODE_PRIVATE);         //copy the groups from shared pref into arraylist

                    Gson gson = new Gson();
                    String json = sharedPreferences.getString("groups", null);
                    Type type = new TypeToken<ArrayList<String>>(){}.getType();
                    mNames = gson.fromJson(json, type);

                    SharedPreferences.Editor editor = sharedPreferences.edit();                     // clear the shared pref
                    editor.clear().apply();

                    json = gson.toJson(mNames);                                                     // add the groups back into shared pref
                    editor.putString("groups", json);
                    editor.apply();


                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    //dialog
                }
            });
        }*/
    }


    // the order of the functions matter.

    LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {

            for (Location location : locationResult.getLocations()) {

                if (getApplicationContext() != null) {

                    if(isLocationEnabled(MainActivity.this) && isNetworkAvailable()) {
                        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                            if(location_setting) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("uIdL");  //ref holds the location where the data will be stored
                                geoFire = new GeoFire(ref);
                                mlastlocation = location;
                                fetchUsers();

                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();//userID with Location info

                                Log.d(TAG, "grabbing users and other information ------------------------- ");

                                geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            }else{
                                Toast.makeText(MainActivity.this,"Location update is off",Toast.LENGTH_SHORT).show();
                            }
                            // geolocation stores lat in 0 and
                            // store long in 1 inside of the database
                        }else{
                            Log.d(TAG, "onLocationResult: auth.getCurrentUser() == null");
                        }
                    }else{
                        Toast.makeText(MainActivity.this,"Check location settings and/or internet connection ",Toast.LENGTH_LONG).show();
                    }

                }

            }
        }
    };



    private void updateLocation(){
        if(isLocationEnabled(this) && isNetworkAvailable()) {
            Log.d(TAG, "updateLocation: has been called");
            checkLocationPermission();
            mlocationRequest = LocationRequest.create();
            mlocationRequest.setInterval(Interval*20); // updates every 10 min
            // if we have travelled 20 meters
            mlocationRequest.setFastestInterval(fastestInterval*20);
            //mlocationRequest.setSmallestDisplacement(20);
            mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.requestLocationUpdates(mlocationRequest, mLocationCallback, Looper.myLooper());
        }else{
            Toast.makeText(MainActivity.this,"Check location settings and/or internet connection ",Toast.LENGTH_LONG).show();
        }
    }

    private void fetchUsers(){
        if(auth.getCurrentUser() != null) {
            fetchCount++;
            if (fetchCount == 1) {
                geoQuery = geoFire.queryAtLocation(new GeoLocation(mlastlocation.getLatitude(), mlastlocation.getLongitude()), (radius * 0.001)); //radius in km

                final String localId = auth.getCurrentUser().getUid();
                geoQuery.removeAllListeners();                                              //cleared all listeners here
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {

                        if (!(localId.equals(key))) { // dont add the current user to the list
                            updateArrays(key);
                            Log.d(LOG_TAG, "onKeyEntered: " + key);
                        }
                    }

                    @Override
                    public void onKeyExited(String key) {

                        if (!(localId.equals(key))) { // dont add the current user to the list
                            removeAndUpdate(key);
                            Log.d(LOG_TAG, "onKeyExited: " + key);
                        }

                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {
                        // possible call for initRecyclerView
                        //geoReadySet = true;
                    }


                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                        Log.d(TAG, "onGeoQueryError: Errrrror!!!! details:  " + error.getDetails() + " ....message: " + error.getMessage());
                    }
                });

                SharedPreferences pref =
                        getSharedPreferences("shared preferences", MODE_PRIVATE);         //copy the groups from shared pref into arraylist
                radius = pref.getInt("radius", basevalue);
                Toast.makeText(this, "location updates are on and \n radius = " + radius + "m", Toast.LENGTH_SHORT).show();


            }
        }
    }


    private void removeAndUpdate(String name){
        Log.d(TAG, "removeAndUpdate: removing user"); // i should probably make this an object at some point
        int position = mUserIds.indexOf(name);   //indexOf() returns the index of the first occurrence of the element in this list, or -1 if this list does not contain the element.
        mUserIds.remove(position);
        mUserNames.remove(position);
        mSuperOne.remove(position);
        mSuperTwo.remove(position);
        mImageUrls.remove(position);
        matching.remove(position);
        GroupArraylist.remove(position);
        groupStatus.remove(position);
        numberOfPeopleUpdatedToTheSystem--;
        adapter.notifyItemRemoved(position);
    }

    private void removeAllProfiles(){
        mUserNames.clear();
        mSuperOne.clear();
        mSuperTwo.clear();
        matching.clear();
        mImageUrls.clear();
        mUserIds.clear();
        GroupArraylist.clear();
        groupStatus.clear();
        numberOfPeopleUpdatedToTheSystem = 0;

    }

    private void updateArrays(final String name){         //name needs to be the userId not actual name
                                                    //only updates the array if not already inside of the Array
        int isInArr = mUserIds.indexOf(name);
        Log.d(TAG, "updateArrays: checking if the user has already been added ");
        if(isInArr == -1){
            mUserIds.add(name);
            Log.d(TAG, "updateArrays: the user wasnt in the array: " + name);
            mDatabase = FirebaseDatabase.getInstance().getReference().child("uInfo/" + name);    // should be child("uInfo"+ name);
            // this is where i get the users information to add to the arrays.

            //make this a thread -------------------------------------------------------------------------------------------------------------------------------------

            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {          // update super1, super2, mUserNames, pic
                    String superone3 = "E";
                    String supertwo3 = "E";
                    String pic3 = "https://lh3.googleusercontent.com/-EQsUpT5V1Y4/AAAAAAAAAAI/AAAAAAAAAAA/ACHi3rc2uiMFKtst7pGn-eCzPh0ltQEEhA/s96-c/photo.jpg";
                    String actualName = "E";     // someone might have the name E
                    SharedPreferences pref = getSharedPreferences("shared preferences",MODE_PRIVATE);
                    int matchCount = 0;

                    if(dataSnapshot.child("pic").getValue() != null) {
                        pic3 = dataSnapshot.child("pic").getValue().toString();
                        Log.d(TAG, "onDataChange: pic " + pic3);
                    }
                    mImageUrls.add(pic3);

                    if(dataSnapshot.child("name").getValue() != null) {
                        Log.d(TAG, "onDataChange: part way downloading user information");
                        actualName = dataSnapshot.child("name").getValue().toString();
                    }
                    mUserNames.add(actualName);
                    if(dataSnapshot.child("s1").getValue() != null) {
                        superone3 = dataSnapshot.child("s1").getValue().toString();
                    }
                    if(dataSnapshot.child("s2").getValue() != null) {
                        supertwo3 = dataSnapshot.child("s2").getValue().toString();
                    }
                    if(dataSnapshot.child("groups").getValue() != null){       // getValue can return null but getKey will return the string of the child in this case groups
                        GroupArraylist.add(new ArrayList<String>());
                        groupStatus.add(new ArrayList<String>());
                        int groupSize = GroupArraylist.size()-1;
                        for(DataSnapshot ds : dataSnapshot.child("groups").getChildren()){
                            String groupName, status;
                            groupName = ds.getKey();
                            status = ds.getValue().toString();
                            GroupArraylist.get(groupSize).add(groupName);
                            groupStatus.get(groupSize).add(status);


                            if(superone3.equals(groupName) && status.equals("f")){              //check if the super is private
                                if(pref.getString(superone3, "Ntng").equals("Ntng")){  // check if the current user has the super in a group
                                    superone3 = "E";                                            // if they dont have the group make it empyt
                                }
                            }
                            if(supertwo3.equals(groupName) && status.equals("f")){
                                if(pref.getString(supertwo3, "Ntng").equals("Ntng")){
                                    supertwo3 = "E";
                                }
                            }
                            boolean match = !(("Ntg").equals(pref.getString(groupName, "Ntg")));
                            Log.d(TAG, "onDataChange: match: " + match);
                            if(match){
                                // add to an arraylist the information and pass it to the recyclerview and make color change.
                                matchCount++;
                            }

                            //need to find out how many things you have in common with the the people around you.


                        }
                        Log.d(TAG, "onDataChange: match count: " + matchCount);
                        matching.add(matchCount);
                        Log.d(TAG, "onDataChange: matching size: " + matching.size());
                    }else {
                        matching.add(0);
                        GroupArraylist.add(new ArrayList<String>());
                        groupStatus.add(new ArrayList<String>());
                        int groupSize = GroupArraylist.size()-1;
                        GroupArraylist.get(groupSize).add("E");
                        groupStatus.get(groupSize).add("t");
                        Log.d(TAG, "onDataChange: almost done loading persons information");
                    }

                    mSuperOne.add(superone3);
                    mSuperTwo.add(supertwo3);
                    numberOfPeopleUpdatedToTheSystem++;
                    Log.d(TAG, "onDataChange: finished loading all information for this person");
                    //if(recyclerViewHasBeenInitiated){

                        adapter.notifyItemInserted(numberOfPeopleUpdatedToTheSystem-1);


                   // }
                    //database updates the system too slow so i wait unit the counts are the same before i start recyclerview
                    /*if((!recyclerViewHasBeenInitiated) && numberOfPeopleUpdatedToTheSystem == numberOfPeopleInArea && geoReadySet){

                        Log.d(TAG, "onDataChange: $$$$$$$$$ onready has been called and the system has caught up");
                        geoReadySet= false;
                        initRecyclerView();
                        recyclerViewHasBeenInitiated = true;
                    }
                   */
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: when getting info about the people in the area - error: " + databaseError.getDetails());
                }
            });
        }else{
            Log.d(TAG, "updateArrays: the user was already in the array");
        }

    }


    @Override
    public void onNoteClick(int position) {
        if(screenWidth == 0) {
            screenWidth = findViewById(R.id.main_activity_layout).getWidth();
            Log.d(TAG, "onNoteClick: screenWidth = " + screenWidth);
        }
        ArrayList<String> tempGroup = new ArrayList<String>();
        Log.d(TAG, "onNoteClick: clicked" + mUserNames.get(position));
        // since this is public it will be called from recyclerViewAdapter when
        //something is clicked. position will be the item clicked
        //mUserNames.get(position);
        SharedPreferences pref = getSharedPreferences("shared preferences",MODE_PRIVATE);

        for(int i=0; i < GroupArraylist.get(position).size();i++){
            String compareGroup = GroupArraylist.get(position).get(i);   // 'f' means that it is private 't' means it is public
            if(groupStatus.get(position).get(i).equals("t") ||  !pref.getString(compareGroup, "Ntng").equals("Ntng")){  // if i dont have group i will get Ntng
                tempGroup.add(GroupArraylist.get(position).get(i));
            }
        }
        Log.d(TAG, "onNoteClick: screenWidth = " + screenWidth);
        final Intent intent = new Intent(this, profile.class);
        intent.putExtra("screenWidth", screenWidth);
        intent.putExtra("name", mUserNames.get(position));
        intent.putExtra("id", mUserIds.get(position));
        intent.putExtra("currentUser", auth.getCurrentUser().getUid());
        intent.putExtra("groups", tempGroup);   // for some reason this doesn't let pass List only Arraylist
        intent.putExtra("pic", mImageUrls.get(position));
        if(mAdView != null){
        if(mAdView.isLoaded()){
            mAdView.show();
            Log.d(TAG, "onNoteClick: ad was shown");
            Log.d(TAG, "onCreate: about to load ad");
            mAdView.setAdListener(new AdListener(){
                @Override
                public void onAdClosed() {
                    startActivity(intent);
                    mAdView.loadAd( new AdRequest.Builder().build());
                }
            });
        }else{
            Log.d(TAG, "onNoteClick: ad is not loaded");
            startActivity(intent);
        }
        }else{
            Log.d(TAG, "onNoteClick: mAdview is null");
            startActivity(intent);}
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            screenWidth = findViewById(R.id.main_activity_layout).getWidth();
            Log.d(TAG, "focus changed screenWidth = " + screenWidth);
            if (auth.getCurrentUser() != null) {
                Log.d(TAG, "onWindowFocusChanged: updateLocation was called");
                updateLocation();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mFusedLocationClient != null && !location_setting){
            mFusedLocationClient.removeLocationUpdates(pendingIntent);
            mFusedLocationClient = null;
            mlocationRequest = null;
            mLocationCallback = null;
        }

        if(stickyService != null){
        stopService(stickyService);
        }

        Log.d(TAG, "onDestroy: says something");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(auth.getCurrentUser() != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            if(geoQuery != null){
                geoQuery.removeAllListeners();
            }
            removeAllProfiles();
            if(location_setting) {
                Log.d(TAG, "start of onstop");
                // for some reason onDestory in not called it this service is activated
                stickyService = new Intent(this, isAppDead.class);
                startService(stickyService);
                SharedPreferences pref = getSharedPreferences("shared preferences",MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                checkLocationPermission();
                mlocationRequest = LocationRequest.create();
                mlocationRequest.setFastestInterval(1000 * 60 * 7);
                mlocationRequest.setInterval(1000 * 60 * 8); // updates every 10 min
                mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mlocationRequest.setExpirationDuration(1000 * 60 * 60 * 12); //1000 * 60 * 60 * 6
                // looks like you can only update every 10 min while in the background
                final int locationUpdateRC = 0;
                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                Intent intent = new Intent(this, MyLocationUpdateReceiver.class);
                pendingIntent = PendingIntent.getBroadcast(this, locationUpdateRC, intent, flags);
                Log.d(TAG, "requestingLocationUpdates in the background ---------");
                mFusedLocationClient.requestLocationUpdates(mlocationRequest, pendingIntent);
                editor.putBoolean("backgroundloc", false).apply(); // will be false if background  updates are allowed
            }
        }
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        AuthPlusUpdate();
        fetchCount = 0;

        Interval = 1000;
        SharedPreferences pref = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("backgroundloc", true).apply(); // will be false if background updates are allowed
        editor.putInt("backgroundCount", 0).apply();
        location_setting = pref.getBoolean("locSetting", true);
        fastestInterval = 500;
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(pendingIntent);
        }

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    checkBilling();
                    SharedPreferences pref = getSharedPreferences("shared preferences", MODE_PRIVATE);
                    if (pref.getInt("radius", 0) > 160) {
                        String name = auth.getCurrentUser().getUid();
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("superUsers/" + name);
                        mDatabase.setValue("t");
                    }
                }
            };
        new Thread(runnable).start();

        updateLocation();
        Log.d(TAG, "onRestart" );

    }

    private void getUserGroupsAndAddToSharedPref(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("uInfo/" + auth.getCurrentUser().getUid());    // should be child("uInfo"+ name);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {          // update super1, super2, mUserNames, pic
                String superone3 = "E";
                String supertwo3 = "E";
                //String pic3 = "https://lh3.googleusercontent.com/-EQsUpT5V1Y4/AAAAAAAAAAI/AAAAAAAAAAA/ACHi3rc2uiMFKtst7pGn-eCzPh0ltQEEhA/s96-c/photo.jpg";
                //String actualName = "E";     // someone might have the name E
                if(dataSnapshot.child("s1").getValue() != null) {
                    superone3 = dataSnapshot.child("s1").getValue().toString();
                }

                SharedPreferences pref = getSharedPreferences("shared preferences",MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                editor.putString("s1", superone3);

                if(dataSnapshot.child("s2").getValue() != null) {
                    supertwo3 = dataSnapshot.child("s2").getValue().toString();
                }

                editor.putString("s2", supertwo3);


                if(dataSnapshot.child("groups").getValue() != null){       // getValue can return null but getKey will return the string of the child in this case groups
                    for(DataSnapshot ds : dataSnapshot.child("groups").getChildren()){
                        editor.putString(ds.getKey(),ds.getValue().toString());
                    }
                }
                editor.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getDetails());
            }
        });
    }

    private void addBasicInformation(){
        Thread uploadUserAccount = new Thread(){
            @Override
            public void run() {

                final String uid = auth.getCurrentUser().getUid();
                mDatabase = FirebaseDatabase.getInstance().getReference().child("users/"+ uid);
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //dataSnapshot.getValue() doesnt download everything
                        if(dataSnapshot.getValue() == null){
                            // this checks if the user is in users and add them if they are not
                            //only when they log in
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
                            Map<String, Object> userUpdate = new HashMap<String, Object>();
                            userUpdate.put(uid, "true");
                            mDatabase.updateChildren(userUpdate);
                            // need to create a users account for the first time ----------------------------------------------------------------------------------------
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mDatabase = FirebaseDatabase.getInstance().getReference().child("uInfo/"+ uid);
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //dataSnapshot.getValue() doesnt download everything
                        String pic = auth.getCurrentUser().getPhotoUrl().toString();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String facebookUserId = "";
                        for(UserInfo profile : user.getProviderData()) {
                            // check if the provider id matches "facebook.com"
                            if(FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                                facebookUserId = profile.getUid();
                            }
                        }
                        if(!facebookUserId.equals("")) {
                            pic = "https://graph.facebook.com/" + facebookUserId + "/picture?type=large";
                        }
                        Log.d(TAG, "onDataChange: " + pic);
                        if (dataSnapshot.child("pic").getValue() == null || !(dataSnapshot.child("pic").getValue().toString().equals(pic))) {
                            // this checks if the user is in users and add them if they are not
                            //only when they log in
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("uInfo/"+ uid +"/pic");
                            mDatabase.setValue(pic);
                        }
                        String fullName = auth.getCurrentUser().getDisplayName();
                        if(dataSnapshot.child("name").getValue()== null || !(dataSnapshot.child("name")).getValue().toString().equals(fullName)){
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("uInfo/"+ uid +"/name");
                            mDatabase.setValue(fullName);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

        };
        uploadUserAccount.start();

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
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void hasUserInfoChanged(){
        Log.d(TAG, "hasUserInfoChanged: in here");
        Thread updateUserInfo = new Thread(){
            @Override
            public void run() {


                final String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                mDatabase = FirebaseDatabase.getInstance().getReference().child("uInfo/" + uid);
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //dataSnapshot.getValue() doesnt download everything
                        String pic = Objects.requireNonNull(auth.getCurrentUser().getPhotoUrl()).toString();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String facebookUserId = "";
                        for (UserInfo profile : user.getProviderData()) {
                            // check if the provider id matches "facebook.com"
                            if (FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                                facebookUserId = profile.getUid();
                            }
                        }
                        if (!facebookUserId.equals("")) {
                            pic = "https://graph.facebook.com/" + facebookUserId + "/picture?type=large";
                        }

                        Log.d(TAG, "onDataChange: pic new---" + pic);
                        if (dataSnapshot.child("pic").getValue() == null || !(Objects.requireNonNull(dataSnapshot.child("pic").getValue()).toString().equals(pic))) {
                            // this checks if the user is in users and add them if they are not
                            //only when they log in
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("uInfo/" + uid + "/pic");
                            mDatabase.setValue(pic);

                        }
                        String fullName = auth.getCurrentUser().getDisplayName();
                        if (dataSnapshot.child("name").getValue() == null || !(dataSnapshot.child("name")).getValue().toString().equals(fullName)) {
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("uInfo/" + uid + "/name");
                            mDatabase.setValue(fullName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                }

        };
        updateUserInfo.start();
    }
    private void checkBilling(){
        SharedPreferences pref =
                getSharedPreferences("shared preferences", MODE_PRIVATE);         //copy the groups from shared pref into arraylist

        SharedPreferences.Editor mEditor = pref.edit();
        mEditor.putInt("radius", basevalue).apply();
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    if(billingClient.isReady()){
                        Log.d(TAG, "onBillingSetupFinished: lvl 1");
                        //billingClient.queryPurchases(BillingClient.SkuType.SUBS);
                        onPurchasesUpdated(billingResult, billingClient.queryPurchases(BillingClient.SkuType.SUBS).getPurchasesList());
                    }else{
                        Log.d(TAG, "onBillingSetupFinished: lvl 4");
                        Toast.makeText(MainActivity.this, "Billing client is not ready", Toast.LENGTH_SHORT).show();

                    }
                    Log.d(TAG, "onBillingSetupFinished: lvl 5");
                }else{
                    Log.d(TAG, "onBillingSetupFinished: lvl 6");
                    Toast.makeText(MainActivity.this, ""+billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingSetupFinished: lvl 7");
                Toast.makeText(MainActivity.this, "You were disconnected from billing",Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void handleHistoryPurchase(Purchase purchaseHistoryRecord){
        Log.d(TAG, "handleHistoryPurchase: " + purchaseHistoryRecord.getSku());
        Log.d(TAG, "onBillingSetupFinished: lvl 9");
        if (purchaseHistoryRecord.getSku().equals("100m")) {
            SharedPreferences pref =
                    getSharedPreferences("shared preferences", MODE_PRIVATE);         //copy the groups from shared pref into arraylist

            SharedPreferences.Editor mEditor = pref.edit();
            mEditor.putInt("radius", 100).apply();
        }
        if (purchaseHistoryRecord.getSku().equals("50m")) {
            SharedPreferences pref =
                    getSharedPreferences("shared preferences", MODE_PRIVATE);         //copy the groups from shared pref into arraylist

            SharedPreferences.Editor mEditor = pref.edit();
            mEditor.putInt("radius", 50).apply();
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        Log.d(TAG, "onPurchasesUpdated: lvl 10");
        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases!= null){
            Log.d(TAG, "onPurchasesUpdated: purchases size =" + purchases.size());
            for(Purchase purchase : purchases){
                Log.d(TAG, "onPurchasesUpdated: " + purchase.getSku());
                handleHistoryPurchase(purchase);
            }
        }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.d(TAG, "onPurchasesUpdated: " + billingResult.getResponseCode());
        }
    }
}