package com.example.tanguy.soundshareandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.tanguy.soundshareandroid.models.Friend;
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
import java.util.Map;

public class ShareSong extends AppCompatActivity {

    private String songID;
    private ArrayList<Friend> friendList;
    private FriendAdapter adapter;

    public ShareSong(){
        this.friendList = new ArrayList<Friend>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_song);

        Bundle bundle = getIntent().getExtras();

        songID = bundle.getString("SONGID");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null){
            startActivity(new Intent(this, MainActivity.class));
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String userID = currentUser.getUid();

            db.collection("users").document(userID).collection("friends")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Object> friendJson = document.getData();

                                    String ID = document.getId();
                                    String username = (String) friendJson.get("username");
                                    String email = (String) friendJson.get("email");

                                    Friend friend = new Friend(ID, email, username);
                                    friendList.add(friend);
                                }
                                RecyclerView recyclerView = findViewById(R.id.rvSongs);
                                recyclerView.setLayoutManager(new LinearLayoutManager(ShareSong.this));
                                adapter = new FriendAdapter(ShareSong.this, friendList);
                                adapter.setClickListener(new FriendAdapter.ItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {

                                        final Friend friend = friendList.get(position);
                                        Log.e("CHOSEN FRIEND", friend.getID());
                                    }
                                });

                                recyclerView.setAdapter(adapter);
                            }
                        }
                    });
        }
    }
}
