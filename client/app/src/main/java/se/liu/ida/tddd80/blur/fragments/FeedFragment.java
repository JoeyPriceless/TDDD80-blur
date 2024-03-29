package se.liu.ida.tddd80.blur.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.activities.PostActivity;
import se.liu.ida.tddd80.blur.adapters.FeedAdapter;
import se.liu.ida.tddd80.blur.models.Feed;
import se.liu.ida.tddd80.blur.models.FeedType;
import se.liu.ida.tddd80.blur.models.ReactionType;
import se.liu.ida.tddd80.blur.utilities.GsonUtil;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.ResponseListeners;
import se.liu.ida.tddd80.blur.utilities.StringUtil;

public class FeedFragment extends Fragment implements ReactDialogFragment.ReactDialogListener,
        Response.Listener<JSONObject>, Response.ErrorListener, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = getClass().getSimpleName();
    private static final String ARG_FEED_NAME = "feedName";
    public static final int DIALOG_FRAGENT = 1;

    private FeedType feedType;
    private FeedAdapter adapter;
    private NetworkUtil netUtil;
    private RecyclerView rv;
    private SwipeRefreshLayout swipeLayout;

    public FeedFragment() {
        // Required empty public constructor
    }

    public static FeedFragment newInstance(String feedName) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FEED_NAME, feedName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            feedType = FeedType.valueOf((String)getArguments().get(ARG_FEED_NAME));
        }
        netUtil = NetworkUtil.getInstance(getContext());
        netUtil.getFeed(feedType, this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_feed, container, false);

        swipeLayout = inflatedView.findViewById(R.id.swiperefreshlayout_feed);
        swipeLayout.setOnRefreshListener(this);

        rv = inflatedView.findViewById(R.id.recyclerview_feed);
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(lm);
        rv.addItemDecoration(new DividerItemDecoration(rv.getContext(), lm.getOrientation()));
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);
        return inflatedView;
    }

    @Override
    public void onRefresh() {
        netUtil.getFeed(feedType, this, this);
    }

    @Override
    public void onResponse(JSONObject response) {
        Feed feed = GsonUtil.getInstance().parseFeed(response);
        if (adapter == null) {
            adapter = new FeedAdapter(feed, FeedFragment.this,
                    getFragmentManager(), new PostActivityListener());
            rv.setAdapter(adapter);
        } else {
            adapter.replaceFeed(feed);
        }
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        try {
            int statusCode = error.networkResponse.statusCode;
            Log.e(TAG, StringUtil.parsePlainJsonResponse(error));
            Toast.makeText(getContext(), statusCode + " Failed to fetch feed.", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {}
        swipeLayout.setRefreshing(false);
    }

    /**
     * Starts a PostActivity when user clicks on a post in the feed
     */
    private class PostActivityListener implements FeedAdapter.OnPostClickListener {
        @Override
        public void onPostClick(String postId) {
            Intent postActivityIntent = new Intent(getContext(), PostActivity.class);
            postActivityIntent.putExtra(PostActivity.EXTRA_POST_ID, postId);
            startActivity(postActivityIntent);
        }
    }

    /**
     * Sends the ReactionType chosen in a ReactDialogFragment to the server.
     */
    @Override
    public void onClickReactionDialog(ReactDialogFragment dialog) {
        ReactionType type = ReactionType.values()[dialog.getIndex()];
        NetworkUtil.getInstance(getContext()).reactToPost(dialog.getPostId(), type,
                new ResponseListeners.FeedReactionSuccess(adapter, dialog.getAdapterPosition()),
                new ResponseListeners.DefaultError(getContext()));
    }
}
