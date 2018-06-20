package com.example.tanguy.soundshareandroid;

import java.util.ArrayList;

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

    public Boolean addSong(String songID){
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

    public String getID(){
        return this.ID;
    }

    public String getName(){
        return this.name;
    }

    public ArrayList<String> getSongsID() {
        return this.songsID;
    }

    public ArrayList<String> getLastSongs() { return this.lastSongs; }
}
