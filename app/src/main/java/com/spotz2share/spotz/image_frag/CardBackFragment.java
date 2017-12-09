package com.spotz2share.spotz.image_frag;

/**
 * Created by spotzdevelopment on 11/9/2017.
 */

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spotz2share.spotz.R;
import com.spotz2share.spotz.SingleUserActivity;
import com.spotz2share.spotz.getters.CommentGetter;
import com.squareup.picasso.Picasso;

/**
 * A fragment representing the back of the card.
 */
public class CardBackFragment extends Fragment {
    public CardBackFragment() {
    }

    private String postID=null;
    private String userUID;
    private DatabaseReference databaseComments,databaseImages,databaseUsers;
    private ValueEventListener listenerU;
    private RecyclerView mRecycle;
    private TextView deletebtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View bView=inflater.inflate(R.layout.fragment_card_back, container, false);

        //gets postID
        postID = getArguments().getString("postID");

        //get Auth
        FirebaseAuth mAuth= FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        //databasereferences
        databaseImages= FirebaseDatabase.getInstance().getReference().child("Images");
        databaseImages.keepSynced(true);
        databaseComments= FirebaseDatabase.getInstance().getReference().child("Comments").child(postID);
        databaseComments.keepSynced(true);
        databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);

        deletebtn=(TextView)bView.findViewById(R.id.deletebtn);

        //edittext-add comment
        final EditText comment_input=(EditText)bView.findViewById(R.id.comment_input);
        comment_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                String comment=comment_input.getText().toString().trim();
                if(!comment.isEmpty()){
                    uploadComment(comment);
                    comment_input.getText().clear();

                    //dismisses keyboard
                    InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }


                return true;
            }
        });

        //recyclerview
        mRecycle= (RecyclerView)bView.findViewById(R.id.mRecycle);
        mRecycle.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycle.setLayoutManager(mLayoutManager);

        //load info and comments
        loadInfo(bView);
        loadComments();
        return bView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }


    private void loadInfo(View bView){
        final TextView post_tag=(TextView)bView.findViewById(R.id.post_tag);
        final TextView asterik=(TextView)bView.findViewById(R.id.asterik);
        databaseImages.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String tag = (String) dataSnapshot.child("tag").getValue();
                String userUIDdb=(String)dataSnapshot.child("userUID").getValue();

                //checks if the image is the user's
                if(userUIDdb!=null&&userUIDdb.equals(userUID)){
                    isUser(post_tag,asterik);
                }
                //sets tag
                post_tag.setText(tag);
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                }
            });
    }

    private void loadComments(){
        FirebaseRecyclerAdapter<CommentGetter, ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<CommentGetter, CardBackFragment.ImageViewHolder>(
                CommentGetter.class,
                R.layout.image_row_comments,
                CardBackFragment.ImageViewHolder.class,
                databaseComments

        ) {
            @Override
            protected void populateViewHolder(CardBackFragment.ImageViewHolder viewHolder, CommentGetter model, int position) {

                final String user_key=getRef(position).getKey();

                viewHolder.setUserUID(getActivity(), model.getUserUID());
                viewHolder.setName(model.getName());
                viewHolder.setComment(model.getComment());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

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

        public void setName(String name){
            TextView mName=(TextView) mView.findViewById(R.id.cName);
            mName.setText(name);
        }

        public void setComment(String comment){
            TextView cComment=(TextView) mView.findViewById(R.id.cComment);
            cComment.setText(comment);
        }

        public void setUserUID(final Context ctx, final String userUID){
            DatabaseReference databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
            databaseUsers.keepSynced(true);
            databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String profilepic=(String)dataSnapshot.child(userUID).child("profilepic").getValue();

                    ImageView post_image=(ImageView)mView.findViewById(R.id.cImage);
                    Picasso.with(ctx).load(profilepic).into(post_image);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }


    }

    private void uploadComment(final String comment){
        //unique comment id
        final DatabaseReference newComment=databaseComments.push();
        //final String commentID=newComment.getKey();

        listenerU=databaseUsers.child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //gets users name and profile pic to add the comment
                String name=(String)dataSnapshot.child("name").getValue();
                String profilepic=(String)dataSnapshot.child("profilepic").getValue();

                //sets comment
                newComment.child("name").setValue(name);
                newComment.child("userUID").setValue(userUID);
                newComment.child("comment").setValue(comment);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //reloads comments
        loadComments();
    }

    private void isUser(final TextView post_tag, TextView asterik){
        deletebtn.setVisibility(View.VISIBLE);
        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAll();
            }
        });

        post_tag.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Vibration
                Vibrator vibrate = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrate.vibrate(50);
                editTag(post_tag);
                return true;
            }
        });

        asterik.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Vibration
                Vibrator vibrate = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrate.vibrate(50);
                editTag(post_tag);
                return true;
            }
        });

    }

    private void editTag(final TextView post_tag){
        //opens popup
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
        View pview = getActivity().getLayoutInflater().inflate(R.layout.pop_edit_tag, null);

        mBuilder.setView(pview);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        TextView edit_ok=(TextView)pview.findViewById(R.id.edit_ok);
        final EditText edit_et=(EditText)pview.findViewById(R.id.edit_et);


        edit_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newTag=edit_et.getText().toString().trim();
                post_tag.setText(newTag);

                databaseImages.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        databaseImages.child(postID).child("tag").setValue(newTag);
                        databaseImages.child(postID).child("utag").setValue(newTag+userUID);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                dialog.dismiss();
            }
        });


    }

    //deleting all image ID from the database. Can't believe this worked tbh
    //Still requires reconfiguration=remember storage
    private void deleteAll(){
        String databaseList[]={"Images", "Comments", "Saved","UserImages"};
        DatabaseReference databaseAll;

        for(int i=0; i<4;i++){
            if(databaseList[i].equals("Saved")||databaseList[i].equals("UserImages")){
                //goes node deeper in Likes and UserImages
                databaseAll = FirebaseDatabase.getInstance().getReference().child(databaseList[i]).child(userUID);
            }else{
                databaseAll = FirebaseDatabase.getInstance().getReference().child(databaseList[i]);
            }
            databaseAll.keepSynced(true);
            Query queryAll = databaseAll.orderByKey().equalTo(postID);

            queryAll.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ds.getRef().removeValue();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        getActivity().finish();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (listenerU!= null) {
            databaseUsers.removeEventListener(listenerU);
        }
    }
}