package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;

import com.android.volley.VolleyError;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import se.liu.ida.tddd80.blur.R;

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

    public static String parsePlainJsonResponse(VolleyError error) throws JSONException {
        JSONObject body = new JSONObject(new String(error.networkResponse.data, StandardCharsets.UTF_8));
        return parsePlainJsonResponse(body);
    }

    public static boolean isValidUsername(Context context, String username) {
        int userLength = username.length();
        int minLength = context.getResources().getInteger(R.integer.username_min_length);
        int maxLength = context.getResources().getInteger(R.integer.username_max_length);
        return userLength >= minLength && userLength <= maxLength;
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }

    public static boolean isValidPassword(Context context, String password) {
        int minLength = context.getResources().getInteger(R.integer.password_min_length);
        return password.length() >= minLength;
    }

    public static String MethodToString(int i) {
        switch(i) {
            case 0: return "GET";
            case 1: return "POST";
            case 2: return "PUT";
            case 3: return "DELETE";
            default: return "";
        }
    }
}
