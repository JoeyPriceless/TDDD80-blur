package se.liu.ida.tddd80.blur.models;

import java.time.LocalDateTime;

public class Post {
    private String id;
    private String author;
    private String content;
    private LocalDateTime timestamp; // Check json compatibility
    private Reactions reactions;
    // Should comments be stored here?


    public Post(String id, String author, String content, LocalDateTime timestamp,
                Reactions reactions) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
        this.reactions = reactions;
    }
}
