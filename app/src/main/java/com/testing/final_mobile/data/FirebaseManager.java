package com.testing.final_mobile.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {

    private static FirebaseFirestore firestore;
    private static FirebaseAuth auth;

    public static FirebaseFirestore getFirestore() {
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance();
        }
        return firestore;
    }

    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static String getCurrentUserId() {
        return getAuth().getCurrentUser() != null
                ? getAuth().getCurrentUser().getUid()
                : null;
    }
}
