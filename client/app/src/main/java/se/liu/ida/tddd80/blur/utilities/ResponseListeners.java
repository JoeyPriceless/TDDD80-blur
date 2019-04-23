package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import se.liu.ida.tddd80.blur.models.Post;

public class ResponseListeners {

    public static class ReactionSuccess implements Response.Listener<JSONObject> {
        private Post post;
        private Button btnReact;

        public ReactionSuccess(Post post, Button reactionButton) {
            this.post = post;
            btnReact = reactionButton;
        }

        public ReactionSuccess(View v, int buttonId) {
            this.post = new Post();
            this.btnReact = v.findViewById(buttonId);
        }

        @Override
        public void onResponse(JSONObject response) {
            post.setReactions(GsonUtil.getInstance().parseReactions(response));
            ViewUtil.updateReactionButton(btnReact, post.getReactions());
        }
    }

    public static class DefaultError implements Response.ErrorListener {
        Context context;

        public DefaultError(Context context) {
            this.context = context;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            String errorString = error.toString();
            Log.e(context.getClass().getSimpleName(), errorString);
            Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show();
        }
    }
}
