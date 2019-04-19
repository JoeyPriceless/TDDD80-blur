package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.time.LocalDateTime;
import java.util.Date;

import se.liu.ida.tddd80.blur.utilities.StringUtil;

public class Post {
    private String id;
    private User author;
    private String content;
    @SerializedName("timestamp")
    private DateTime timeCreated; // Check json compatibility
    private Reactions reactions;

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

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}