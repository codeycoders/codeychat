package com.tejasmehta.codeychat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ChatsViewActivity extends AppCompatActivity {

    String name;
    String admin;
    String groupNum;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    Boolean firstClick = true;
    ListView listView;
    EditText msgContent;
    ArrayList<chatBubble> objects;
    CustomAdapter customAdapter;
    Boolean filter;
    Boolean onLoad = true;
    Button sendMsg;
    String emailSHA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_view);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        listView = findViewById(R.id.chats);
        msgContent = findViewById(R.id.msgContent);
        sendMsg = findViewById(R.id.send);
        msgContent.setImeOptions(EditorInfo.IME_ACTION_GO);
        msgContent.setRawInputType(InputType.TYPE_CLASS_TEXT);
        objects = new ArrayList<>();
        customAdapter = new CustomAdapter(this, objects);
        listView.setAdapter(customAdapter);
        listView.setDivider(null);
        try {
            name = getIntent().getExtras().getString("name");
            admin = getIntent().getExtras().getString("admin");
            groupNum = getIntent().getExtras().getString("groupNum");

        } catch (Exception e) {


        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name);

        PublicChat();

        msgContent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //make the view scroll down to the bottom
                listView.setSelection(customAdapter.getCount() - 1);

            }
        });

        msgContent.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                if (i == KeyEvent.KEYCODE_ENTER) {
                    // Perform action on key press
                    sendMsg.performClick();
                    return true;
                }

                return false;
            }
        });

        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String msg = msgContent.getText().toString();
                final String msgLower = msg.toLowerCase();


                if (mAuth.getCurrentUser() != null) {


                    final String uid = mAuth.getCurrentUser().getUid();
                    final String email = mAuth.getCurrentUser().getEmail().replace(".", ",");
                    mDatabase.child("users").child("c").child("emailToUsername").child(email).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String username = dataSnapshot.getValue(String.class);
                            if (username != null) {

                                Log.i("test", "in");

                                try {
                                    emailSHA = sha256(mAuth.getCurrentUser().getEmail());
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                                if (emailSHA.equals("e95f5a1b9b51735c91297cc9aef9ca0bda53d3a7f5818cbb406244132688ffba") || emailSHA.equals("5c6f46d3354efa78707c19ade5b3c59f9ce20de97da510d322bbe6c9dfac3795")) {

                                    Log.i("superUser", "YASSS");
                                    if (filter) {


                                        if (msgLower.contains("fuck") || msgLower.contains("shit") || msgLower.contains("slut") || msgLower.contains("fu") || msgLower.contains("ass ") || msgLower.contains("bitch") || msgLower.contains("fuq") || msgLower.contains("cock") || msgLower.contains("pussy") || msgLower.contains("asshole") || msgLower.contains("whore")) {

                                            new AlertDialog.Builder(ChatsViewActivity.this)
                                                    .setTitle("Language!")
                                                    .setMessage("No Profane Language! If You Must, Turn The Filter off With '!filter'")
                                                    .setPositiveButton("Ok", null).show();
                                            msgContent.setText("");

                                        } else if (msg.equals("")) {

                                            new AlertDialog.Builder(ChatsViewActivity.this)
                                                    .setTitle("Message is empty!")
                                                    .setMessage("Your Message Needs some Sort of Content!")
                                                    .setPositiveButton("Ok", null).show();

                                        } else if (msg.equals("!filter")) {

                                            filter = false;
                                            Log.i("grp", String.valueOf(groupNum));
                                            mDatabase.child("users").child(uid).child("filter").setValue(filter.toString());
                                            mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String lastMessage = dataSnapshot.getValue(String.class);
                                                    if (lastMessage != null) {

                                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                        String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                        chatBubble ChatBubble = new chatBubble("Filter: false", timeComp, "server");
                                                        objects.add(ChatBubble);
                                                        msgContent.setText("");
                                                        customAdapter.notifyDataSetChanged();

                                                        listView.post(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                listView.setSelection(customAdapter.getCount() - 1);

                                                            }
                                                        });

                                                    } else {

                                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                        String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                        chatBubble ChatBubble = new chatBubble("Filter: false", timeComp, "server");
                                                        objects.add(ChatBubble);
                                                        msgContent.setText("");
                                                        customAdapter.notifyDataSetChanged();

                                                        listView.post(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                listView.setSelection(customAdapter.getCount() - 1);

                                                            }
                                                        });

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            Log.i("filter", filter.toString());

                                        } else {


                                            mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String lastMessage = dataSnapshot.getValue(String.class);
                                                    if (lastMessage != null) {

                                                        mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                Long countOfMsg = dataSnapshot.getValue(Long.class);
                                                                if (countOfMsg != null) {
                                                                    int msgs = Integer.parseInt(String.valueOf(countOfMsg)) + 1;

                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message" + msgs).setValue(lastMessage);
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").setValue(msgs);

                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


                                                                } else {

                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message1").setValue(lastMessage);
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").setValue(1);

                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    } else {

                                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                        String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                        String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                        mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(msgToSend);
                                                        msgContent.setText("");

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                        }


                                    } else if (!filter) {

                                        if (msg.equals("")) {

                                            new AlertDialog.Builder(ChatsViewActivity.this)
                                                    .setTitle("Message is empty!")
                                                    .setMessage("Your Message Needs some Sort of Content!")
                                                    .setPositiveButton("Ok", null).show();

                                        } else if (msg.equals("!filter")) {

                                            filter = true;
                                            mDatabase.child("users").child(uid).child("filter").setValue(filter.toString());
                                            mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String lastMessage = dataSnapshot.getValue(String.class);
                                                    if (lastMessage != null) {

                                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                        String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                        chatBubble ChatBubble = new chatBubble("Filter: true", timeComp, "server");
                                                        objects.add(ChatBubble);
                                                        msgContent.setText("");
                                                        customAdapter.notifyDataSetChanged();

                                                        listView.post(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                listView.setSelection(customAdapter.getCount() - 1);

                                                            }
                                                        });

                                                    } else {

                                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                        String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                        chatBubble ChatBubble = new chatBubble("Filter: true", timeComp, "server");
                                                        objects.add(ChatBubble);
                                                        msgContent.setText("");
                                                        customAdapter.notifyDataSetChanged();

                                                        listView.post(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                listView.setSelection(customAdapter.getCount() - 1);

                                                            }
                                                        });

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            Log.i("filter", filter.toString());

                                        } else {


                                            mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String lastMessage = dataSnapshot.getValue(String.class);
                                                    if (lastMessage != null) {

                                                        mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                Long countOfMsg = dataSnapshot.getValue(Long.class);
                                                                if (countOfMsg != null) {
                                                                    int msgs = Integer.parseInt(String.valueOf(countOfMsg)) + 1;

                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message" + msgs).setValue(lastMessage);
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").setValue(msgs);


                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


                                                                } else {

                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message1").setValue(lastMessage);
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").setValue(1);

                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });


                                                    } else {

                                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                        String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                        String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                        mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(msgToSend);
                                                        msgContent.setText("");

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                        }

                                    }

                                } else {

                                    if (filter) {


                                        if (msgLower.contains("fuck") || msgLower.contains("shit") || msgLower.contains("slut") || msgLower.contains("fu") || msgLower.contains("ass ") || msgLower.contains("bitch") || msgLower.contains("fuq") || msgLower.contains("cock") || msgLower.contains("pussy") || msgLower.contains("asshole") || msgLower.contains("whore")) {

                                            new AlertDialog.Builder(ChatsViewActivity.this)
                                                    .setTitle("Language!")
                                                    .setMessage("No Profane Language! If You Must, Turn The Filter off With '!filter'")
                                                    .setPositiveButton("Ok", null).show();
                                            msgContent.setText("");

                                        } else if (msg.equals("")) {

                                            new AlertDialog.Builder(ChatsViewActivity.this)
                                                    .setTitle("Message is empty!")
                                                    .setMessage("Your Message Needs some Sort of Content!")
                                                    .setPositiveButton("Ok", null).show();

                                        } else if (msg.equals("!filter")) {

                                            filter = false;
                                            mDatabase.child("users").child(uid).child("filter").setValue(filter.toString());
                                            mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String lastMessage = dataSnapshot.getValue(String.class);
                                                    if (lastMessage != null) {

                                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                        String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                        chatBubble ChatBubble = new chatBubble("Filter: false", timeComp, "server");
                                                        objects.add(ChatBubble);
                                                        msgContent.setText("");
                                                        customAdapter.notifyDataSetChanged();

                                                        listView.post(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                listView.setSelection(customAdapter.getCount() - 1);

                                                            }
                                                        });

                                                    } else {

                                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                        String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                        chatBubble ChatBubble = new chatBubble("Filter: false", timeComp, "server");
                                                        objects.add(ChatBubble);
                                                        msgContent.setText("");
                                                        customAdapter.notifyDataSetChanged();

                                                        listView.post(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                listView.setSelection(customAdapter.getCount() - 1);

                                                            }
                                                        });

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            Log.i("filter", filter.toString());

                                        } else {

                                            Log.i("sending", "y");

                                            mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String lastMessage = dataSnapshot.getValue(String.class);
                                                    if (lastMessage != null) {

                                                        Log.i("msg", "y");
                                                        mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                Long countOfMsg = dataSnapshot.getValue(Long.class);
                                                                if (countOfMsg != null) {
                                                                    int msgs = Integer.parseInt(String.valueOf(countOfMsg)) + 1;

                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message" + msgs).setValue(lastMessage);
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").setValue(msgs);

                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


                                                                } else {

                                                                    Log.i("cnt", "null");
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message1").setValue(lastMessage);
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").setValue(1);

                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    } else {

                                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                        String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                        String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                        mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(msgToSend);
                                                        msgContent.setText("");

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                        }


                                    } else {

                                        if (msg.equals("")) {

                                            new AlertDialog.Builder(ChatsViewActivity.this)
                                                    .setTitle("Message is empty!")
                                                    .setMessage("Your Message Needs some Sort of Content!")
                                                    .setPositiveButton("Ok", null).show();

                                        } else if (msg.equals("!filter")) {

                                            filter = true;
                                            mDatabase.child("users").child(uid).child("filter").setValue(filter.toString());
                                            mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String lastMessage = dataSnapshot.getValue(String.class);
                                                    if (lastMessage != null) {

                                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                        String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                        chatBubble ChatBubble = new chatBubble("Filter: true", timeComp, "server");
                                                        objects.add(ChatBubble);
                                                        msgContent.setText("");
                                                        customAdapter.notifyDataSetChanged();

                                                        listView.post(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                listView.setSelection(customAdapter.getCount() - 1);

                                                            }
                                                        });

                                                    } else {

                                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                        String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                        chatBubble ChatBubble = new chatBubble("Filter: true", timeComp, "server");
                                                        objects.add(ChatBubble);
                                                        msgContent.setText("");
                                                        customAdapter.notifyDataSetChanged();

                                                        listView.post(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                listView.setSelection(customAdapter.getCount() - 1);

                                                            }
                                                        });

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            Log.i("filter", filter.toString());

                                        } else {


                                            mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String lastMessage = dataSnapshot.getValue(String.class);
                                                    if (lastMessage != null) {

                                                        mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                Long countOfMsg = dataSnapshot.getValue(Long.class);
                                                                if (countOfMsg != null) {
                                                                    int msgs = Integer.parseInt(String.valueOf(countOfMsg)) + 1;

                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message" + msgs).setValue(lastMessage);
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").setValue(msgs);


                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


                                                                } else {

                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message1").setValue(lastMessage);
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").setValue(1);

                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });


                                                    } else {

                                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                        String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                        String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                        mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(msgToSend);
                                                        msgContent.setText("");

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                        }

                                    }

                                }

                                Log.i("message", msg);


                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }
        });


    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        finish();
    }

    String sha256(String textToHash) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // Change this to UTF-16 if needed
        md.update(textToHash.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();

        String hex = String.format("%064x", new BigInteger(1, digest));
        return hex;
    }

    public boolean filterCheck(String msgLower) {

        if (msgLower.contains("fuck") || msgLower.contains("shit") || msgLower.contains("slut") || msgLower.contains("fu") || msgLower.contains("ass ") || msgLower.contains("bitch") || msgLower.contains("fuq") || msgLower.contains("cock") || msgLower.contains("pussy") || msgLower.contains("asshole") || msgLower.contains("whore")) {

            return false;

        } else {

            return true;

        }

    }

    public void PublicChat() {

        objects.clear();

        if (mAuth.getCurrentUser() != null) {

            final String uid = mAuth.getCurrentUser().getUid();
            String email = mAuth.getCurrentUser().getEmail().replace(".", ",");

            mDatabase.child("users").child(uid).child("filter").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String filterState = dataSnapshot.getValue(String.class);
                    if (filterState != null) {

                        if (filterState.equals("true")) {

                            filter = true;

                        } else if (filterState.equals("false")) {

                            filter = false;

                        }

                    } else {

                        filter = true;
                        mDatabase.child("users").child(uid).child("filter").setValue("true");

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    mDatabase.child("users").child(uid).child("filter").setValue("true");
                    filter = true;

                }
            });

            mDatabase.child("users").child("c").child("emailToUsername").child(email).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final String username = dataSnapshot.getValue(String.class);
                    if (username != null) {


                        mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final Long countOfChats = dataSnapshot.getValue(Long.class);

                                if (countOfChats != null) {

                                    if (countOfChats >= 30) {

                                        for (int i = (Integer.parseInt(String.valueOf(countOfChats)) - 30); i <= countOfChats; i++) {

                                            mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message" + i).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    String message = dataSnapshot.getValue(String.class);
                                                    if (message != null) {

                                                        if (message.contains("<") && message.contains(">")) {

                                                            final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));
                                                            if (extract.equals(username)) {

                                                                int semicnt = 0;
                                                                int num = 0;
                                                                //Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                            String finalM = message.replace(time + ": ", "").replace("<" + extract + "> ", "");

                                                                            if (filter) {

                                                                                if (filterCheck(finalM.toLowerCase())) {

                                                                                    chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                    objects.add(chat);

                                                                                } else {

                                                                                    chatBubble chat = new chatBubble("***Censored Message***", "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                    objects.add(chat);


                                                                                }

                                                                            } else {

                                                                                chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                objects.add(chat);

                                                                            }


                                                                        }

                                                                    }

                                                                }

                                                            } else {

                                                                int semicnt = 0;
                                                                int num = 0;
                                                                //Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                            String finalM = message.replace(time + ": ", "").replace("<" + extract + "> ", "");

                                                                            if (filter) {

                                                                                if (filterCheck(finalM.toLowerCase())) {

                                                                                    chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                    objects.add(chat);


                                                                                } else {

                                                                                    chatBubble chat = new chatBubble("***Censored Message***", "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                    objects.add(chat);


                                                                                }

                                                                            } else {

                                                                                chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                objects.add(chat);


                                                                            }


                                                                        }

                                                                    }

                                                                }

                                                            }

                                                            customAdapter.notifyDataSetChanged();
                                                            listView.post(new Runnable() {
                                                                @Override
                                                                public void run() {

                                                                    listView.setSelection(customAdapter.getCount() - 1);

                                                                }
                                                            });

                                                        }

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            if (i == countOfChats) {

                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Log.i("adapter", String.valueOf(customAdapter.getCount()));
                                                        mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                String message = dataSnapshot.getValue(String.class);

                                                                if (message != null) {

                                                                    if (message.contains("<") && message.contains(">")) {

                                                                        final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));
                                                                        if (extract.equals(username)) {

                                                                            int semicnt = 0;
                                                                            int num = 0;
                                                                            //Log.i("Loop", "Yes");

                                                                            for (int i = 0; i < message.length(); i++) {

                                                                                //Log.i("Loop", "y");

                                                                                if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                                    semicnt++;
                                                                                    //Log.i("cnt", String.valueOf(semicnt));

                                                                                    if (semicnt == 3) {

                                                                                        num = i;
                                                                                        i = message.length() - 1;
                                                                                        String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                                        String finalM = message.replace(time + ": ", "").replace("<" + extract + "> ", "");

                                                                                        if (filter) {

                                                                                            if (filterCheck(finalM.toLowerCase())) {

                                                                                                chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                                objects.add(chat);


                                                                                            } else {

                                                                                                chatBubble chat = new chatBubble("***Censored Message***", "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                                objects.add(chat);


                                                                                            }

                                                                                        } else {

                                                                                            chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                            objects.add(chat);


                                                                                        }


                                                                                    }

                                                                                }

                                                                            }

                                                                        } else {

                                                                            int semicnt = 0;
                                                                            int num = 0;
                                                                            //Log.i("Loop", "Yes");

                                                                            for (int i = 0; i < message.length(); i++) {

                                                                                //Log.i("Loop", "y");

                                                                                if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                                    semicnt++;
                                                                                    //Log.i("cnt", String.valueOf(semicnt));

                                                                                    if (semicnt == 3) {

                                                                                        num = i;
                                                                                        i = message.length() - 1;
                                                                                        String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                                        String finalM = message.replace(time + ": ", "").replace("<" + extract + "> ", "");

                                                                                        if (filter) {

                                                                                            if (filterCheck(finalM.toLowerCase())) {

                                                                                                chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                                objects.add(chat);

                                                                                            } else {

                                                                                                chatBubble chat = new chatBubble("***Censored Message***", "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                                objects.add(chat);


                                                                                            }

                                                                                        } else {

                                                                                            chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                            objects.add(chat);


                                                                                        }


                                                                                    }

                                                                                }

                                                                            }

                                                                        }

                                                                        customAdapter.notifyDataSetChanged();
                                                                        listView.post(new Runnable() {
                                                                            @Override
                                                                            public void run() {

                                                                                listView.setSelection(customAdapter.getCount() - 1);

                                                                            }
                                                                        });

                                                                    } else {

                                                                        int semicnt = 0;
                                                                        int num = 0;
                                                                        //Log.i("Loop", "Yes");

                                                                        for (int i = 0; i < message.length(); i++) {

                                                                            //Log.i("Loop", "y");

                                                                            if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                                semicnt++;
                                                                                //Log.i("cnt", String.valueOf(semicnt));

                                                                                if (semicnt == 3) {

                                                                                    num = i;
                                                                                    i = message.length() - 1;
                                                                                    String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                                    String finalM = message.replace(time + ": ", "");

                                                                                    if (filter) {

                                                                                        if (filterCheck(finalM.toLowerCase())) {

                                                                                            chatBubble chat = new chatBubble(finalM, "At: " + time, "server");
                                                                                            objects.add(chat);

                                                                                        } else {

                                                                                            chatBubble chat = new chatBubble("***Censored Message***", "At: " + time, "server");
                                                                                            objects.add(chat);

                                                                                        }

                                                                                    } else {

                                                                                        chatBubble chat = new chatBubble(finalM, "At: " + time, "server");
                                                                                        objects.add(chat);


                                                                                    }


                                                                                }

                                                                            }

                                                                        }

                                                                    }

                                                                    customAdapter.notifyDataSetChanged();
                                                                    listView.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {

                                                                            listView.setSelection(customAdapter.getCount() - 1);

                                                                        }
                                                                    });


                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                        Handler handle2 = new Handler();
                                                        handle2.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                final String timeComp = time_format.format(Calendar.getInstance().getTime());

                                                                mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        String lastMsg = dataSnapshot.getValue(String.class);
                                                                        if (lastMsg != null) {

                                                                            int msgs = Integer.parseInt(String.valueOf(countOfChats)) + 1;

                                                                            mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message" + msgs).setValue(lastMsg);
                                                                            mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").setValue(msgs);
                                                                            mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(timeComp + ": " + username + " has joined the channel");
                                                                            listView.post(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    listView.setSelection(customAdapter.getCount() - 1);
                                                                                }
                                                                            });


                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            }
                                                        }, 500);

                                                    }
                                                }, 500);


                                            }

                                        }

                                    } else if (countOfChats > 0 && countOfChats < 30) {

                                        for (int i = 1; i <= Integer.parseInt(String.valueOf(countOfChats)); i++) {

                                            mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message" + i).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    String message = dataSnapshot.getValue(String.class);
                                                    if (message != null) {

                                                        if (message.contains("<") && message.contains(">")) {

                                                            final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));
                                                            if (extract.equals(username)) {

                                                                int semicnt = 0;
                                                                int num = 0;
                                                                //Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                            String finalM = message.replace(time + ": ", "").replace("<" + extract + "> ", "");

                                                                            if (filter) {

                                                                                if (filterCheck(finalM.toLowerCase())) {

                                                                                    chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                    objects.add(chat);

                                                                                } else {

                                                                                    chatBubble chat = new chatBubble("***Censored Message***", "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                    objects.add(chat);

                                                                                }

                                                                            } else {

                                                                                chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                objects.add(chat);

                                                                            }


                                                                        }

                                                                    }

                                                                }

                                                            } else {

                                                                int semicnt = 0;
                                                                int num = 0;
                                                                //Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                            String finalM = message.replace(time + ": ", "").replace("<" + extract + "> ", "");

                                                                            if (filter) {

                                                                                if (filterCheck(finalM.toLowerCase())) {

                                                                                    chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                    objects.add(chat);

                                                                                } else {

                                                                                    chatBubble chat = new chatBubble("***Censored Message***", "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                    objects.add(chat);

                                                                                }

                                                                            } else {

                                                                                chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                objects.add(chat);

                                                                            }


                                                                        }

                                                                    }

                                                                }

                                                            }

                                                            customAdapter.notifyDataSetChanged();
                                                            listView.post(new Runnable() {
                                                                @Override
                                                                public void run() {

                                                                    listView.setSelection(customAdapter.getCount() - 1);

                                                                }
                                                            });

                                                        }

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            if (i == countOfChats) {

                                                customAdapter.notifyDataSetChanged();

                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                String message = dataSnapshot.getValue(String.class);

                                                                if (message != null) {

                                                                    if (message.contains("<") && message.contains(">")) {

                                                                        final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));
                                                                        if (extract.equals(username)) {

                                                                            int semicnt = 0;
                                                                            int num = 0;
                                                                            //Log.i("Loop", "Yes");

                                                                            for (int i = 0; i < message.length(); i++) {

                                                                                //Log.i("Loop", "y");

                                                                                if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                                    semicnt++;
                                                                                    //Log.i("cnt", String.valueOf(semicnt));

                                                                                    if (semicnt == 3) {

                                                                                        num = i;
                                                                                        i = message.length() - 1;
                                                                                        String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                                        String finalM = message.replace(time + ": ", "").replace("<" + extract + "> ", "");

                                                                                        if (filter) {

                                                                                            if (filterCheck(finalM.toLowerCase())) {

                                                                                                chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                                objects.add(chat);

                                                                                            } else {

                                                                                                chatBubble chat = new chatBubble("***Censored Message***", "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                                objects.add( chat);

                                                                                            }

                                                                                        } else {

                                                                                            chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                            objects.add( chat);


                                                                                        }


                                                                                    }

                                                                                }

                                                                            }

                                                                        } else {

                                                                            int semicnt = 0;
                                                                            int num = 0;
                                                                            //Log.i("Loop", "Yes");

                                                                            for (int i = 0; i < message.length(); i++) {

                                                                                //Log.i("Loop", "y");

                                                                                if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                                    semicnt++;
                                                                                    //Log.i("cnt", String.valueOf(semicnt));

                                                                                    if (semicnt == 3) {

                                                                                        num = i;
                                                                                        i = message.length() - 1;
                                                                                        String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                                        String finalM = message.replace(time + ": ", "").replace("<" + extract + "> ", "");

                                                                                        if (filter) {

                                                                                            if (filterCheck(finalM.toLowerCase())) {

                                                                                                chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                                objects.add(chat);

                                                                                            } else {

                                                                                                chatBubble chat = new chatBubble("***Censored Message***", "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                                objects.add(chat);


                                                                                            }

                                                                                        } else {

                                                                                            chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                            objects.add( chat);

                                                                                        }


                                                                                    }

                                                                                }

                                                                            }

                                                                        }

                                                                        customAdapter.notifyDataSetChanged();
                                                                        listView.post(new Runnable() {
                                                                            @Override
                                                                            public void run() {

                                                                                listView.setSelection(customAdapter.getCount() - 1);

                                                                            }
                                                                        });

                                                                    } else {

                                                                        int semicnt = 0;
                                                                        int num = 0;
                                                                        //Log.i("Loop", "Yes");

                                                                        for (int i = 0; i < message.length(); i++) {

                                                                            //Log.i("Loop", "y");

                                                                            if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                                semicnt++;
                                                                                //Log.i("cnt", String.valueOf(semicnt));

                                                                                if (semicnt == 3) {

                                                                                    num = i;
                                                                                    i = message.length() - 1;
                                                                                    String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                                    String finalM = message.replace(time + ": ", "");

                                                                                    if (filter) {

                                                                                        if (filterCheck(finalM.toLowerCase())) {

                                                                                            chatBubble chat = new chatBubble(finalM, "At: " + time, "server");
                                                                                            objects.add(chat);


                                                                                        } else {

                                                                                            chatBubble chat = new chatBubble("***Censored Message***", "At: " + time, "server");
                                                                                            objects.add( chat);


                                                                                        }

                                                                                    } else {

                                                                                        chatBubble chat = new chatBubble(finalM, "At: " + time, "server");
                                                                                        objects.add( chat);

                                                                                    }


                                                                                    customAdapter.notifyDataSetChanged();
                                                                                    listView.post(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            listView.setSelection(customAdapter.getCount() - 1);
                                                                                        }
                                                                                    });


                                                                                }

                                                                            }

                                                                        }

                                                                    }


                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                        Handler handle2 = new Handler();
                                                        handle2.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                final String timeComp = time_format.format(Calendar.getInstance().getTime());

                                                                mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        String lastMsg = dataSnapshot.getValue(String.class);
                                                                        if (lastMsg != null) {

                                                                            int msgs = Integer.parseInt(String.valueOf(countOfChats)) + 1;

                                                                            mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message" + msgs).setValue(lastMsg);
                                                                            mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").setValue(msgs);
                                                                            mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(timeComp + ": " + username + " has joined the channel");
                                                                            listView.post(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    listView.setSelection(customAdapter.getCount() - 1);
                                                                                }
                                                                            });


                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            }
                                                        }, 500);

                                                    }
                                                }, 500);


                                            }

                                        }

                                    }

                                } else {

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    String message = dataSnapshot.getValue(String.class);

                                                    if (message != null) {

                                                        if (message.contains("<") && message.contains(">")) {

                                                            final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));
                                                            if (extract.equals(username)) {

                                                                int semicnt = 0;
                                                                int num = 0;
                                                                //Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                            String finalM = message.replace(time + ": ", "").replace("<" + extract + "> ", "");

                                                                            if (filter) {

                                                                                if (filterCheck(finalM.toLowerCase())) {

                                                                                    chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                    objects.add(chat);
                                                                                    customAdapter.notifyDataSetChanged();
                                                                                    listView.post(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            listView.setSelection(customAdapter.getCount() - 1);
                                                                                        }
                                                                                    });

                                                                                } else {

                                                                                    chatBubble chat = new chatBubble("***Censored Message***", "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                    objects.add(chat);
                                                                                    customAdapter.notifyDataSetChanged();
                                                                                    listView.post(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            listView.setSelection(customAdapter.getCount() - 1);
                                                                                        }
                                                                                    });

                                                                                }

                                                                            } else {

                                                                                chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "myMessage");
                                                                                objects.add(chat);
                                                                                customAdapter.notifyDataSetChanged();
                                                                                listView.post(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        listView.setSelection(customAdapter.getCount() - 1);
                                                                                    }
                                                                                });

                                                                            }


                                                                        }

                                                                    }

                                                                }

                                                            } else {

                                                                int semicnt = 0;
                                                                int num = 0;
                                                                //Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                            String finalM = message.replace(time + ": ", "").replace("<" + extract + "> ", "");

                                                                            if (filter) {

                                                                                if (filterCheck(finalM.toLowerCase())) {

                                                                                    chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                    objects.add(chat);
                                                                                    customAdapter.notifyDataSetChanged();
                                                                                    listView.post(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            listView.setSelection(customAdapter.getCount() - 1);
                                                                                        }
                                                                                    });

                                                                                } else {

                                                                                    chatBubble chat = new chatBubble("***Censored Message***", "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                    objects.add(chat);
                                                                                    customAdapter.notifyDataSetChanged();
                                                                                    listView.post(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            listView.setSelection(customAdapter.getCount() - 1);
                                                                                        }
                                                                                    });

                                                                                }

                                                                            } else {

                                                                                chatBubble chat = new chatBubble(finalM, "From: " + extract + "\nAt: " + time, "notMyMessage");
                                                                                objects.add(chat);
                                                                                customAdapter.notifyDataSetChanged();
                                                                                listView.post(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        listView.setSelection(customAdapter.getCount() - 1);
                                                                                    }
                                                                                });

                                                                            }


                                                                        }

                                                                    }

                                                                }

                                                            }

                                                        } else {

                                                            int semicnt = 0;
                                                            int num = 0;
                                                            //Log.i("Loop", "Yes");

                                                            for (int i = 0; i < message.length(); i++) {

                                                                //Log.i("Loop", "y");

                                                                if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                    semicnt++;
                                                                    //Log.i("cnt", String.valueOf(semicnt));

                                                                    if (semicnt == 3) {

                                                                        num = i;
                                                                        i = message.length() - 1;
                                                                        String time = message.substring(0, (Math.min(message.length() - 1, num)));
                                                                        String finalM = message.replace(time + ": ", "");

                                                                        if (filter) {

                                                                            if (filterCheck(finalM.toLowerCase())) {

                                                                                chatBubble chat = new chatBubble(finalM, "At: " + time, "server");
                                                                                objects.add(chat);
                                                                                customAdapter.notifyDataSetChanged();
                                                                                listView.post(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        listView.setSelection(customAdapter.getCount() - 1);
                                                                                    }
                                                                                });

                                                                            } else {

                                                                                chatBubble chat = new chatBubble("***Censored Message***", "At: " + time, "server");
                                                                                objects.add(chat);
                                                                                customAdapter.notifyDataSetChanged();
                                                                                listView.post(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        listView.setSelection(customAdapter.getCount() - 1);
                                                                                    }
                                                                                });

                                                                            }

                                                                        } else {

                                                                            chatBubble chat = new chatBubble(finalM, "At: " + time, "server");
                                                                            objects.add(chat);
                                                                            customAdapter.notifyDataSetChanged();
                                                                            listView.post(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    listView.setSelection(customAdapter.getCount() - 1);
                                                                                }
                                                                            });

                                                                        }


                                                                    }

                                                                }

                                                            }

                                                        }


                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            Handler handle2 = new Handler();
                                            handle2.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                    final String timeComp = time_format.format(Calendar.getInstance().getTime());

                                                    mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            String lastMsg = dataSnapshot.getValue(String.class);
                                                            if (lastMsg != null) {

                                                                int msgs = 1;

                                                                mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("dumpedMessages").child("message" + msgs).setValue(lastMsg);
                                                                mDatabase.child("chat").child("groups").child(groupNum).child("msgDump").child("count").setValue(msgs);
                                                                mDatabase.child("chat").child("groups").child(groupNum).child("message").child("msg").setValue(timeComp + ": " + username + " has joined the channel");
                                                                listView.post(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        listView.setSelection(customAdapter.getCount() - 1);
                                                                    }
                                                                });


                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                            }, 500);
                                        }
                                    }, 500);


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

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.basic_menu, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
