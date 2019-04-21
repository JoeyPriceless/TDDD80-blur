package se.liu.ida.tddd80.blur.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.json.JSONObject;

import se.liu.ida.tddd80.blur.models.Feed;
import se.liu.ida.tddd80.blur.models.Post;

/**
 * Singleton class which contains a Gson instance and methods to deserialize JSONObjects into java
 * objects.
 */
public class GsonUtil {
    private static GsonUtil instance;
    private Gson gson;

    public static GsonUtil getInstance() {
        if (instance == null)
            instance = new GsonUtil();
        return instance;
    }

    private GsonUtil() {
        GsonBuilder gb = new GsonBuilder();
        // Register a type adapter to properly parse server datetime format.
        gb.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gson = gb.create();
    }

    /**
     * Returns a Post from a successful getPostWithExtras request.
     */
    public Post PostFromPostWithExtras(JSONObject object) {
        return gson.fromJson(object.toString(), Post.class);
    }

    public Feed FeedFromJson(JSONObject object) {
        return gson.fromJson(object.toString(), Feed.class);
    }
}
