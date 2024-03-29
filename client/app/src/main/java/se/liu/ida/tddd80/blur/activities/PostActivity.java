package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.fragments.CommentFragment;
import se.liu.ida.tddd80.blur.fragments.ReactDialogFragment;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.models.ReactionType;
import se.liu.ida.tddd80.blur.utilities.GsonUtil;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.ResponseListeners;
import se.liu.ida.tddd80.blur.utilities.StringUtil;
import se.liu.ida.tddd80.blur.utilities.ViewUtil;


public class PostActivity extends AppCompatActivity
        implements ReactDialogFragment.ReactDialogListener {
    private final String TAG = getClass().getSimpleName();
    public static final String EXTRA_POST_ID = "POST_ID";
    NetworkUtil netUtil;
    GsonUtil gsonUtil;
	private Post post;

	private Button btnReact;
	private Button btnComment;
	private ImageView ivAuthor;
    private TextView tvLocation;
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
        btnComment = findViewById(R.id.button_post_comment);
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent submitCommentIntent = new Intent(PostActivity.this, SubmitCommentActivity.class);
                submitCommentIntent.putExtra("postId", post.getId());
                startActivity(submitCommentIntent);
            }
        });
	}

	private void initializeComments() {
        CommentFragment commentFragment = CommentFragment.newInstance(post.getId());
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_commentsplaceholder, commentFragment);
        transaction.commit();
    }

	private class GetPostResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                post = gsonUtil.parsePost(response);
                tvAuthor.setText(StringUtil.padString(post.getAuthor().getUsername()));
                ((TextView)findViewById(R.id.textview_post_time)).setText(
                        StringUtil.formatDateTimeLong(post.getTimeCreated()));

                tvLocation = findViewById(R.id.textview_post_location);
                String location = post.getLocation();
                if (location != null) {

                    tvLocation.setText(StringUtil.padString(location));
                    tvLocation.setVisibility(View.VISIBLE);
                } else {
                    tvLocation.setText("");
                    tvLocation.setVisibility(View.GONE);
                }

                ((TextView)findViewById(R.id.textview_post_content)).setText(post.getContent());

                btnReact = findViewById(R.id.button_post_react);
                ImageView attachment = findViewById(R.id.imageview_post_attachment);
                Picasso.get().load(post.getAttachmentUri())
                        .noFade()
                        .transform(new RoundedCornersTransformation(20, 0))
                        .into(attachment);
                ViewUtil.refreshPostViews(btnReact, post, tvAuthor, tvLocation, ivAuthor);
                btnComment.setText(String.valueOf(post.getCommentCount()));
                initializeComments();
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
                new ResponseListeners.ReactionSuccess(post, btnReact, tvAuthor, tvLocation, ivAuthor),
                new ResponseListeners.DefaultError(this));
    }
}