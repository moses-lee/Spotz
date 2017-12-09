package com.spotz2share.spotz;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar lProgress;
    private FirebaseAuth mAuth;
    private CoordinatorLayout v;
    private DatabaseReference databaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        v=(CoordinatorLayout)findViewById(R.id.lView);
        TextView loginBtn=(TextView)findViewById(R.id.loginBtn);
        TextView registerBtn=(TextView)findViewById(R.id.registerBtn);
        lProgress=(ProgressBar)findViewById(R.id.lProgress);


        databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");

        //login action
        final EditText lPass=(EditText)findViewById(R.id.lPass);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn(lPass);
                //dismisses keyboard
                InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        //edittext -> button
        lPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                logIn(lPass);
                return false;
            }
        });

        //register
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rIntent=new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(rIntent);
            }
        });
    }

    private void logIn(EditText lPass){
        EditText lEmail=(EditText)findViewById(R.id.lEmail);

        String email = lEmail.getText().toString().trim();
        String password = lPass.getText().toString().trim();

        //checks if fields are empty
        if (TextUtils.isEmpty(email)&&TextUtils.isEmpty(password)){
            Snackbar.make(v, "Both fields are missing", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Snackbar.make(v, "Email field is missing", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Snackbar.make(v, "Password field is missing", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            return;
        }

        lProgress.setVisibility(View.VISIBLE);
        auth(email, password, lEmail, lPass);
    }

    //authenticate user
    private void auth(final String email, final String password, final EditText lEmail, final EditText lPass){
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    lProgress.setVisibility(View.GONE);
                    if (!task.isSuccessful()) {
                        if(!isEmailValid(email)){
                            lEmail.requestFocus();
                            lEmail.setError("Invalid Email!");
                        }else
                            if (password.length() < 8) {
                                lPass.requestFocus();
                                lPass.setError("Password must be at least 8 characters!");
                            }else {
                                Snackbar.make(v, "Account Not Registered", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    } else {
                        String userUID=mAuth.getCurrentUser().getUid();
                        String deviceToken= FirebaseInstanceId.getInstance().getToken();
                        databaseUsers.child(userUID).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent loginintent = new Intent(LoginActivity.this, MainActivity.class);
                                loginintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(loginintent);
                                finish();
                            }
                        });
                    }
                }
            });

    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
        Log.v("LoginActivity:", "backpress");
    }
}
