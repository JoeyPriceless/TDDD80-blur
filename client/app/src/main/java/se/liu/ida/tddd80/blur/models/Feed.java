package se.liu.ida.tddd80.blur.models;

import java.util.ArrayList;

public class Feed extends ArrayList<Post> {
    private FeedType type;

    public FeedType getType() {
        return type;
    }

    public Feed(FeedType type) {
        this.type = type;
    }

    public Feed(FeedType type, String json) {
        this.type = type;
        fromJson(json);
    }

    public static Feed fromJson(String json) {
        // TODO serialize json
        return new Feed(FeedType.HOT);
    }
}
