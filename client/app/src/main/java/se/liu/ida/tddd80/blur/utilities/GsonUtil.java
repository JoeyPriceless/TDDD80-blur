package se.liu.ida.tddd80.blur.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import se.liu.ida.tddd80.blur.models.Feed;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.models.Reactions;
import se.liu.ida.tddd80.blur.models.User;

/**
 * Singleton class which contains a Gson instance and methods to deserialize JSONObjects into java
 * objects.
 */
public class GsonUtil {
    private static GsonUtil instance;
    private Gson gson;

    public Gson getGson() {
        return gson;
    }

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
     * Returns a Post from a successful getPost request.
     */
    public Post parsePost(JSONObject object) {
        return gson.fromJson(object.toString(), Post.class);
    }

    public User parseUser(JSONObject object) {
        return gson.fromJson(object.toString(), User.class);
    }

    public Feed parseFeed(JSONObject object) {
        return gson.fromJson(object.toString(), Feed.class);
    }

    public Reactions parseReactions(JSONObject object) {
        return gson.fromJson(object.toString(), Reactions.class);
    }

    public String parseString(JSONObject object) {
        try {
            return object.getString("response");
        } catch (JSONException ex) {
            return "";
        }
    }

    public int parseInt(JSONObject object) {
        try {
            return object.getInt("response");
        } catch (JSONException ex) {
            return 0;
        }
    }
}
