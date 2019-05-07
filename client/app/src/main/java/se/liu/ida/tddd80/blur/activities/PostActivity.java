package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.fragments.ReactDialogFragment;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.models.ReactionType;
import se.liu.ida.tddd80.blur.utilities.GsonUtil;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.ResponseListeners;
import se.liu.ida.tddd80.blur.utilities.StringUtil;
import se.liu.ida.tddd80.blur.utilities.UserImageView;
import se.liu.ida.tddd80.blur.utilities.ViewUtil;


public class PostActivity extends AppCompatActivity
        implements ReactDialogFragment.ReactDialogListener {
    private final String TAG = getClass().getSimpleName();
    public static final String EXTRA_POST_ID = "POST_ID";
    public static final Character AUTHOR_SPACE_PADDING = ' ';
    NetworkUtil netUtil;
    GsonUtil gsonUtil;
	private Post post;

	private Button btnReact;
	private Button btnComment;
	private UserImageView ivAuthor;
	private TextView tvAuthor;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);
		Intent intent = getIntent();
		String postId = intent.getStringExtra(EXTRA_POST_ID);

		netUtil = NetworkUtil.getInstance(this);
		gsonUtil = GsonUtil.getInstance();
        netUtil.getPost(postId, new GetPostResponseListener(),
                new ResponseListeners.DefaultError(this));
		tvAuthor = findViewById(R.id.textview_post_author);
		ivAuthor = findViewById(R.id.imageview_post_author);


	}

	private class GetPostResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                post = gsonUtil.parsePost(response);
                // If there isn't some horizontal padding around the text, the blur ends with a
                // very noticeable edge rather than fading out. The space is there to provide
                // padding. I tried adding a layout padding but the filter still used the text's
                // position rather than it's background.
                tvAuthor.setText(AUTHOR_SPACE_PADDING +
                        post.getAuthor().getUsername());
                ((TextView)findViewById(R.id.textview_post_time)).setText(
                        StringUtil.formatDateTimeLong(post.getTimeCreated()));
                ((TextView)findViewById(R.id.textview_post_content)).setText(post.getContent());

                btnReact = findViewById(R.id.button_post_react);
                ViewUtil.refreshPostViews(btnReact, post, tvAuthor, ivAuthor);
                btnComment = findViewById(R.id.button_post_comment);
                // TODO
                btnComment.setText("1024");
            } catch (JsonSyntaxException ex) {
                Log.e(TAG, ExceptionUtils.getStackTrace(ex));
                Toast.makeText(PostActivity.this, "Error when parsing response",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onClickReactionButton(View v) {
	    ViewUtil.showReactionDialog(this, getSupportFragmentManager(), post.getId(),
                post.getReactions().getOwnReaction());
    }

    @Override
    public void onClickReactionDialog(ReactDialogFragment dialog) {
        ReactionType type = ReactionType.values()[dialog.getIndex()];
        netUtil.reactToPost(post.getId(), type,
                new ResponseListeners.ReactionSuccess(post, btnReact, tvAuthor, ivAuthor),
                new ResponseListeners.DefaultError(this));
    }
}