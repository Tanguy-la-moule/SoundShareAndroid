package com.example.tanguy.soundshareandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class Streamer extends AppCompatActivity {

    private ImageButton btn;
    private boolean playPause;
    private MediaPlayer mediaPlayer;
    private ProgressDialog progressDialog;
    private boolean initialStage = true;
    private String songId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streamer);

        Intent previousIntent = getIntent();
        final String songId = previousIntent.getStringExtra("SONG");
        Log.i("INTENT", songId);

        btn = (ImageButton) findViewById(R.id.audioStreamBtn);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        progressDialog = new ProgressDialog(this);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(!playPause){
                    btn.setImageResource(R.drawable.ic_pause_white);
                    if (initialStage){
                        //new Player().execute(songId);
                        new Player().execute(songId);
                    } else {
                        if (!mediaPlayer.isPlaying()){
                            mediaPlayer.start();
                        }
                    }
                    playPause = true;
                } else {
                    btn.setImageResource(R.drawable.ic_play_arrow_white);
                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                    }
                    playPause = false;
                }
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
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
                        btn.setImageResource(R.drawable.ic_play_arrow_white);
                        mediaPlayer.stop();
                        mediaPlayer.reset();
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

    public void nextSong(View view){
        Intent intent = new Intent(this, Streamer.class);
        intent.putExtra("SONG", "https://firebasestorage.googleapis.com/v0/b/soundshareandroid.appspot.com/o/Biffty%20-%20Roule%20un%20boze%20(Remix%20Dj%20Weedim).mp3?alt=media&token=c98f5532-1047-49e3-a55b-f418e39c2732");
        startActivity(intent);
    }
    public void previousSong(View view){
        Intent intent = new Intent(this, Streamer.class);
        intent.putExtra("SONG", "https://firebasestorage.googleapis.com/v0/b/soundshareandroid.appspot.com/o/Suicide%20Social%20%5BVID%C3%89O%20OFFICIELLE%5D.mp3?alt=media&token=fc09ee22-5154-4d09-95d6-ecb6d40df43a");
        startActivity(intent);
    }
}
