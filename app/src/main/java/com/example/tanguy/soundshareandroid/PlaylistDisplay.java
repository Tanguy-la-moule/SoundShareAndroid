package com.example.tanguy.soundshareandroid;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.tanguy.soundshareandroid.models.SongInPlaylist;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class PlaylistDisplay extends AppCompatActivity {
    SongAdapter adapter;

    private ArrayList<SongInPlaylist> songList;

    public PlaylistDisplay(){
        this.songList = new ArrayList<SongInPlaylist>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_display);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        Bundle bundle = getIntent().getExtras();

        final String playlistName = bundle.getString("NAME");
        final ArrayList<String> playlistSongs = bundle.getStringArrayList("SONGSID");

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(playlistName);

        if (currentUser == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
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

                                if(isIn(song.getSongID(), playlistSongs)){
                                    songList.add(song);
                                }

                            }

                            Log.d("SONG REQUEST", "playlist pulled successfully");

                            // set up the RecyclerView
                            RecyclerView recyclerView = findViewById(R.id.rvSongs);
                            recyclerView.setLayoutManager(new LinearLayoutManager(PlaylistDisplay.this));
                            adapter = new SongAdapter(PlaylistDisplay.this, songList);
                            adapter.setClickListener(new SongAdapter.ItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    SongInPlaylist song = songList.get(position);
                                    goToStreamer(view, position, playlistName, song.getSongID(), song.getTitle(), song.getArtist(), song.getStorageID(), song.getCoverURL(), convertSongToString(songList));
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

    public void goToHome(View view){
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    public void goToStreamer(View view, int position, String playlistName, String songID, String title, String artist, String storageID, String coverURL, ArrayList<String> playlistSongID){
        Log.e("RANDOM", playlistSongID.toString());

        playlistSongID.remove(position);
        ArrayList<String> orderedPlaylist = new ArrayList<String>();
        orderedPlaylist.add(songID);
        while(playlistSongID.size() > 0){
            int rand = (int) Math.random() * (playlistSongID.size() - 1);
            String temporary = playlistSongID.get(rand);
            playlistSongID.remove(rand);
            orderedPlaylist.add(temporary);
        }

        Log.e("RANDOM", orderedPlaylist.toString());

        Intent intent = new Intent(this, Streamer.class);
        Bundle bundle = new Bundle();
        bundle.putString("SONGID", songID);
        bundle.putString("PLAYLISTNAME", playlistName);
        bundle.putString("TITLE", title);
        bundle.putString("ARTIST", artist);
        bundle.putString("STORAGEID", storageID);
        bundle.putString("COVERURL", coverURL);
        bundle.putStringArrayList("SONGSID", orderedPlaylist);
        intent.putExtras(bundle);
        intent.putExtra("LECTURENB", 0);
        startActivity(intent);
    }

    public ArrayList<String> convertSongToString(ArrayList<SongInPlaylist> songList){
        ArrayList<String> songIdList = new ArrayList<String>();
        for(int i = 0; i < songList.size(); i++){
            songIdList.add(songList.get(i).getSongID());
        }
        return songIdList;
    }
}
