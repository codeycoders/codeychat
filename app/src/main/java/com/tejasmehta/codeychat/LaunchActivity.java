package com.tejasmehta.codeychat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LaunchActivity extends AbstractActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        MobileAds.initialize(this, "ca-app-pub-3858453173804119~8679926603");


        final Button registerNew = findViewById(R.id.button3);
        final Button signIn = findViewById(R.id.button2);
        final EditText emailOrUser = findViewById(R.id.editText);
        final EditText password = findViewById(R.id.editText2);
        password.setVisibility(View.GONE);
        emailOrUser.setVisibility(View.GONE);
        registerNew.setVisibility(View.GONE);
        signIn.setVisibility(View.GONE);
        registerNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));

            }
        });



        if (mAuth.getCurrentUser() != null) {

            getSupportActionBar().setTitle("My Chats");


            mDatabase.child("versionCode").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String version = dataSnapshot.getValue(String.class);
                    if (version != null) {

                        try {
                            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
                            String versionA = pInfo.versionName;

                            if (!version.equals(versionA)) {

                                AlertDialog.Builder builder1 = new AlertDialog.Builder(LaunchActivity.this);
                                builder1.setMessage("Please Update to the Latest Version");
                                builder1.setCancelable(false);

                                builder1.setPositiveButton(
                                        "Show",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.tejasmehta.codeychat"));
                                                startActivity(intent);

                                            }
                                        });


                                AlertDialog alert11 = builder1.create();
                                alert11.show();


                            } else {

                                startActivity(new Intent(getApplicationContext(), ChatsActivity.class));


                            }

                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                    } else {


                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        } else {

            mDatabase.child("versionCode").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String version = dataSnapshot.getValue(String.class);
                    if (version != null) {

                        try {
                            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
                            String versionA = pInfo.versionName;

                            if (!version.equals(versionA)) {

                                AlertDialog.Builder builder1 = new AlertDialog.Builder(LaunchActivity.this);
                                builder1.setMessage("Please Update to the Latest Version");
                                builder1.setCancelable(false);

                                builder1.setPositiveButton(
                                        "Show",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.tejasmehta.codeychat"));
                                                startActivity(intent);

                                            }
                                        });


                                AlertDialog alert11 = builder1.create();
                                alert11.show();


                            } else {

                                getSupportActionBar().setTitle("Sign In");
                                password.setVisibility(View.VISIBLE);
                                emailOrUser.setVisibility(View.VISIBLE);
                                registerNew.setVisibility(View.VISIBLE);
                                signIn.setVisibility(View.VISIBLE);

                            }

                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                    } else {


                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!emailOrUser.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {

                    if (emailOrUser.getText().toString().contains("@")) {

                        mAuth.signInWithEmailAndPassword(emailOrUser.getText().toString().toLowerCase(), password.getText().toString())
                                .addOnCompleteListener(LaunchActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            startActivity(new Intent(getApplicationContext(), ChatsActivity.class));

                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w("Failed Auth", "signInWithEmail:failure", task.getException());
                                            Toast.makeText(LaunchActivity.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                        // ...
                                    }
                                });

                    } else {

                        mDatabase.child("users").child("c").child("usernameToEmail").child(emailOrUser.getText().toString()).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String email = dataSnapshot.getValue(String.class);
                                if (email != null) {

                                    mAuth.signInWithEmailAndPassword(email.replace(",", "."), password.getText().toString())
                                            .addOnCompleteListener(LaunchActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        // Sign in success, update UI with the signed-in user's information
                                                        startActivity(new Intent(getApplicationContext(), ChatsActivity.class));

                                                    } else {
                                                        // If sign in fails, display a message to the user.
                                                        Log.w("Failed Auth", "signInWithEmail:failure", task.getException());
                                                        Toast.makeText(LaunchActivity.this, "Authentication failed.",
                                                                Toast.LENGTH_SHORT).show();
                                                    }

                                                    // ...
                                                }
                                            });

                                } else {

                                    Toast.makeText(LaunchActivity.this, "Username not Found", Toast.LENGTH_SHORT).show();

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                }

            }
        });


    }
}
