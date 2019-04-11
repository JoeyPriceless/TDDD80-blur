package se.liu.ida.tddd80.blur.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.models.User;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;


public class PostActivity extends AppCompatActivity {
	private Post post;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);

        String username = "testuser";
        String email = "test@test.com";
        String password = "password123";

        User user = new User(username, email);

        NetworkUtil netUtil = new NetworkUtil(this);

        netUtil.createUser(user, password, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(getClass().getSimpleName(), "Create user successful. Response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(getClass().getSimpleName(), "Create user unsuccessful. Error: " + error);
            }
        });
	}
}