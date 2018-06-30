package com.example.tanguy.soundshareandroid;

public class Friend {
    private String ID;
    private String email;
    private String username;

    public Friend(String ID, String email, String username){
        this.ID = ID;
        this.email = email;
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }
}
