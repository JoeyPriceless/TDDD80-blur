package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import se.liu.ida.tddd80.blur.models.User;

public class NetworkUtil {
    private RequestQueue queue;
    private Gson gson;

    public NetworkUtil(Context context) {
        this.queue = Volley.newRequestQueue(context);
        this.gson = new Gson();
    }

    // For GET, DELETE
    private void requestJson(String url, int method, Listener<String> responseListener,
                             ErrorListener errorListener) {
        if (method == Method.POST || method == Method.PUT) {
            requestJson(url, method, responseListener, errorListener);
            String errorMsg = "GET or DELETE request sent without map";
            Log.w(this.getClass().getSimpleName(), errorMsg);
        }

  		StringRequest stringRequest = new StringRequest(method, url, responseListener,
                errorListener);
  		queue.add(stringRequest);
  	}

  	// POST, PUT need a mapping
  	private void requestJson(String url, int method, Listener<String> responseListener,
                             ErrorListener errorListener, final Map<String, String> params) {
        if (method == Method.GET || method == Method.DELETE) {
            requestJson(url, method, responseListener, errorListener);
            Log.w(this.getClass().getSimpleName(), "GET or DELETE request sent to wrong method.");
        }

        StringRequest stringRequest = new StringRequest(method, url, responseListener,
                errorListener) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        queue.add(stringRequest);
    }

  	public void createUser(User user, String password, Listener<String> responseListener,
                           ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("username", user.getUsername());
        params.put("email", user.getEmail());
        params.put("password", password);

        requestJson(Url.build(Url.CREATE_USER), Method.POST, responseListener,
                    errorListener, params);
    }

    private enum Url {
        ROOT("http://127.0.0.1:5000"),
        CREATE_USER("/user");

        private String adress;

        Url(String adress) {
            this.adress = adress;
        }

        public static String build(Url... urls) {
            StringBuilder adress = new StringBuilder(Url.ROOT.adress);
            for (Url url : urls) {
                adress.append(url.adress);
            }
            return adress.toString();
        }
    }
}
