package com.spotz2share.spotz;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE=1;
    private ImageView imageView;
    private ProgressBar uProgress;
    private EditText uTag;
    private FirebaseAuth mAuth;
    private String userUID=null;
    private String eventID=null;

    private ValueEventListener listener1, listener2, listener3;
    private DatabaseReference databaseUsers, databaseEvents, databaseGroups;

    //TODO IF STATEMENT FOR WHEN IMAGE IS NULL+CHANGE PROGRESS COLOR
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //save image
        if (savedInstanceState == null) {

        } else {

        }

        imageView=(ImageView)findViewById(R.id.imageView);
        uProgress = (ProgressBar) findViewById(R.id.uProgress);
        uTag=(EditText)findViewById(R.id.uTag);
        TextView uBtn=(TextView)findViewById(R.id.uploadbtn);

        mAuth= FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        //gets event id from put extra
        eventID=getIntent().getExtras().getString("eventID");




        //get permissions
        permissions();

        uBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //displaying a progress dialog while upload is going on
                uProgress.setVisibility(View.VISIBLE);
                bitMap();
            }
        });
    }

    //camera permissions
    private void permissions(){
        if( ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        CAMERA_REQUEST_CODE);
            }
        }else{
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE&& resultCode == RESULT_OK && data != null ) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            imageView.setImageBitmap(bitmap);

        }
    }



    private void bitMap() {

        //gets bitmap from ImageView
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap =  imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        storageTask(data);
    }

    private void storageTask(byte[] data){
      //uploads image to firebase storage
        StorageReference mStorage= FirebaseStorage.getInstance().getReference();
        StorageReference mountainsRef = mStorage.child("uploads").child(userUID+String.valueOf(System.currentTimeMillis()));
        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //if the upload has failed
                // Handle unsuccessful uploads
                uProgress.setVisibility(View.GONE);
                //and displaying error message
                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
            }
            //if successful
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                getEventGroup(taskSnapshot);

            }
        });
    }

    private void getEventGroup(final UploadTask.TaskSnapshot taskSnapshot){


        databaseEvents= FirebaseDatabase.getInstance().getReference().child("Events");
        databaseEvents.keepSynced(true);

        listener1= databaseEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String group =(String)dataSnapshot.child(eventID).child("group").getValue();
                databaseUploadImage(taskSnapshot, group);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void databaseUploadImage(UploadTask.TaskSnapshot taskSnapshot, final String group){
        @SuppressWarnings("VisibleForTests") final Uri downloadUrl = taskSnapshot.getDownloadUrl();
        DatabaseReference databaseImages=FirebaseDatabase.getInstance().getReference().child("Images");
        databaseImages.keepSynced(true);

        databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users").child(userUID);
        databaseUsers.keepSynced(true);

        databaseGroups= FirebaseDatabase.getInstance().getReference().child("Groups").child(userUID);
        databaseGroups.keepSynced(true);

        final DatabaseReference databaseUserImages= FirebaseDatabase.getInstance().getReference().child("UserImages");
        databaseUserImages.keepSynced(true);
        final DatabaseReference databaseDates=FirebaseDatabase.getInstance().getReference().child("Dates");
        databaseDates.keepSynced(true);

        //image id
        final DatabaseReference newPost=databaseImages.push();
        final String postID=newPost.getKey();

        //gets date
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        final String date=sdf.format(Calendar.getInstance().getTime());

        listener2=databaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //checks if user already has taken pictures on this date
                if(databaseDates.child(userUID).child("date").equals(date)){
                    Log.d("UploadActivity", "Date Exists");
                }else{
                    databaseDates.child(userUID).child(date).child("date").setValue(date);
                }

                newPost.child("tag").setValue(uTag.getText().toString().trim());
                //for when the user searches only their pictures
                newPost.child("utag").setValue(uTag.getText().toString().trim()+userUID);

                newPost.child("image").setValue(downloadUrl.toString());
                newPost.child("userUID").setValue(userUID);
                newPost.child("date").setValue(date);
                newPost.child("udate").setValue(date+userUID);

                newPost.child("event").setValue(eventID);

                newPost.child("group").setValue(group);

                newPost.child("name").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            uProgress.setVisibility(View.GONE);
                            Intent eintent=new Intent(CameraActivity.this, SingleEventActivity.class);
                            eintent.putExtra("event_id", eventID);
                            startActivity(eintent);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //adds image to each friend of the user
        listener3=databaseGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dsp : dataSnapshot.getChildren()){
                    String userFriends=dsp.getKey();
                    databaseUserImages.child(userFriends).child(postID).setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
            else {
                Toast.makeText(CameraActivity.this, "Needs Access to Camera to Work!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(imageView!=null){
            startActivity(new Intent(CameraActivity.this, MainActivity.class));
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (listener1 != null) {
            databaseEvents.removeEventListener(listener1);
        }
        if (listener2 != null) {
            databaseUsers.removeEventListener(listener2);
        }
        if (listener3 != null) {
            databaseGroups.removeEventListener(listener3);
        }
    }
}