package se.liu.ida.tddd80.blur.models;

import java.time.LocalDateTime;

public class Post {
    private String id;
    private User author;
    private String content;
    private LocalDateTime timeCreated; // Check json compatibility
    private Reactions reactions;

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimeCreated() {
        return timeCreated;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Post(String id, User author, String content, LocalDateTime timestamp,
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