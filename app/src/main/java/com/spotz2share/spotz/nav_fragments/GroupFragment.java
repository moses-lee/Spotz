package com.spotz2share.spotz.nav_fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spotz2share.spotz.MainActivity;
import com.spotz2share.spotz.R;
import com.spotz2share.spotz.SingleImageActivity;
import com.spotz2share.spotz.SingleUserActivity;
import com.spotz2share.spotz.getters.ExploreGetter;
import com.squareup.picasso.Picasso;

/**
 * Created by spotzdevelopment on 10/26/2017.
 */

public class GroupFragment extends Fragment {

    private Query eQuery;
    private RecyclerView mRecycle;
    private DatabaseReference databaseImages, databaseUserImages;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        final EditText etSearch=(EditText)view.findViewById(R.id.etSearch);

        //userUID
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        final String userUID=mAuth.getCurrentUser().getUid();

        //edittext -> button
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchAction(userUID, etSearch);
                return true;
            }
        });


        databaseUserImages= FirebaseDatabase.getInstance().getReference().child("UserImages").child(userUID);
        databaseUserImages.keepSynced(true);
        databaseImages= FirebaseDatabase.getInstance().getReference().child("Images");
        databaseImages.keepSynced(true);

        //recycle set reverse
        mRecycle= (RecyclerView)view.findViewById(R.id.mRecycle);
        mRecycle.setHasFixedSize(true);
        mRecycle.setLayoutManager(new GridLayoutManager(getActivity(), 3, LinearLayoutManager.VERTICAL, false));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseIndexRecyclerAdapter<ExploreGetter, ExploreFragment.ImageViewHolder>
                firebaseRecyclerAdapter= new FirebaseIndexRecyclerAdapter<ExploreGetter, ExploreFragment.ImageViewHolder>(
                ExploreGetter.class,
                R.layout.image_row,
                ExploreFragment.ImageViewHolder.class,
                databaseUserImages, //ex. UserImages
                databaseImages //Images

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


    private void searchAction(final String userUID, final EditText etSearch ){

        final DatabaseReference databaseNames= FirebaseDatabase.getInstance().getReference().child("Names");
        databaseNames.keepSynced(true);
        final String input=etSearch.getText().toString().toLowerCase().trim();


        databaseNames.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(input)) {
                    String searchID = (String) dataSnapshot.child(input).getValue();
                    if (searchID.equals(userUID)) {
                        Intent ownintent = new Intent(getActivity(), MainActivity.class);
                        ownintent.putExtra("tab_code", "profile");
                        startActivity(ownintent);
                    } else {
                        Intent userIntent = new Intent(getActivity(), SingleUserActivity.class);
                        userIntent.putExtra("user2UID", searchID);
                        startActivity(userIntent);
                    }
                }else{
                    Toast.makeText(getActivity(), "User Does Not Exist", Toast.LENGTH_LONG).show();
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

        //REMOVES KEYBOARD
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

