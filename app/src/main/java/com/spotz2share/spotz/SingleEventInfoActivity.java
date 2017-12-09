package com.spotz2share.spotz;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spotz2share.spotz.getters.RSVPGetter;
import com.squareup.picasso.Picasso;

public class SingleEventInfoActivity extends AppCompatActivity {


    private String eventID=null;
    private RecyclerView mRecycle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_event_info);

        //recyclerview
        mRecycle= (RecyclerView)findViewById(R.id.mRecycle);
        mRecycle.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecycle.setLayoutManager(mLayoutManager);

        eventID=getIntent().getExtras().getString("eventID");
        getEventInfo();
        getRSVP();
    }

    private void getEventInfo(){
        DatabaseReference databaseEvents= FirebaseDatabase.getInstance().getReference().child("Events");
        databaseEvents.keepSynced(true);
        final TextView eventTitle=(TextView)findViewById(R.id.eventTitle);
        final TextView eventHost=(TextView)findViewById(R.id.eventHost);
        final TextView eventLocation=(TextView)findViewById(R.id.eventLocation);
        final TextView eventDesc=(TextView)findViewById(R.id.eventDesc);


        databaseEvents.child(eventID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title= (String)dataSnapshot.child("title").getValue();
                String hostname= (String)dataSnapshot.child("hostname").getValue();
                String desc= (String)dataSnapshot.child("desc").getValue();
                Double lat= (Double)dataSnapshot.child("lat").getValue();
                Double lng= (Double)dataSnapshot.child("lng").getValue();

                eventTitle.setText(title);
                eventHost.setText(hostname);
                eventDesc.setText(desc);
                if(lat!=0.0||lng!=0.0)
                    eventLocation.setText(String.valueOf(lat)+","+String.valueOf(lng));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getRSVP(){
        DatabaseReference databaseEvents= FirebaseDatabase.getInstance().getReference().child("Events").child(eventID).child("RSVP");
        databaseEvents.keepSynced(true);
        FirebaseRecyclerAdapter<RSVPGetter, ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<RSVPGetter, ImageViewHolder>(
                RSVPGetter.class,
                R.layout.image_row_rsvp,
                ImageViewHolder.class,
                databaseEvents

        ) {
            @Override
            protected void populateViewHolder(ImageViewHolder viewHolder, RSVPGetter model, int position) {

                final String user2ID=getRef(position).getKey();

                viewHolder.setAttending(model.getAttending());
                viewHolder.setUserUID(getApplicationContext(), model.getUserUID());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

            }
        };
        mRecycle.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder{
        View mView;
        FirebaseAuth mAuth;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            mAuth=FirebaseAuth.getInstance();

        }

        public void setAttending(long attending){
            TextView rStatus=(TextView) mView.findViewById(R.id.rStatus);

            switch ((int)attending){
                case 2:
                    rStatus.setText(R.string.yes);
                    rStatus.setTextColor(Color.parseColor("#00BFFF"));
                    break;
                case 1:
                    rStatus.setText(R.string.no);
                    rStatus.setTextColor(Color.parseColor("#FF0033"));
                    break;
                case 0:
                    rStatus.setText("MAYBE");
                    break;
            }

        }

        public void setUserUID(final Context ctx, final String userUID){
            DatabaseReference databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
            databaseUsers.keepSynced(true);
            databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String profilepic=(String)dataSnapshot.child(userUID).child("profilepic").getValue();
                    String name=(String)dataSnapshot.child(userUID).child("name").getValue();

                    ImageView rImage=(ImageView)mView.findViewById(R.id.rImage);
                    TextView rName=(TextView) mView.findViewById(R.id.rName);
                    Picasso.with(ctx).load(profilepic).into(rImage);
                    rName.setHint(name);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}
