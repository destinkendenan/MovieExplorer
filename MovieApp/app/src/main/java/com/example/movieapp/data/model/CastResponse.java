package com.example.movieapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CastResponse {
    @SerializedName("id")
    private int id;
    
    @SerializedName("cast")
    private List<Cast> cast;
    
    public int getId() {
        return id;
    }
    
    public List<Cast> getCast() {
        return cast;
    }
}