package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

public class Post extends PictureHolder{
    private String id;
    private User author;
    private String content;
    @SerializedName("time_created")
    private DateTime timeCreated; // Check json compatibility
    private Reactions reactions;
    private String location;

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public DateTime getTimeCreated() {
        return timeCreated;
    }

    public Reactions getReactions() {
        return reactions;
    }

    public void setReactions(Reactions reactions) {
        this.reactions = reactions;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}