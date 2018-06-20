package com.example.tanguy.soundshareandroid;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Home extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    PlaylistAdapter adapter;

    //private HashMap<String, SongInPlaylist> songs;
    //private ArrayList<SongInPlaylist> songList;
    private ArrayList<Playlist> playlistList;

    public Home() {
        //this.songs = new HashMap<>();
        //this.songList = new ArrayList<SongInPlaylist>();
        this.playlistList = new ArrayList<Playlist>();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String userID = currentUser.getUid();

            db.collection("users").document(userID).collection("playlists")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    Map<String, Object> playlistJson = document.getData();

                                    String ID = document.getId();
                                    String name = (String) playlistJson.get("name");
                                    ArrayList<String> songsID = (ArrayList<String>) playlistJson.get("songs");

                                    ArrayList<String> lastSongs = new ArrayList<String>();

                                    Playlist playlist = new Playlist(ID, name, songsID, lastSongs);
                                    playlistList.add(playlist);
                                }

                                Log.d("PLAYLIST REQUEST", "playlists pulled successfully");

                                RecyclerView recyclerView = findViewById(R.id.rvAnimals);
                                recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));
                                adapter = new PlaylistAdapter(Home.this, playlistList);
                                adapter.setClickListener(new PlaylistAdapter.ItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        Playlist playlist = playlistList.get(position);
                                        goToPlaylistDisplay(view, playlist.getID(), playlist.getName(), playlist.getSongsID());
                                    }
                                });

                                recyclerView.setAdapter(adapter);





                            } else {
                                Log.e("ERROR", "error getting playlists" + task.getException());
                            }
                        }
                    });

        }




        /*db.collection("songs")
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
                            adapter.setClickListener(new MyRecyclerViewAdapter.ItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    SongInPlaylist song = songList.get(position);
                                    goToStreamer(view, song.getTitle(), song.getArtist(), song.getStorageID(), song.getCoverURL());
                                }
                            });
                            recyclerView.setAdapter(adapter);

                        } else {
                            Log.w("SONG REQUEST", "Error getting documents.", task.getException());
                        }
                    }
                });*/
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

    public void goToPlaylistDisplay(View view, String ID, String name, ArrayList<String> songsID){
        Intent intent = new Intent(this, PlaylistDisplay.class);
        Bundle bundle = new Bundle();
        bundle.putString("ID", ID);
        bundle.putString("NAME", name);
        bundle.putStringArrayList("SONGSID", songsID);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}