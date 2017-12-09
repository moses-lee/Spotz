package com.spotz2share.spotz.nav_fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spotz2share.spotz.CameraActivity;
import com.spotz2share.spotz.CreateEventActivity;
import com.spotz2share.spotz.R;
import com.spotz2share.spotz.SingleEventActivity;
import com.spotz2share.spotz.SingleEventInfoActivity;


public class MapsFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "Map Style Failed";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mMap;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;

    private DatabaseReference databaseEvents, databaseUsers, databaseGroups, databaseUserEvents;
    private String userUID;
    private double ulat,ulng;
    private ChildEventListener listener1,listener3;
    private ValueEventListener listener4;
    private FloatingActionButton fab_add;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        //checks for android version for permissions
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();

        }
        fab_add=(FloatingActionButton)view.findViewById(R.id.fab_add);
        final ImageView mPublic=(ImageView)view.findViewById(R.id.mPublic);
        final ImageView mFriends=(ImageView)view.findViewById(R.id.mFriends);
        final ImageView mPrivate=(ImageView)view.findViewById(R.id.mPrivate);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //grabs user's id
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        //grabs databases
        databaseEvents = FirebaseDatabase.getInstance().getReference().child("Events");
        databaseEvents.keepSynced(true);
        databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);
        databaseGroups = FirebaseDatabase.getInstance().getReference().child("Groups");
        databaseGroups.keepSynced(true);
        databaseUserEvents = FirebaseDatabase.getInstance().getReference().child("UserEvents").child(userUID);
        databaseUserEvents.keepSynced(true);


        //sets up map with public as default
        mPublic.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        mPublic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mPublic.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                mFriends.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorGray));
                mPrivate.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorGray));
                //clears markers
                mMap.clear();
                dataPublic();
            }
        });

        mFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPublic.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorGray));
                mFriends.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                mPrivate.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorGray));
                mMap.clear();
                dataGroups();
            }
        });

        mPrivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPublic.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorGray));
                mFriends.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorGray));
                mPrivate.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                mMap.clear();
                dataPrivate();
            }
        });



        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

