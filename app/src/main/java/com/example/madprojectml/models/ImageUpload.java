package com.example.madprojectml.models;

public class ImageUpload {
    private String imageUrl;
    private String result;

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
}
