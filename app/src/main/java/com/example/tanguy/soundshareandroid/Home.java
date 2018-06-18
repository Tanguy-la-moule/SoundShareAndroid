package com.example.tanguy.soundshareandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    @Override
    public void onBackPressed(){
        Boolean disableButton =  true;
    }

    public void logOut(View view){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void goToStreamer(View view){
        Intent intent = new Intent(this, Streamer.class);
        intent.putExtra("SONG", "https://firebasestorage.googleapis.com/v0/b/soundshareandroid.appspot.com/o/MC%20Fioti%20-%20Bum%20Bum%20Tam%20Tam%20(Felckin%20X%20Moontrackers%20Remix).mp3?alt=media&token=363c8fbd-2349-48dc-a892-056bfc1bb304");
        startActivity(intent);
    }
}