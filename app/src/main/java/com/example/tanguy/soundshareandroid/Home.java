package com.example.tanguy.soundshareandroid;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    MyRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ArrayList<String> songList = new ArrayList<>();

        songList.add("poudlard");
        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rvAnimals);
        recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));
        adapter = new MyRecyclerViewAdapter(Home.this, songList);
        //adapter.setClickListener (this);
        recyclerView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("songs")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TESTFIRESTORE", document.getId() + " => " + document.getData());
                            }

                            Log.d("SONG REQUEST", "playlist pulled successfully");


                        } else {
                            Log.w("SONG REQUEST", "Error getting documents.", task.getException());
                        }
                    }
                });
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