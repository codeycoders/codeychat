package com.tejasmehta.codeychat;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class FinalGroupChooseActivity extends AppCompatActivity {

    String names;
    String[] split;
    TextView namesPeople;
    EditText groupName;
    EditText groupPass;
    EditText groupPassConf;
    Button createG;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    Boolean groupN = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_group_choose);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        namesPeople = findViewById(R.id.textView3);
        groupName = findViewById(R.id.editText7);
        groupPass = findViewById(R.id.editText9);
        groupPassConf = findViewById(R.id.editText10);
        createG = findViewById(R.id.button8);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        try {
            names = getIntent().getExtras().getString("people");
            Log.i("names", names);
        }catch(Exception e) {


        }

        if (names != null) {

            split = names.split(",");
            Log.i("s", "y");
            for (int i = 0; i < (split.length - 1); i++) {

                String nameC = "-" + split[i];

                if (nameC.contains(" ")) {

                    namesPeople.setText(namesPeople.getText() + nameC + "\n");

                } else {

                    nameC = nameC.replace("-", "- ");
                    namesPeople.setText(namesPeople.getText() + nameC + "\n");


                }

            }

        }

        createG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            if (!groupName.getText().toString().isEmpty() && !groupPass.getText().toString().isEmpty() && !groupPassConf.getText().toString().isEmpty()) {

                if (mAuth.getCurrentUser() != null) {

                    if (groupPass.getText().toString().equals(groupPassConf.getText().toString())) {

                        final String name = groupName.getText().toString();

                        mDatabase.child("chat").child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final Map<String, Object> groupNames = (Map<String, Object>) dataSnapshot.getValue();
                                if (groupNames != null) {

                                    for(int i = 0; i < groupNames.keySet().size(); i++) {

                                        final int num = i;
                                        if (name.equals(groupNames.keySet().toArray()[i])) {

                                            groupN = true;

                                        }

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                             if (num == (groupNames.keySet().size() - 1)) {

                                                 if (groupN) {

                                                     Toast.makeText(FinalGroupChooseActivity.this, "Group getName already in Use", Toast.LENGTH_SHORT).show();

                                                 } else {

                                                     for (int n = 0; n < split.length; n++) {

                                                         final int numero = n;

                                                         mDatabase.child("users").child("c").child("usernameToEmail").child(split[numero].replace(" ", "")).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                                                             @Override
                                                             public void onDataChange(DataSnapshot dataSnapshot) {

                                                                 String email = dataSnapshot.getValue(String.class);
                                                                 if (email != null) {

                                                                     mDatabase.child("users").child("c").child("emailToUid").child(email).child("uid").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                         @Override
                                                                         public void onDataChange(DataSnapshot dataSnapshot) {

                                                                             String uid = dataSnapshot.getValue(String.class);
                                                                             mDatabase.child("chat").child("groups").child(name).child("people").child(uid).setValue(split[numero].replace(" ", ""));

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

                                                         if (n == split.length - 1) {

                                                             mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                 @Override
                                                                 public void onDataChange(DataSnapshot dataSnapshot) {

                                                                     String username = dataSnapshot.getValue(String.class);
                                                                     if (username != null) {

                                                                         mDatabase.child("chat").child("groups").child(name).child("admin").setValue(username);
                                                                         mDatabase.child("chat").child("groups").child(name).child("name").setValue(name);
                                                                         try {
                                                                             mDatabase.child("chat").child("groups").child(name).child("password").setValue(sha256(groupPass.getText().toString()));
                                                                         } catch (NoSuchAlgorithmException e) {
                                                                             e.printStackTrace();
                                                                         } catch (UnsupportedEncodingException e) {
                                                                             e.printStackTrace();
                                                                         }

                                                                         mDatabase.child("chat").child("groups").child("notifRefs").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                             @Override
                                                                             public void onDataChange(DataSnapshot dataSnapshot) {

                                                                                 Long countOfGrpNotif = dataSnapshot.getValue(Long.class);
                                                                                 Log.i("in", "y");
                                                                                 if (countOfGrpNotif != null) {

                                                                                     Long notfsCnt = countOfGrpNotif + 1;

                                                                                     mDatabase.child("chat").child("groups").child("notifRefs").child(name).setValue("group" + notfsCnt);
                                                                                     mDatabase.child("chat").child("groups").child("notifRefs").child("count").setValue(notfsCnt);
                                                                                     Intent newA = new Intent(getApplicationContext(), ChatsActivity.class);
                                                                                     newA.putExtra("tab", 2);
                                                                                     startActivity(newA);
                                                                                     Log.i("exist", "Y");

                                                                                 } else {

                                                                                     Log.i("exist", "N");

                                                                                     mDatabase.child("chat").child("groups").child("notifRefs").child(name).setValue("group1");
                                                                                     mDatabase.child("chat").child("groups").child("notifRefs").child("count").setValue(1);
                                                                                     Intent newA = new Intent(getApplicationContext(), ChatsActivity.class);
                                                                                     newA.putExtra("tab", 2);
                                                                                     startActivity(newA);

                                                                                 }

                                                                             }

                                                                             @Override
                                                                             public void onCancelled(DatabaseError databaseError) {

                                                                                 mDatabase.child("chat").child("groups").child("notifRefs").child(name).setValue("group1");
                                                                                 mDatabase.child("chat").child("groups").child("notifRefs").child("count").setValue(1);
                                                                                 Intent newA = new Intent(getApplicationContext(), ChatsActivity.class);
                                                                                 newA.putExtra("tab", 2);
                                                                                 startActivity(newA);

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

                                             }

                                            }
                                        }, 500);

                                    }

                                } else {

                                    for (int n = 0; n < split.length; n++) {

                                        final int numero = n;

                                        mDatabase.child("users").child("c").child("usernameToEmail").child(split[numero].replace(" ", "")).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                String email = dataSnapshot.getValue(String.class);
                                                if (email != null) {

                                                    mDatabase.child("users").child("c").child("emailToUid").child(email).child("uid").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            String uid = dataSnapshot.getValue(String.class);
                                                            mDatabase.child("chat").child("groups").child(name).child("people").child(uid).setValue(split[numero].replace(" ", ""));

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

                                        if (n == split.length - 1) {

                                            mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    String username = dataSnapshot.getValue(String.class);
                                                    if (username != null) {

                                                        mDatabase.child("chat").child("groups").child(name).child("admin").setValue(username);
                                                        mDatabase.child("chat").child("groups").child(name).child("name").setValue(name);
                                                        try {
                                                            mDatabase.child("chat").child("groups").child(name).child("password").setValue(sha256(groupPass.getText().toString()));
                                                        } catch (NoSuchAlgorithmException e) {
                                                            e.printStackTrace();
                                                        } catch (UnsupportedEncodingException e) {
                                                            e.printStackTrace();
                                                        }

                                                        mDatabase.child("chat").child("groups").child("notifRefs").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                Long countOfGrpNotif = dataSnapshot.getValue(Long.class);
                                                                Log.i("in", "y");
                                                                if (countOfGrpNotif != null) {

                                                                    Long notfsCnt = countOfGrpNotif + 1;

                                                                    mDatabase.child("chat").child("groups").child("notifRefs").child(name).setValue("group" + notfsCnt);
                                                                    mDatabase.child("chat").child("groups").child("notifRefs").child("count").setValue(notfsCnt);
                                                                    Intent newA = new Intent(getApplicationContext(), ChatsActivity.class);
                                                                    newA.putExtra("tab", 2);
                                                                    startActivity(newA);
                                                                    Log.i("exist", "Y");

                                                                } else {

                                                                    Log.i("exist", "N");

                                                                    mDatabase.child("chat").child("groups").child("notifRefs").child(name).setValue("group1");
                                                                    mDatabase.child("chat").child("groups").child("notifRefs").child("count").setValue(1);
                                                                    Intent newA = new Intent(getApplicationContext(), ChatsActivity.class);
                                                                    newA.putExtra("tab", 2);
                                                                    startActivity(newA);

                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                                mDatabase.child("chat").child("groups").child("notifRefs").child(name).setValue("group1");
                                                                mDatabase.child("chat").child("groups").child("notifRefs").child("count").setValue(1);
                                                                Intent newA = new Intent(getApplicationContext(), ChatsActivity.class);
                                                                newA.putExtra("tab", 2);
                                                                startActivity(newA);

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

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });



                    }

                }

            } else {

                Toast.makeText(FinalGroupChooseActivity.this, "Fill out All Fields Please", Toast.LENGTH_SHORT).show();

            }

            }
        });


    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    String sha256(String textToHash) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // Change this to UTF-16 if needed
        md.update(textToHash.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();

        String hex = String.format( "%064x", new BigInteger( 1, digest ) );
        return hex;
    }
}
