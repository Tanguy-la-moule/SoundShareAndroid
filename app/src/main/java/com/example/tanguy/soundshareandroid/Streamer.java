package com.example.tanguy.soundshareandroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Streamer extends AppCompatActivity {

    private ImageButton playOrPause;
    private ImageButton nextSongButton;

    private boolean playPause = true;
    private boolean initialStage = false;

    private String title;
    private String artist;
    private String coverURL;
    private String playlistName;
    private String playlistID;
    private String storageID;

    private String nextTitle;
    private String nextArtist;
    private String nextCoverURL;
    private String nextStorageID;

    private int lectureNb = 0;
    private ArrayList<String> orderedPlaylist;

    private MediaPlayer mediaPlayer;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streamer);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvArtist = findViewById(R.id.tvArtist);
        ImageView ivCover = findViewById(R.id.ivAlbumCover);
        TextView tvPlaylist = findViewById(R.id.tvPlaylist);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        this.title = bundle.getString("TITLE");
        this.artist = bundle.getString("ARTIST");
        this.coverURL = bundle.getString("COVERURL");
        this.playlistName = bundle.getString("PLAYLISTNAME");
        this.playlistID = bundle.getString("PLAYLISTID");
        this.storageID = bundle.getString("STORAGEID");
        this.orderedPlaylist = bundle.getStringArrayList("SONGSID");
        this.lectureNb = intent.getExtras().getInt("LECTURENB");

        notificationCall();
        Picasso.with(this).load(this.coverURL).resize(650, 650).into(ivCover);

        tvTitle.setText(this.title);
        tvArtist.setText(this.artist);
        tvPlaylist.setText(this.playlistName);

        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.progressDialog = new ProgressDialog(this);
        new Player().execute(storageID);

        playOrPause = (ImageButton) findViewById(R.id.audioStreamBtn);
        playOrPause.setImageResource(R.drawable.ic_pause_white);
        playOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playPause) {
                    playOrPause.setImageResource(R.drawable.ic_pause_white);
                    if (initialStage) {
                        new Player().execute(storageID);
                    } else {
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        }
                    }
                    playPause = true;
                } else {
                    playOrPause.setImageResource(R.drawable.ic_play_arrow_white);
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                    playPause = false;
                }
            }
        });

        if(this.lectureNb + 1 == this.orderedPlaylist.size()){
            this.lectureNb = 0;
        } else {
            this.lectureNb = this.lectureNb + 1;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("songs").document(orderedPlaylist.get(lectureNb))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Map<String, Object> songJson = document.getData();
                        String artist = (String) songJson.get("artist");
                        String title = (String) songJson.get("title");
                        String storageID = (String) songJson.get("storageID");
                        String coverURL = (String) songJson.get("coverURL");

                        setNextSong(title, artist, coverURL, storageID);

                        nextSongButton = (ImageButton) findViewById(R.id.ibNextSong);
                        nextSongButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                nextStream(view);
                            }
                        });
                    } else {
                        Log.i("DATABASE", "No such document in the database");
                    }


                } else {
                    Log.i("DATABASE", "couldn't get next song from db");
                }
            }
        });
    }

    public void resetSong(View view){
        if (this.mediaPlayer != null){
            this.mediaPlayer.reset();
            this.progressDialog = new ProgressDialog(this);
            new Player().execute(storageID);


        }
    }

    public void deleteSongFromPlaylist(View view){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String userID = firebaseAuth.getCurrentUser().getUid();

        final Context currentContext = this;
        final String finalSongID = this.orderedPlaylist.get(this.lectureNb);
        final String finalPlaylistID = this.playlistID;


        new android.support.v7.app.AlertDialog.Builder(currentContext)
                .setIcon(R.drawable.ic_delete_white)
                .setTitle("Delete song")
                .setMessage("Are you sure you want to delete " + this.title + " from the " + this.playlistName + " playlist?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection("users").document(userID).collection("playlists").document(finalPlaylistID).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                String name = (String) document.getData().get("name");
                                                ArrayList<String> songsID = (ArrayList<String>) document.getData().get("songs");
                                                songsID.remove(finalSongID);

                                                Map<String, Object> newPlaylist = new HashMap<>();
                                                newPlaylist.put("name", name);
                                                newPlaylist.put("songs", songsID);

                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                db.collection("users").document(userID).collection("playlists").document(playlistID).set(newPlaylist);
                                                Log.e("DELETE SONG", "song deleted from playlist database");

                                                orderedPlaylist.remove(finalSongID);
                                                lectureNb = lectureNb - 1;

                                                nextSongButton = (ImageButton) findViewById(R.id.ibNextSong);
                                                nextSongButton.performClick();
                                            } else {
                                                Log.d("DELETE SONG", "No such playlistID");
                                            }
                                        } else {
                                            Log.d("DELETE SONG", "get failed with ", task.getException());
                                        }
                                    }
                                });
                    }

                })
                .setNegativeButton("No", null)
                .show();




    }



    class Player extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings){
            Boolean prepared = false;

            try {
                mediaPlayer.setDataSource(strings[0]);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        initialStage = true;
                        playPause = false;
                        playOrPause.setImageResource(R.drawable.ic_play_arrow_white);
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        nextSongButton = (ImageButton) findViewById(R.id.ibNextSong);
                        nextSongButton.performClick();
                    }
                });

                mediaPlayer.prepare();
                prepared = true;

            } catch (Exception e){
                Log.e("MyAudioStreamingApp", e.getMessage());
                prepared = false;
            }
            return prepared;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean){
            super.onPostExecute(aBoolean);

            if (progressDialog.isShowing()){
                progressDialog.cancel();
            }

            mediaPlayer.start();
            initialStage = false;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            progressDialog.setMessage("Buffering ...");
            progressDialog.show();
        }
    }

    @Override
    public void onBackPressed(){
        if (mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onBackPressed();

    }


    public void goToPlaylistDisplay(View view){
        if (mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        Bundle previousBundle = getIntent().getExtras();

        String playlistName = previousBundle.getString("PLAYLISTNAME");
        ArrayList<String> orderedPlaylist = previousBundle.getStringArrayList("SONGSID");

        cancelNotification(getBaseContext());

        Intent intent = new Intent(this, PlaylistDisplay.class);
        Bundle bundle = new Bundle();
        bundle.putString("NAME", playlistName);
        bundle.putStringArrayList("SONGSID", orderedPlaylist);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void notificationCall(){
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo_miniature)
                .setContentTitle(this.title)
                .setContentText(this.artist)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0L}); // Passing null here silently fails

        /*Intent intent = new Intent(this, Home.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        notificationBuilder.setContentIntent(pendingIntent);*/

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());


    }

    public static void cancelNotification(Context context) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(ns);
        notificationManager.cancel(1);
    }

    public void setNextSong(String title, String artist, String coverURL, String storageID){
        this.title = this.nextTitle;
        this.artist = this.nextArtist;
        this.coverURL = this.nextCoverURL;
        this.storageID = this.nextStorageID;
        this.nextTitle = title;
        this.nextArtist = artist;
        this.nextCoverURL = coverURL;
        this.nextStorageID = storageID;
    }

    public void nextStream(View view){
        if (this.mediaPlayer != null){
            this.mediaPlayer.reset();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }

        final String finalNextStorageID = this.nextStorageID;

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvArtist = findViewById(R.id.tvArtist);
        ImageView ivCover = findViewById(R.id.ivAlbumCover);

        tvTitle.setText(this.nextTitle);
        tvArtist.setText(this.nextArtist);
        Picasso.with(this).load(this.nextCoverURL).resize(650, 650).into(ivCover);

        cancelNotification(getBaseContext());
        notificationCall();

        playOrPause = (ImageButton) findViewById(R.id.audioStreamBtn);

        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        progressDialog = new ProgressDialog(this);
        new Player().execute(this.nextStorageID);
        playOrPause.setImageResource(R.drawable.ic_pause_white);
        playOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playPause) {
                    playOrPause.setImageResource(R.drawable.ic_pause_white);
                    if (initialStage) {
                        new Player().execute(finalNextStorageID);
                    } else {
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        }
                    }
                    playPause = true;
                } else {
                    playOrPause.setImageResource(R.drawable.ic_play_arrow_white);
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                    playPause = false;
                }
            }
        });

        if(this.lectureNb + 1 == this.orderedPlaylist.size()){
            this.lectureNb = 0;
        } else {
            this.lectureNb = this.lectureNb + 1;
        }

        nextSongButton = (ImageButton) findViewById(R.id.ibNextSong);
        nextSongButton.setOnClickListener(null);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("songs").document(orderedPlaylist.get(lectureNb))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Map<String, Object> songJson = document.getData();
                        String artist = (String) songJson.get("artist");
                        String title = (String) songJson.get("title");
                        String storageID = (String) songJson.get("storageID");
                        String coverURL = (String) songJson.get("coverURL");

                        setNextSong(title, artist, coverURL, storageID);

                        nextSongButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                nextStream(view);
                            }
                        });
                    } else {
                        Log.i("DATABASE", "No such document in the database");
                    }
                } else {
                    Log.i("DATABASE", "couldn't get next song from db");
                }
            }
        });
    }
}