package com.fireflyglobe.kmamo.click;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

//import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> super1 = new ArrayList<>();
    private ArrayList<String> super2 = new ArrayList<>();
    private List<List<String>> groups = new ArrayList<List<String>>();
    private ArrayList<Integer> matching;
    private Context mContext;
    private onNoteListener mOnNoteListener;

    public RecyclerViewAdapter(ArrayList<Integer> matching,ArrayList<String> mImages, ArrayList<String> names, ArrayList<String> super1, ArrayList<String> super2, Context mContext, List<List<String>> groups, onNoteListener onNoteListener) {
        int count = 0;
        count++;
        Log.d(TAG, "RecyclerViewAdapter: count =" + count + "names = " + names );
        this.matching = matching;
        this.mImages = mImages;
        this.names = names;
        this.super1 = super1;
        this.super2 = super2;
        this.mContext = mContext;
        this.groups = groups;
        this.mOnNoteListener = onNoteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //this inflates the view
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.customlayout, viewGroup, false);
        return new ViewHolder(view, mOnNoteListener);
    }

    // this links the information with the
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        //Uri uri = Uri.parse(mImages.get(i));
        Glide.with(mContext).asBitmap().load(mImages.get(i)).diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(viewHolder.userImage);
        Log.d(TAG, "onBindViewHolder: pict - " + mImages.get(i));
        Log.d(TAG, "onBindViewHolder: matching i: " + i);
        if(matching.size() > 0) {
            if (matching.get(i) == 2 || matching.get(i) == 3) {
                viewHolder.UserName.setTextColor(ContextCompat.getColor(mContext, R.color.alertGreen));
            } else if (matching.get(i) == 4 || matching.get(i) == 5) {
                viewHolder.UserName.setTextColor(ContextCompat.getColor(mContext, R.color.alertYellow));
            } else if (matching.get(i) >= 6) {
                viewHolder.UserName.setTextColor(ContextCompat.getColor(mContext, R.color.alertRedBrown));
            }
        }

        if(names.get(i).contains(" ")){
        String[] partOfName = names.get(i).split(" ",2);
        String name =partOfName[0].substring(0,1).toUpperCase() + partOfName[0].substring(1).toLowerCase() + " " + partOfName[1].substring(0,1).toUpperCase() + ".";
        viewHolder.UserName.setText(name);
        }else{
            String singleName = names.get(i).substring(0,1).toUpperCase() + names.get(i).substring(1).toUpperCase();
            viewHolder.UserName.setText(singleName);
        }


        if(!super1.get(i).equals("E")){
        viewHolder.SuperOne.setText(super1.get(i));
        }
        if(!super2.get(i).equals("E")){
        viewHolder.SuperTwo.setText(super2.get(i));
        }
        /*

        viewHolder.reViewlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: clicked on: " + names.get(i));

                // this is where you have an intent to switch to a new activity ----------------------------------------------------------------------------------
                // when one of the RecycleViews are clicked

                //Toast.makeText(mContext, names.get(i), Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext, groups.get(i).toString(), Toast.LENGTH_LONG).show();
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        CircleImageView userImage;
        TextView UserName, SuperOne, SuperTwo;
        ConstraintLayout reViewlayout;
        onNoteListener onNoteListener;
        ConstraintLayout single_group;

        public ViewHolder(@NonNull View itemView, onNoteListener onNoteListener) {
            super(itemView);
            single_group = itemView.findViewById(R.id.single_group);
            userImage = itemView.findViewById(R.id.userImage);
            UserName = itemView.findViewById(R.id.UserName);
            SuperOne = itemView.findViewById(R.id.superOne);
            SuperTwo = itemView.findViewById(R.id.superTwo);
            reViewlayout = itemView.findViewById(R.id.reVeiwLayout);
            this.onNoteListener = onNoteListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onNoteListener.onNoteClick(getAdapterPosition());
        }
    }
    public interface onNoteListener{
        void onNoteClick(int position);
    }
}
