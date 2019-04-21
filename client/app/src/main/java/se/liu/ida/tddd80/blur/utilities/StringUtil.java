package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;

import com.android.volley.VolleyError;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import se.liu.ida.tddd80.blur.R;

public class StringUtil {
    /**
     * Returns a string saying how long ago time was with an appropriate time suffix.
     * If time was more than a day ago, the date is used and if it was more than a year ago, the
     * year is added as well.
     * @return String representation of time delta
     */
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

    /**
     * Returns a complete timestamp, using the current time zone.
     */
    public static String formatDateTimeLong(DateTime time) {
        return time.toString("HH:mm - dd MMM yyyy");
    }

    /**
     * Get the string representation of a value in a JSONObject.
     * @param key Key which contains value
     * @param object JSONObject with keys
     * @return the value of key
     */
    public static String parsePlainJsonResponse(String key, JSONObject object)
            throws JSONException {
        return object.getString(key);
    }

    /**
     * Get the string representation of the value under the default key "response" in a JSONObject.
     * @param object JSONObject with keys
     * @return the value of "response"
     */
    public static String parsePlainJsonResponse(JSONObject object) throws JSONException {
        return parsePlainJsonResponse("response", object);
    }

    /**
     * Gets the error message from a VolleyError.
     * @param error Error returned from server
     */
    public static String parsePlainJsonResponse(VolleyError error) {
        try {
            JSONObject body = new JSONObject(new String(error.networkResponse.data,
                    StandardCharsets.UTF_8));
            return parsePlainJsonResponse(body);
        } catch (Exception ex) {
            if (error.getCause() != null)
                return ExceptionUtils.getStackTrace(error.getCause());
            else
                return "Error parsing error response.";
        }
    }

    /**
     * Checks whether a username is valid according to length criteria.
     * @param context Context used to get resources from XML
     * @param username Username to validate
     */
    public static boolean isValidUsername(Context context, String username) {
        int userLength = username.length();
        int minLength = context.getResources().getInteger(R.integer.username_min_length);
        int maxLength = context.getResources().getInteger(R.integer.username_max_length);
        return userLength >= minLength && userLength <= maxLength;
    }

    /**
     * Checks whether email is avlid according to a default android RegEx pattern.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }

    /**
     * Checks whether a password is valid according to length criteria
     * @param context Context used to get resources from XML
     * @param password password to validate
     */
    public static boolean isValidPassword(Context context, String password) {
        int minLength = context.getResources().getInteger(R.integer.password_min_length);
        return password.length() >= minLength;
    }

    /**
     * Returns string representation of Volley's Method enum
     * @param i Ordinal of enum instance
     * @return "GET", "POST", "PUT", "DELETE" or "" if unrecognized integer.
     */
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
