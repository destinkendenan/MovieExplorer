package com.example.movieapp.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieResponse {
    @SerializedName("results")
    private List<Film> results;
    
    @SerializedName("page")
    private int page;
    
    @SerializedName("total_pages")
    private int totalPages;
    
    @SerializedName("total_results")
    private int totalResults;
    
    // Getters
    public List<Film> getResults() {
        return results;
    }
    
    public int getPage() {
        return page;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public int getTotalResults() {
        return totalResults;
    }
}