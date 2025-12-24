package com.testing.final_mobile;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

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

        Map<String, String> config =  new HashMap<>();
        config.put("cloud_name", "dipl441kt");
        config.put("api_key", "367527812397522");
        config.put("api_secret", "bYH8A3DO1sRvuzk-UoJDTE7oCSc");

        try {
            MediaManager.init(this, config);
        } catch (IllegalStateException e){
            // MediaManager is already initialized
        }
    }
}
