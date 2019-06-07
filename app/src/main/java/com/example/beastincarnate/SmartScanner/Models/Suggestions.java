package com.example.beastincarnate.SmartScanner.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Suggestions {

    @SerializedName("suggestions")
    @Expose
    private ArrayList<Info> suggestions;

    public Suggestions(ArrayList<Info> suggestions) {
        this.suggestions = suggestions;
    }

    public ArrayList<Info> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(ArrayList<Info> suggestions) {
        this.suggestions = suggestions;
    }
}
