package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import se.liu.ida.tddd80.blur.models.Feed;
import se.liu.ida.tddd80.blur.models.FeedType;
import se.liu.ida.tddd80.blur.models.User;

// Singleton Volley class as recommended in docs.
public class NetworkUtil {
    private static NetworkUtil instance;
    private RequestQueue queue;
    private Gson gson;

    private NetworkUtil(Context context) {
        // Application context to make it last throughout app lifetime.
        this.queue = Volley.newRequestQueue(context.getApplicationContext());
        this.gson = new Gson();
    }

    public static NetworkUtil getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkUtil(context);
        }
        return instance;
    }

    // For GET, DELETE
    private void requestJson(String url, int method, Listener<JSONObject> responseListener,
                             ErrorListener errorListener) {
        if (method == Method.POST || method == Method.PUT) {
            requestJson(url, method, responseListener, errorListener);
            String errorMsg = "GET or DELETE request sent without map";
            Log.w(this.getClass().getSimpleName(), errorMsg);
        }

  		JsonObjectRequest jsonRequest = new JsonObjectRequest(method, url, null, responseListener,
                errorListener);
  		queue.add(jsonRequest);
  	}

  	// POST, PUT need a mapping
  	private void requestJson(String url, int method, Listener<JSONObject> responseListener,
                             ErrorListener errorListener, final Map<String, String> data) {
        if (method == Method.GET || method == Method.DELETE) {
            requestJson(url, method, responseListener, errorListener);
            Log.w(this.getClass().getSimpleName(), "GET or DELETE request sent to wrong method.");
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(method, url, new JSONObject(data),
                responseListener, errorListener);
        queue.add(jsonRequest);
    }

  	public void createUser(User user, String password, Listener<JSONObject> responseListener,
                           ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("username", user.getUsername());
        params.put("email", user.getEmail());
        params.put("password", password);

        requestJson(Url.build(Url.CREATE_USER), Method.POST, responseListener,
                    errorListener, params);
    }

    public Feed getFeed(FeedType type, Listener<String> responseListener, ErrorListener
            errorListener) {
        // TODO fetch from server
        return new Feed(type);
    }

    private enum Url {
        ROOT("https://tddd80-server.herokuapp.com"),
        CREATE_USER("/user");

        private String address;

        Url(String address) {
            this.address = address;
        }

        public static String build(Url... urls) {
            StringBuilder address = new StringBuilder(Url.ROOT.address);
            for (Url url : urls) {
                address.append(url.address);
            }
            return address.toString();
        }
    }
}
