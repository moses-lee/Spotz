package com.spotz2share.spotz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.View;

import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.spotz2share.spotz.getters.ExploreGetter;
import com.spotz2share.spotz.nav_fragments.ExploreFragment;
import com.spotz2share.spotz.nav_fragments.MapsFragment;
import com.squareup.picasso.Picasso;


public class SingleEventActivity extends AppCompatActivity {

    private String eventID=null;
    private String userUID;

    private Query eQuery;

    private FirebaseStorage mStorage;
    private ValueEventListener listenerS, listenerI;
    private RecyclerView mRecycle;
    private FloatingActionButton camFab;
    private boolean closeEnough=false;
    private boolean backbtn=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_event);

        //get event id
        eventID=getIntent().getExtras().getString("event_id");
        //check distance
        closeEnough=getIntent().getExtras().getBoolean("closeEnough");
        //backbtn
        backbtn=getIntent().getExtras().getBoolean("backbtn");

        //get AUth
        FirebaseAuth mAuth= FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        //get database reference
        DatabaseReference databaseImages= FirebaseDatabase.getInstance().getReference().child("Images");
        databaseImages.keepSynced(true);
        eQuery=databaseImages.orderByChild("event").equalTo(eventID);

        //recycle set reverse
        mRecycle= (RecyclerView)findViewById(R.id.mRecycle);
        mRecycle.setHasFixedSize(true);
        mRecycle.setLayoutManager(new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false));


        final FloatingActionButton eventFab=(FloatingActionButton)findViewById(R.id.eventFab);
        camFab=(FloatingActionButton)findViewById(R.id.camFab);
        eventFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent info=new Intent(SingleEventActivity.this, SingleEventInfoActivity.class);
                info.putExtra("eventID", eventID);
                startActivity(info);


            }
        });

        getEventInfo();

        mRecycle.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && eventFab.getVisibility() == View.VISIBLE&&camFab.getVisibility()==View.VISIBLE) {
                    eventFab.hide();
                    camFab.hide();
                } else if (dy < 0 && eventFab.getVisibility() != View.VISIBLE&&camFab.getVisibility()!=View.VISIBLE) {
                    eventFab.show();
                    camFab.show();
                }
            }
        });
    }


    private void getEventInfo(){
        final DatabaseReference databaseEvents= FirebaseDatabase.getInstance().getReference().child("Events");
        databaseEvents.keepSynced(true);

        final TextView eventName=(TextView)findViewById(R.id.eventName);


        databaseEvents.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title= (String)dataSnapshot.child(eventID).child("title").getValue();


                eventName.setText(title);



                camFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(closeEnough){
                            Intent cam=new Intent(SingleEventActivity.this, CameraActivity.class);
                            cam.putExtra("eventID", eventID);
                            startActivity(cam);
                        }else{
                            Toast.makeText(SingleEventActivity.this, "You are not close enough!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<ExploreGetter, ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<ExploreGetter, ImageViewHolder>(
                ExploreGetter.class,
                R.layout.image_row,
                SingleEventActivity.ImageViewHolder.class,
                eQuery

        )  {
            @Override
            protected void populateViewHolder(SingleEventActivity.ImageViewHolder viewHolder, ExploreGetter model, int position) {

                final String post_key=getRef(position).getKey();

                viewHolder.setImage(SingleEventActivity.this, model.getImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent singlePostIntent=new Intent(SingleEventActivity.this, SingleImageActivity.class);
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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(!backbtn){
            startActivity(new Intent(SingleEventActivity.this, MainActivity.class));
        }else{
            finish();
        }

    }



}
