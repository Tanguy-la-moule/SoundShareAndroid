package com.example.tanguy.soundshareandroid;

public class SongInPlaylist {
    private String songID;
    private String artist;
    private String title;
    private String storageID;
    private String coverURL;
    private int skippedEarly;
    private int skippedLate;
    private int notSkipped;
    private float score;

    public SongInPlaylist(String songID, String artist, String title, String storageID, String coverURL){
        this.songID = songID;
        this.artist = artist;
        this.title = title;
        this.storageID = storageID;
        this.coverURL = coverURL;
        this.skippedEarly = 0;
        this.skippedLate = 0;
        this.notSkipped = 1;
        this.score = 1;
    }

    public String getSongID(){
        return this.songID;
    }

    public String getArtist(){
        return this.artist;
    }

    public String getTitle(){
        return this.title;
    }

    public String getStorageID(){
        return this.storageID;
    }

    public String getCoverURL(){
        return this.coverURL;
    }
}