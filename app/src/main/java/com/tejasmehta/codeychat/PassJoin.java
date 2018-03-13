package com.tejasmehta.codeychat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PassJoin extends AppCompatActivity {

    EditText grpName;
    EditText grpPass;
    Button joinG;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    Boolean groupExist;
    String grpVal;
    String groupPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_join);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        grpName = findViewById(R.id.editText11);
        grpPass = findViewById(R.id.editText12);
        joinG = findViewById(R.id.button10);

        joinG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!grpName.getText().toString().isEmpty() && !grpPass.getText().toString().isEmpty()) {

                    try {
                        groupPass = sha256(grpPass.getText().toString());
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if (mAuth.getCurrentUser() != null) {

                        String email = mAuth.getCurrentUser().getEmail().replace(".", ",");
                        mDatabase.child("users").child("c").child("emailToUsername").child(email).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final String username = dataSnapshot.getValue(String.class);
                                if (username != null) {

                                    mDatabase.child("chat").child("groups").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            Long count = dataSnapshot.getValue(Long.class);
                                            if (count != null) {

                                                for (int i = 1; i <= count; i++) {

                                                    final int num = i;
                                                    mDatabase.child("chat").child("groups").child("group" + i).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            String grpJoinN = dataSnapshot.getValue(String.class);
                                                            if (grpJoinN != null) {

                                                                if (grpJoinN.equals(grpName.getText().toString())) {

                                                                    groupExist = true;
                                                                    grpVal = "group" + num;

                                                                }

                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                    if (i == count && groupExist) {

                                                        mDatabase.child("chat").child("groups").child(grpVal).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                String password = dataSnapshot.getValue(String.class);
                                                                if (password != null) {

                                                                    if (password.equals(groupPass)) {

                                                                        mDatabase.child("chat").child("groups").child(grpVal).child("pplCnt").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                                Long pplCount = dataSnapshot.getValue(Long.class);
                                                                                if (pplCount != null) {

                                                                                    mDatabase.child("chat").child("groups").child(grpVal).child("people").child("person" + pplCount).setValue(username);
                                                                                    mDatabase.child("chat").child("groups").child(grpVal).child("people").child("pplCnt").setValue(pplCount + 1);


                                                                                }

                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    }

                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }

                                                }

                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

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

    String sha256(String textToHash) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // Change this to UTF-16 if needed
        md.update(textToHash.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();

        String hex = String.format("%064x", new BigInteger(1, digest));
        return hex;
    }
}
