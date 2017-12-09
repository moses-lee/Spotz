package com.spotz2share.spotz;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private ProgressBar rProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);




        TextView rButton = (TextView) findViewById(R.id.rButton);
        rProgress = (ProgressBar) findViewById(R.id.rProgress);

        rButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                registerCheck();
            }
        });
    }

    private void registerCheck(){
        EditText rEmail = (EditText) findViewById(R.id.rEmail);
        EditText rName=(EditText)findViewById(R.id.rName);
        EditText rPassword = (EditText) findViewById(R.id.rPass);

        String name = rName.getText().toString().trim().toLowerCase();
        String email = rEmail.getText().toString().trim();
        String password = rPassword.getText().toString().trim();

        //checks for correct format
        if (TextUtils.isEmpty(name)) {
            rName.requestFocus();
            rName.setError("Name field is empty!");
            return;
        }
        if (name.length()>15) {
            rName.requestFocus();
            rName.setError("Name must be 15 characters max!");
            return;
        }
        if (name.contains(" ")) {
            rName.requestFocus();
            rName.setError("No Spaces!");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            rEmail.requestFocus();
            rEmail.setError("Email field is empty!");
            return;
        }
        if(!isEmailValid(email)){
            rEmail.requestFocus();
            rEmail.setError("Invalid Email!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            rPassword.requestFocus();
            rPassword.setError("Password field is empty!");
            return;
        }
        if (password.length() < 8) {
            rPassword.requestFocus();
            rPassword.setError("Password must be 8 characters minimum!");
            return;
        }
        if (password.contains(" ")) {
            rPassword.requestFocus();
            rPassword.setError("No Spaces Allowed!");
            return;
        }
        //adds user to auth
        createUser(name, email, password);
    }

    private void createUser(final String name, final String email, final String password){
        //Get Firebase auth instance
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        rProgress.setVisibility(View.VISIBLE);
        //create user
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //adds user to Database
                        register(mAuth, name, email);
                    } else {
                        rProgress.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "Registration Failed" + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    public void register(FirebaseAuth mAuth, String name, String email){
        String userUID = mAuth.getCurrentUser().getUid();
        DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(userUID);
        databaseUsers.keepSynced(true);
        DatabaseReference databaseNames = FirebaseDatabase.getInstance().getReference().child("Names");
        databaseNames.keepSynced(true);

        String deviceToken= FirebaseInstanceId.getInstance().getToken();

        //sets name
        databaseNames.child(name).setValue(userUID);
        //sets values in the database
        databaseUsers.child("name").setValue(name);
        databaseUsers.child("email").setValue(email);
        databaseUsers.child("userUID").setValue(userUID);
        //sets default profile pic
        databaseUsers.child("profilepic").setValue("https://firebasestorage.googleapis.com/v0/b/spotz-d4399.appspot.com/o/profilepics%2Fprofilepic_default.png?alt=media&token=24b93147-22db-42a5-913f-e21c99286708");
        databaseUsers.child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent registeri = new Intent(RegisterActivity.this, MainActivity.class);
                registeri.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(registeri);
                rProgress.setVisibility(View.GONE);
                //finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        rProgress.setVisibility(View.GONE);
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


}