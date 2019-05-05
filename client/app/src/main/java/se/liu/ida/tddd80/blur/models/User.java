package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.SerializedName;

public class User extends PictureHolder {
    private String id;
    private String username;
    private String email;

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
