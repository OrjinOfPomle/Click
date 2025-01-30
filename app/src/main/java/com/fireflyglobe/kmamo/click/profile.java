// colors for the groups and the app came from - https://blog.hubspot.com/marketing/color-combinations
package com.fireflyglobe.kmamo.click;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;


import de.hdodenhof.circleimageview.CircleImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;


public class profile extends AppCompatActivity {
    private static final String TAG = "profile";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        Log.d(TAG, "onCreate: started profile activity");
        getIncomingIntent();
        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");

    }
    private void getIncomingIntent(){
        Log.d(TAG, "getIncomingIntent: checking for incoming intents.");
        if(getIntent().hasExtra("name") && getIntent().hasExtra("pic") && getIntent().hasExtra("id") && getIntent().hasExtra("groups")){
            Log.d(TAG, "getIncomingIntent: found intent extras.");

            int screenWidth = getIntent().getIntExtra("screenWidth",-1);
            String name = getIntent().getStringExtra("name");
            String id = getIntent().getStringExtra("id");
            String pic = getIntent().getStringExtra("pic");
            ArrayList<String> groups = getIntent().getStringArrayListExtra("groups");
            setProfile(pic, name, groups,screenWidth);

        }

    }

    private  void setProfile(String pic, String name, ArrayList<String> groups, int screenWidth){
        Log.d(TAG, "setProfile: setting the profile");
        Log.d(TAG, "setProfile: screenWidth = " + screenWidth);
        //set up the profile here
        CircleImageView picture;

        TextView profileName;
        RelativeLayout profile = (RelativeLayout) findViewById(R.id.profile);
        int previousId = findViewById(R.id.profilePic).getId();
        int previousTvWidth = 0;
        int totalWidthInPixils = 0;

        //save the id of the previous group and use it to as margin

        picture = findViewById(R.id.profilePic);
        int topBound = findViewById(R.id.profileName).getId();
        profileName = findViewById(R.id.profileName);
        //int additionalSpacing = 0;
        String originalPieceOfUrl = "s96-c";

        // Variable holding the new String portion of the url that does the replacing, to improve image quality
        String newPieceOfUrlToAdd = "s400-c";
        String newPic = pic.replace(originalPieceOfUrl,newPieceOfUrlToAdd);





                                                    //https://android--code.blogspot.com/2015/05/how-to-create-textview-programmatically.html
        for(int i = 0; i < groups.size(); i++ ){    //groups.size()

            LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                    RelativeLayout.LayoutParams.WRAP_CONTENT); // Height of TextView


            lp.addRule(RelativeLayout.BELOW, topBound);
            TextView tv = new TextView(profile.this);
            tv.setId(View.generateViewId());// for some reason view.generateViewId wouldnt be set to an int outside of the
            if(groups.get(i).equals("E")){tv.setText("Empty");}else{tv.setText(groups.get(i));}//groups.get(i)  //26 char before we reach the end of the screen
            tv.setTextSize(20);
            lp.topMargin = 10;//+ additionalSpacing
            // getColor has depreciated -  https://dominoc925.blogspot.com/2016/08/identifying-deprecated-classes-and.html
            //tv.setBackgroundColor(ContextCompat.getColor(profile.this,R.color.colorLightBackground));
            TextViewCompat.setTextAppearance(tv, R.style.font_sans_light);
            tv.setTextColor(ContextCompat.getColor(profile.this, R.color.colorFontColorMatchBackground));
            tv.setBackgroundResource(R.drawable.round_edges);
            GradientDrawable drawable = (GradientDrawable) tv.getBackground();
            drawable.setColor(ContextCompat.getColor(profile.this, R.color.colorLightBackground));

            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.AT_MOST);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            tv.measure(widthMeasureSpec, heightMeasureSpec);
            previousTvWidth = tv.getMeasuredWidth();

            Log.d(TAG, "setProfile: textsize in pixils - " + groups.get(i) + " = " + tv.getMeasuredWidth());
            if((totalWidthInPixils + previousTvWidth)/(screenWidth - 100) < 1){   //groups.get(i).length()
                if (i > 0) {
                    lp.addRule(RelativeLayout.RIGHT_OF, previousId);
                    //lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    lp.leftMargin = 10;
                } else {
                    lp.addRule(RelativeLayout.ALIGN_PARENT_START, findViewById(R.id.profile).getId());
                    lp.leftMargin = 50;
                    //need to remove some how
                }
                String currentGroup = groups.get(i);
                totalWidthInPixils = totalWidthInPixils + previousTvWidth;  // currentGroup.length()

                //need to have some way of checking that we just crossed a multiple of 26------------------------------------------------------------------
                previousId = tv.getId();


            }else{
                Log.d(TAG, "setProfile: we are in the else statement");
                topBound = previousId;
                lp.addRule(RelativeLayout.BELOW, topBound);
                lp.addRule(RelativeLayout.ALIGN_PARENT_START, findViewById(R.id.profile).getId());
                lp.leftMargin = 50;


                totalWidthInPixils = previousTvWidth; //currentGroup.length()

                //need to have some way of checking that we just crossed a multiple of 26------------------------------------------------------------------
                previousId = tv.getId();

            }
            tv.setLayoutParams(lp);
            profile.addView(tv);
        }





        if(name.contains(" ")){
            String[] partOfName = name.split(" ", 2);
            String hold =partOfName[0].substring(0,1).toUpperCase() + partOfName[0].substring(1).toLowerCase() + " " + partOfName[1].substring(0,1).toUpperCase() + ".";
            profileName.setText(hold);
        }else{
            profileName.setText(name);
        }

        Glide.with(profile.this).asBitmap().load(newPic).diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(picture);
    }

}
/*
2019-08-11 18:18:32.429 26187-26187/? D/ViewRootImpl@6532846[MainActivity]: MSG_RESIZED: frame=Rect(0, 0 - 1440, 2960) ci=Rect(0, 96 - 0, 192) vi=Rect(0, 96 - 0, 192) or=1
2019-08-11 18:18:32.430 26187-26187/? D/InputMethodManager: prepareNavigationBarInfo() DecorView@b91095d[MainActivity]
2019-08-11 18:18:32.470 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: grabbing users and other information -------------------------
2019-08-11 18:18:32.505 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: Getting nearby users... 1...fetchCount: 1
2019-08-11 18:18:33.381 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: grabbing users and other information -------------------------
2019-08-11 18:18:33.398 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: Getting nearby users... 2...fetchCount: 2
2019-08-11 18:18:34.494 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: adding user
2019-08-11 18:18:34.495 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: the user wasnt in the array: XPLa1uhlqzPMBlA1z9G0RJ3BMWv1
2019-08-11 18:18:34.497 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onKeyEntered: XPLa1uhlqzPMBlA1z9G0RJ3BMWv1
2019-08-11 18:18:34.497 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: adding user
2019-08-11 18:18:34.497 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: the user wasnt in the array: XPLa1uhlqzPMBlA1z9G0RJ3BMWv1
2019-08-11 18:18:34.498 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onKeyEntered: XPLa1uhlqzPMBlA1z9G0RJ3BMWv1
2019-08-11 18:18:34.498 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: adding user
2019-08-11 18:18:34.498 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: the user wasnt in the array: TEt6hFU7hJOBSpgnaQgdP2Vr5NE2
2019-08-11 18:18:34.499 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onKeyEntered: TEt6hFU7hJOBSpgnaQgdP2Vr5NE2
2019-08-11 18:18:34.499 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: adding user
2019-08-11 18:18:34.499 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: the user wasnt in the array: TEt6hFU7hJOBSpgnaQgdP2Vr5NE2
2019-08-11 18:18:34.499 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onKeyEntered: TEt6hFU7hJOBSpgnaQgdP2Vr5NE2
2019-08-11 18:18:34.499 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onGeoQueryReady: starting recyclerView
2019-08-11 18:18:34.499 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: initRecyclerView:  init recycler view.
2019-08-11 18:18:34.508 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: initRecyclerView: finished
2019-08-11 18:18:34.508 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onGeoQueryReady: starting recyclerView
2019-08-11 18:18:34.508 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: initRecyclerView:  init recycler view.
2019-08-11 18:18:34.508 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: initRecyclerView: finished
2019-08-11 18:18:34.509 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: grabbing users and other information -------------------------
2019-08-11 18:18:34.519 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: Getting nearby users... 3...fetchCount: 3
2019-08-11 18:18:34.522 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onLocationResult: changed interval at:3
2019-08-11 18:18:34.581 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: adding user
2019-08-11 18:18:34.581 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onKeyEntered: XPLa1uhlqzPMBlA1z9G0RJ3BMWv1
2019-08-11 18:18:34.581 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: adding user
2019-08-11 18:18:34.581 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: the user wasnt in the array: TEt6hFU7hJOBSpgnaQgdP2Vr5NE2
2019-08-11 18:18:34.581 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onKeyEntered: TEt6hFU7hJOBSpgnaQgdP2Vr5NE2
2019-08-11 18:18:34.581 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onGeoQueryReady: starting recyclerView
2019-08-11 18:18:34.581 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: initRecyclerView:  init recycler view.
2019-08-11 18:18:34.582 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: initRecyclerView: finished
2019-08-11 18:18:34.725 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: grabbing users and other information -------------------------
2019-08-11 18:18:34.734 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: Getting nearby users... 4...fetchCount: 4
2019-08-11 18:18:34.884 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: adding user
2019-08-11 18:18:34.884 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onKeyEntered: XPLa1uhlqzPMBlA1z9G0RJ3BMWv1
2019-08-11 18:18:34.884 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: updateArrays: adding user
2019-08-11 18:18:34.884 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onKeyEntered: TEt6hFU7hJOBSpgnaQgdP2Vr5NE2
2019-08-11 18:18:34.884 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: onGeoQueryReady: starting recyclerView
2019-08-11 18:18:34.884 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: initRecyclerView:  init recycler view.
2019-08-11 18:18:34.890 26187-26187/com.example.kmamo.firebasepractice D/MainActivity: initRecyclerView: finished
2019-08-11 18:18:42.292 26187-26231/com.example.kmamo.firebasepractice D/FA: Logging event (FE): session_start(_s), Bundle[{firebase_event_origin(_o)=auto, firebase_screen_class(_sc)=MainActivity, firebase_screen_id(_si)=-2604474281712381194, session_id(_sid)=1565565522}]

 */