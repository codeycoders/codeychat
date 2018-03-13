package com.tejasmehta.codeychat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import android.util.Log;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

/**
 * Created by tejasmehta on 2/24/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    String channelId;
    String channelName;

    @Override
    public void onMessageReceived(final RemoteMessage message) {

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

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

                            String username = dataSnapshot.getValue(String.class);
                            if (username != null) {

                                if (!username.equals(extract)) {

                                    Log.i("notis", "yyyy");

                                    sendMyNotification(message.getData().get("message"), message.getData().get("title"), message.getData().get("tab"));

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

                                    sendMyNotification(message.getData().get("message"), message.getData().get("title"), message.getData().get("tab"));

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

    private void sendMyNotification(String message, String title, String tab) {

        //On click of notification it redirect to this Activity
        Intent intent = new Intent(this, ChatsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("tab", tab);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (tab.equals("1")) {

            channelId = "channel-01";
            channelName = "Private Chats";

        } else if (tab.equals("2")) {

            channelId = "channel-02";
            channelName = "Group Chats";

        } else {

            channelId = "channel-03";
            channelName = "Public Messages";
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }



        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),channelId )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.chat_notif_round))
                .setSmallIcon(R.drawable.chatty)
                .setContentIntent(pendingIntent);


        notificationManager.notify(0, notificationBuilder.build());
    }



}