/*        // Customise the styling of the base map using a JSON object defined
        // in a raw resource file.
        try{

            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity(), R.raw.raw_maps));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }*/


        //map long click
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(mMap != null){
                    markerLong(latLng);
                }else{
                    Log.d("mMaps:", "Error");
                }

            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        //connects to google api client
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //after connection, sets interval for location updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(350);
        mLocationRequest.setFastestInterval(350);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(final Location location) {
        //gets last known location
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //gets new latLng
        ulat=location.getLatitude();
        ulng= location.getLongitude();
        final LatLng latLng = new LatLng(ulat, ulng);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Connection Failed", Toast.LENGTH_LONG).show();
    }


    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            }
            return false;
        } else {
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        //deals with permission result
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(getActivity(), "App Needs Location to Show Events", Toast.LENGTH_LONG).show();
                }
            }
        }}


     /************
     *Actual code*
     ************/

    Marker tempMarker=null;
    @Override
    public void onStart() {
        super.onStart();
        dataPublic();

        //removes the tempmarker
        if(tempMarker!=null){
            tempMarker.remove();
        }

        //adding an event, makes sure the lat and lng is not null

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ulat!=0.0&&ulng!=0.0) {
                    Intent newevent = new Intent(getActivity(), CreateEventActivity.class);
                    newevent.putExtra("lat", ulat);
                    newevent.putExtra("lng", ulng);
                    startActivity(newevent);
                }else{
                   Log.d("MapsFrag:", "Error, clocation=null");
                }
            }
        });

    }

    private void markerLong(LatLng latLng){
            // Vibration
            Vibrator vibrate = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            vibrate.vibrate(10);

            //gets location of the long click and creates event
             tempMarker= mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_lightning)));


            Double lcLat=latLng.latitude;
            Double lcLng=latLng.longitude;

            //sends latlng to createeventactivity
            Intent ceIntent=new Intent(getActivity(), CreateEventActivity.class);
            ceIntent.putExtra("lcLat", lcLat);
            ceIntent.putExtra("lcLng", lcLng);


            startActivity(ceIntent);

    }




    //places public markers
    private void dataPublic(){
        //grabs public events from database
        Query publicQuery= databaseEvents.orderByChild("group").equalTo("public");
        listener1=publicQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //loops markers
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    //grabs lat+lng values from /events
                    Double dlat = (Double) dataSnapshot.child("lat").getValue();
                    Double dlng = (Double) dataSnapshot.child("lng").getValue();
                    String key=(String)dataSnapshot.getKey();

                    //checks if double values are null
                    if(dlat==null||dlng==null){
                        Log.d(TAG, "onDataChange: Error Loading Doubles");
                    }else{
                        String slat = String.valueOf(dlat);
                        String slng = String.valueOf(dlng);
                        LatLng latLng=new LatLng(dlat,dlng);

                        Log.d("coordinates:", slat+slng);

                        //marker options
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                .title(key));

                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(final Marker marker) {
                                markerClick(marker, 2);
                                return true;
                            }

                        });
                    }
                }
               }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Grabbing PuMarkers:", "Cancelled");
            }
        });

    }

    //places friend markers
    private void dataGroups(){
        Query groupQuery= databaseUserEvents.orderByChild("group").equalTo("friends");
        listener3=groupQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //loops markers
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    //grabs lat+lng values from /events
                    final long attend = (long) dataSnapshot.child("attending").getValue();
                    Double dlat = (Double) dataSnapshot.child("lat").getValue();
                    Double dlng = (Double) dataSnapshot.child("lng").getValue();
                    String key=(String)dataSnapshot.getKey();

                    //checks if double values are null
                    if(dlat==null||dlng==null){
                        Log.d(TAG, "onDataChange: Error Loading Doubles");
                    }else{
                        String slat = String.valueOf(dlat);
                        String slng = String.valueOf(dlng);
                        LatLng latLng=new LatLng(dlat,dlng);

                        Log.d("coordinates:", slat+slng);

                        //marker options
                        MarkerOptions mOptions=new MarkerOptions();

                        if(attend==0){
                            mMap.addMarker(mOptions
                                    .position(latLng)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red))
                                    .title(key));
                        }else{
                            mMap.addMarker(mOptions
                                    .position(latLng)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                    .title(key));
                        }




                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(final Marker marker) {
                                markerClick(marker, attend);
                                return true;
                            }

                        });
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Grabbing FMarkers:", "Cancelled");
            }
        });
    }

    //places private markers
    private void dataPrivate(){
        //grabs private events from database
        Query privateQuery= databaseUserEvents.orderByChild("group").equalTo(userUID);
        listener3=privateQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //loops markers
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    //grabs lat+lng values from /events
                    Double dlat = (Double) dataSnapshot.child("lat").getValue();
                    Double dlng = (Double) dataSnapshot.child("lng").getValue();
                    String key=(String)dataSnapshot.getKey();

                    //checks if double values are null
                    if(dlat==null||dlng==null){
                        Log.d(TAG, "onDataChange: Error Loading Doubles");
                    }else{
                        String slat = String.valueOf(dlat);
                        String slng = String.valueOf(dlng);
                        LatLng latLng=new LatLng(dlat,dlng);

                        Log.d("coordinates:", slat+slng);

                        //marker options
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                .title(key));

                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(final Marker marker) {
                                markerClick(marker, 2);
                                return true;
                            }

                        });
                    }
                }
            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Grabbing PriMarkers:", "Cancelled");
            }
        });
    }


    //marker click action
    private void markerClick(final Marker marker, final long attend){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        float zoom=mMap.getCameraPosition().zoom;

        //zooms map only if too far away
        if(zoom<17){
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        }

        //querys for that specific event
        Query sQuery=databaseEvents.orderByKey().equalTo(marker.getTitle());
        listener1=sQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //grabs event key+tag for the selected event

                String eventID = dataSnapshot.getKey();
                String name = (String) dataSnapshot.child("title").getValue();

                Double plat = marker.getPosition().latitude;
                Double plng = marker.getPosition().longitude;

                if(attend==0){
                    popUpYN(eventID, name);
                }else{
                    popUp(eventID, name, plat, plng);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    //distance calculator
    private boolean distanceCalc(Double plat, Double plng){
        Boolean distance=false;

        Location locationA = new Location("User's Location");
        locationA.setLatitude(ulat);
        locationA.setLongitude(ulng);
        Location locationB = new Location("Marker Location");

        locationB.setLatitude(plat);
        locationB.setLongitude(plng);

        float fDistance = locationA.distanceTo(locationB);
        fDistance *= 0.000621371;

        //checks if distance from marker is less than 3 miles
        if(fDistance<3){
            distance=true;
        }

        return distance;
    }


    //opens popup for new event add
    private void popUp(final String eventID, String name, final Double plat, final Double plng){
        //opens popup
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
        View pview = getActivity().getLayoutInflater().inflate(R.layout.pop_view_event, null);

        mBuilder.setView(pview);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        //boolean to see if user is close enough to event
        final boolean fDistance=distanceCalc(plat, plng);

        TextView pop_tag=(TextView)pview.findViewById(R.id.pop_tag);
        TextView pop_cancel=(TextView)pview.findViewById(R.id.pop_cancel);
        TextView pop_view=(TextView)pview.findViewById(R.id.pop_view);
        TextView pop_go=(TextView)pview.findViewById(R.id.pop_go);
        final ImageView pop_cam=(ImageView)pview.findViewById(R.id.pop_cam);

        pop_tag.setText(name);

        //dismisses popup
        pop_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //opens google maps
        pop_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://maps.google.com/maps?daddr=" + plat + "," + plng;
                Intent gointent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(gointent);
            }
        });

        //view the event
        pop_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventviewintent=new Intent(getActivity(), SingleEventActivity.class);
                eventviewintent.putExtra("event_id", eventID);
                eventviewintent.putExtra("closeEnough", fDistance);
                eventviewintent.putExtra("backbtn", true);
                startActivity(eventviewintent);
            }
        });

        //changes color and allows user to add to an event if close enough
        if(!fDistance){
            pop_cam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "You are too far away!", Toast.LENGTH_LONG).show();
                }
            });
        }else{
            pop_cam.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
            pop_cam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent camintent=new Intent(getActivity(), CameraActivity.class);
                    camintent.putExtra("eventID", eventID);
                    camintent.putExtra("closeEnough", true);
                    startActivity(camintent);
                }
            });
        }
    }



    //opens popup for rsvp
    private void popUpYN(final String eventID, String name){
        //opens popup
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
        View pview = getActivity().getLayoutInflater().inflate(R.layout.pop_invite_event, null);

        mBuilder.setView(pview);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        TextView pop_title=(TextView)pview.findViewById(R.id.pop_title);
        TextView pop_yes=(TextView)pview.findViewById(R.id.pop_yes);
        TextView pop_no=(TextView)pview.findViewById(R.id.pop_no);
        TextView pop_info=(TextView)pview.findViewById(R.id.pop_info);

        pop_title.setText(name);
        //if user says yes to event
        //2==yes
        //1==no
        //0==haven't responded
        pop_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseUserEvents.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        databaseEvents.child(eventID).child("RSVP").child(userUID).child("attending").setValue(2);
                        databaseUserEvents.child(eventID).child("attending").setValue(2);
                        Toast.makeText(getActivity(), "You've Been Added to the Event!", Toast.LENGTH_LONG).show();
                        //updates the status
                        dataGroups();
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        pop_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseUserEvents.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        databaseEvents.child(eventID).child("RSVP").child(userUID).child("attending").setValue(1);
                        Toast.makeText(getActivity(), "You've Declined the Event", Toast.LENGTH_LONG).show();
                        //updates the status
                        dataGroups();
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                dialog.dismiss();
            }
        });

        //view event info
        pop_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventviewintent=new Intent(getActivity(), SingleEventInfoActivity.class);
                eventviewintent.putExtra("eventID", eventID);
                startActivity(eventviewintent);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        if (listener1 != null) {
            databaseEvents.removeEventListener(listener1);
        }
        if (listener3 != null) {
            databaseUserEvents.removeEventListener(listener3);
        }
        if (listener4 != null) {
            databaseUserEvents.removeEventListener(listener4);
        }

    }
}