package com.example.tanguy.soundshareandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class Streamer extends AppCompatActivity {

    private ImageButton playOrPause;
    private ImageButton nextSongButton;
    private boolean playPause;
    private MediaPlayer mediaPlayer;
    private ProgressDialog progressDialog;
    private boolean initialStage = false;
    private ArrayList<SongInPlaylist> nextSongs;

    public Streamer(){
        this.nextSongs = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streamer);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvArtist = findViewById(R.id.tvArtist);
        ImageView ivCover = findViewById(R.id.ivAlbumCover);

        Bundle bundle = getIntent().getExtras();

        String title = bundle.getString("TITLE");
        String artist = bundle.getString("ARTIST");
        final String storageID = bundle.getString("STORAGEID");
        String coverURL = bundle.getString("COVERURL");

        Picasso.with(this).load(coverURL).resize(650, 650).into(ivCover);

        tvTitle.setText(title);
        tvArtist.setText(artist);

        playOrPause = (ImageButton) findViewById(R.id.audioStreamBtn);

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        progressDialog = new ProgressDialog(this);
        new Player().execute(storageID);
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

        Intent intent = getIntent();

        final ArrayList<String> orderedPlaylist = bundle.getStringArrayList("SONGSID");
        int previousLectureNb = intent.getExtras().getInt("LECTURENB");
        final int lectureNb;
        if(previousLectureNb + 1 == orderedPlaylist.size()){
            lectureNb = 0;
        } else {
            lectureNb = previousLectureNb + 1;
        }

        nextSongButton = (ImageButton) findViewById(R.id.ibNextSong);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("songs").document(orderedPlaylist.get(lectureNb))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Map<String, Object> songJson = document.getData();
                        String ID = document.getId();
                        String artist = (String) songJson.get("artist");
                        String title = (String) songJson.get("title");
                        String storageID = (String) songJson.get("storageID");
                        String coverURL = (String) songJson.get("coverURL");

                        final SongInPlaylist nextSong = new SongInPlaylist(ID, artist, title, storageID, coverURL);

                        nextSongButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                streamNextSong(view, nextSong, orderedPlaylist, lectureNb);
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

    public void lookForNextSong (View view){

    }

    public void streamNextSong(View view, SongInPlaylist nextSong, ArrayList<String> orderedPlaylist, int lectureNb) {
        Intent intent = new Intent(this, Streamer.class);
        Bundle bundle = new Bundle();

        if (mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        bundle.putString("SONGID", nextSong.getSongID());
        bundle.putString("TITLE", nextSong.getTitle());
        bundle.putString("ARTIST", nextSong.getArtist());
        bundle.putString("STORAGEID", nextSong.getStorageID());
        bundle.putString("COVERURL", nextSong.getCoverURL());
        bundle.putStringArrayList("SONGSID", orderedPlaylist);
        intent.putExtras(bundle);
        intent.putExtra("LECTURENB", lectureNb);
        startActivity(intent);
    }

    public void resetSong(View view){
        if (mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.start();
        }
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

    public void goToHome(View view){
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }
}
