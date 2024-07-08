package com.example.madprojectml.models;

public class ImageUpload {
    private String imageUrl;
    private String result;
    private String key; // Add a key field to identify the object in the database

    public ImageUpload() {
        // Default constructor required for calls to DataSnapshot.getValue(ImageUpload.class)
    }

    public ImageUpload(String imageUrl, String result) {
        this.imageUrl = imageUrl;
        this.result = result;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
