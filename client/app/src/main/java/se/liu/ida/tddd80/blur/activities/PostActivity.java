package se.liu.ida.tddd80.blur.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.models.User;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.StringUtil;


public class PostActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
	private Post post;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);
	}

	private class userResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {

        }
    }

    private class userResponseErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    }
}