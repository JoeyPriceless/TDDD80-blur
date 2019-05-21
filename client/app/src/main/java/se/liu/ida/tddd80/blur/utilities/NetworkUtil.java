package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.models.FeedType;
import se.liu.ida.tddd80.blur.models.ReactionType;
import se.liu.ida.tddd80.blur.models.User;

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
    private User user;
    private String userIdStringKey;
    private String tokenStringKey;
    private String token;

    public void login(String token, String userId) {
        this.token = token;
        fetchCurrentUser(userId);
        storeLogin(userId);
    }

    public void logout() {
        token = null;
        user = null;
        storeLogin(null);
    }

    public String getUserId() {
        if (user == null)
            return null;
        else
            return user.getId();
    }

    private void fetchCurrentUser(String userId) {
        // Request user object from ID
        getUser(userId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                user = GsonUtil.getInstance().parseUser(response);
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                user = null;
            }
        });
    }

    private NetworkUtil(Context context) {
        // Application context to make it last throughout app lifetime.
        appContext = context.getApplicationContext();
        this.queue = Volley.newRequestQueue(appContext);
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

    private void storeLogin(String userId) {
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
        String userId = prefs.getString(userIdStringKey, null);
        if (token != null && userId != null) {
            fetchCurrentUser(userId);
        }
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
     * @param responseListener Listener which contains actions upon success
     * @param errorListener Listener which contains actions upon failure
     * @param params JSON data mapped in key/value format
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

    @SuppressWarnings("unchecked")
    public void addToQueue(Request request) {
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

    public void setUserPictureUrl() {

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

    public void reactToComment(String commentId, int reaction,
                               Listener<JSONObject> responseListener, ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        if (reaction < 1)
            reaction = 1;
        params.put("comment_id", commentId);
        params.put("reaction", String.valueOf(reaction));
        requestJsonObject(Url.build(Url.COMMENT_REACT), Method.POST, responseListener,
                errorListener, params);
    }

    public void getFeed(FeedType type, Listener<JSONObject> responseListener, ErrorListener
            errorListener) {
        requestJsonObject(Url.build(Url.FEED_GET, type.getTypeString()), Method.GET,
                responseListener, errorListener);
    }

    public void getComments(String postID, Listener<JSONObject> responseListener, ErrorListener
            errorListener) {
        requestJsonObject(Url.build(Url.POST_COMMENTS_GET, postID), Method.GET,
                responseListener, errorListener);
    }

    public void createComment(String content, String authorId, String postId,
                           Listener<JSONObject> responseListener, ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("post_id", postId);
        params.put("content", content);
        params.put("parent", null);
        params.put("user_id", authorId);
        requestJsonObject(Url.build(Url.POST_COMMENT_ADD), Method.POST, responseListener,
                errorListener, params);
    }

    public static String getPostAttachmentUrl(String postId) {
        return Url.build(Url.POST_ATTACHMENT, postId);
    }

    public static String getUserPictureUri(String userId) {
        return Url.build(Url.USER_PICTURE, userId);
    }

    public String getUserPictureUri() {
        return user == null ? null : user.getPictureUri();
    }

    public void sendPostAttachment(String postId, Bitmap bitmap, String filepath,
                                   Listener<String> responseListener,
                                   ErrorListener errorListener) {
        String url = Url.build(Url.POST_ATTACHMENT, postId);
        new SendImageTask(url, bitmap, filepath, "image/jpeg", responseListener, errorListener)
                .execute();
    }

    public void sendProfilePicture(Bitmap bitmap, String filepath, Listener<String> responseListener,
                                   ErrorListener errorListener) {
        String url = Url.build(Url.USER_PICTURE, getUserId());
        new SendImageTask(url, bitmap, filepath, "image/jpeg", responseListener, errorListener)
                .execute();
    }

    public static class SendImageTask extends AsyncTask<Void, Void, Void> {
        private String urlString;
        private Bitmap image;
        private String filepath;
        private String contentType;
        private Response.Listener<String> responseListener;
        private ErrorListener errorListener;

        public SendImageTask(String urlString, Bitmap image, String filepath, String contentType,
                             Response.Listener<String> responseListener,
                             ErrorListener errorListener) {
            this.urlString = urlString;
            this.image = image;
            this.filepath = filepath;
            this.contentType = contentType;
            this.responseListener = responseListener;
            this.errorListener = errorListener;
        }

        @Override
        protected Void doInBackground(Void... Voids) {
            HttpURLConnection connection = null;
            DataOutputStream outputStream = null;
            InputStream inputStream = null;

            String twoHyphens = "--";
            String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
            String lineEnd = "\r\n";

            String result = "";

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            String[] q = filepath.split("/");
            int idx = q.length - 1;

            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                image.compress(Bitmap.CompressFormat.JPEG, 70, bos);
                inputStream = new ByteArrayInputStream(bos.toByteArray());

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();

                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + q[idx] + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: " + contentType + lineEnd);
                outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

                outputStream.writeBytes(lineEnd);

                bytesAvailable = inputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = inputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = inputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = inputStream.read(buffer, 0, bufferSize);
                }

                outputStream.writeBytes(lineEnd);

                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                inputStream = connection.getInputStream();

                result = FileUtil.convertStreamToString(inputStream);

                inputStream.close();
                outputStream.flush();
                outputStream.close();

                responseListener.onResponse(result);
            } catch (Exception e) {
                errorListener.onErrorResponse(new VolleyError(e));
            }
            return null;
        }
    }

    /**
     * Contains endpoints for server requests which can be used to build URLs.
     */
    private enum Url {
        FEED_GET("/feed/"),
        POST_ATTACHMENT("/post/attachment/"),
        POST_CREATE("/post"),
        POST_GET("/post/"),
        POST_REACTIONS_ADD("/post/reactions"),
        POST_REACTIONS_GET("/post/reactions/"),
        POST_COMMENTS_GET("/comments/"),
        POST_COMMENT_ADD("/comment"),
        COMMENT_REACT("/comment/reactions"),
        ROOT("https://tddd80-server.herokuapp.com"),
        USER_CREATE("/user"),
        USER_GET("/user/"),
        USER_LOGIN("/user/login"),
        USER_LOGOUT("/user/logout"),
        USER_PICTURE("/user/picture/");

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
                if (o != null)
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