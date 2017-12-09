package com.spotz2share.spotz.nav_fragments;


import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.spotz2share.spotz.MainActivity;
import com.spotz2share.spotz.R;
import com.spotz2share.spotz.SingleImageActivity;
import com.spotz2share.spotz.getters.ExploreGetter;
import com.squareup.picasso.Picasso;




public class ExploreFragment extends Fragment {

    private DatabaseReference databaseImages;
    private Query eQuery;
    private RecyclerView mRecycle;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        //query
        databaseImages=FirebaseDatabase.getInstance().getReference().child("Images");
        eQuery= databaseImages.orderByChild("group").equalTo("public");

        //recycle set reverse
        mRecycle= (RecyclerView)view.findViewById(R.id.mRecycle);
        mRecycle.setHasFixedSize(true);
        mRecycle.setLayoutManager(new GridLayoutManager(getActivity(), 3, LinearLayoutManager.VERTICAL, false));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<ExploreGetter, ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<ExploreGetter, ExploreFragment.ImageViewHolder>(
                ExploreGetter.class,
                R.layout.image_row,
                ExploreFragment.ImageViewHolder.class,
                eQuery

        ) {
            @Override
            protected void populateViewHolder(ExploreFragment.ImageViewHolder viewHolder, ExploreGetter model, int position) {

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

        mRecycle.setAdapter(firebaseRecyclerAdapter);

    }



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

}

