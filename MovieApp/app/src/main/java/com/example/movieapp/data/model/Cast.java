package com.example.movieapp.data.model;

import com.google.gson.annotations.SerializedName;

public class Cast {
    @SerializedName("id")
    private int id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("character")
    private String character;
    
    @SerializedName("profile_path")
    private String profilePath;
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCharacter() {
        return character;
    }
    
    public String getProfilePath() {
        return profilePath;
    }
}