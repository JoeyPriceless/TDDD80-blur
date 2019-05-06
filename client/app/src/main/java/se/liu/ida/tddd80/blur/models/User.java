package se.liu.ida.tddd80.blur.models;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

public class User {
    private String id;
    private String username;
    private String email;
    @SerializedName("picture_path")
    private String picturePath = null;
    private Bitmap picture = null;
    private Bitmap pictureBlurred = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
