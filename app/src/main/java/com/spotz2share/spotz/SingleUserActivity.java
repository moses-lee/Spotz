package com.spotz2share.spotz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spotz2share.spotz.getters.ExploreGetter;
import com.squareup.picasso.Picasso;

public class SingleUserActivity extends AppCompatActivity {


    private ValueEventListener listener2;
    private DatabaseReference databaseUsers,databaseFriends;

    private String user2Name=null;
    private String user2UID=null;
    private boolean  processFriend=false;

    private ImageView user2_add;
    private boolean isFriend;
    private RecyclerView mRecycle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_user);

        user2UID=getIntent().getExtras().getString("user2UID");

        user2_add=(ImageView)findViewById(R.id.user2_add);
        user2_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUp();
            }
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final String userUID=mAuth.getCurrentUser().getUid();

        databaseFriends= FirebaseDatabase.getInstance().getReference().child("Groups").child(userUID);
        databaseFriends.keepSynced(true);

        //recycle set reverse
        mRecycle= (RecyclerView)findViewById(R.id.mRecycle);
        mRecycle.setHasFixedSize(true);
        mRecycle.setLayoutManager(new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false));

        getUser();

    }


    private void getUser(){
        final ImageView user2_pic=(ImageView)findViewById(R.id.user2_pic);
        final TextView user2_name=(TextView) findViewById(R.id.user2_name);
        databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);

        //gets user's info from user's name search
        databaseUsers.child(user2UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                user2Name=(String)dataSnapshot.child("name").getValue();
                String user2Pic=(String)dataSnapshot.child("profilepic").getValue();
                user2_name.setText(user2Name);
                Picasso.with(SingleUserActivity.this).load(user2Pic).into(user2_pic);
                updateFriendIcon();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
   /* @Override
    public void onStart() {
        super.onStart();

        DatabaseReference databaseImages=FirebaseDatabase.getInstance().getReference().child("Images");

        FirebaseRecyclerAdapter<ExploreGetter, SingleEventActivity.ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<ExploreGetter, SingleEventActivity.ImageViewHolder>(
                ExploreGetter.class,
                R.layout.image_row,
                SingleEventActivity.ImageViewHolder.class,
                databaseFriends

        )  {
            @Override
            protected void populateViewHolder(SingleEventActivity.ImageViewHolder viewHolder, ExploreGetter model, int position) {

                final String post_key=getRef(position).getKey();

                viewHolder.setImage(SingleUserActivity.this, model.getImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent singlePostIntent=new Intent(SingleUserActivity.this, SingleImageActivity.class);
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
    }*/

    private void popUp(){
        //opens popup
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View pview = getLayoutInflater().inflate(R.layout.pop_category, null);
        mBuilder.setView(pview);
        final AlertDialog dialog = mBuilder.create();
        TextView cHeader=(TextView)pview.findViewById(R.id.cHeader);
        //changes text of header if user2 is already user1's friend
        if(isFriend){
            cHeader.setText("Unfriend "+user2Name+"?");
        }
        dialog.show();

        TextView cNo=(TextView)pview.findViewById(R.id.cNo);
        TextView cYes=(TextView)pview.findViewById(R.id.cYes);


        cYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
                dialog.dismiss();
            }
        });
        cNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }


    private void addUser(){
        processFriend = true;

        listener2=databaseFriends.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (processFriend) {
                    if (dataSnapshot.hasChild(user2UID)) {
                        databaseFriends.child(user2UID).removeValue();
                        processFriend = false;
                        Toast.makeText(SingleUserActivity.this, user2Name+" will no longer able to view your events and pictures", Toast.LENGTH_LONG).show();
                        return;
                    }
                    databaseFriends.child(user2UID).setValue(true);
                    processFriend = false;
                    Toast.makeText(SingleUserActivity.this, user2Name+" will now be able to view your events and pictures", Toast.LENGTH_LONG).show();

                    updateFriendIcon();
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateFriendIcon(){

        listener2=databaseFriends.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(user2UID)) {
                    user2_add.setImageResource(R.drawable.ic_done);
                    isFriend=true;
                }else{
                    user2_add.setImageResource(R.drawable.ic_add_white);
                    isFriend=false;
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

        if (listener2 != null) {
            databaseFriends.removeEventListener(listener2);
        }
    }
}
