package com.example.tanguy.soundshareandroid;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendManagement extends AppCompatActivity {
    public Friend user;
    public Friend friend;

    private ArrayList<Friend> friendList;
    FriendAdapter adapter;

    public FriendManagement(){
        this.friendList = new ArrayList<Friend>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_management);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String userID = currentUser.getUid();

            db.collection("users").document(userID).collection("friends")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    Map<String, Object> friendJson = document.getData();

                                    String ID = document.getId();
                                    String username = (String) friendJson.get("username");
                                    String email = (String) friendJson.get("email");

                                    Friend friend = new Friend(ID, email, username);
                                    friendList.add(friend);
                                }

                                RecyclerView recyclerView = findViewById(R.id.rvFriends);
                                recyclerView.setLayoutManager(new LinearLayoutManager(FriendManagement.this));
                                adapter = new FriendAdapter(FriendManagement.this, friendList);
                                /*adapter.setClickListener(new PlaylistAdapter.ItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        Friend friend = friendList.get(position);
                                        goToPlaylistDisplay(view, playlist.getName(), playlist.getSongsID());
                                    }
                                });*/

                                recyclerView.setAdapter(adapter);





                            } else {
                                Log.e("ERROR", "error getting playlists" + task.getException());
                            }
                        }
                    });


        }
    }

    public void goToHome(View view){
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    public void logOut(View view){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void checkFriendEmail(View view){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        EditText editTextEmail = (EditText) findViewById(R.id.etNewFriend);

        final String friendEmail = (String) editTextEmail.getText().toString();

        Log.e("EMAIL", friendEmail);

        firebaseAuth.fetchProvidersForEmail(friendEmail)
                .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                        if (!task.getResult().getProviders().isEmpty() && !friendEmail.equals(currentUser.getEmail())) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users")
                                    .document(currentUser.getUid())
                                    .collection("friends")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                boolean alreadyFriend = false;
                                                String friendID = "";

                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    if (!alreadyFriend && document.getData().get("email").equals(friendEmail)) {
                                                        Log.e("FRIEND", "ALREADY A FRIEND");
                                                        alreadyFriend = true;
                                                    }
                                                }
                                                if (!alreadyFriend) {
                                                    Log.e("FRIEND", "EXIST AND NOT FRIEND ALREADY" + friendID.toString());
                                                    addFriend(currentUser.getUid(), friendEmail);
                                                }
                                            }
                                        }
                                    });


                        } else {
                            Log.e("FRIENDS", "l'email est celui de l'utilisateur ou n'existe pas dans la bdd");
                        }
                    }});
    }

    public void addFriend(String userID, final String friendEmail){
        final String finalUserID = userID;
        final String finalFriendEmail = friendEmail;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getData().get("email").equals(finalFriendEmail)) {
                                    Log.e("ADD FRIEND", "coucou" + finalFriendEmail + finalUserID);

                                    FirebaseFirestore db = FirebaseFirestore.getInstance();


                                    final String friendID = document.getData().get("id").toString();
                                    String friendUsername = document.getData().get("username").toString();

                                    Map<String, Object> friend = new HashMap<>();
                                    friend.put("ID", friendID);
                                    friend.put("email", finalFriendEmail);
                                    friend.put("username", friendUsername);

                                    db.collection("users").document(finalUserID).collection("friends").document(friendID)
                                            .set(friend)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.e("ADD FRIEND", "friend added to your list");
                                                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                                                    db.collection("users")
                                                            .document(finalUserID).get()
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                                                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                                                                        DocumentSnapshot document = task.getResult();

                                                                        String userUsername = document.getData().get("username").toString();


                                                                        Map<String, Object> user = new HashMap<>();
                                                                        user.put("ID", finalUserID);
                                                                        user.put("email", firebaseAuth.getCurrentUser().getEmail().toString());
                                                                        user.put("username", userUsername);

                                                                        db.collection("users").document(friendID).collection("friends").document(finalUserID)
                                                                                .set(user)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Log.e("ADD FRIEND", "added to your new friend's list");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.e("ADD FRIEND", "erreur"+ e.getMessage());
                                                                                    }
                                                                                });


                                                                    }
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("ADD FRIEND", "erreur"+ e.getMessage());
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }
}
