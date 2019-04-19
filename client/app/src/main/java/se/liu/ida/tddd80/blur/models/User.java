package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.SerializedName;

public class User {
    private String id;
    private String username;
    private String email;
    @SerializedName("picture_path")
    private String picturePath = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
