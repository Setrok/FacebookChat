package com.example.facebook.facebookchat;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class FacebookChat extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
