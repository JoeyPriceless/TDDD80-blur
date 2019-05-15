package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

public class Post {
    private String id;
    private User author;
    private String content;
    @SerializedName("time_created")
    private DateTime timeCreated; // Check json compatibility
    private Reactions reactions;
    @SerializedName("attachment_uri")
    private String attachmentUri;
    private String location;

    public String getAuthorPictureUri() {
        return author.getPictureUri();
    }

    public String getAttachmentUri() {
        return attachmentUri;
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

    public String getLocation() {
        return location;
    }

    public boolean hasReacted() {
        if (reactions == null) return false;
        ReactionType ownReaction = reactions.getOwnReaction();
        return ownReaction != null && ownReaction != ReactionType.NEUTRAL;
    }
}