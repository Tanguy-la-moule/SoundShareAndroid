package com.example.tanguy.soundshareandroid;

public class SongInPlaylist {
    private String artist;
    private String title;
    private String storageID;
    private int skippedEarly;
    private int skippedLate;
    private int notSkipped;
    private float score;

    public SongInPlaylist(String artist, String title, String storageID){
        this.artist = artist;
        this.title = title;
        this.storageID = storageID;
        this.skippedEarly = 0;
        this.skippedLate = 0;
        this.notSkipped = 1;
        this.score = 1;
    }

    public String getArtist(){
        return this.artist;
    }
}