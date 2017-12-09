package com.spotz2share.spotz.nav_fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.spotz2share.spotz.LoginActivity;
import com.spotz2share.spotz.Manifest;
import com.spotz2share.spotz.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

/**
 * Created by spotzdevelopment on 10/27/2017.
 */

public class SettingsFragment extends Fragment {

    private String userUID;
    private Uri eImageUri=null;
    private static final int GALLERY_REQUEST=5;
    private ImageView sPic;
    private DatabaseReference databaseUsers;
    private ValueEventListener listener1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button signout=(Button)view.findViewById(R.id.signout);
        final FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });


        Button changeBtn=(Button)view.findViewById(R.id.changeBtn);
        sPic=(ImageView)view.findViewById(R.id.sPic);


        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissions();

            }
        });

        getUser();
        return view;
    }

    //asks permission for reading storage
    private void permissions(){
        Intent gallery_intent=new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("image/*");
        startActivityForResult(gallery_intent,GALLERY_REQUEST);


        if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        GALLERY_REQUEST);

            }


        }
    }

    //gets user's current profile pic
    private void getUser(){
        databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);

        listener1=databaseUsers.child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String post_image=(String)dataSnapshot.child("profilepic").getValue();
                Picasso.with(getActivity()).load(post_image).resize(150,150).centerCrop().into(sPic);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    //saves the image to storage & database
    private void saveEdit() {

        if(eImageUri!=null){
            StorageReference mStorage= FirebaseStorage.getInstance().getReference().child("profilepics/");
            StorageReference filepath= mStorage.child(userUID);
            filepath.putFile(eImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                @SuppressWarnings("VisibleForTests")public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri=taskSnapshot.getDownloadUrl().toString();
                    databaseUsers.child(userUID).child("profilepic").setValue(downloadUri);

                }
            });


        }
    }


    //activates cropper after permission is granted
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQUEST && resultCode==Activity.RESULT_OK&& data != null){
            Uri imageUri=data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .setMinCropResultSize(200,200)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(getContext(), this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == Activity.RESULT_OK) {
                eImageUri = result.getUri();

                sPic.setImageURI(eImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("CropError", String.valueOf(error));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, GALLERY_REQUEST);


            }
            else {
                Toast.makeText(getActivity(), "Needs Access to Gallery to Update Image", Toast.LENGTH_LONG).show();
            }
        }
    }





    @Override
    public void onStop() {
        super.onStop();
        saveEdit();
        if (listener1 != null) {
            databaseUsers.removeEventListener(listener1);
        }
    }

/*    @Override
    public void onPause() {
        super.onPause();
        saveEdit();
    }*/
}