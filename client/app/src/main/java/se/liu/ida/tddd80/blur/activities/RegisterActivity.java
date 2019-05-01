package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.utilities.StringUtil;

public class RegisterActivity extends AbstractLoginActivity {
    private EditText etUsername;
    private int usernameMinLength;
    private int usernameMaxLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        usernameMinLength = getResources().getInteger(R.integer.username_min_length);
        usernameMaxLength = getResources().getInteger(R.integer.username_max_length);
        btn = findViewById(R.id.button_register_register);
        etUsername = findViewById(R.id.edittext_register_username);
        etUsername.addTextChangedListener(new UsernameWatcher());

        etEmail = findViewById(R.id.edittext_register_email);
        etEmail.addTextChangedListener(new EmailWatcher());

        etPassword = findViewById(R.id.edittext_register_password);
        etPassword.addTextChangedListener(new PasswordWatcher());
    }

    private boolean isUsernameValid() {
        return StringUtil.isValidUsername(this, etUsername.getText().toString());
    }

    public void onClickLogin(View v) {
        String username = etUsername.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        if (validateCredentials())
            netUtil.createUser(username, email, password, new LoginSuccess(), new LoginError());
    }

    public void onClickGoToLogin(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private boolean validateCredentials() {
        String username = etUsername.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        boolean isUsernameValid = StringUtil.isValidUsername(this, username);
        boolean isEmailValid = StringUtil.isValidEmail(email);
        boolean isPassValid = StringUtil.isValidPassword(this, password);

        if (!isUsernameValid)
            etUsername.setError(String.format("Username not between %d-%d characters",
                    usernameMinLength, usernameMaxLength));
        else
            etUsername.setError(null);
        if (!isEmailValid)
            etEmail.setError("Invalid email");
        else
            etEmail.setError(null);
        if (!isPassValid)
            etPassword.setError("Less than minimum length " + passwordMinLength);
        else
            etPassword.setError(null);

        boolean allValid = isEmailValid && isPassValid && isUsernameValid;
        btn.setEnabled(allValid);
        return allValid;
    }

    @Override
    protected void setButton() {
        btn.setEnabled(isPasswordValid() && isEmailValid() && isUsernameValid());
    }

    private class UsernameWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            boolean isUsernameValid = StringUtil.isValidUsername(getBaseContext(),
                    s.toString());
            if (isUsernameValid)
                etUsername.setError(null);
            else
                etUsername.setError(String.format("%d-%d characters", usernameMinLength,
                        usernameMaxLength));

            setButton();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }


}
