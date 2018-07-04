package com.example.tanguy.soundshareandroid;

import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlaylistDisplay extends AppCompatActivity {

    private SongAdapter adapter;
    private String playlistID;
    private String playlistName;
    private String userID;
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

        playlistName = bundle.getString("NAME");
        playlistID = bundle.getString("PLAYLISTID");
        final ArrayList<String> playlistSongs = bundle.getStringArrayList("SONGSID");

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
                                    goToStreamer(view, position, playlistName, playlistID, song.getSongID(), song.getTitle(), song.getArtist(), song.getStorageID(), song.getCoverURL(), convertSongToString(songList));
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

    @Override
    public void onBackPressed(){
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.performClick();

    }

    public void goToStreamer(View view, int position, String playlistName, String playlistID,
                             String songID, String title, String artist, String storageID,
                             String coverURL, ArrayList<String> playlistSongID){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userID = firebaseAuth.getCurrentUser().getUid();

        Log.e("RANDOM", playlistSongID.toString());

        playlistSongID.remove(position);
        ArrayList<String> orderedPlaylist = new ArrayList<>();
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
        bundle.putString("PLAYLISTID", playlistID);
        bundle.putStringArrayList("SONGSID", orderedPlaylist);

        Map<String, Object> last_song = new HashMap<>();
        last_song.put("TITLE", title);
        last_song.put("ARTIST", artist);
        last_song.put("STORAGEID", storageID);
        last_song.put("COVERURL", coverURL);

        db.collection("users").document(userID).collection("marker").document("last song")
                .set(last_song)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("LAST SONG", "Last song successfully added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("LAST SONG", "Error writing document", e);
                    }
                });

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

    public void deletePlaylist(View view){
        final Context currentContext = this;


        new android.support.v7.app.AlertDialog.Builder(currentContext)
                .setIcon(R.drawable.ic_delete_white)
                .setTitle("Delete playlist")
                .setMessage("Are you sure you want to delete " + playlistName + " ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection("users").document(userID).collection("playlists").document(playlistID)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.e("DELETE PLAYLIST", "playlist deleted from firestore");
                                        ImageButton previousButton = (ImageButton) findViewById(R.id.backButton);
                                        previousButton.performClick();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("DELETE PLAYLIST", "Error deleting playlist", e);
                                    }
                                });
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    public void goToAddSong(View view){
        Intent intent = new Intent(this, addSongToPlaylist.class);
        Bundle bundle = new Bundle();
        bundle.putString("PLAYLISTID", playlistID);
        bundle.putString("PLAYLISTNAME", playlistName);
        bundle.putStringArrayList("SONGSID", convertSongToString(songList));
        intent.putExtras(bundle);
        startActivity(intent);
    }
}