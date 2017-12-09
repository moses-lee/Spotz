package com.spotz2share.spotz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.spotz2share.spotz.getters.ExploreGetter;
import com.spotz2share.spotz.nav_fragments.ExploreFragment;
import com.squareup.picasso.Picasso;

import java.util.EventListener;

public class SearchTagActivity extends AppCompatActivity {

    private RecyclerView mRecycle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tag);

        final EditText et_tagsearch=(EditText)findViewById(R.id.et_tagsearch);

        //userUID
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        final String userUID=mAuth.getCurrentUser().getUid();

        //edittext -> button
        et_tagsearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchTag(userUID, et_tagsearch.getText().toString().toLowerCase().trim());
                return true;
            }
        });

        //recycle set reverse
        mRecycle= (RecyclerView)findViewById(R.id.mRecycle);
        mRecycle.setHasFixedSize(true);
        mRecycle.setLayoutManager(new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false));
    }





    private void searchTag(String userUID, String tag){
        DatabaseReference databaseImages=FirebaseDatabase.getInstance().getReference().child("Images");
        databaseImages.keepSynced(true);

        Query tag_query=databaseImages.orderByChild("utag").equalTo(tag+userUID);
        FirebaseRecyclerAdapter<ExploreGetter, SearchTagActivity.ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<ExploreGetter, SearchTagActivity.ImageViewHolder>(
                ExploreGetter.class,
                R.layout.image_row,
                SearchTagActivity.ImageViewHolder.class,
                tag_query

        ) {
            @Override
            protected void populateViewHolder(SearchTagActivity.ImageViewHolder viewHolder, ExploreGetter model, int position) {

                final String post_key=getRef(position).getKey();

                viewHolder.setImage(SearchTagActivity.this, model.getImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent singlePostIntent=new Intent(SearchTagActivity.this, SingleImageActivity.class);
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
