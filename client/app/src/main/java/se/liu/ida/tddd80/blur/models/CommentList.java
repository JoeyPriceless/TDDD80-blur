package se.liu.ida.tddd80.blur.models;

import java.util.ArrayList;
import java.util.List;

/**
 * A container class for easy converting a list of comments back and forth to JSON with GSON.
 */
public class CommentList {
    private List<Comment> comments;

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public Comment get(int i) { return comments.get(i); }
}
