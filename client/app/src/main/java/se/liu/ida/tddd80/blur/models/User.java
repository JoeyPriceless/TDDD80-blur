package se.liu.ida.tddd80.blur.models;

import se.liu.ida.tddd80.blur.utilities.NetworkUtil;

public class User {
    private String id;
    private String username;
    private String email;
    private String pictureUrl;

    public String getPictureUrl() {
        return NetworkUtil.getUserPictureUrl(id);
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

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
