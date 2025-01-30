package com.fireflyglobe.kmamo.click;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.ArrayList;

public class groups extends AppCompatActivity {

    private ArrayList<String> mNames = new ArrayList<>();
    private static final String TAG = "groups";
    private DatabaseReference mDatabase2;
    private int groupcount;
    groupAdapter adapter;

    private String uId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        FirebaseAuth auth;
        Log.d(TAG, "onCreate: started and getting groups");
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            uId = auth.getCurrentUser().getUid();
            getGroups();
        }else{
            Intent intent = new Intent(groups.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_action_bar, menu);

        MenuItem searchItem = menu.findItem(R.id.group_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    private void getGroups() {
        mDatabase2 = FirebaseDatabase.getInstance().getReference().child("groupCount");
        //check if list exists
        mDatabase2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: checking if the count is the same #");
                groupcount = Integer.parseInt(dataSnapshot.getValue().toString());
                //check to see if i have the  sharedPreferences called groups
                final SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
                Gson gson = new Gson();
                String json = sharedPreferences.getString("groups", null);
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                mNames = gson.fromJson(json, type);
                Log.d(TAG, "onDataChange: %%%%% groupcount: " + groupcount);
                //Log.d(TAG, "onDataChange: %%%%% nNames.size()" + mNames.size());
                if (mNames == null || (groupcount) != mNames.size()) {
                    // if mNames wasn't filled with it will be null so have to redefine it and fill it in
                    mNames = new ArrayList<>();
                    mDatabase2 = FirebaseDatabase.getInstance().getReference().child("groups");
                    mDatabase2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.e(TAG, "onDataChange: " + dataSnapshot.getChildrenCount());
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                mNames.add(ds.getKey());
                            }
                            //saving the data if not already saved.
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            Gson gson = new Gson();
                            String json = gson.toJson(mNames);
                            editor.putString("groups", json);
                            editor.apply();
                            initGroupRView();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled: " + databaseError.getDetails());
                        }

                    });
                } else {
                    initGroupRView();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void initGroupRView() {
        RecyclerView recyclerView2 = (RecyclerView) findViewById(R.id.groupsRV);
        adapter = new groupAdapter(mNames, this,uId);
        recyclerView2.setAdapter(adapter);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));

    }

}