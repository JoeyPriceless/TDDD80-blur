package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.models.User;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;


public class PostActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    NetworkUtil netUtil;
    Gson gson;
	private Post post;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);
		Intent intent = getIntent();
		String postId = intent.getStringExtra(getResources().getString(R.string.extra_post_id));

		netUtil = NetworkUtil.getInstance(this);
        gson = netUtil.getGson();
		netUtil.getPostWithExtras(postId, new postExtraResponseListener(), new postExtraErrorListener());
	}

	private class postExtraResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                JSONObject postObj = response.getJSONObject("post");
                post = gson.fromJson(postObj.toString(), Post.class);

                JSONObject authorObj = response.getJSONObject("author");
                post.setAuthor(gson.fromJson(authorObj.toString(), User.class));
            } catch (JSONException ex) {
                Log.e(TAG, ExceptionUtils.getStackTrace(ex));
                Toast.makeText(PostActivity.this, "Could not parse response",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class postExtraErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.toString());
            Toast.makeText(PostActivity.this, "Failed to receive response.",
                Toast.LENGTH_SHORT).show();
        }
    }
}