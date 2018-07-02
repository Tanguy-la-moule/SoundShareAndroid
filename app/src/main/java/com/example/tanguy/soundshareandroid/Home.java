package com.example.tanguy.soundshareandroid;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.tanguy.soundshareandroid.models.Playlist;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
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

    public void createPlaylist(View view){

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        final EditText editPlaylistName = (EditText) findViewById(R.id.playlistName);
        final String newPlaylistName = editPlaylistName.getText().toString();
        if(newPlaylistName.length() > 5){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            String userID = firebaseAuth.getCurrentUser().getUid();

            Map<String, Object> playlist = new HashMap<>();
            playlist.put("name", newPlaylistName);
            playlist.put("songs", new ArrayList<String>());

            db.collection("users").document(userID).collection("playlists").add(playlist)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Playlist newPlaylist = new Playlist(documentReference.getId(), newPlaylistName, new ArrayList<String>(), new ArrayList<String>());
                            playlistList.add(newPlaylist);
                            adapter.notifyDataSetChanged();
                            editPlaylistName.setText("");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("NEW PLAYLIST", "Error adding playlist" + e.getMessage());
                        }
                    });
        } else {
            Snackbar.make(view, "Playlist name has to be at least 6 characters long", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

    public void createPlaylist(View view){

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        final EditText editPlaylistName = (EditText) findViewById(R.id.playlistName);
        final String newPlaylistName = editPlaylistName.getText().toString();
        if(newPlaylistName.length() > 5){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            String userID = firebaseAuth.getCurrentUser().getUid();

            Map<String, Object> playlist = new HashMap<>();
            playlist.put("name", newPlaylistName);
            playlist.put("songs", new ArrayList<String>());

            db.collection("users").document(userID).collection("playlists").add(playlist)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Playlist newPlaylist = new Playlist(documentReference.getId(), newPlaylistName, new ArrayList<String>(), new ArrayList<String>());
                            playlistList.add(newPlaylist);
                            adapter.notifyDataSetChanged();
                            editPlaylistName.setText("");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("NEW PLAYLIST", "Error adding playlist" + e.getMessage());
                        }
                    });
        } else {
            Snackbar.make(view, "Playlist name has to be at least 6 characters long", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
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

    public void addPlaylist(View view){
        final EditText namePlaylist = findViewById(R.id.nameAddPlaylist);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String userID = currentUser.getUid();
        Map<String, Object> playlist = new HashMap<>();
        playlist.put("name", namePlaylist);
        playlist.put("songs", new ArrayList<String>());
//        playlist.put("lastSongs", new ArrayList<String>());

        db.collection("users").document(userID).collection("playlists").document().set(playlist)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(" ", "DocumentSnapshot written with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(" ", "Error adding document", e);
//                    }
//                });
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("ADD PLAYLIST", namePlaylist + "added to your playlists");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("ADD PLAYLIST", "erreur"+ e.getMessage());
                    }
                });
    }

//    public void goToAddPlaylist(View view){
//        Intent intent = new Intent(this, AddPlaylist.class);
//        startActivity(intent);
//    }
}