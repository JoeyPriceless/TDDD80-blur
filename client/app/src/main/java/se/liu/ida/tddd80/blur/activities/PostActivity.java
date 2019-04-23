package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.fragments.ReactDialogFragment;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.models.ReactionType;
import se.liu.ida.tddd80.blur.models.Reactions;
import se.liu.ida.tddd80.blur.utilities.GsonUtil;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.StringUtil;
import se.liu.ida.tddd80.blur.utilities.ViewUtil;


public class PostActivity extends AppCompatActivity
        implements ReactDialogFragment.ReactDialogListener {
    private final String TAG = getClass().getSimpleName();
    NetworkUtil netUtil;
    GsonUtil gsonUtil;
	private Post post;

	private Button btnReact;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);
		Intent intent = getIntent();
		String postId = intent.getStringExtra(getResources().getString(R.string.extra_post_id));

		netUtil = NetworkUtil.getInstance(this);
		gsonUtil = GsonUtil.getInstance();
		netUtil.getPostWithExtras(postId, new postExtraResponseListener(), new postExtraErrorListener());
	}

	private class postExtraResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                post = gsonUtil.parsePostWithExtras(response);
                // TODO set image
                ((TextView)findViewById(R.id.textview_post_author)).setText(
                        post.getAuthor().getUsername());
                ((TextView)findViewById(R.id.textview_post_time)).setText(
                        StringUtil.formatDateTimeLong(post.getTimeCreated()));
                ((TextView)findViewById(R.id.textview_post_content)).setText(post.getContent());

                btnReact = findViewById(R.id.button_post_react);
                ViewUtil.updateReactionButton(btnReact, post.getReactions());

            } catch (JsonSyntaxException ex) {
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

    public void onClickReactionButton(View v) {
	    if (!netUtil.isUserLoggedIn()) {
            Toast.makeText(this, "You must be logged in to react",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ReactDialogFragment dialog = new ReactDialogFragment();
        dialog.show(getSupportFragmentManager(), TAG);
    }

    @Override
    public void onClickReactionDialog(ReactDialogFragment dialog) {
        ReactionType type = ReactionType.values()[dialog.getIndex()];
        netUtil.reactToPost(post.getId(), type, new ReactionSuccess(),
                new ReactionError());
    }

    private class ReactionSuccess implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            post.setReactions(gsonUtil.parseReactions(response));
            ViewUtil.updateReactionButton(btnReact, post.getReactions());
        }
    }

    private class ReactionError implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    }
}