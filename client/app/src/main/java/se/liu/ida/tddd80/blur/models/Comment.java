package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

public class Comment {

    private String id;
    private User author;
    private String content;
    @SerializedName("time_created")
    private DateTime timeCreated;
    private int score;
    private int ownScore;

    public void setScore(int score) {
        this.score = score;
    }

    public void setOwnScore(int reaction) {
        this.ownScore = reaction;
    }

    public int getScore() {
        return score;
    }

    public int getOwnScore() {
        return ownScore;
    }

    public String getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public DateTime getTimeCreated() {
        return timeCreated;
    }
}
