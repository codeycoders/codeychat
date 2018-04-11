package com.tejasmehta.codeychat;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
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
import java.util.Map;

public class PassJoin extends AppCompatActivity {

    EditText grpName;
    EditText grpPass;
    Button joinG;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    Boolean groupExist = false;
    String grpVal;
    String groupPass;
    AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_join);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        grpName = findViewById(R.id.editText11);
        grpPass = findViewById(R.id.editText12);
        joinG = findViewById(R.id.button10);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        MobileAds.initialize(this, "ca-app-pub-3858453173804119~8679926603");

        AdView adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-3858453173804119/1398959114");
        mAdView = findViewById(R.id.adView3);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

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

                                    mDatabase.child("chat").child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            final Map<String, Object> count = (Map<String, Object>) dataSnapshot.getValue();
                                            if (count != null) {

                                                final String[] grps = count.keySet().toArray(new String[0]);
                                                Log.i("grps", count.keySet().toString());

                                                for (int i = 0; i < count.keySet().size(); i++) {

                                                    Log.i("grp", grps[i]);
                                                    final int num = i;
                                                    mDatabase.child("chat").child("groups").child(grps[i]).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            String grpJoinN = dataSnapshot.getValue(String.class);
                                                            if (grpJoinN != null) {

                                                                Log.i("grpN", grpJoinN);

                                                                if (grpJoinN.equals(grpName.getText().toString())) {

                                                                    Log.i("equal", "y");
                                                                    groupExist = true;
                                                                    grpVal = grps[num];

                                                                }

                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });



                                                        Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                if (num == (grps.length - 1)) {

                                                                    if (groupExist) {

                                                                        mDatabase.child("chat").child("groups").child(grpVal).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                                String password = dataSnapshot.getValue(String.class);
                                                                                if (password != null) {

                                                                                    if (password.equals(groupPass)) {


                                                                                        mDatabase.child("chat").child("groups").child(grpVal).child("people").child(mAuth.getCurrentUser().getUid()).setValue(username);
                                                                                        Intent chat = new Intent(getApplicationContext(), ChatsActivity.class);
                                                                                        chat.putExtra("tab", 2);
                                                                                        startActivity(chat);


                                                                                    }

                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                                    } else {

                                                                        Toast.makeText(PassJoin.this, "Group Does not Exist or the getName Entered is Wrong", Toast.LENGTH_SHORT).show();

                                                                    }

                                                                }

                                                            }

                                                        }, 500);





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

                } else {

                    Toast.makeText(PassJoin.this, "Enter a Group getName and Password Please", Toast.LENGTH_SHORT).show();

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
