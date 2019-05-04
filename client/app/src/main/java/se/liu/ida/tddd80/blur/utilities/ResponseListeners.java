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

import se.liu.ida.tddd80.blur.adapters.FeedAdapter;
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

        public ReactionSuccess(Post post, Button btnReact, TextView tvAuthor,
                               ImageView ivAuthor) {
            this.post = post;
            this.btnReact = btnReact;
            this.tvAuthor = tvAuthor;
            this.ivAuthor = ivAuthor;
        }

        @Override
        public void onResponse(JSONObject response) {
            post.setReactions(GsonUtil.getInstance().parseReactions(response));
            ViewUtil.onReactionUpdateViews(btnReact, post.getReactions(), tvAuthor, ivAuthor);
        }
    }

    /**
     * ResponseListener for a reactToPost reaction call. Let's a FeedAdapter take care of updating
     * the post's a views.
     */
    public static class FeedReactionSuccess implements Response.Listener<JSONObject> {
        private FeedAdapter adapter;
        private int position;

        public FeedReactionSuccess(FeedAdapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        public void onResponse(JSONObject response) {
            adapter.setPostReactions(position, GsonUtil.getInstance().parseReactions(response));
        }
    }

    /**
     * Provides a default ErrorListener for Volley requests. Notifies the log and creates a toast.
     */
    // TODO fix parsing for both JSON and raw responses
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
