package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.models.User;
import se.liu.ida.tddd80.blur.utilities.DateTimeDeserializer;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.StringUtil;


public class PostActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    NetworkUtil netUtil;
    Gson gson;
	private Post post;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);
		Intent intent = getIntent();
		//String postId = intent.getStringExtra(getResources().getString(R.string.extra_post_id));
        String postId = "87370e447bbc4765a3a3f334379bbcba";

		netUtil = NetworkUtil.getInstance(this);
		netUtil.getPostWithExtras(postId, new postExtraResponseListener(), new postExtraErrorListener());
	}

	private class postExtraResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
                gson = gsonBuilder.create();

                JSONObject postObj = response.getJSONObject("post");
                post = gson.fromJson(postObj.toString(), Post.class);

                JSONObject authorObj = response.getJSONObject("author");
                User author = gson.fromJson(authorObj.toString(), User.class);
                post.setAuthor(author);

                // TODO set image
                ((TextView)findViewById(R.id.textview_post_author)).setText(author.getUsername());
                ((TextView)findViewById(R.id.textview_post_time)).setText(
                        StringUtil.formatDateTimeLong(post.getTimeCreated()));
                ((TextView)findViewById(R.id.textview_post_content)).setText(post.getContent());

            } catch (JSONException | JsonSyntaxException ex) {
                Log.e(TAG, ExceptionUtils.getStackTrace(ex));
                Toast.makeText(PostActivity.this, "Could not parse response",
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