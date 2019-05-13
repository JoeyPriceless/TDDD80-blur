package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.SerializedName;

public class User {
    private String id;
    private String username;
    private String email;
    @SerializedName("picture")
    private String pictureUri;

    public String getPictureUri() {
        return pictureUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public User(String id) {
        this.id = id;
    }
}
