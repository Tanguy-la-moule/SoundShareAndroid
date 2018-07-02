package com.example.tanguy.soundshareandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for signing up the application
 */
public class Registration extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    public Registration(){
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
    }

    public void backHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void sendToConnection(View view){
        Intent intent = new Intent(this, Connection.class);
        startActivity(intent);
    }

    public void signUp(View view) {
        EditText editEmail = (EditText) findViewById(R.id.editText6);
        EditText editPassword = (EditText) findViewById(R.id.editText7);
        EditText editUsername = (EditText) findViewById(R.id.editText);
        final String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();
        final String username = editUsername.getText().toString();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        final Intent intent = new Intent(this, Home.class);
        final View currentView = view;

        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (email.length() > 0 && password.length() > 0) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("REGISTRATION", "registered auth user successfully");

                                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                                FirebaseFirestore.setLoggingEnabled(true);
                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                // Create a new user with a first and last name
                                Map<String, Object> user = new HashMap<>();
                                user.put("id", currentUser.getUid());
                                user.put("email", email);
                                user.put("username", username);
                                // Add a new document with a generated ID
                                db.collection("users").document(currentUser.getUid())
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("DATABASE", "database user registered");
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("REGISTRATION", "erreur"+ e.getMessage());
                                            }
                                        });
                            } else {
                                Log.w("REGISTRATION", task.getException());
                                Snackbar.make(currentView, "Your inscription failed", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                    });
        }
    }
}
