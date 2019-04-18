package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.time.LocalDateTime;
import java.util.Date;

import se.liu.ida.tddd80.blur.utilities.StringUtil;

public class Post {
    @Expose
    private String id;
    private User author;
    @Expose
    private String content;
    @Expose
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

    public String getFormatedTimeCreated() {
        // TODO format
        return timeCreated.toString();
    }

    public String getTimeSinceCreation() {
        return StringUtil.timeSinceCreation(timeCreated);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Post(String id, User author, String content, DateTime timestamp,
                Reactions reactions) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.timeCreated = timestamp;
        this.reactions = reactions;
    }

    public Post(User author, String content) {
        this.author = author;
        this.content = content;
    }
}