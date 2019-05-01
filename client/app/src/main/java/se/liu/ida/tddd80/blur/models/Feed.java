package se.liu.ida.tddd80.blur.models;

import java.util.List;

public class Feed {
    private FeedType type;
    private List<Post> posts;

    public FeedType getType() {
        return type;
    }

    public Feed(FeedType type) {
        this.type = type;
    }

    public Post get(int i) {
        return posts.get(i);
    }

    public int size() {
        return posts.size();
    }
}
