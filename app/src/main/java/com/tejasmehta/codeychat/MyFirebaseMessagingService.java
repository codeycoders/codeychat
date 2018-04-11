package com.tejasmehta.codeychat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.logging.Handler;

/**
 * Created by tejasmehta on 2/24/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    String channelId;
    String channelName;
    String notifGroup;
    int notID;
    int idNum;
    String type;
    Intent intent;
    String username;
    PendingIntent pendingIntent;

    @Override
    public void onMessageReceived(final RemoteMessage message) {

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (message.getData().get("id").contains("group")) {

            notID = Integer.parseInt(message.getData().get("id").replace("group", ""));

        } else if(message.getData().get("id").contains("all")) {

            notID = 0;

        }

        if (isAppOnForeground(getApplicationContext())) {

            Log.i("fg", "yess");

        } else {


            if (message.getData().get("message").contains("<") && message.getData().get("message").contains(">")) {

                final String extract = message.getData().get("message").substring(message.getData().get("message").lastIndexOf("<") + 1, message.getData().get("message").indexOf(">"));
                if (mAuth.getCurrentUser() != null) {

                    String email = mAuth.getCurrentUser().getEmail().replace(".", ",");
                    mDatabase.child("users").child("c").child("emailToUsername").child(email).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            username = dataSnapshot.getValue(String.class);
                            if (username != null) {

                                if (!username.equals(extract)) {

                                    Log.i("notis", "yyyy");

                                    sendMyNotification(message.getData().get("message"), message.getData().get("title"), message.getData().get("tab"), Integer.parseInt(String.valueOf(notID)));





                                }

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            } else {

                final String[] split = message.getData().get("message").split(" ");
                if (mAuth.getCurrentUser() != null) {

                    String email = mAuth.getCurrentUser().getEmail().replace(".", ",");
                    mDatabase.child("users").child("c").child("emailToUsername").child(email).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String username = dataSnapshot.getValue(String.class);
                            if (username != null) {

                                if (!username.equals(split[1])) {

                                    Log.i("notis", "yyyy");

                                    sendMyNotification(message.getData().get("message"), message.getData().get("title"), message.getData().get("tab"), Integer.parseInt(String.valueOf(notID)));

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



    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void sendMyNotification(final String message, final String title, String tab, final int id) {

        //On click of notification it redirect to this Activity

        if (tab.equals("1")) {

            channelId = "channel-01";
            channelName = "Private Chats";
            intent = new Intent(this, ChatsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("tab", tab);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            sendNotif(title, message, pendingIntent, id);


        } else if (tab.equals("2")) {

            channelId = "channel-02";
            channelName = "Group Chats";
            notifGroup = title;
            intent = new Intent(this, ChatsViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mDatabase.child("chat").child("groups").child("notifRefs").child(title).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final String groupNames = dataSnapshot.getValue(String.class);
                    if (groupNames != null) {


                        intent.putExtra("name", title);
                        intent.putExtra("notif", "true");

                        mDatabase.child("chat").child("groups").child(title).child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String adminUser = dataSnapshot.getValue(String.class);

                                if (adminUser != null) {

                                    if (username.equals(adminUser)) {

                                        intent.putExtra("admin", true);
                                        Log.i("is", "y");
                                        Log.i("in", "actStart");
                                        FirebaseMessaging.getInstance().subscribeToTopic(groupNames);
                                        intent.putExtra("groupNum", title);
                                        intent.putExtra("notifTpc", groupNames);
                                        pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                        sendNotif(title, message, pendingIntent, id);



                                    } else {

                                        intent.putExtra("admin", false);
                                        Log.i("not", "y");
                                        Log.i("in", "actStart");
                                        FirebaseMessaging.getInstance().subscribeToTopic(groupNames);
                                        intent.putExtra("groupNum", title);
                                        intent.putExtra("notifTpc", groupNames);
                                        pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                        sendNotif(title, message, pendingIntent, id);



                                    }


                                } else {

                                    intent.putExtra("name", title);
                                    intent.putExtra("admin", false);
                                    FirebaseMessaging.getInstance().subscribeToTopic(groupNames);
                                    intent.putExtra("groupNum", title);
                                    pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                    sendNotif(title, message, pendingIntent, id);


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

            type = "group" + notID;


        } else {

            channelId = "channel-03";
            channelName = "Public Messages";
            notifGroup = "Public";

            type = "public";

        }


/*

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);



        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            NotificationManager notificationMan =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationMan.createNotificationChannel(mChannel);
        }


        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),channelId )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setGroup(notifGroup)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.chat_notif_round))
                .setSmallIcon(R.drawable.chatty)
                .setDeleteIntent(getDeleteIntent(type))
                .setContentIntent(pendingIntent);



        notificationManager.notify(id, notificationBuilder.build()); */


    }

    protected PendingIntent getDeleteIntent(String Ntype) {
        Intent intent = new Intent(getApplicationContext(), NotificationBroadcastReceiver.class);
        intent.setAction("notification_cancelled");
        intent.putExtra("notifType", Ntype);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    public void sendNotif(String title, String message, PendingIntent pendingIntent2, int id2) {

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            NotificationManager notificationMan =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationMan.createNotificationChannel(mChannel);
        }


        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),channelId )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setGroup(notifGroup)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.chat_notif_round))
                .setSmallIcon(R.drawable.chatty)
                .setDeleteIntent(getDeleteIntent(type))
                .setContentIntent(pendingIntent2);



        notificationManager.notify(id2, notificationBuilder.build());

    }


}
