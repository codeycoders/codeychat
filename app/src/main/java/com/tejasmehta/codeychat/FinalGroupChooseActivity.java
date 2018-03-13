package com.tejasmehta.codeychat;

import android.content.Intent;
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
                        final String email = mAuth.getCurrentUser().getEmail().replace(".", ",");

                        mDatabase.child("chat").child("groups").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final Long countOfGrp = dataSnapshot.getValue(Long.class);
                                if (countOfGrp != null) {

                                    mDatabase.child("users").child("c").child("emailToUsername").child(email).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            String username = dataSnapshot.getValue(String.class);
                                            if (username != null) {

                                                int newCnt = Integer.parseInt(String.valueOf(countOfGrp)) + 1;

                                                mDatabase.child("chat").child("groups").child("count").setValue(newCnt);
                                                mDatabase.child("chat").child("groups").child("group" + newCnt).child("name").setValue(name);
                                                try {
                                                    mDatabase.child("chat").child("groups").child("group" + newCnt).child("password").setValue(sha256(groupPass.getText().toString()));
                                                } catch (NoSuchAlgorithmException e) {
                                                    e.printStackTrace();
                                                } catch (UnsupportedEncodingException e) {
                                                    e.printStackTrace();
                                                }
                                                for (int i = 0; i < (split.length - i); i++) {

                                                    mDatabase.child("chat").child("groups").child("group" + newCnt).child("people").child("person" + i).setValue(split[i]);

                                                }
                                                mDatabase.child("chat").child("groups").child("group" + newCnt).child("admin").setValue(username);



                                                Intent nextAct = new Intent(getApplicationContext(), ChatsActivity.class);
                                                nextAct.putExtra("tab", "2");
                                                startActivity(nextAct);

                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });



                                } else {

                                    mDatabase.child("users").child("c").child("emailToUsername").child(email).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            String username = dataSnapshot.getValue(String.class);
                                            if (username != null) {

                                                int newCnt = 1;

                                                mDatabase.child("chat").child("groups").child("count").setValue(newCnt);
                                                mDatabase.child("chat").child("groups").child("group" + newCnt).child("name").setValue(name);
                                                try {
                                                    mDatabase.child("chat").child("groups").child("group" + newCnt).child("password").setValue(sha256(groupPass.getText().toString()));
                                                } catch (NoSuchAlgorithmException e) {
                                                    e.printStackTrace();
                                                } catch (UnsupportedEncodingException e) {
                                                    e.printStackTrace();
                                                }
                                                for (int i = 0; i < (split.length - i); i++) {

                                                    mDatabase.child("chat").child("groups").child("group" + newCnt).child("people").child("person" + i).setValue(split[i]);

                                                }
                                                mDatabase.child("chat").child("groups").child("group" + newCnt).child("admin").setValue(username);



                                                Intent nextAct = new Intent(getApplicationContext(), ChatsActivity.class);
                                                nextAct.putExtra("tab", "2");
                                                startActivity(nextAct);

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
