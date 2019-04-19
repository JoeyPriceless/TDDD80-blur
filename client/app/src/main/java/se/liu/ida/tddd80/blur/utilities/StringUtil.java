package se.liu.ida.tddd80.blur.utilities;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;

public class StringUtil {
    public static String formatDateTimeShort(DateTime time) {
        Duration diff = new Duration(time, DateTime.now());
        long duration;
        String unit;
        if (diff.isShorterThan(Duration.standardHours(1))) {
            duration = diff.getStandardMinutes();
            unit = "m";
        } else if (diff.isShorterThan(Duration.standardDays(1))) {
            duration = diff.getStandardHours();
            unit = "h";
        } else if (diff.isShorterThan(Duration.standardDays(365))) {
            return time.toString("dd MMM");
        } else {
            return time.toString("dd MMM yyyy");
        }
        return String.format("%d%s ago", duration, unit);
    }

    public static String formatDateTimeLong(DateTime time) {
        return time.toString("HH:mm - dd MMM yyyy");
    }

    public static String parsePlainJsonResponse(String key, JSONObject object) throws JSONException {
            return object.getString(key);
    }

    public static String parsePlainJsonResponse(JSONObject object) throws JSONException {
        return parsePlainJsonResponse("response", object);
    }
}
