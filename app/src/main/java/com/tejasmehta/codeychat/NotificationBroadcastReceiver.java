package com.tejasmehta.codeychat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    DatabaseReference mDatabase;
    FirebaseAuth mAuth;

    @Override
    public void onReceive(Context context, Intent intent) {

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String action = intent.getAction();
        String type = intent.getStringExtra("notifType");
        if(action.equals("notification_cancelled")) {




        }
    }
}
