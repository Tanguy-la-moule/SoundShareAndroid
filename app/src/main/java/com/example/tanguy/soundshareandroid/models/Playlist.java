package com.example.tanguy.soundshareandroid.models;

import java.util.ArrayList;

/**
 * Playlist Object
 */
public class Playlist {
    private String ID;
    private String name;
    private ArrayList<String> songsID;
    private ArrayList<String> lastSongs;

    public Playlist(String ID, String name, ArrayList<String> songsID, ArrayList<String> lastSongs){
        this.ID = ID;
        this.name = name;
        this.songsID = songsID;
        this.lastSongs = lastSongs;
    }

    /**
     * Add a song to a playlist and return true if it is new
     * @param songID ID of the song
     * @return boolean
     */
    public boolean addSong(String songID){
        Boolean found = false;
        for(int i = 0 ; i < this.songsID.size(); i++){
            if(songID.equals(this.songsID.get(i))){
                found = true;
            }
        }
        if(found){
            return false;
        } else {
            this.songsID.add(songID);
            return true;
        }
    }

    /**
     * Get ID of the playlist
     * @return String
     */
    public String getID(){
        return this.ID;
    }

    /**
     * Get name of playlist
     * @return String
     */
    public String getName(){
        return this.name;
    }

    /**
     * Get IDs of songs of playlist
     * @return ArrayList<String>
     */
    public ArrayList<String> getSongsID() {
        return this.songsID;
    }

    public ArrayList<String> getLastSongs() { return this.lastSongs; }
}
