package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import se.liu.ida.tddd80.blur.models.Post;

public class ResponseListeners {

    /**
     * Provides a default responselistener for a NetworkUtil.reactToPost call. Updates a given
     * reaction button with the new score.
     */
    public static class ReactionSuccess implements Response.Listener<JSONObject> {
        private Post post;
        private Button btnReact;
        private TextView tvAuthor;
        private ImageView ivAuthor;

        public ReactionSuccess(Post post, Button reactionButton, TextView tvAuthor,
                               ImageView ivAuthor) {
            this.post = post;
            btnReact = reactionButton;
            this.tvAuthor = tvAuthor;
            this.ivAuthor = ivAuthor;
        }

        public ReactionSuccess(Button btnReact) {
            this.post = new Post();
            this.btnReact = btnReact;
        }

        @Override
        public void onResponse(JSONObject response) {
            post.setReactions(GsonUtil.getInstance().parseReactions(response));
            ViewUtil.onReactionUpdateViews(btnReact, post.getReactions(), tvAuthor, ivAuthor);
        }
    }

    /**
     * Provides a defailt ErrorListener for Volley requests. Notifies the log and creates a toast.
     */
    public static class DefaultError implements Response.ErrorListener {
        Context context;

        public DefaultError(Context context) {
            this.context = context;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            String errorString = error.toString();
            Log.e(context.getClass().getSimpleName(), errorString);
            Toast.makeText(context, errorString, Toast.LENGTH_LONG).show();
        }
    }
}
