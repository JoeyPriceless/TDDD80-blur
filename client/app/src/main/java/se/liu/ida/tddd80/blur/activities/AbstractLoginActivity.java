package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.StringUtil;

/**
 * Contains common logic for LoginActivity and RegisterActivity
 */
public abstract class AbstractLoginActivity extends AppCompatActivity {
    protected final String TAG = getClass().getSimpleName();
    protected Class targetActivity = FeedActivity.class;
    protected NetworkUtil netUtil;
    protected EditText etEmail;
    protected EditText etPassword;
    protected Button btn;
    protected int passwordMinLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        passwordMinLength = getResources().getInteger(R.integer.password_min_length);
        netUtil = NetworkUtil.getInstance(this);
        // User is already logged in.
        if (netUtil.isUserLoggedIn())
            continueToTarget();
    }

    public void onClickNoAccount(View v) {
        continueToTarget();
    }

    protected void continueToTarget() {
        Intent intent = new Intent(getBaseContext(), targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

    protected boolean isEmailValid() {
        return StringUtil.isValidEmail(etEmail.getText().toString());
    }

    protected boolean isPasswordValid() {
        return StringUtil.isValidPassword(this, etPassword.getText().toString());
    }

    protected class LoginSuccess implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                netUtil.login(response.getString(("token")), response.getString("user_id"));
            } catch (JSONException | JsonSyntaxException ex) {
                parseError(ex);
            }
            continueToTarget();
        }
    }

    protected class LoginError implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            String message;
            message = StringUtil.parsePlainJsonResponse(error);
            Log.w(TAG, message);
            Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Button is only enabled when all fields are valid.
     */
    protected void setButton() {
        btn.setEnabled(isPasswordValid() && isEmailValid());
    }

    protected class EmailWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            boolean isEmailValid = StringUtil.isValidEmail(s.toString());
            if (isEmailValid)
                etEmail.setError(null);
            else
                etEmail.setError("Invalid email");
            setButton();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    protected class PasswordWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            boolean isPasswordValid = StringUtil.isValidPassword(getBaseContext(),
                    s.toString());
            if (isPasswordValid)
                etPassword.setError(null);
            else
                etPassword.setError(String.format("Minimum %d characters and no spaces.",
                        passwordMinLength));
            setButton();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    protected void parseError(Throwable ex) {
        Log.e(TAG, ExceptionUtils.getStackTrace(ex));
        Toast.makeText(getBaseContext(), "Error when parsing response",
                Toast.LENGTH_LONG).show();
    }
}
