package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.utilities.GsonUtil;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.StringUtil;


public class PostActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    NetworkUtil netUtil;
	private Post post;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);
		Intent intent = getIntent();
		String postId = intent.getStringExtra(getResources().getString(R.string.extra_post_id));

		netUtil = NetworkUtil.getInstance(this);
		netUtil.getPostWithExtras(postId, new postExtraResponseListener(), new postExtraErrorListener());
	}

	private class postExtraResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                post = GsonUtil.getInstance().PostFromPostWithExtras(response);
                // TODO set image
                ((TextView)findViewById(R.id.textview_post_author)).setText(
                        post.getAuthor().getUsername());
                ((TextView)findViewById(R.id.textview_post_time)).setText(
                        StringUtil.formatDateTimeLong(post.getTimeCreated()));
                ((TextView)findViewById(R.id.textview_post_content)).setText(post.getContent());

            } catch (JSONException | JsonSyntaxException ex) {
                Log.e(TAG, ExceptionUtils.getStackTrace(ex));
                Toast.makeText(PostActivity.this, "Error when parsing response",
                        Toast.LENGTH_LONG).show();
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