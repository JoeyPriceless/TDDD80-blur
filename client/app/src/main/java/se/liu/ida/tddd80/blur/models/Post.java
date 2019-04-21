package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

public class Post {
    private String id;
    private User author;
    private String content;
    @SerializedName("time_created")
    private DateTime timeCreated; // Check json compatibility
    //private Reactions reactions;
    private int score;

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public DateTime getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(DateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    //public Reactions getReactions() {
    //    return reactions;
    //}


    public int getScore() {
        return score;
    }

    public void incrementScore(int amount) {
        score += amount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}