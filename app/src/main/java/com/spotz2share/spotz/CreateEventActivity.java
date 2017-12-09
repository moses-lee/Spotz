package com.spotz2share.spotz;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {

    private EditText eName,eDesc,eDate;
    private MultiAutoCompleteTextView  eEmail;
    private TextView eLocation;
    private ValueEventListener listener1,listener2,listener3;


    private String group;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseEvents,databaseGroups, databaseUserEvents;

    private Double fLat=null, fLng=null;
    private static final int CONTACTS_REQUEST=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        //receives extra from current location
        Double cLat=getIntent().getExtras().getDouble("lat");
        Double cLng=getIntent().getExtras().getDouble("lng");

        //if event was made through long click
        Double lcLat=getIntent().getExtras().getDouble("lcLat");
        Double lcLng=getIntent().getExtras().getDouble("lcLng");

        eName=(EditText)findViewById(R.id.eName);
        eDate=(EditText)findViewById(R.id.eDate);
        eLocation =(TextView)findViewById(R.id.eLocation);
        eDesc=(EditText)findViewById(R.id.eDesc);
        eEmail=(MultiAutoCompleteTextView)findViewById(R.id.eEmail);



        ImageView backbtn=(ImageView)findViewById(R.id.backbtn);
        TextView ePublic=(TextView) findViewById(R.id.ePublic);
        TextView eFriends=(TextView) findViewById(R.id.eFriends);
        TextView ePrivate=(TextView) findViewById(R.id.ePrivate);



        //eTitle -> button
        eName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                calendarPop();
                return true;
            }
        });
        eDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarPop();
            }
        });

        //backbtn action
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //sets the current date to edittext
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd-yyyy", Locale.US);
        String date=sdf.format(java.util.Calendar.getInstance().getTime());
        eDate.setText(date);

        //sets the given location
        if(lcLat!=0.0&&lcLng!=0.0){
            fLat=lcLat;
            fLng=lcLng;
            eLocation.setText(fLat+","+fLng);

        }else{
            fLat=cLat;
            fLng=cLng;
            eLocation.setText(fLat+","+fLng);
        }



        permission();

        //upload
        ePublic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if event requirements are all set
                //gets emails
                final String email_list=eEmail.getText().toString().trim();
                if(eventCheck()){
                    group="public";
                    uploadEvent(group,email_list);
                }

            }
        });
        eFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if event requirements are all set
                final String email_list=eEmail.getText().toString().trim();
                if(eventCheck()){
                    group="friends";
                    uploadEvent(group,email_list);
                }

            }
        });
        ePrivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if event requirements are all set
                final String email_list=eEmail.getText().toString().trim();
                if(eventCheck()){
                    group="private";
                    uploadEvent(group,email_list);
                }

            }
        });


    }


    private void permission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                        CONTACTS_REQUEST);
            }
        }else{
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getNameEmailDetails());
            eEmail.setAdapter(adapter);
            eEmail.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        }
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONTACTS_REQUEST&& resultCode == RESULT_OK && data != null ) {
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getNameEmailDetails());
            eEmail.setAdapter(adapter);
            eEmail.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        }
    }
    private boolean eventCheck(){
        if(eName.getText().toString().isEmpty()){
            eName.requestFocus();
            eName.setError("Event Requires Name!");
            return false;
        }
        if(eDate.getText().toString().isEmpty()){
            eDate.requestFocus();
            eDate.setError("Event Requires Date!");
            return false;
        }

        return true;
    }


    //gets date
    @TargetApi(Build.VERSION_CODES.N)
    private void calendarPop(){

        final Calendar myCalendar = Calendar.getInstance();

         DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                addDate(myCalendar);

            }

        };

        //picks min date+shows the dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();

    }

    //sets date on edittext
    @TargetApi(Build.VERSION_CODES.N)
    private void addDate(Calendar myCalendar) {
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        eDate.setText(sdf.format(myCalendar.getTime()));

    }

    private void uploadEvent(final String group, String email_list){
        final String title=eName.getText().toString().trim();
        final String date=eDate.getText().toString().trim();




        //gets user's UID
        mAuth= FirebaseAuth.getInstance();
        final String userUID=mAuth.getCurrentUser().getUid();

        databaseEvents= FirebaseDatabase.getInstance().getReference().child("Events");
        databaseEvents.keepSynced(true);
        final DatabaseReference databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);
        databaseGroups= FirebaseDatabase.getInstance().getReference().child("Groups").child(userUID);
        databaseEvents.keepSynced(true);
        databaseUserEvents= FirebaseDatabase.getInstance().getReference().child("UserEvents");
        databaseUserEvents.keepSynced(true);
        final DatabaseReference databaseNotif= FirebaseDatabase.getInstance().getReference().child("Notifications");
        databaseNotif.keepSynced(true);



        //random key
        final DatabaseReference newPost=databaseEvents.push();
        final String event_key=newPost.getKey();

        //2==yes
        //1==no
        //0==haven't responded
        databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String hostName=(String)dataSnapshot.child(userUID).child("name").getValue();

                databaseUserEvents.child(userUID).child(event_key).child("attending").setValue(true);
                newPost.child("eventID").setValue(event_key);
                newPost.child("title").setValue(title);
                newPost.child("date").setValue(date);
                newPost.child("desc").setValue(eDesc.getText().toString().trim());
                newPost.child("lat").setValue(fLat);
                newPost.child("lng").setValue(fLng);
                newPost.child("group").setValue(group);
                newPost.child("hostID").setValue(userUID);
                newPost.child("hostname").setValue(hostName);
                newPost.child("RSVP").child(userUID).child("attending").setValue(2);
                newPost.child("RSVP").child(userUID).child("userUID").setValue(userUID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //loops to the users' friends
        if(group.equals("friends")){

            listener2=databaseGroups.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    databaseUserEvents.child(userUID).child(event_key).child("attending").setValue(2);
                    databaseUserEvents.child(userUID).child(event_key).child("lat").setValue(fLat);
                    databaseUserEvents.child(userUID).child(event_key).child("lng").setValue(fLng);
                    databaseUserEvents.child(userUID).child(event_key).child("group").setValue(group);
                    databaseUserEvents.child(userUID).child(event_key).child("userUID").setValue(userUID);

                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        //invited_list.add(String.valueOf(dsp.getKey())); //add result into array list
                        String user2UID = dsp.getKey();
                        Log.d("FRIENDLIST", user2UID);
                        databaseEvents.child(event_key).child("RSVP").child(user2UID).child("attending").setValue(0);
                        databaseEvents.child(event_key).child("RSVP").child(user2UID).child("userUID").setValue(user2UID);

                        databaseUserEvents.child(user2UID).child(event_key).child("attending").setValue(0);
                        databaseUserEvents.child(user2UID).child(event_key).child("lat").setValue(fLat);
                        databaseUserEvents.child(user2UID).child(event_key).child("lng").setValue(fLng);
                        databaseUserEvents.child(user2UID).child(event_key).child("group").setValue(group);
                        databaseUserEvents.child(user2UID).child(event_key).child("userUID").setValue(user2UID);

                        //NOTIFICATIONS
                        HashMap<String, String> notifData=new HashMap<>();
                        notifData.put("from", userUID);
                        notifData.put("type", "event");
                        databaseNotif.child(user2UID).push().setValue(notifData);

                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        //adds event to user
        if(group.equals("private")){
            listener3=databaseUserEvents.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    databaseUserEvents.child(userUID).child(event_key).child("attending").setValue(true);
                    databaseUserEvents.child(userUID).child(event_key).child("lat").setValue(fLat);
                    databaseUserEvents.child(userUID).child(event_key).child("lng").setValue(fLng);
                    databaseUserEvents.child(userUID).child(event_key).child("group").setValue(userUID);

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        if(email_list.isEmpty()){
            finish();
        }else{
            //sends email;
            sendEmail(title, date, email_list);
        }



    }


    public String[] getNameEmailDetails() {
        ArrayList<String> emlRecs = new ArrayList<String>();
        HashSet<String> emlRecsHS = new HashSet<String>();
        Context context = CreateEventActivity.this;
        ContentResolver cr = context.getContentResolver();
        String[] PROJECTION = new String[] { ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID };
        String order = "CASE WHEN "
                + ContactsContract.Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + ContactsContract.Contacts.DISPLAY_NAME
                + ", "
                + ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE NOCASE";
        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
        if (cur.moveToFirst()) {
            do {
                // names comes in hand sometimes
                String name = cur.getString(1);
                String emlAddr = cur.getString(3);

                // keep unique only
                if (emlRecsHS.add(emlAddr.toLowerCase())) {
                    emlRecs.add(emlAddr);
                }
            } while (cur.moveToNext());
        }

        cur.close();

        String[] array = emlRecs.toArray(new String[0]);
        return array;
    }


    private void sendEmail(String title, String date, String email_list){
        List<String> email_array = new ArrayList<String>(Arrays.asList(email_list.split(" ")));

        //sendto?
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");

        //sends email to multiple people
        for(int j = 0; j < email_array.size(); j++){
            i.putExtra(android.content.Intent.EXTRA_EMAIL,
                    email_array.toArray(new String[email_array.size()]));
        }

        i.putExtra(Intent.EXTRA_SUBJECT, title+" Event");
        i.putExtra(Intent.EXTRA_TEXT   , "I am inviting you to an event on "+date+
                ".\nPlease download the app Spotz for RSVP!\nlink ");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (listener1 != null) {
            databaseEvents.removeEventListener(listener1);
        }
        if (listener2 != null) {
            databaseGroups.removeEventListener(listener2);
        }

        if (listener3 != null) {
            databaseUserEvents.removeEventListener(listener3);
        }
    }
}
