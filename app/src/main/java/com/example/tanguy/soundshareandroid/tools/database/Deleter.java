package com.example.tanguy.soundshareandroid.tools.database;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Tool for deleting documents in the database
 */
public class Deleter {

    public Deleter(){
    }

    /**
     * Delete a document from the database at a certain deepness
     * @param db database
     * @param root_collection root collection
     * @param ID_1 root document
     * @param second_collection category
     * @param ID_2 target document
     * @param message ..
     */
    public void delete(FirebaseFirestore db, String root_collection, String ID_1, String second_collection, String ID_2, final String message){
        db.collection(root_collection)
                .document(ID_1)
                .collection(second_collection
                ).document(ID_2)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e("DELETE FRIEND", "friend deleted " + message + " side");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DELETE FRIEND", "Error deleting document", e);
                    }
                });
    }
}
