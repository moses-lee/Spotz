package com.spotz2share.spotz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.spotz2share.spotz.getters.ExploreGetter;
import com.squareup.picasso.Picasso;

public class SingleDateActivity extends AppCompatActivity {

    private RecyclerView mRecycle;
    private Query date_query;
    private String post_date;
    private String userUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_date);
        //get event id
        post_date=getIntent().getExtras().getString("post_date");


        //get AUth
        FirebaseAuth mAuth= FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        //get database reference
        DatabaseReference databaseImages= FirebaseDatabase.getInstance().getReference().child("Images");
        databaseImages.keepSynced(true);
        date_query=databaseImages.orderByChild("date").equalTo(post_date+userUID);

        //recycle set reverse
        mRecycle= (RecyclerView)findViewById(R.id.mRecycle);
        mRecycle.setHasFixedSize(true);
        mRecycle.setLayoutManager(new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false));

    }



    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<ExploreGetter, ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<ExploreGetter, ImageViewHolder>(
                ExploreGetter.class,
                R.layout.image_row,
                SingleDateActivity.ImageViewHolder.class,
                date_query

        )  {
            @Override
            protected void populateViewHolder(SingleDateActivity.ImageViewHolder viewHolder, ExploreGetter model, int position) {

                final String post_key=getRef(position).getKey();

                viewHolder.setImage(SingleDateActivity.this, model.getImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent singlePostIntent=new Intent(SingleDateActivity.this, SingleImageActivity.class);
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
