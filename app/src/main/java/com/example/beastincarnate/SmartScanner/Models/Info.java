package com.example.beastincarnate.SmartScanner.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Info {

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("imageURL")
    @Expose
    private String imageURL;

    @SerializedName("link")
    @Expose
    private String link;

    @SerializedName("price")
    @Expose
    private String price;


    public Info(String title, String imageURL, String link, String price) {
        this.title = title;
        this.imageURL = imageURL;
        this.link = link;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
