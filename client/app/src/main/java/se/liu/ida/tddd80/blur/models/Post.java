package se.liu.ida.tddd80.blur.models;

import java.time.LocalDateTime;

public class Post {
    private String id;
    private String author;
    private String content;
    private LocalDateTime timeCreated; // Check json compatibility
    private Reactions reactions;

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimeCreated() {
        return timeCreated;
    }

    public Post(String id, String author, String content, LocalDateTime timestamp,
                Reactions reactions) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.timeCreated = timestamp;
        this.reactions = reactions;
    }
}