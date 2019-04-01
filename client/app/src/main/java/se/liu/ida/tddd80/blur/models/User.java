package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.SerializedName;

public class User {
    private String id;
    private String username;
    private String email;
    @SerializedName("picture_path")
    private String picturePath;

    public User(String id, String username, String email, String picturePath) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.picturePath = picturePath;
    }

    public User(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.picturePath = null;
    }
}
