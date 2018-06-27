package com.example.tanguy.soundshareandroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streamer);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvArtist = findViewById(R.id.tvArtist);
        ImageView ivCover = findViewById(R.id.ivAlbumCover);
        TextView tvPlaylist = findViewById(R.id.tvPlaylist);

        Bundle bundle = getIntent().getExtras();

        String title = bundle.getString("TITLE");
        String artist = bundle.getString("ARTIST");
        final String storageID = bundle.getString("STORAGEID");
        String coverURL = bundle.getString("COVERURL");
        String playlistName = bundle.getString("PLAYLISTNAME");

        notificationCall(title, artist);

        Picasso.with(this).load(coverURL).resize(650, 650).into(ivCover);

        tvTitle.setText(title);
        tvArtist.setText(artist);
        tvPlaylist.setText(playlistName);

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

    public void notificationCall(String title, String artist){
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo_miniature)
                .setContentTitle(title)
                .setContentText(artist)
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
}