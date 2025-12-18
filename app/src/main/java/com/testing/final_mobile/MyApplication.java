package com.testing.final_mobile;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Build settings with persistence enabled
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        
        db.setFirestoreSettings(settings);
    }
}
