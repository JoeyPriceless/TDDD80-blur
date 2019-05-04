package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONObject;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.utilities.GsonUtil;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.ResponseListeners;

public class SubmitPostActivity extends AppCompatActivity {
    private NetworkUtil netUtil;
    private EditText etContent;
    private Editable contentEditable;
    private TextView tvCharCount;
    private int maxLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_post);

        setupActionBar();

        netUtil = NetworkUtil.getInstance(this);
        maxLength = getResources().getInteger(R.integer.post_max_length);

        etContent = findViewById(R.id.edittext_content_submit);
        etContent.addTextChangedListener(new ContentWatcher());
        // Set content box as focused automatically.
        etContent.requestFocus();
        contentEditable = etContent.getText();

        tvCharCount = findViewById(R.id.textview_charcount_submit);
        refreshCharCount(contentEditable);
    }

    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_submit);
        setSupportActionBar(toolbar);
        // Enable ActionBar back button.
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setHomeAsUpIndicator(getDrawable(R.drawable.ic_close_black_24dp));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Submit").setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return submitPost();
                    }
        })
        .setIcon(getDrawable(R.drawable.ic_submit_black_24dp))
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    /**
     * Neccessary for Actionbar back button to work.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean submitPost() {
        if (!netUtil.isUserLoggedIn()) {
            Toast.makeText(this, "Submission unsuccessful. You are not logged in.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        int length = contentEditable.length();
        if (length > maxLength || length == 0) {
            Toast.makeText(this, "Submission must be between 1 and 240 characters.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        netUtil.createPost(contentEditable.toString(), netUtil.getUserId(),
                new SubmitSuccessfulListener(), new ResponseListeners.DefaultError(this));
        return true;
    }

    private void refreshCharCount(Editable editable) {
        tvCharCount.setText(String.format("%d/%d", editable.length(), maxLength));
    }

    /**
     * Watches changes to etContent in order to update character counter
     */
    private class ContentWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            refreshCharCount(s);
        }
    }

    /**
     * Get the PostId and launch a PostActivity with the new post.
     */
    private class SubmitSuccessfulListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            String postId = GsonUtil.getInstance().parseString(response);
            Intent postIntent = new Intent(SubmitPostActivity.this, PostActivity.class);
            postIntent.putExtra(PostActivity.EXTRA_POST_ID, postId);
            // TODO remove this Activity from history so back button doesn't navigate to
            //  SubmitPostActivity
            startActivity(postIntent);
        }
    }
}
