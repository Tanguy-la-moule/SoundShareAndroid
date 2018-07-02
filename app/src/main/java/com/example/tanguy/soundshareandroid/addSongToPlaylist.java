package com.example.tanguy.soundshareandroid;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.tanguy.soundshareandroid.models.SongInPlaylist;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class addSongToPlaylist extends AppCompatActivity {

    private String playlistID;
    private String playlistName;
    private String userID;
    private ArrayList<String> playlistSongs;
    private ArrayList<SongInPlaylist> songList;

    private SongAdapter adapter;

    public addSongToPlaylist(){
        this.songList = new ArrayList<SongInPlaylist>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song_to_playlist);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        Bundle bundle = getIntent().getExtras();

        playlistID = bundle.getString("PLAYLISTID");
        playlistName = bundle.getString("PLAYLISTNAME");
        playlistSongs = bundle.getStringArrayList("SONGSID");

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(playlistName);

        if (currentUser == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            this.userID = currentUser.getUid();
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
                                    String coverURL = (String) songJson.get("coverURL");

                                    SongInPlaylist song = new SongInPlaylist(ID, artist, title, storageID, coverURL);

                                    if (!isIn(song.getSongID(), playlistSongs)) {
                                        songList.add(song);
                                    }

                                }

                                Log.d("SONG REQUEST", "playlist pulled successfully");

                                // set up the RecyclerView
                                RecyclerView recyclerView = findViewById(R.id.rvSongs);
                                recyclerView.setLayoutManager(new LinearLayoutManager(addSongToPlaylist.this));
                                adapter = new SongAdapter(addSongToPlaylist.this, songList);
                                adapter.setClickListener(new SongAdapter.ItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        final String finalSongID = songList.get(position).getSongID();
                                        final View currentView = view;

                                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                                        db.collection("users").document(userID).collection("playlists").document(playlistID).get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot document = task.getResult();
                                                            if (document.exists()) {
                                                                ArrayList<String> songsID = (ArrayList<String>) document.getData().get("songs");
                                                                songsID.add(finalSongID);

                                                                Map<String, Object> newPlaylist = new HashMap<>();
                                                                newPlaylist.put("name", playlistName);
                                                                newPlaylist.put("songs", songsID);

                                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                                db.collection("users").document(userID).collection("playlists").document(playlistID).set(newPlaylist);
                                                                Log.e("ADD SONG", "song added to the playlist");

                                                                Log.e("TEST", songsID.toString());

                                                                goToPlaylistDisplay2(currentView, songsID);
                                                            } else {
                                                                Log.d("ADD SONG", "No such playlistID");
                                                            }
                                                        } else {
                                                            Log.d("ADD SONG", "get failed with ", task.getException());
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                recyclerView.setAdapter(adapter);

                            } else {
                                Log.w("SONG REQUEST", "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
    }

    public Boolean isIn(String ID, ArrayList<String> listID){
        Boolean found = false;
        for(int i = 0; i < listID.size(); i++){
            if(ID.equals(listID.get(i))){
                found = true;
            }
        }
        return found;
    }

    public void goToPlaylistDisplay(View view){
        Intent intent = new Intent(this, PlaylistDisplay.class);
        Bundle bundle = new Bundle();
        bundle.putString("NAME", playlistName);
        bundle.putString("PLAYLISTID", playlistID);
        bundle.putStringArrayList("SONGSID", playlistSongs);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void goToPlaylistDisplay2(View view, ArrayList<String> playlistSongs){
        Intent intent = new Intent(this, PlaylistDisplay.class);
        Bundle bundle = new Bundle();
        bundle.putString("NAME", playlistName);
        bundle.putString("PLAYLISTID", playlistID);
        bundle.putStringArrayList("SONGSID", playlistSongs);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
