package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.models.FeedType;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.models.ReactionType;
import se.liu.ida.tddd80.blur.models.User;

import static android.content.SharedPreferences.*;

// Singleton Volley class as recommended in docs.
public class NetworkUtil {
    private final String TAG = getClass().getSimpleName();
    private Context appContext;
    private static NetworkUtil instance;
    private RequestQueue queue;
    private Gson gson;
    private String tokenStringKey;
    private String token;

    public Gson getGson() {
        return gson;
    }

    public void setToken(String token) {
        this.token = token;
        storeToken();
    }

    private NetworkUtil(Context context) {
        // Application context to make it last throughout app lifetime.
        appContext = context.getApplicationContext();
        this.queue = Volley.newRequestQueue(appContext);
        GsonBuilder gb = new GsonBuilder();
        // Register a type adapter to properly parse server datetime format.
        gb.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        this.gson = gb.create();
        tokenStringKey = appContext.getResources().getString(R.string.token_pref_key);
        loadToken();
    }

    public static NetworkUtil getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkUtil(context);
        }
        return instance;
    }

    private void storeToken() {
        SharedPreferences prefs = appContext.getSharedPreferences(appContext.getPackageName(),
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(tokenStringKey, token);
        editor.apply();
    }

    private void loadToken() {
        SharedPreferences prefs = appContext.getSharedPreferences(appContext.getPackageName(),
                Context.MODE_PRIVATE);
        token = prefs.getString(tokenStringKey, null);
    }

    public boolean isTokenValid() {
        // TODO: update logic to deal with expired tokens
        return token != null;
    }

    private Map<String, String> getHeadersAuth() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        // Only send authorization if there is a token.
        if (token != null && token.isEmpty())
            headers.put("Authorization", "Bearer ".concat(token));
        return headers;
    }

    // For methods without body (GET, DELETE)
    private void requestJson(String url, int method, Listener<JSONObject> responseListener,
                             ErrorListener errorListener) {
  		JsonObjectRequest jsonRequest = new JsonObjectRequest(method, url, null, responseListener,
                errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return getHeadersAuth();
            }
        };
  		Log.i(TAG, String.format("%s: %s", StringUtil.MethodToString(method), url));
  		queue.add(jsonRequest);
  	}

  	// For methods with body (POST, PUT)
  	private void requestJson(String url, int method, Listener<JSONObject> responseListener,
                             ErrorListener errorListener, final Map<String, String> data) {
        if (method == Method.GET || method == Method.DELETE) {
            requestJson(url, method, responseListener, errorListener);
            Log.w(this.getClass().getSimpleName(), "GET or DELETE request sent to wrong method.");
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(method, url, new JSONObject(data),
                responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return getHeadersAuth();
            }
        };
        Log.i(TAG, String.format("%s: %s", StringUtil.MethodToString(method), url));
        queue.add(jsonRequest);
    }

    public void login(String email, String password, Listener<JSONObject> responseListener,
                       ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        requestJson(Url.build(Url.USER_LOGIN), Method.POST, responseListener, errorListener, params);
    }

    public void logout(Listener<JSONObject> responseListener, ErrorListener errorListener) {
        requestJson(Url.build(Url.USER_LOGOUT), Method.POST, responseListener, errorListener);
    }

  	public void createUser(String username, String email, String password,
                           Listener<JSONObject> responseListener, ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("email", email);
        params.put("password", password);

        requestJson(Url.build(Url.USER_CREATE), Method.POST, responseListener,
                    errorListener, params);
    }

    public void getUser(String id, Listener<JSONObject> responseListener,
                        ErrorListener errorListener) {
        requestJson(Url.build(Url.USER_GET, id), Method.GET, responseListener, errorListener);
    }

    public void createPost(Post post, Listener<JSONObject> responseListener,
                               ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
            params.put("content", post.getContent());
            params.put("user_id", post.getAuthor().getId());
        requestJson(Url.build(Url.POST_CREATE), Method.POST, responseListener,
                            errorListener, params);
    }

    public void getPost(String id, Listener<JSONObject> responseListener,
                            ErrorListener errorListener) {
        requestJson(Url.build(Url.POST_GET, id), Method.GET, responseListener, errorListener);
    }

    public void getPostWithExtras(String id, Listener<JSONObject> responseListener,
                                ErrorListener errorListener) {
        requestJson(Url.build(Url.POST_GET_EXTRAS, id), Method.GET, responseListener, errorListener);
    }

    public void getReactions(String postId, Listener<JSONObject> responseListener,
                                ErrorListener errorListener) {
        requestJson(Url.build(Url.POST_REACTIONS_GET, postId), Method.GET, responseListener,
                errorListener);
    }

    public void reactToPost(Post post, ReactionType reaction, Listener<JSONObject> responseListener,
                                    ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
                    params.put("post_id", post.getId());
                    params.put("user_id", post.getAuthor().getId());
                    params.put("reaction", String.valueOf(reaction.ordinal()));
        requestJson(Url.build(Url.POST_REACTIONS_ADD), Method.POST, responseListener,
                                    errorListener, params);
    }

    public void getFeed(FeedType type, Listener<JSONObject> responseListener, ErrorListener
            errorListener) {
        requestJson(Url.build(Url.FEED_GET, type.toString().toLowerCase()), Method.GET, responseListener, errorListener);
    }

    private enum Url {
        ROOT("https://tddd80-server.herokuapp.com"),
        USER_LOGIN("/user/login"),
        USER_LOGOUT("/user/logout"),
        USER_CREATE("/user"),
        USER_GET("/user/"),
        POST_CREATE("/post"),
        POST_GET("/post/"),
        POST_GET_EXTRAS("/post/extras/"),
        POST_REACTIONS_ADD("/post/reactions"),
        POST_REACTIONS_GET("/post/reactions/"),
        FEED_GET("/feed/");

        private String address;

        Url(String address) {
            this.address = address;
        }



        public static String build(Object... elements) {
            StringBuilder address = new StringBuilder(Url.ROOT.address);
            for (Object o : elements) {
                address.append(o.toString());
            }
            return address.toString();
        }

        @Override
        public String toString() {
            return address;
        }
    }
}
