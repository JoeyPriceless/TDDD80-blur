package se.liu.ida.tddd80.blur.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONObject;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;

public abstract class SubmitActivity extends AppCompatActivity implements Response.Listener<JSONObject> {

    protected NetworkUtil netUtil;
    protected EditText etContent;
    protected Editable contentEditable;
    protected TextView tvCharCount;
    protected int maxLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActionBar();
        netUtil = NetworkUtil.getInstance(this);
        maxLength = getResources().getInteger(R.integer.post_max_length);

        etContent = findViewById(R.id.edittext_submit_content);
        tvCharCount = findViewById(R.id.textview_submit_charcount);
        etContent.addTextChangedListener(new SubmitActivity.ContentWatcher());
        // Set content box as focused automatically.
        etContent.requestFocus();
        contentEditable = etContent.getText();

        refreshCharCount(contentEditable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Submit").setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return submit();
                    }
                })
                .setIcon(getDrawable(R.drawable.ic_submit_black_24dp))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
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

    abstract boolean submit();

    protected boolean checkTextLength() {
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
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        // TODO: Doesn't work in the comment submitting
        onBackPressed();
        return true;
    }
}
