package com.testing.final_mobile.data.remote.core;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

public class FirestoreService {

    private final FirebaseFirestore db;

    public FirestoreService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public CollectionReference getCollection(String collectionPath) {
        return db.collection(collectionPath);
    }

    public DocumentReference getDocument(String collectionPath, String documentId) {
        return db.collection(collectionPath).document(documentId);
    }

    public void getCollection(Query query, OnCompleteListener<QuerySnapshot> listener) {
        query.get().addOnCompleteListener(listener);
    }

    public void getDocument(DocumentReference docRef, OnCompleteListener<DocumentSnapshot> listener) {
        docRef.get().addOnCompleteListener(listener);
    }

    // You can add more generic methods here in the future, e.g., for adding/updating documents.
}
