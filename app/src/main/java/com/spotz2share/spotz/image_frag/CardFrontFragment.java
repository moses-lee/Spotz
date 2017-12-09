package com.spotz2share.spotz.image_frag;

/**
 * Created by spotzdevelopment on 11/9/2017.
 */

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spotz2share.spotz.R;
import com.spotz2share.spotz.SingleImageActivity;
import com.squareup.picasso.Picasso;

/**
 * A fragment representing the front of the card.
 */
public class CardFrontFragment extends Fragment {
    public CardFrontFragment() {
    }

    private ValueEventListener listenerS, listenerI;
    private boolean processSaved=false;
    private String userUID;
    private String postID=null;

    private DatabaseReference databaseSaved,databaseImages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fView=inflater.inflate(R.layout.fragment_card_front, container, false);

        //gets postID
        postID = getArguments().getString("postID");

        ImageView backbtn=(ImageView)fView.findViewById(R.id.backbtn);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        //get Auth
        FirebaseAuth mAuth= FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        //get database reference
        databaseSaved= FirebaseDatabase.getInstance().getReference().child("Saved");
        databaseSaved.keepSynced(true);
        databaseImages= FirebaseDatabase.getInstance().getReference().child("Images");
        databaseImages.keepSynced(true);

        loadImage(fView);
        return fView;
    }
    private void loadImage(View fView) {
        final ImageView post_image=(ImageView)fView.findViewById(R.id.post_image);
        final TextView post_name=(TextView)fView.findViewById(R.id.post_name);

        post_image.post(new Runnable() {
            @Override
            public void run() {
                final int height=post_image.getHeight(); //height is ready
                final int width=post_image.getWidth(); //width is ready

                listenerI = databaseImages.child(postID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String iName = (String) dataSnapshot.child("name").getValue();
                        String iTag = (String) dataSnapshot.child("tag").getValue();
                        String iImage = (String) dataSnapshot.child("image").getValue();
                        final String iuserUID = (String) dataSnapshot.child("userUID").getValue();
                        String iDate = (String) dataSnapshot.child("date").getValue();

                        post_name.setText(iName);

                        //load pic into imageview
                        Log.d("WIDTH:", String.valueOf(height));
                        Picasso.with(getActivity()).load(iImage).resize(width, height).centerCrop().into(post_image);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        post_image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Vibration
                Vibrator vibrate = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrate.vibrate(50);
                imageLike();

                return true;
            }
        });

        if (listenerI!= null) {
            databaseImages.removeEventListener(listenerI);
        }
    }





    private void imageLike(){
        processSaved=true;

        //saves image onto user
        listenerS=databaseSaved.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (processSaved) {
                    //if the image is already saved, image is removed
                    if (dataSnapshot.child(userUID).hasChild(postID)){
                        databaseSaved.child(userUID).child(postID).removeValue();
                        Toast.makeText(getActivity(), "Image Unaved", Toast.LENGTH_LONG).show();
                        processSaved = false;
                    } else {
                        //saves image
                        databaseSaved.child(userUID).child(postID).setValue(true);
                        Toast.makeText(getActivity(), "Image Saved", Toast.LENGTH_LONG).show();
                        processSaved = false;
                    }
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

        if (listenerI!= null) {
            databaseImages.removeEventListener(listenerI);
        }
        if (listenerS!= null) {
            databaseImages.removeEventListener(listenerS);
        }
    }
}
