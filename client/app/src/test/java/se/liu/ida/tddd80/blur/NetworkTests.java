package se.liu.ida.tddd80.blur;

import android.content.Context;
import android.util.Log;

import com.android.volley.Network;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import se.liu.ida.tddd80.blur.models.User;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;

@RunWith(MockitoJUnitRunner.class)
public class NetworkTests {
    private final String TAG = this.getClass().getSimpleName();
    @Mock
    Context context;

    @Test
    public void createUser() {
        String username = "testuser";
        String email = "test@test.com";
        String password = "password123";

        User user = new User(username, email);

        NetworkUtil netUtil = NetworkUtil.getInstance(context);

        netUtil.createUser(user, password, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "Create user successful. Response: " + response);
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Create user unsuccessful. Error: " + error);
            }
        });
    }
}
