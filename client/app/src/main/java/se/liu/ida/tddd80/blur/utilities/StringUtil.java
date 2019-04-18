package se.liu.ida.tddd80.blur.utilities;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;

public class StringUtil {
    public static String timeSinceCreation(DateTime time) {
        // TODO
        return time.toString();
    }
    public static String parsePlainJsonResponse(String key, JSONObject object) throws JSONException {
            return object.getString(key);
    }

    public static String parsePlainJsonResponse(JSONObject object) throws JSONException {
        return parsePlainJsonResponse("response", object);
    }
}
