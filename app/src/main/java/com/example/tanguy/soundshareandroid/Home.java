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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Home extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    MyRecyclerViewAdapter adapter;

    private HashMap<String, SongInPlaylist> songs;
    private ArrayList<SongInPlaylist> songList;

    public Home() {
        songs = new HashMap<>();
        songList = new ArrayList<SongInPlaylist>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("songs")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> songJson = document.getData();
                                String ID = document.getId();
                                String artist = (String) songJson.get("artist");
                                String title = (String) songJson.get("title");
                                String storageID = (String) songJson.get("storageID");

                                SongInPlaylist song = new SongInPlaylist(artist, title, storageID);
                                songs.put(ID, song);

                            }

                            Log.d("SONG REQUEST", "playlist pulled successfully");

                            for(Map.Entry<String, SongInPlaylist> entry : songs.entrySet()) {
                                SongInPlaylist valeur = entry.getValue();

                                songList.add(valeur);
                            }

                            // set up the RecyclerView
                            RecyclerView recyclerView = findViewById(R.id.rvAnimals);
                            recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));
                            adapter = new MyRecyclerViewAdapter(Home.this, songList);
                            //adapter.setClickListener (this);
                            recyclerView.setAdapter(adapter);

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
        intent.putExtra("SONG", "https://firebasestorage.googleapis.com/v0/b/soundshareandroid.appspot.com/o/Veerus%20-%20P%C3%A9riple.mp3?alt=media&token=c36271ce-6be2-4d42-8099-02dc6ca18414");
        startActivity(intent);
    }
}