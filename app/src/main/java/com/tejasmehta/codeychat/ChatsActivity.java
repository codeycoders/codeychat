package com.tejasmehta.codeychat;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatsActivity extends AbstractActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    Boolean firstClick = true;
    Boolean start = true;
    Button sendMsg;
    int tab;
    Boolean onLoad = true;
    String emailSHA;
    ListView listView;
    Boolean filter;
    String extraVal;
    EditText msgContent;
    ArrayList<chatBubble> objects;
    CustomAdapter customAdapter;
    FloatingActionButton fab;
    FloatingActionButton fabPassJ;
    ListView groupList;
    ArrayList<groupLayout> groups;
    AdapterGroup groupAdapter;
    Map<String, Boolean> admin;
    Map<String, Boolean> groupMember;
    Map<String, String> groupToName;
    int clickCnt = 0;
    ValueEventListener mListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Chats");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        sendMsg = findViewById(R.id.button6);
        listView = findViewById(R.id.list);
        msgContent = findViewById(R.id.editText8);
        objects = new ArrayList<>();
        customAdapter = new CustomAdapter(this, objects);
        listView.setAdapter(customAdapter);
        msgContent.setImeOptions(EditorInfo.IME_ACTION_GO);
        msgContent.setRawInputType(InputType.TYPE_CLASS_TEXT);
        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        final TabHost host = findViewById(R.id.tabHost);
        groupList = findViewById(R.id.listGroup);
        groups = new ArrayList<>();
        groupAdapter = new AdapterGroup(this, groups);
        groupList.setAdapter(groupAdapter);
        admin = new HashMap<>();
        groupMember = new HashMap<>();
        fabPassJ = findViewById(R.id.floatingActionButton);
        fabPassJ.hide();
        groupToName = new HashMap<>();
        listView.setDivider(null);


        try {
            extraVal = getIntent().getExtras().getString("tab");
        }catch(Exception e) {


        }


        Log.i("Token", token);


        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Chats");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Chats");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("GroupChats");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Group Chats");
        host.addTab(spec);

        spec = host.newTabSpec("PublicChat");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Public Chat");
        host.addTab(spec);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    startActivity(new Intent(getApplicationContext(), UserGroupSeachActivity.class));

            }
        });


        fabPassJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });



        if (extraVal != null) {

            int count = host.getTabWidget().getTabCount();

            Log.i("extra", extraVal + " " +  String.valueOf(count));

            if (extraVal.equals("3")) {

                host.setCurrentTab(count - 1);
                PublicChat();

            } else if (extraVal.equals("2")) {

                host.setCurrentTab(count - 2);
                GroupChat();

            }



        }

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

                                            new AlertDialog.Builder(ChatsActivity.this)
                                                    .setTitle("Language!")
                                                    .setMessage("No Profane Language! If You Must, Turn The Filter off With '!filter'")
                                                    .setPositiveButton("Ok", null).show();
                                            msgContent.setText("");

                                        } else if (msg.equals("")) {

                                            new AlertDialog.Builder(ChatsActivity.this)
                                                    .setTitle("Message is empty!")
                                                    .setMessage("Your Message Needs some Sort of Content!")
                                                    .setPositiveButton("Ok", null).show();

                                        } else if (msg.equals("!filter")) {

                                            filter = false;
                                            mDatabase.child("users").child(uid).child("filter").setValue(filter.toString());
                                            mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
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


                                            mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String lastMessage = dataSnapshot.getValue(String.class);
                                                    if (lastMessage != null) {

                                                        mDatabase.child("chat").child("publicDump").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                Long countOfMsg = dataSnapshot.getValue(Long.class);
                                                                if (countOfMsg != null) {
                                                                    int msgs = Integer.parseInt(String.valueOf(countOfMsg)) + 1;

                                                                    mDatabase.child("chat").child("publicDump").child("dumpedMessages").child("message" + msgs).setValue(lastMessage);
                                                                    mDatabase.child("chat").child("publicDump").child("count").setValue(msgs);

                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


                                                                } else {

                                                                    mDatabase.child("chat").child("publicDump").child("dumpedMessages").child("message1").setValue(lastMessage);
                                                                    mDatabase.child("chat").child("publicDump").child("count").setValue(1);

                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


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


                                    } else if (!filter) {

                                        if (msg.equals("")) {

                                            new AlertDialog.Builder(ChatsActivity.this)
                                                    .setTitle("Message is empty!")
                                                    .setMessage("Your Message Needs some Sort of Content!")
                                                    .setPositiveButton("Ok", null).show();

                                        } else if (msg.equals("!filter")) {

                                            filter = true;
                                            mDatabase.child("users").child(uid).child("filter").setValue(filter.toString());
                                            mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
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


                                            mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String lastMessage = dataSnapshot.getValue(String.class);
                                                    if (lastMessage != null) {

                                                        mDatabase.child("chat").child("publicDump").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                Long countOfMsg = dataSnapshot.getValue(Long.class);
                                                                if (countOfMsg != null) {
                                                                    int msgs = Integer.parseInt(String.valueOf(countOfMsg)) + 1;

                                                                    mDatabase.child("chat").child("publicDump").child("dumpedMessages").child("message" + msgs).setValue(lastMessage);
                                                                    mDatabase.child("chat").child("publicDump").child("count").setValue(msgs);


                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


                                                                } else {

                                                                    mDatabase.child("chat").child("publicDump").child("dumpedMessages").child("message1").setValue(lastMessage);
                                                                    mDatabase.child("chat").child("publicDump").child("count").setValue(1);

                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


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

                                    if (filter) {


                                        if (msgLower.contains("fuck") || msgLower.contains("shit") || msgLower.contains("slut") || msgLower.contains("fu") || msgLower.contains("ass ") || msgLower.contains("bitch") || msgLower.contains("fuq") || msgLower.contains("cock") || msgLower.contains("pussy") || msgLower.contains("asshole") || msgLower.contains("whore")) {

                                            new AlertDialog.Builder(ChatsActivity.this)
                                                    .setTitle("Language!")
                                                    .setMessage("No Profane Language! If You Must, Turn The Filter off With '!filter'")
                                                    .setPositiveButton("Ok", null).show();
                                            msgContent.setText("");

                                        } else if (msg.equals("")) {

                                            new AlertDialog.Builder(ChatsActivity.this)
                                                    .setTitle("Message is empty!")
                                                    .setMessage("Your Message Needs some Sort of Content!")
                                                    .setPositiveButton("Ok", null).show();

                                        } else if (msg.equals("!filter")) {

                                            filter = false;
                                            mDatabase.child("users").child(uid).child("filter").setValue(filter.toString());
                                            mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
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


                                            mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String lastMessage = dataSnapshot.getValue(String.class);
                                                    if (lastMessage != null) {

                                                        mDatabase.child("chat").child("publicDump").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                Long countOfMsg = dataSnapshot.getValue(Long.class);
                                                                if (countOfMsg != null) {
                                                                    int msgs = Integer.parseInt(String.valueOf(countOfMsg)) + 1;

                                                                    mDatabase.child("chat").child("publicDump").child("dumpedMessages").child("message" + msgs).setValue(lastMessage);
                                                                    mDatabase.child("chat").child("publicDump").child("count").setValue(msgs);

                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


                                                                } else {

                                                                    mDatabase.child("chat").child("publicDump").child("dumpedMessages").child("message1").setValue(lastMessage);
                                                                    mDatabase.child("chat").child("publicDump").child("count").setValue(1);

                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


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


                                    } else if (!filter) {

                                        if (msg.equals("")) {

                                            new AlertDialog.Builder(ChatsActivity.this)
                                                    .setTitle("Message is empty!")
                                                    .setMessage("Your Message Needs some Sort of Content!")
                                                    .setPositiveButton("Ok", null).show();

                                        } else if (msg.equals("!filter")) {

                                            filter = true;
                                            mDatabase.child("users").child(uid).child("filter").setValue(filter.toString());
                                            mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
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


                                            mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    final String lastMessage = dataSnapshot.getValue(String.class);
                                                    if (lastMessage != null) {

                                                        mDatabase.child("chat").child("publicDump").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                Long countOfMsg = dataSnapshot.getValue(Long.class);
                                                                if (countOfMsg != null) {
                                                                    int msgs = Integer.parseInt(String.valueOf(countOfMsg)) + 1;

                                                                    mDatabase.child("chat").child("publicDump").child("dumpedMessages").child("message" + msgs).setValue(lastMessage);
                                                                    mDatabase.child("chat").child("publicDump").child("count").setValue(msgs);


                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


                                                                } else {

                                                                    mDatabase.child("chat").child("publicDump").child("dumpedMessages").child("message1").setValue(lastMessage);
                                                                    mDatabase.child("chat").child("publicDump").child("count").setValue(1);

                                                                    SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                                                    String timeComp = time_format.format(Calendar.getInstance().getTime());
                                                                    String msgToSend = timeComp + ": <" + username + "> " + msg;
                                                                    mDatabase.child("chat").child("message").child("msg").setValue(msgToSend);
                                                                    msgContent.setText("");


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

        

        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {

                if (s.equals("Chats")) {

                    fab.show();
                    tab = 1;
                    fabPassJ.hide();

                } else if (s.equals("GroupChats")) {

                    GroupChat();
                    tab = 2;
                    if (mListener != null) {

                        mDatabase.child("chat").child("message").child("msg").removeEventListener(mListener);
                    }

                } else if (s.equals("PublicChat")) {

                    PublicChat();
                    tab = 3;

                }

            }
        });

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                RelativeLayout rl = (RelativeLayout) view;
                TextView groupName = rl.findViewById(R.id.groupName);
                Intent newAct = new Intent(getApplicationContext(), ChatsViewActivity.class);
                if (!admin.keySet().isEmpty()) {

                    newAct.putExtra("name", groupName.getText().toString());

                    for (int cnt = 0; cnt < admin.keySet().size(); cnt++) {

                        Log.i("cnt", String.valueOf(cnt));
                        String[] admins = admin.keySet().toArray(new String[0]);

                        if (groupName.getText().toString().equals(admins[cnt])) {

                            newAct.putExtra("admin", true);
                            Log.i("is", "y");

                        } else {

                            newAct.putExtra("admin", false);
                            Log.i("not", "y");

                        }

                        if (cnt == (admin.keySet().size() - 1)) {

                            Log.i("in", "actStart");
                            FirebaseMessaging.getInstance().subscribeToTopic(groupToName.get(groupName.getText().toString()));
                            newAct.putExtra("groupNum", groupToName.get(groupName.getText().toString()));

                            startActivity(newAct);

                        }

                    }

                } else {

                    newAct.putExtra("name", groupName.getText().toString());
                    newAct.putExtra("admin", false);
                    FirebaseMessaging.getInstance().subscribeToTopic(groupToName.get(groupName.getText().toString()));
                    newAct.putExtra("groupNum", groupToName.get(groupName.getText().toString()));

                    startActivity(newAct);

                }


            }
        });
        

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.basic_menu, menu);

        return true;

    }

    public boolean filterCheck(String msgLower) {

        if (msgLower.contains("fuck") || msgLower.contains("shit") || msgLower.contains("slut") || msgLower.contains("fu") || msgLower.contains("ass ") || msgLower.contains("bitch") || msgLower.contains("fuq") || msgLower.contains("cock") || msgLower.contains("pussy") || msgLower.contains("asshole") || msgLower.contains("whore")) {

            return false;

        } else {

            return true;

        }

    }


    String sha256(String textToHash) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // Change this to UTF-16 if needed
        md.update(textToHash.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();

        String hex = String.format( "%064x", new BigInteger( 1, digest ) );
        return hex;
    }

    @Override
    protected void onStop() {
        super.onStop();
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


    public void GroupChat() {

        fab.show();
        fabPassJ.show();
        tab = 2;
        clickCnt = 0;
        groups.clear();
        groupList.post(new Runnable() {
            @Override
            public void run() {
                groupList.setSelection(groupAdapter.getCount() - 1);
            }
        });

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

                    Log.i("user", "y");

                    final String username = dataSnapshot.getValue(String.class);
                    mDatabase.child("chat").child("groups").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Long count = dataSnapshot.getValue(Long.class);
                            if (count != null) {

                                Log.i("cnt", String.valueOf(count));
                                for (int i = 1; i <= count; i++) {

                                    final int num = i;
                                    Log.i("loop", String.valueOf(i));

                                    mDatabase.child("chat").child("groups").child("group" + i).child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            String adminName = dataSnapshot.getValue(String.class);
                                            if (adminName != null) {

                                                if (adminName.equals(username)) {

                                                    Log.i("admin" + num, adminName);

                                                    mDatabase.child("chat").child("groups").child("group" + num).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            final String lastMessage = dataSnapshot.getValue(String.class);
                                                            if (lastMessage != null) {

                                                                mDatabase.child("chat").child("groups").child("group" + num).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                                        String grpName = dataSnapshot.getValue(String.class);
                                                                        if (grpName != null) {

                                                                            groupLayout group = new groupLayout(grpName, lastMessage);
                                                                            groups.add(group);
                                                                            admin.put(grpName, true);
                                                                            groupToName.put(grpName, "group" + num);
                                                                            Log.i("groups", grpName);
                                                                            groupAdapter.notifyDataSetChanged();

                                                                        }

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });

                                                            } else {

                                                                mDatabase.child("chat").child("groups").child("group" + num).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                                        String grpName = dataSnapshot.getValue(String.class);
                                                                        if (grpName != null) {

                                                                            Log.i("m", String.valueOf(num));

                                                                            groupLayout group = new groupLayout(grpName, "N/A");
                                                                            groups.add(group);
                                                                            admin.put(grpName, true);
                                                                            groupToName.put(grpName, "group" + num);
                                                                            Log.i("groups", grpName);
                                                                            groupAdapter.notifyDataSetChanged();


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

                                                } else {

                                                    mDatabase.child("chat").child("groups").child("group" + num).child("people").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            Map<String, Object> people = (Map<String, Object>) dataSnapshot.getValue();
                                                            if (people != null) {

                                                                String[] peopleUsers = people.values().toArray(new String[0]);
                                                                for (int i = 0; i <= peopleUsers.length - 1; i++) {

                                                                    if (username.equals(peopleUsers[i])) {

                                                                        mDatabase.child("chat").child("groups").child("group" + num).child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                                final String lastMessage = dataSnapshot.getValue(String.class);
                                                                                if (lastMessage != null) {

                                                                                    mDatabase.child("chat").child("groups").child("group" + num).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                                                            String grpName = dataSnapshot.getValue(String.class);
                                                                                            if (grpName != null) {

                                                                                                groupLayout group = new groupLayout(grpName, lastMessage);
                                                                                                groups.add(group);
                                                                                                groupMember.put(grpName, true);
                                                                                                groupToName.put(grpName, "group" + num);
                                                                                                Log.i("groups", groups.toString());
                                                                                                groupAdapter.notifyDataSetChanged();

                                                                                            }

                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                                        }
                                                                                    });

                                                                                } else {

                                                                                    mDatabase.child("chat").child("groups").child("group" + num).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                                                            String grpName = dataSnapshot.getValue(String.class);
                                                                                            if (grpName != null) {

                                                                                                groupLayout group = new groupLayout(grpName, "N/A");
                                                                                                groups.add(group);
                                                                                                groupMember.put(grpName, true);
                                                                                                groupToName.put(grpName, "group" + num);
                                                                                                Log.i("groups", groups.toString());
                                                                                                groupAdapter.notifyDataSetChanged();

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

                                    if (i == count) {

                                        groupAdapter.notifyDataSetChanged();

                                    }

                                }


                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }

    }

    public void PublicChat() {
        objects.clear();
        fab.hide();
        fabPassJ.hide();

        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(customAdapter.getCount() - 1);

            }
        });
        tab = 3;

        if (mAuth.getCurrentUser() != null) {

            final String uid = mAuth.getCurrentUser().getUid();

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

            mDatabase.child("chat").child("publicDump").child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final Long count = dataSnapshot.getValue(Long.class);
                    if (count != null) {

                        if (start) {

                            mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    final String username = dataSnapshot.getValue(String.class);
                                    if (username != null) {

                                        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
                                        final String timeComp = time_format.format(Calendar.getInstance().getTime());

                                        mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String lastMsg = dataSnapshot.getValue(String.class);
                                                if (lastMsg != null) {

                                                    int msgs = Integer.parseInt(String.valueOf(count)) + 1;

                                                    mDatabase.child("chat").child("publicDump").child("dumpedMessages").child("message" + msgs).setValue(lastMsg);
                                                    mDatabase.child("chat").child("publicDump").child("count").setValue(msgs);
                                                    mDatabase.child("chat").child("message").child("msg").setValue(timeComp + ": " + username + " has joined the channel");
                                                    listView.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            listView.setSelection(customAdapter.getCount() - 1);
                                                        }
                                                    });

                                                    start = false;


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

                        if (count >= 30) {

                            for (int i = Integer.parseInt(String.valueOf(count)) - 30; i <= count; i++) {

                                Log.i("time", String.valueOf(i));
                                mDatabase.child("chat").child("publicDump").child("dumpedMessages").child("message" + i).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String message = dataSnapshot.getValue(String.class);
                                        if (message != null) {

                                            if (message.contains("<") && message.contains(">")) {

                                                final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));

                                                mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        String username = dataSnapshot.getValue(String.class);
                                                        if (username != null) {

                                                            if (extract.equals(username)) {

                                                                Log.i("Extract", extract);

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
                                                                // Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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

                                if (firstClick) {

                                    Log.i("FClick", "Y");

                                    if (i == count) {

                                        Log.i("fclk2", "y");
                                        Handler handler = new Handler();

                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                if (onLoad) {

                                                    mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            final String message = dataSnapshot.getValue(String.class);
                                                            if (message != null) {

                                                                if (message.contains("<") && message.contains(">")) {

                                                                    final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));


                                                                    mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                                            String username = dataSnapshot.getValue(String.class);
                                                                            if (username != null) {

                                                                                Log.i("Listener", "listenerA");
                                                                                if (extract.equals(username)) {

                                                                                    Log.i("Extract", extract);

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
                                                                                                String time = message.substring(0, (Math.min(message.length(), num)));
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
                                                                                    // Log.i("Loop", "Yes");

                                                                                    for (int i = 0; i < message.length(); i++) {

                                                                                        //Log.i("Loop", "y");

                                                                                        if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                                            semicnt++;
                                                                                            //Log.i("cnt", String.valueOf(semicnt));

                                                                                            if (semicnt == 3) {

                                                                                                num = i;
                                                                                                i = message.length() - 1;
                                                                                                String time = message.substring(0, (Math.min(message.length(), num)));
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

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                                } else {


                                                                    int semicnt = 0;
                                                                    int num = 0;
                                                                    // Log.i("Loop", "Yes");

                                                                    for (int i = 0; i < message.length(); i++) {

                                                                        //Log.i("Loop", "y");

                                                                        if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                            semicnt++;
                                                                            //Log.i("cnt", String.valueOf(semicnt));

                                                                            if (semicnt == 3) {

                                                                                num = i;
                                                                                i = message.length() - 1;
                                                                                String time = message.substring(0, (Math.min(message.length(), num)));
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

                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                }

                                                mListener = mDatabase.child("chat").child("message").child("msg").addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        final String message = dataSnapshot.getValue(String.class);
                                                        if (message != null) {

                                                            if (message.contains("<") && message.contains(">")) {

                                                                final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));


                                                                mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                                        String username = dataSnapshot.getValue(String.class);
                                                                        if (username != null) {

                                                                            Log.i("Listener", "listenerA");
                                                                            if (extract.equals(username)) {

                                                                                Log.i("Extract", extract);

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
                                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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
                                                                                // Log.i("Loop", "Yes");

                                                                                for (int i = 0; i < message.length(); i++) {

                                                                                    //Log.i("Loop", "y");

                                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                                        semicnt++;
                                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                                        if (semicnt == 3) {

                                                                                            num = i;
                                                                                            i = message.length() - 1;
                                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            } else {


                                                                int semicnt = 0;
                                                                int num = 0;
                                                                // Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                            }
                                        }, 700);


                                    }
                                } else {
                                    if (i == count) {


                                        Handler newH = new Handler();

                                        newH.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        final String message = dataSnapshot.getValue(String.class);
                                                        if (message != null) {
                                                            if (message.contains("<") && message.contains(">")) {

                                                                final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));

                                                                mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                                        String username = dataSnapshot.getValue(String.class);
                                                                        if (username != null) {
                                                                            if (extract.equals(username)) {

                                                                                Log.i("Extract", extract);

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
                                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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
                                                                                // Log.i("Loop", "Yes");

                                                                                for (int i = 0; i < message.length(); i++) {

                                                                                    //Log.i("Loop", "y");

                                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                                        semicnt++;
                                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                                        if (semicnt == 3) {

                                                                                            num = i;
                                                                                            i = message.length() - 1;
                                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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


                                                                        }

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            } else {

                                                                int semicnt = 0;
                                                                int num = 0;
                                                                // Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
                                                                            String finalM = message.replace(time + ": ", "");
                                                                            if (filter) {

                                                                                if (filterCheck(finalM.toLowerCase())) {

                                                                                    chatBubble chat = new chatBubble(finalM, "At: " + time, "server");
                                                                                    objects.add(chat);

                                                                                } else {

                                                                                    chatBubble chat = new chatBubble("***Censored Message***", "nAt: " + time, "server");
                                                                                    objects.add(chat);

                                                                                }

                                                                            } else {

                                                                                chatBubble chat = new chatBubble(finalM,  "At: " + time, "server");
                                                                                objects.add(chat);

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

                                            }
                                        }, 700);


                                    }
                                }



                            }

                        } else if (count > 0 && count < 30) {

                            for (int i = 1; i <= Integer.parseInt(String.valueOf(count)); i++) {

                                mDatabase.child("chat").child("publicDump").child("dumpedMessages").child("message" + i).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String message = dataSnapshot.getValue(String.class);
                                        if (message != null) {

                                            if (message.contains("<") && message.contains(">")) {

                                                final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));
                                                Log.i("User", "yes");

                                                mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        String username = dataSnapshot.getValue(String.class);
                                                        if (username != null) {
                                                            if (extract.equals(username)) {

                                                                Log.i("Extract", extract);

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
                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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

                                                                Log.i("Other", extract);

                                                                int semicnt = 0;
                                                                int num = 0;
                                                                // Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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

                                if (firstClick) {

                                    if (i == count) {


                                        Handler handle2 = new Handler();

                                        handle2.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                mListener = mDatabase.child("chat").child("message").child("msg").addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        final String message = dataSnapshot.getValue(String.class);
                                                        if (message != null) {

                                                            if (message.contains("<") && message.contains(">")) {

                                                                final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));

                                                                mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                                        String username = dataSnapshot.getValue(String.class);
                                                                        if (username != null) {

                                                                            if (extract.equals(username)) {

                                                                                Log.i("Extract", extract);

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
                                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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

                                                                                Log.i("Other", extract);


                                                                                int semicnt = 0;
                                                                                int num = 0;
                                                                                // Log.i("Loop", "Yes");

                                                                                for (int i = 0; i < message.length(); i++) {

                                                                                    //Log.i("Loop", "y");

                                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                                        semicnt++;
                                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                                        if (semicnt == 3) {

                                                                                            num = i;
                                                                                            i = message.length() - 1;
                                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            } else {

                                                                int semicnt = 0;
                                                                int num = 0;
                                                                // Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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

                                                                customAdapter.notifyDataSetChanged();

                                                            }

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                            }
                                        }, 700);


                                    }
                                } else {

                                    if (i == count) {


                                        Handler newH = new Handler();

                                        newH.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        final String message = dataSnapshot.getValue(String.class);
                                                        if (message != null) {

                                                            if (message.contains("<") && message.contains(">")) {

                                                                final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));

                                                                mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                                        String username = dataSnapshot.getValue(String.class);
                                                                        if (username != null) {

                                                                            if (extract.equals(username)) {

                                                                                Log.i("Extract", extract);

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
                                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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

                                                                                Log.i("Other", extract);

                                                                                int semicnt = 0;
                                                                                int num = 0;
                                                                                // Log.i("Loop", "Yes");

                                                                                for (int i = 0; i < message.length(); i++) {

                                                                                    //Log.i("Loop", "y");

                                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                                        semicnt++;
                                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                                        if (semicnt == 3) {

                                                                                            num = i;
                                                                                            i = message.length() - 1;
                                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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
                                        }, 700);


                                    }

                                }



                            }

                        } else {

                            if (firstClick) {

                                mListener = mDatabase.child("chat").child("message").child("msg").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String message = dataSnapshot.getValue(String.class);
                                        if (message != null) {
                                            if (message.contains("<") && message.contains(">")) {

                                                final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));

                                                mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        String username = dataSnapshot.getValue(String.class);
                                                        if (username != null) {

                                                            if (extract.equals(username)) {

                                                                Log.i("Extract", extract);

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
                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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
                                                                // Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            } else {

                                                int semicnt = 0;
                                                int num = 0;
                                                // Log.i("Loop", "Yes");

                                                for (int i = 0; i < message.length(); i++) {

                                                    //Log.i("Loop", "y");

                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                        semicnt++;
                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                        if (semicnt == 3) {

                                                            num = i;
                                                            i = message.length() - 1;
                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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

                                                                chatBubble chat = new chatBubble(finalM,"At: " + time, "server");
                                                                objects.add(chat);

                                                            }
                                                        }

                                                    }


                                                }

                                                customAdapter.notifyDataSetChanged();

                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            } else {

                                mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String message = dataSnapshot.getValue(String.class);
                                        if (message != null) {

                                            if (message.contains("<") && message.contains(">")) {

                                                final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));

                                                mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        String username = dataSnapshot.getValue(String.class);
                                                        if (username != null) {

                                                            if (extract.equals(username)) {

                                                                Log.i("Extract", extract);

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
                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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
                                                                // Log.i("Loop", "Yes");

                                                                for (int i = 0; i < message.length(); i++) {

                                                                    //Log.i("Loop", "y");

                                                                    if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                        semicnt++;
                                                                        //Log.i("cnt", String.valueOf(semicnt));

                                                                        if (semicnt == 3) {

                                                                            num = i;
                                                                            i = message.length() - 1;
                                                                            String time = message.substring(0, (Math.min(message.length(), num)));
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

                    } else {

                        if (firstClick) {

                            mListener = mDatabase.child("chat").child("message").child("msg").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    final String message = dataSnapshot.getValue(String.class);
                                    if (message != null) {

                                        if (message.contains("<") && message.contains(">")) {

                                            final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));

                                            mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    String username = dataSnapshot.getValue(String.class);
                                                    if (username != null) {

                                                        if (extract.equals(username)) {

                                                            Log.i("Extract", extract);

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
                                                                        String time = message.substring(0, (Math.min(message.length(), num)));
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
                                                            // Log.i("Loop", "Yes");

                                                            for (int i = 0; i < message.length(); i++) {

                                                                //Log.i("Loop", "y");

                                                                if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                    semicnt++;
                                                                    //Log.i("cnt", String.valueOf(semicnt));

                                                                    if (semicnt == 3) {

                                                                        num = i;
                                                                        i = message.length() - 1;
                                                                        String time = message.substring(0, (Math.min(message.length(), num)));
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

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        } else {

                                            int semicnt = 0;
                                            int num = 0;
                                            // Log.i("Loop", "Yes");

                                            for (int i = 0; i < message.length(); i++) {

                                                //Log.i("Loop", "y");

                                                if (String.valueOf(message.charAt(i)).equals(":")) {

                                                    semicnt++;
                                                    //Log.i("cnt", String.valueOf(semicnt));

                                                    if (semicnt == 3) {

                                                        num = i;
                                                        i = message.length() - 1;
                                                        String time = message.substring(0, (Math.min(message.length(), num)));
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

                                            customAdapter.notifyDataSetChanged();

                                        }


                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        } else {

                            mDatabase.child("chat").child("message").child("msg").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    final String message = dataSnapshot.getValue(String.class);
                                    if (message != null) {

                                        if (message.contains("<") && message.contains(">")) {

                                            final String extract = message.substring(message.lastIndexOf("<") + 1, message.indexOf(">"));

                                            mDatabase.child("users").child("c").child("emailToUsername").child(mAuth.getCurrentUser().getEmail().replace(".", ",")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    String username = dataSnapshot.getValue(String.class);
                                                    if (username != null) {

                                                        if (extract.equals(username)) {

                                                            Log.i("Extract", extract);

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
                                                                        String time = message.substring(0, (Math.min(message.length(), num)));
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
                                                            // Log.i("Loop", "Yes");

                                                            for (int i = 0; i < message.length(); i++) {

                                                                //Log.i("Loop", "y");

                                                                if (String.valueOf(message.charAt(i)).equals(":")) {

                                                                    semicnt++;
                                                                    //Log.i("cnt", String.valueOf(semicnt));

                                                                    if (semicnt == 3) {

                                                                        num = i;
                                                                        i = message.length() - 1;
                                                                        String time = message.substring(0, (Math.min(message.length(), num)));
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

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }
    }

}