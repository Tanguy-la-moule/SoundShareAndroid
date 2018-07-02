package com.example.tanguy.soundshareandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Main page of application, before logging in
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * On start, if user is already connected, send to home page
     */
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        Intent intent = new Intent(this, Home.class);
        if(!(currentUser == null)){
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        Intent intent = new Intent(this, Home.class);
        if(!(currentUser == null)){
            startActivity(intent);
        }
    }

    /**
     * For logging in the application
     * @param view Current view
     */
    public void sendToConnection(View view){
        Intent intent = new Intent(this, Connection.class);
        startActivity(intent);
    }

    /**
     * For signing up the application
     * @param view Current view
     */
    public void sendToRegistration(View view){
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
    }
}
