package com.spotz2share.spotz;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import android.view.Display;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spotz2share.spotz.nav_fragments.ExploreFragment;
import com.spotz2share.spotz.nav_fragments.GroupFragment;
import com.spotz2share.spotz.nav_fragments.MapsFragment;
import com.spotz2share.spotz.nav_fragments.NotifFragment;
import com.spotz2share.spotz.nav_fragments.ProfileFragment;
import com.spotz2share.spotz.nav_fragments.SettingsFragment;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment fragment ;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private ValueEventListener listener;
    private DatabaseReference databaseUsers;
    private String tab_code=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //drawer stuff
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        //sets ExploreFragment when started
        fragmentManager.beginTransaction().add(R.id.frame, new ExploreFragment()).commit();
        navigationView.setCheckedItem(R.id.nav_explore);

        //checks if user is logged in or not
        auth();

        databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);

        //set nav bar profile
        mAuth = FirebaseAuth.getInstance();
        String currentUserId=mAuth.getCurrentUser().getUid();
         navProfile(navigationView, currentUserId);

        //saves screen size to sp
        SharedPreferences sharedPref = getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("screensize", String.valueOf(screenSize()));
        editor.apply();

        try{
            tab_code=getIntent().getExtras().getString("tab_code");
            if(tab_code.equals("profile")){
                fragmentManager.beginTransaction().replace(R.id.frame, new ProfileFragment()).addToBackStack("fragBack").commit();
                navigationView.getMenu().getItem(3).setChecked(true);
            }
        }catch (NullPointerException e){
            Log.v("Main:", "Starting");
        }


    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        switch (id){
            case R.id.nav_explore:
                fragment= new ExploreFragment();
                // Set action bar title
                setTitle(getResources().getString(R.string.app_name));
                break;
            case R.id.nav_map:
                fragment= new MapsFragment();
                setTitle(item.getTitle());
                break;

            case R.id.nav_groups:
                fragment= new GroupFragment();
                setTitle(item.getTitle());
                break;
            case R.id.nav_profile:
                fragment= new ProfileFragment();
                setTitle(item.getTitle());
                break;
            case R.id.nav_notif:
                fragment= new NotifFragment();
                setTitle(item.getTitle());
                break;
            case R.id.nav_settings:
                fragment= new SettingsFragment();
                setTitle(item.getTitle());
                break;

            default:
                Toast.makeText(this, "Error:Null", Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Insert the fragment by replacing any existing fragment

        fragmentManager.beginTransaction().replace(R.id.frame, fragment).addToBackStack("fragBack").commit();


        // Highlight the selected item has been done by NavigationView
        item.setChecked(true);

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void auth(){

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final String userUID=firebaseAuth.getCurrentUser().getUid();
                if (userUID.isEmpty()||userUID.equals("")) {
                    Log.d("TAG", "onAuthStateChanged:signed_out:");
                    //User Signed out
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        };


    }

    private void navProfile(NavigationView navigationView, String currentUserId){
        View nView =  navigationView.getHeaderView(0);
        final TextView nName=(TextView)nView.findViewById(R.id.nav_name);
        final ImageView nPic=(ImageView)nView.findViewById(R.id.nav_pic);

        //grabs profile pic and name from database
        listener=databaseUsers.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                String profilepic=(String)dataSnapshot.child("profilepic").getValue();

                //saves
                SharedPreferences sharedPref = getSharedPreferences("pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", name);
                editor.apply();

                //sets pic and name
                nName.setText(name);
                if(profilepic!=null&&!profilepic.isEmpty()){
                    Picasso.with(MainActivity.this).load(profilepic).into(nPic);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public int screenSize(){
        //gets screen size, divide by 3 for grid of pictures
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        return width/3;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (listener != null) {
            databaseUsers.removeEventListener(listener);
        }
    }
}
