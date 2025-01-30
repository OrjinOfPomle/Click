package com.fireflyglobe.kmamo.click;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

// <needs to match the class >
public class groupAdapter extends RecyclerView.Adapter<groupAdapter.ViewHolder>  implements Filterable{

    private static final String TAG = "groupAdapter";
    private ArrayList<String> mGroupNames;
    private Context mGroupContext;
    private FirebaseAuth auth2 = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase2;
    private String uId;
    private int allowedCountP = 4; // set it to 5 for now
    private int allowedCountForGroups =7;
    private ArrayList<String> mGroupNamesFull;

    public groupAdapter(ArrayList<String> mGroupNames, Context mGroupContext,String uId) {
        this.mGroupNames = mGroupNames;
        mGroupNamesFull = new ArrayList<>(mGroupNames);
        this.mGroupContext = mGroupContext;
        this.uId = uId;
    }


    SharedPreferences pref;
    private SharedPreferences.Editor mEditor;



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_group_layout, viewGroup, false); // R.layout.single_group_layout is
                                                                                                               // single_group_layout.xml not single_group from inside the file
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder1, int i) {
        viewHolder1.includeGroup.setOnCheckedChangeListener(null);
        viewHolder1.superLike.setOnCheckedChangeListener(null);
        viewHolder1.makePrivate.setOnCheckedChangeListener(null);

        pref = mGroupContext.getSharedPreferences("shared preferences",MODE_PRIVATE);
        mEditor = pref.edit();
        /*if(i == 0) {            // used to reset Private count to 0;
            mEditor.putString("pCount", "0");
            mEditor.putString("gCount", "0");
            mEditor.commit();
        }*/
        String groupName = mGroupNames.get(i);
        viewHolder1.includeGroup.setText(groupName);
        String selected = pref.getString(groupName, "E");



        if(!(selected.equals("E"))){  // if the group is in shared preferences then
            viewHolder1.includeGroup.setChecked(true);
            if (selected.equals("f")) {
                viewHolder1.makePrivate.setChecked(true);

            }else{
                viewHolder1.makePrivate.setChecked(false);
            }

            if(getCurrentSuper1().equals(groupName))  // make this more efficient by storing in a variable
            {viewHolder1.superLike.setChecked(true);
                Log.d(TAG, "onBindViewHolder: %%%% true s1 =" + groupName);
            }
            else{
                if(getCurrentSuper2().equals(groupName)) {
                viewHolder1.superLike.setChecked(true);
                Log.d(TAG, "onBindViewHolder: %%%% true s2 =" + groupName);

                }else{
                viewHolder1.superLike.setChecked(false);
                Log.d(TAG, "onBindViewHolder: %%%% false s2 =" + groupName);
                }
            }


        }else{
            viewHolder1.includeGroup.setChecked(false);
            viewHolder1.makePrivate.setChecked(false);
            viewHolder1.superLike.setChecked(false);
        }


        final ViewHolder viewHolder = viewHolder1;
        final int b = i; //b need to be a final
        //might need to do this is a thread ------------------------------------
        viewHolder.includeGroup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(viewHolder.includeGroup.isPressed()  ) {
                    if(isNetworkAvailable()) {
                        if (!isChecked) {
                            //remove from the database

                            FirebaseDatabase.getInstance().getReference()
                                    .child("uInfo/" + uId + "/groups/" + mGroupNames.get(b))
                                    .removeValue();
                            Log.d(TAG, "onCheckedChanged: removed group from database");
                            Log.d(TAG, "onBindViewHolder: group count before update: " + getCurrentGroupCount());
                            mEditor.putString("gCount", Integer.toString((getCurrentGroupCount() - 1)));
                            mEditor.apply();
                            Log.d(TAG, "onBindViewHolder: group count after update: " + getCurrentGroupCount());

                            if (viewHolder.makePrivate.isChecked()) {
                                Log.d(TAG, "onBindViewHolder: private count one: =========== " + getCurrentCount());
                                mEditor.putString("pCount", Integer.toString((getCurrentCount() - 1)));
                                mEditor.apply();
                                viewHolder.makePrivate.setChecked(false);
                                Log.d(TAG, "onBindViewHolder: private count two: ========= " + getCurrentCount());
                            }
                            if (viewHolder.superLike.isChecked()) {


                                Log.d(TAG, "onCheckedChanged: we got this far :supers:");
                                // make the database say it is empty and make the correct shared pref say it is empty
                                Log.d(TAG, "onCheckedChanged: \n s1--- before-----: " + getCurrentSuper1());
                                if (getCurrentSuper1().equals(mGroupNames.get(b))) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("uInfo/" + uId + "/s1").setValue("E");
                                    mEditor.putString("s1", "E");
                                    mEditor.commit();
                                    viewHolder.superLike.setChecked(false);
                                    Log.d(TAG, "onCheckedChanged: group was deleted while s1 was on");
                                }
                                Log.d(TAG, "onCheckedChanged: \n s1--- after-----: " + getCurrentSuper1());
                                Log.d(TAG, "onCheckedChanged: \n s2----before----: " + getCurrentSuper2());

                                if (getCurrentSuper2().equals(mGroupNames.get(b))) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("uInfo/" + uId + "/s2").setValue("E");
                                    mEditor.putString("s2", "E");
                                    mEditor.commit();
                                    viewHolder.superLike.setChecked(false);
                                }
                                Log.d(TAG, "onCheckedChanged: \n s2----after----: " + getCurrentSuper2());
                            }
                            mEditor.remove(mGroupNames.get(b));
                            mEditor.apply();
                        }

                    if (isChecked) {
                        if(getCurrentGroupCount() < allowedCountForGroups) {
                            // add to the database
                            Log.d(TAG, "onCheckedChanged: added to the database");


                            mDatabase2 = FirebaseDatabase.getInstance().getReference().child("uInfo/" + uId + "/groups");
                            mDatabase2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Map<String, Object> updategroup = new HashMap<>(); // 'f' means that it is private 't' means it is public
                                    updategroup.put(mGroupNames.get(b), "t");           //this can be optimized
                                    mDatabase2.updateChildren(updategroup);
                                    mEditor.putString(mGroupNames.get(b), "t");
                                    Log.d(TAG, "onBindViewHolder: group count before update: " + getCurrentGroupCount());
                                    mEditor.putString("gCount", Integer.toString((getCurrentGroupCount() + 1)));
                                    mEditor.apply();
                                    Log.d(TAG, "onBindViewHolder: group count after update: " + getCurrentGroupCount());



                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }else{
                            viewHolder.includeGroup.setChecked(false);
                            Toast.makeText(mGroupContext, "only " + allowedCountForGroups + " groups allowed currently", Toast.LENGTH_SHORT).show();
                        }
                    }
                    }else{
                        if(isChecked){
                            viewHolder.includeGroup.setChecked(false);

                        }else{
                            viewHolder.includeGroup.setChecked(true);
                        }
                        Toast.makeText(mGroupContext,"check internet connection ",Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        //----------------------------------------------------------------------------------------------------------------------------------------------------

        viewHolder.makePrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(viewHolder.makePrivate.isPressed()) {
                    if (viewHolder.includeGroup.isChecked()) {   // used to check if the group has been clicked before making it private
                        if(isNetworkAvailable()){
                            Log.d(TAG, "onCheckedChanged: there is internet connection");
                            if (isChecked) {

                                // add this to the database
                                // use String instead of bool because bool is 8 bytes and string is 2 bytes
                                // keep sharedpref under 100 kb to be safe but i read it can be <1000

                                    if (getCurrentCount() < allowedCountP) {    // check what the limit is for how many private groups is allowed
                                                                                // this can be made more efficient by mocing getCurrentCount higher
                                        mEditor.putString(mGroupNames.get(b), "f"); // 'f' means that it is private 't' means it is public
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("uInfo/" + uId + "/groups/" + mGroupNames.get(b)).setValue("f");
                                        Log.d(TAG, "onCheckedChanged: adding to privates - current: " + getCurrentCount());
                                        mEditor.putString("pCount", Integer.toString((getCurrentCount() + 1)));
                                        //need to add a counter to how many people are part of a group
                                        //https://firebase.google.com/docs/database/android/read-and-write#save_data_as_transactions

                                        mEditor.commit();
                                        Log.d(TAG, "onCheckedChanged: adding to privates - post: " + getCurrentCount());

                                    } else {
                                        viewHolder.makePrivate.setChecked(false);
                                        Toast.makeText(mGroupContext, "only " + allowedCountP + " private groups allowed", Toast.LENGTH_SHORT).show();
                                        //passed the allowed amount of private groups
                                    }
                                    mEditor.commit();



                            }else {
                                                              // turn the private off when clicked only. using setChecked(true) wont activate
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("uInfo/" + uId + "/groups/" + mGroupNames.get(b)).setValue("t");

                                    Log.d(TAG, "onCheckedChanged: adding to privates - current: " + getCurrentCount());

                                    mEditor.putString("pCount", Integer.toString((getCurrentCount() - 1)));
                                    mEditor.putString(mGroupNames.get(b), "t");
                                    mEditor.commit();                                                            // we are changing the count of private groups so it need to be commit
                                    Log.d(TAG, "onCheckedChanged: adding to privates - post: " + getCurrentCount());
                                // remove
                            }
                        }else{
                                if(isChecked){
                                    viewHolder.makePrivate.setChecked(false);
                                }else{
                                    viewHolder.makePrivate.setChecked(true);
                                }
                                Toast.makeText(mGroupContext,"check internet connection",Toast.LENGTH_LONG).show();
                            }
                    } else {
                        viewHolder.makePrivate.setChecked(false);
                        Toast.makeText(mGroupContext, mGroupNames.get(b) + " needs to be checked", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
        //---------------------------------------------------------------------------------------------------------------------------------------------------------------
        viewHolder.superLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(viewHolder.superLike.isPressed()) {
                    if (viewHolder.includeGroup.isChecked()) {
                        if(isNetworkAvailable()){
                            if (isChecked) {
                                if (!getCurrentSuper1().equals("E")) {                                 //if s1 is filled
                                    Log.d(TAG, "onCheckedChanged: s1 is full: " + getCurrentSuper1());

                                    if (!getCurrentSuper2().equals("E")) {// s2 is filled do nothing and inform them that they need to clear an old super
                                        Log.d(TAG, "onCheckedChanged: s2 is full: " + getCurrentSuper2());
                                        Toast.makeText(mGroupContext, "you can only have 2 exceptional groups", Toast.LENGTH_LONG).show();
                                        viewHolder.superLike.setChecked(false);
                                        Log.d(TAG, "onCheckedChanged: more than limit was about to be added");
                                    } else {
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("uInfo/" + uId + "/s2").setValue(mGroupNames.get(b));

                                        mEditor.putString("s2", mGroupNames.get(b));
                                        mEditor.commit();
                                        Log.d(TAG, "onCheckedChanged: added to s2: " + mGroupNames.get(b));

                                    }

                                } else {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("uInfo/" + uId + "/s1").setValue(mGroupNames.get(b));
                                    mEditor.putString("s1", mGroupNames.get(b));
                                    mEditor.commit();
                                    Log.d(TAG, "onCheckedChanged: added to sl: " + mGroupNames.get(b));
                                    Log.d(TAG, "onCheckedChanged: \n s1--- before-----: " + getCurrentSuper1());

                                }

                            } else {
                                if (getCurrentSuper1().equals(mGroupNames.get(b))) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("uInfo/" + uId + "/s1").setValue("E");
                                    mEditor.putString("s1", "E");
                                    mEditor.commit();
                                    Log.d(TAG, "onCheckedChanged: s1 was removed ");
                                }
                                if (getCurrentSuper2().equals(mGroupNames.get(b))) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("uInfo/" + uId + "/s2").setValue("E");
                                    mEditor.putString("s2", "E");
                                    mEditor.commit();
                                    Log.d(TAG, "onCheckedChanged: s2 was removed ");
                                }
                            }
                        }else{
                            if(isChecked){
                                viewHolder.superLike.setChecked(false);
                            }else{
                                viewHolder.superLike.setChecked(true);
                            }
                            Toast.makeText(mGroupContext,"check internet connection ",Toast.LENGTH_LONG).show();

                        }
                    }else{
                        viewHolder.superLike.setChecked(false);   // make sure the group was checked before super was clicked
                    }

                }
            }
        });
    }

    private int getCurrentCount(){
        return Integer.parseInt(pref.getString("pCount", "0"));  //it should be grabbing the current pCount value at every call

    }
    private int getCurrentGroupCount(){
        return Integer.parseInt(pref.getString("gCount", "0"));  //it should be grabbing the current gCount value at every call

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mGroupContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    private String getCurrentSuper1(){
        return pref.getString("s1", "E");
    }
    private String getCurrentSuper2(){
        return pref.getString("s2", "E");
    }
    @Override
    public int getItemCount() {
        return mGroupNames.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }
    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<String> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){  // if empty show the whole list
                filteredList.addAll(mGroupNamesFull);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(String item : mGroupNamesFull){
                    if(item.toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mGroupNames.clear();
            mGroupNames.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };


    public class ViewHolder extends RecyclerView.ViewHolder{

        CheckBox includeGroup;
        Switch makePrivate;
        Switch superLike;
        ConstraintLayout GroupLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            includeGroup = itemView.findViewById(R.id.checkGroup);
            makePrivate = itemView.findViewById(R.id.switch2);
            superLike = itemView.findViewById(R.id.superlike);
            GroupLayout = itemView.findViewById(R.id.single_group);
        }
    }

}
