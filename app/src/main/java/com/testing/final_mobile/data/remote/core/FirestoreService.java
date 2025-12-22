package com.testing.final_mobile.data.remote.core;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Transaction;

public class FirestoreService {

    private final FirebaseFirestore db;

    public FirestoreService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getCollection(Query query, OnCompleteListener<QuerySnapshot> listener) {
        query.get().addOnCompleteListener(listener);
    }

    public void getDocument(DocumentReference docRef, OnCompleteListener<DocumentSnapshot> listener) {
        docRef.get().addOnCompleteListener(listener);
    }

    public <T> void addDocument(String collectionPath, T data, OnCompleteListener<DocumentReference> listener) {
        db.collection(collectionPath).add(data).addOnCompleteListener(listener);
    }

    public <T> void runTransaction(Transaction.Function<T> updateFunction, OnCompleteListener<T> listener) {
        db.runTransaction(updateFunction).addOnCompleteListener(listener);
    }

    // Helper methods to get references if needed
    public CollectionReference getCollection(String collectionPath) {
        return db.collection(collectionPath);
    }

    public DocumentReference getDocument(String collectionPath, String documentId) {
        return db.collection(collectionPath).document(documentId);
    }
}
