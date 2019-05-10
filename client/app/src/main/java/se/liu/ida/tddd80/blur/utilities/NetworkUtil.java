package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.models.FeedType;
import se.liu.ida.tddd80.blur.models.ReactionType;

import static android.content.SharedPreferences.Editor;

/**
 * Singleton utility class that handles requests to the server. Also keeps track of user
 * authentication token.
 */
public class NetworkUtil {
    private final String TAG = getClass().getSimpleName();
    private Context appContext;
    private static NetworkUtil instance;
    private RequestQueue queue;
    private ImageLoader imageLoader;
    private String userId;
    private String userIdStringKey;
    private String tokenStringKey;
    private String token;

    public void login(String token, String userId) {
        this.token = token;
        this.userId = userId;
        storeLogin();
    }

    public void logout() {
        token = null;
        userId = null;
        storeLogin();
    }

    public String getUserId() {
        return userId;
    }

    private NetworkUtil(Context context) {
        // Application context to make it last throughout app lifetime.
        appContext = context.getApplicationContext();
        this.queue = Volley.newRequestQueue(appContext);
        this.imageLoader = new ImageLoader(queue, new ImageLoader.ImageCache() {
            @Override
            public Bitmap getBitmap(String url) {
                return null;
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {

            }
        });
        tokenStringKey = appContext.getResources().getString(R.string.token_pref_key);
        userIdStringKey = appContext.getResources().getString(R.string.userid_pref_key);
        loadLogin();
    }

    public static NetworkUtil getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkUtil(context);
        }
        return instance;
    }

    private void storeLogin() {
        SharedPreferences prefs = appContext.getSharedPreferences(appContext.getPackageName(),
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(tokenStringKey, token);
        editor.putString(userIdStringKey, userId);
        editor.apply();
    }

    private void loadLogin() {
        SharedPreferences prefs = appContext.getSharedPreferences(appContext.getPackageName(),
                Context.MODE_PRIVATE);
        token = prefs.getString(tokenStringKey, null);
        userId = prefs.getString(userIdStringKey, null);
    }

    public boolean isUserLoggedIn() {
        // TODO: update logic to deal with expired tokens
        return token != null;
    }

    /**
     * Adds default headers onto HTTP request.
     */
    private Map<String, String> getHeadersAuth(String contentType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        // Only send authorization if there is a token.
        if (token != null && !token.isEmpty())
            headers.put("Authorization", "Bearer ".concat(token));
        return headers;
    }

    /**
     * Request JSON response from url using method (Usually GET or DELETE).
     * @param url target URL. Complete URL including prefix and hostname
     * @param method HTTP method from enum
     * @param responseListener Listener which contains actions upon success
     * @param errorListener Listener which contains actions upon failure
     */
    private void requestJsonObject(String url, int method, Listener<JSONObject> responseListener,
                                   ErrorListener errorListener) {
  		JsonObjectRequest request = new JsonObjectRequest(method, url, null,
                responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() { return getHeadersAuth("application/json"); }
        };
  		addToQueue(request);
  	}

    /**
     * Sends a payload data and requests JSON response from url using method (Usually POST or PUT).
     * @param url target URL. Complete URL including prefix and hostname
     * @param method HTTP method from enum
     * @param responseListener Listener which contains actions upon success
     * @param errorListener Listener which contains actions upon failure
     * @param data JSON data mapped in key/value format
     */
  	private void requestJsonObject(String url, int method,
                                   Listener<JSONObject> responseListener,
                                   ErrorListener errorListener,
                                   final Map<String, String> data) {
        JsonObjectRequest request = new JsonObjectRequest(method, url, new JSONObject(data),
                responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return getHeadersAuth("application/json");
            }
        };
        addToQueue(request);
    }

    /**
     * Sends a payload data and requests JSON response from url using method (Usually POST or PUT).
     * @param url target URL. Complete URL including prefix and hostname
     * @param method HTTP method from enum
     * @param responseListener Listener which contains actions upon success
     * @param errorListener Listener which contains actions upon failure
     * @param data JSON data mapped in key/value format
     */
    private void requestJsonObject(String url,
                                   Listener<JSONObject> responseListener,
                                   ErrorListener errorListener,
                                   final Map<String, String> params) {
        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url, null,
                responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return getHeadersAuth("multipart/form-data");
            }
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        addToQueue(request);
    }

    /**
     * Request JSON response from url using method (Usually GET or DELETE).
     * @param url target URL. Complete URL including prefix and hostname
     * @param method HTTP method from enum
     * @param responseListener Listener which contains actions upon success
     * @param errorListener Listener which contains actions upon failure
     */
    private void requestJsonArray(String url, int method, Listener<JSONArray> responseListener,
                                   ErrorListener errorListener) {
        JsonArrayRequest request = new JsonArrayRequest(method, url, null,
                responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() { return getHeadersAuth("application/json"); }
        };
        addToQueue(request);
    }

    private void addToQueue(Request request) {
        Log.i(TAG, String.format("%s: %s",
                StringUtil.MethodToString(request.getMethod()), request.getUrl()));
        queue.add(request);
    }


    /**
     * Requests an authorization token for the given credentials. responseListener should call
     * NetworkUtil.login()
     */
    public void login(String email, String password, Listener<JSONObject> responseListener,
                       ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        requestJsonObject(Url.build(Url.USER_LOGIN), Method.POST, responseListener, errorListener,
                params);
    }

    /**
     * Log out of the server, blacklisting the authorization token. responseListener should call
     * NetworkUtil.logout();
     */
    public void logout(Listener<JSONObject> responseListener, ErrorListener errorListener) {
        requestJsonObject(Url.build(Url.USER_LOGOUT), Method.POST, responseListener, errorListener);
    }

  	public void createUser(String username, String email, String password,
                           Listener<JSONObject> responseListener, ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("email", email);
        params.put("password", password);

        requestJsonObject(Url.build(Url.USER_CREATE), Method.POST, responseListener,
                    errorListener, params);
    }

    public void getUser(String id, Listener<JSONObject> responseListener,
                        ErrorListener errorListener) {
        requestJsonObject(Url.build(Url.USER_GET, id), Method.GET, responseListener, errorListener);
    }

    public void createPost(String content, String authorId, String location,
                           Listener<JSONObject> responseListener, ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
            params.put("content", content);
            params.put("user_id", authorId);
            params.put("location", location);
        requestJsonObject(Url.build(Url.POST_CREATE), Method.POST, responseListener,
                            errorListener, params);
    }

    /**
     * Request a post of a given id, along with extras such as it's author, reactions and comments.
     */
    public void getPost(String id, Listener<JSONObject> responseListener,
                        ErrorListener errorListener) {
        requestJsonObject(Url.build(Url.POST_GET, id), Method.GET, responseListener,
                errorListener);
    }

    public void getReactions(String postId, Listener<JSONObject> responseListener,
                                ErrorListener errorListener) {
        requestJsonObject(Url.build(Url.POST_REACTIONS_GET, postId), Method.GET, responseListener,
                errorListener);
    }

    public void reactToPost(String postId, ReactionType reaction,
                            Listener<JSONObject> responseListener, ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
                    params.put("post_id", postId);
                    params.put("reaction", String.valueOf(reaction.ordinal()));
        requestJsonObject(Url.build(Url.POST_REACTIONS_ADD), Method.POST, responseListener,
                                    errorListener, params);
    }

    public void getFeed(FeedType type, Listener<JSONObject> responseListener, ErrorListener
            errorListener) {
        requestJsonObject(Url.build(Url.FEED_GET, type.toString().toLowerCase()), Method.GET,
                responseListener, errorListener);
    }

    public static String getPostAttachmentUrl(String postId) {
        return Url.build(Url.POST_ATTACHMENT_GET, postId);
    }

    public static String getUserPictureUrl(String userId) {
        return Url.build(Url.USER_PICTURE_GET, userId);
    }

    public String getUserPictureUrl() {
        return Url.build(Url.USER_PICTURE_GET, userId);
    }

    public void sendPostAttachment(Bitmap bitmap, String postId,
                                   Listener<JSONObject> responseListener,
                                   ErrorListener errorListener) {
        String encoded = FileUtil.encodeImageFile(bitmap);
        Map<String, String> params = new HashMap<>();
        params.put("file", encoded);

        JsonObjectRequest request = new JsonObjectRequest(Method.POST,
                Url.build(Url.POST_ATTACHMENT_GET, postId),
                new JSONObject(params), responseListener, errorListener);
        addToQueue(request);
    }

    /**
     * Contains endpoints for server requests which can be used to build URLs.
     */
    private enum Url {
        FEED_GET("/feed/"),
        POST_ATTACHMENT_GET("/post/attachment/"),
        POST_CREATE("/post"),
        POST_GET("/post/"),
        POST_REACTIONS_ADD("/post/reactions"),
        POST_REACTIONS_GET("/post/reactions/"),
        ROOT("https://tddd80-server.herokuapp.com"),
        USER_CREATE("/user"),
        USER_GET("/user/"),
        USER_LOGIN("/user/login"),
        USER_LOGOUT("/user/logout"),
        USER_PICTURE_GET("/user/picture/");

        private String address;

        Url(String address) {
            this.address = address;
        }


        /**
         * Concatinates elements of different types to build a URL
         * @param elements Objects to be concatinated. Uses their toString representation.
         * @return build URL.
         */
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