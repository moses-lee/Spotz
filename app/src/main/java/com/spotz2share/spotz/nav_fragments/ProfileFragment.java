package com.spotz2share.spotz.nav_fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spotz2share.spotz.MainActivity;
import com.spotz2share.spotz.R;
import com.spotz2share.spotz.SingleDateActivity;
import com.spotz2share.spotz.SingleImageActivity;
import com.spotz2share.spotz.getters.DateGetter;
import com.spotz2share.spotz.getters.ExploreGetter;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

/**
 * Created by spotzdevelopment on 10/26/2017.
 */


public class ProfileFragment extends Fragment {

    private RecyclerView recycler_all, recycler_saved, recycler_dates;
    private Query queryAll;
    private DatabaseReference databaseDates, databaseUsers,databaseImages, databaseSaved;
    private ValueEventListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        String userUID=mAuth.getCurrentUser().getUid();

        //databases
        databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);

        databaseImages= FirebaseDatabase.getInstance().getReference().child("Images");
        queryAll= databaseImages.orderByChild("userUID").equalTo(userUID);

        databaseSaved= FirebaseDatabase.getInstance().getReference().child("Saved").child(userUID);

        databaseDates= FirebaseDatabase.getInstance().getReference().child("Dates").child(userUID);

        //recycle set reverse, grid lines
        recycler_all= (RecyclerView)view.findViewById(R.id.recycle_all);
        recycler_all.setLayoutManager(new GridLayoutManager(getActivity(), 3, LinearLayoutManager.VERTICAL, false));
        recycler_all.setHasFixedSize(true);

        recycler_saved= (RecyclerView)view.findViewById(R.id.recycle_saved);
        recycler_saved.setLayoutManager(new GridLayoutManager(getActivity(), 3, LinearLayoutManager.VERTICAL, false));
        recycler_saved.setHasFixedSize(true);

        //date grid but not really a grid
        recycler_dates= (RecyclerView)view.findViewById(R.id.recycle_dates);
        recycler_dates.setLayoutManager(new GridLayoutManager(getActivity(), 1, LinearLayoutManager.VERTICAL, false));
        recycler_dates.setHasFixedSize(true);

        ImageView profile_all=(ImageView)view.findViewById(R.id.profile_all);
        ImageView profile_saved=(ImageView)view.findViewById(R.id.profile_saved);
        ImageView profile_dates=(ImageView)view.findViewById(R.id.profile_dates);

        //sets profile name and pics
        setProfile(userUID, view);

        //changes grid accordingly
        profile_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerAll();
            }
        });
        profile_saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recycleSaved();
            }
        });
        profile_dates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recycleDates();
            }
        });



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerAll();
    }

    //recyclerview for ALL
    private void recyclerAll(){
        //shows recylcerview/hides recycler views
        recycler_all.setVisibility(View.VISIBLE);
        recycler_saved.setVisibility(View.GONE);
        recycler_dates.setVisibility(View.GONE);

        FirebaseRecyclerAdapter<ExploreGetter, ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<ExploreGetter, ProfileFragment.ImageViewHolder>(
                ExploreGetter.class,
                R.layout.image_row,
                ProfileFragment.ImageViewHolder.class,
                queryAll

        ) {
            @Override
            protected void populateViewHolder(ProfileFragment.ImageViewHolder viewHolder, ExploreGetter model, int position) {

                final String post_key=getRef(position).getKey();

                viewHolder.setImage(getActivity(), model.getImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singlePostIntent=new Intent(getActivity(), SingleImageActivity.class);
                        singlePostIntent.putExtra("postID",post_key);
                        startActivity(singlePostIntent);
                    }
                });

            }
        };

        recycler_all.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }


    //recyclerview for saved
    private void recycleSaved(){
        //shows recylcerview/hides recycler views
        recycler_all.setVisibility(View.GONE);
        recycler_saved.setVisibility(View.VISIBLE);
        recycler_dates.setVisibility(View.GONE);

        FirebaseIndexRecyclerAdapter<ExploreGetter, ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseIndexRecyclerAdapter<ExploreGetter, ProfileFragment.ImageViewHolder>(
                ExploreGetter.class,
                R.layout.image_row,
                ProfileFragment.ImageViewHolder.class,
                databaseSaved, //ex. UserImages
                databaseImages //Images

        ) {
            @Override
            protected void populateViewHolder(ProfileFragment.ImageViewHolder viewHolder, ExploreGetter model, int position) {

                final String post_key=getRef(position).getKey();

                viewHolder.setImage(getActivity(), model.getImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singlePostIntent=new Intent(getActivity(), SingleImageActivity.class);
                        singlePostIntent.putExtra("postID",post_key);
                        startActivity(singlePostIntent);
                    }
                });

            }
        };

        recycler_saved.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    //recyclerview for dates
    private void recycleDates(){
        //shows recylcerview/hides recycler views
        recycler_all.setVisibility(View.GONE);
        recycler_saved.setVisibility(View.GONE);
        recycler_dates.setVisibility(View.VISIBLE);

        FirebaseRecyclerAdapter<DateGetter, ImageViewHolderDates >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<DateGetter, ProfileFragment.ImageViewHolderDates>(
                DateGetter.class,
                R.layout.image_row_dates,
                ProfileFragment.ImageViewHolderDates.class,
                databaseDates

        ) {
            @Override
            protected void populateViewHolder(ProfileFragment.ImageViewHolderDates viewHolder, DateGetter model, int position) {

                viewHolder.setDate(model.getDate());
                final String post_date=(model.getDate());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singlePostIntent=new Intent(getActivity(), SingleDateActivity.class);
                        singlePostIntent.putExtra("post_date",post_date);
                        startActivity(singlePostIntent);

                    }
                });

            }
        };

        recycler_dates.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }



    //regular imageholder
    public static class ImageViewHolder extends RecyclerView.ViewHolder{
        View mView;
        FirebaseAuth mAuth;

        public ImageViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            mAuth=FirebaseAuth.getInstance();

        }

        public void setImage(Context ctx, String image){
            //gets screen size from sp
            SharedPreferences sharedPref = ctx.getSharedPreferences("pref", Context.MODE_PRIVATE);
            int sWidth = Integer.parseInt(sharedPref.getString("screensize", ""));


            ImageView post_image=(ImageView)mView.findViewById(R.id.mImage);
            Picasso.with(ctx).load(image).resize(sWidth,sWidth).centerCrop().into(post_image);
        }
    }


    //imageholder for dates
    public static class ImageViewHolderDates extends RecyclerView.ViewHolder{
        View mView;
        FirebaseAuth mAuth;

        public ImageViewHolderDates(View itemView) {
            super(itemView);

            mView=itemView;
            mAuth=FirebaseAuth.getInstance();

        }

        public void setDate(String date){
            TextView mDate=(TextView) mView.findViewById(R.id.mDate);
            mDate.setText(date);
        }
    }

    private void setProfile(String userUID, View view){
        final TextView nName=(TextView)view.findViewById(R.id.profile_name);
        final ImageView nPic=(ImageView)view.findViewById(R.id.profile_pic);

        //grabs profile pic and name from database
        listener=databaseUsers.child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                String profilepic=(String)dataSnapshot.child("profilepic").getValue();

                //sets pic and name
                nName.setText(name);
                if(profilepic!=null&&!profilepic.isEmpty()){
                    Picasso.with(getActivity()).load(profilepic).resize(150,150).centerCrop().into(nPic);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onStop() {
        super.onStop();

        if (listener != null) {
            databaseUsers.removeEventListener(listener);
        }

    }
}
