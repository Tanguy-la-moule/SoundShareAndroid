package com.example.tanguy.soundshareandroid;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.tanguy.soundshareandroid.models.Playlist;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class Home extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    PlaylistAdapter adapter;

    private ArrayList<Playlist> playlistList;

    public Home() {
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

                                RecyclerView recyclerView = findViewById(R.id.rvPlaylist);
                                recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));
                                adapter = new PlaylistAdapter(Home.this, playlistList);
                                adapter.setClickListener(new PlaylistAdapter.ItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        Playlist playlist = playlistList.get(position);
                                        goToPlaylistDisplay(view, playlist.getName(), playlist.getID(), playlist.getSongsID());
                                    }
                                });

                                recyclerView.setAdapter(adapter);





                            } else {
                                Log.e("ERROR", "error getting playlists" + task.getException());
                            }
                        }
                    });
        }
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

    public void goToPlaylistDisplay(View view, String name, String playlistID, ArrayList<String> songsID){
        Intent intent = new Intent(this, PlaylistDisplay.class);
        Bundle bundle = new Bundle();
        bundle.putString("NAME", name);
        bundle.putString("PLAYLISTID", playlistID);
        bundle.putStringArrayList("SONGSID", songsID);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void goToFriendManagement(View view){
        Intent intent = new Intent(this, FriendManagement.class);
        startActivity(intent);
    }

    public void goToMap(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}