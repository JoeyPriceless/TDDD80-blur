package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.os.Bundle;

import com.android.volley.Response;

import org.json.JSONObject;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.utilities.ResponseListeners;

public class SubmitCommentActivity extends SubmitActivity implements Response.Listener<JSONObject>{

    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postId = getIntent().getExtras().getString("postId");
    }

    @Override
    void setView() {
        setContentView(R.layout.activity_submit_comment);
    }

    protected void continueToPost() {
        Intent postIntent = new Intent(SubmitCommentActivity.this, PostActivity.class);
        postIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        postIntent.putExtra(PostActivity.EXTRA_POST_ID, postId);
        startActivity(postIntent);
        finish();
    }

    @Override
    protected boolean submit() {
        if (!super.checkTextLength()) {
            return false;
        }
        netUtil.createComment(contentEditable.toString(), netUtil.getUserId(), getIntent().getStringExtra("postId"),
                this, new ResponseListeners.DefaultError(this));
        return true;
    }

    @Override
    public void onResponse(JSONObject response) {
        continueToPost();
    }
}
