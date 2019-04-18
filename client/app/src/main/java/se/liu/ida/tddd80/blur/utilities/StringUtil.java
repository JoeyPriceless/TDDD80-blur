package se.liu.ida.tddd80.blur.utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;

public class StringUtil {
    public static String timeSinceCreation(LocalDateTime time) {
        // TODO
        return time.toString();
    }

    public static String parsePlainJsonResponse(JSONObject object) throws JSONException {
        return object.getString("response");
    }
}
