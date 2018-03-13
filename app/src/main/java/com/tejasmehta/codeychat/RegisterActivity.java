package com.tejasmehta.codeychat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AbstractActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final EditText username = findViewById(R.id.editText3);
        final EditText email = findViewById(R.id.editText4);
        final EditText password1 = findViewById(R.id.editText5);
        final EditText password2 = findViewById(R.id.editText6);
        getSupportActionBar().setTitle("Register");

        Button toSignIn = findViewById(R.id.button5);
        toSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(), LaunchActivity.class));

            }
        });

        Button registerUser = findViewById(R.id.button4);
        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!username.getText().toString().isEmpty() && !email.getText().toString().isEmpty() && !password1.getText().toString().isEmpty() && !password2.getText().toString().isEmpty()) {

                    mDatabase.child("users").child("c").child("usernameToEmail").child(username.getText().toString()).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String emailCheck = dataSnapshot.getValue(String.class);
                            if (emailCheck != null) {

                                Toast.makeText(RegisterActivity.this, "Username is already in use. Please choose another", Toast.LENGTH_LONG).show();

                            } else {

                                if (password1.getText().toString().equals(password2.getText().toString())) {

                                    mAuth.createUserWithEmailAndPassword(email.getText().toString().toLowerCase(), password1.getText().toString())
                                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        String emailReplace = email.getText().toString().replace(".", ",");
                                                        // Sign in success, update UI with the signed-in user's information
                                                        Log.d("SignUp", "createUserWithEmail:success");
                                                        FirebaseUser user = mAuth.getCurrentUser();
                                                        Toast.makeText(RegisterActivity.this, "Registration Success! Please verify your email through the verification email that was sent. Thank you!", Toast.LENGTH_LONG).show();
                                                        mDatabase.child("users").child(user.getUid()).child("username").setValue(username.getText().toString().toLowerCase());
                                                        mDatabase.child("users").child(user.getUid()).child("email").setValue(emailReplace.toLowerCase());
                                                        mDatabase.child("users").child(user.getUid()).child("uid").setValue(user.getUid());
                                                        mDatabase.child("users").child("c").child("emailToUid").child(emailReplace.toLowerCase()).child("uid").setValue(user.getUid());
                                                        mDatabase.child("users").child("c").child("uidToEmail").child(user.getUid()).child("email").setValue(emailReplace.toLowerCase());
                                                        mDatabase.child("users").child("c").child("usernameToEmail").child(username.getText().toString().toLowerCase()).child("email").setValue(emailReplace.toLowerCase());
                                                        mDatabase.child("users").child("c").child("emailToUsername").child(emailReplace.toLowerCase()).child("username").setValue(username.getText().toString().toLowerCase());
                                                        mDatabase.child("users").child("names").child(username.getText().toString().toLowerCase()).child("username").setValue(username.getText().toString().toLowerCase());
                                                        startActivity(new Intent(getApplicationContext(), ChatsActivity.class));
                                                    } else {
                                                        // If sign in fails, display a message to the user.
                                                        try {
                                                            throw task.getException();
                                                        } catch(FirebaseAuthWeakPasswordException e) {
                                                            Toast.makeText(RegisterActivity.this, "Password is too weak(At least 6 characters)", Toast.LENGTH_SHORT).show();
                                                        } catch(FirebaseAuthInvalidCredentialsException e) {
                                                            Toast.makeText(RegisterActivity.this, "Email is invalid", Toast.LENGTH_SHORT).show();
                                                        } catch(FirebaseAuthUserCollisionException e) {
                                                            Toast.makeText(RegisterActivity.this, "Email is already in use", Toast.LENGTH_SHORT).show();
                                                        } catch(Exception e) {
                                                            Toast.makeText(RegisterActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    // ...
                                                }
                                            });

                                } else {

                                    Toast.makeText(RegisterActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();

                                }

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {

                    Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }
}
