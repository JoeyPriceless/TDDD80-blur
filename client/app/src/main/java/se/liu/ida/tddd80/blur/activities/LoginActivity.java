package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.os.Bundle;
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
import se.liu.ida.tddd80.blur.utilities.StringUtil;

public class LoginActivity extends AbstractAccountActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn = findViewById(R.id.button_login_login);
        etEmail = findViewById(R.id.edittext_login_email);
        etEmail.addTextChangedListener(new EmailWatcher());

        etPassword = findViewById(R.id.edittext_login_password);
        etPassword.addTextChangedListener(new PasswordWatcher());
    }

    public void onClickLogin(View v) {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        if (validateCredentials())
            netUtil.login(email, password, new LoginSuccess(), new LoginError());
    }

    public void onClickGoToRegister(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private boolean validateCredentials() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        boolean isEmailValid = StringUtil.isValidEmail(email);
        boolean isPassValid = StringUtil.isValidPassword(this, password);
        if (!isEmailValid)
            etEmail.setError("Invalid email");
        else
            etEmail.setError(null);
        if (!isPassValid)
            etPassword.setError("Less than minimum length " + passwordMinLength);
        else
            etPassword.setError(null);

        boolean allValid = isEmailValid && isPassValid;
        btn.setEnabled(allValid);
        return allValid;
    }
}
