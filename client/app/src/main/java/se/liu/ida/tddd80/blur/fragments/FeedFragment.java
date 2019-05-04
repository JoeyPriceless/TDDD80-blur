package se.liu.ida.tddd80.blur.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import se.liu.ida.tddd80.blur.models.FeedType;
import se.liu.ida.tddd80.blur.models.ReactionType;
import se.liu.ida.tddd80.blur.utilities.GsonUtil;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.ResponseListeners;
import se.liu.ida.tddd80.blur.utilities.StringUtil;

public class FeedFragment extends Fragment implements ReactDialogFragment.ReactDialogListener {
    private final String TAG = getClass().getSimpleName();
    private static final String ARG_FEED_NAME = "feedName";
    public static final int DIALOG_FRAGENT = 1;

    private FeedType feedType;
    private FeedAdapter adapter;
    private NetworkUtil netUtil;
    private RecyclerView rv;

    private OnFragmentInteractionListener mListener;

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
        netUtil.getFeed(feedType, new FeedResponseListener(), new FeedResponseErrorListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_feed, container, false);
        rv = inflatedView.findViewById(R.id.recyclerview_feed);
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(lm);
        rv.addItemDecoration(new DividerItemDecoration(rv.getContext(), lm.getOrientation()));
        rv.setHasFixedSize(true);
        return inflatedView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private class FeedResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            adapter = new FeedAdapter(GsonUtil.getInstance().parseFeed(response), FeedFragment.this,
                    getFragmentManager(), new PostActivityListener());
            rv.setAdapter(adapter);
        }
    }

    private class FeedResponseErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, StringUtil.parsePlainJsonResponse(error));
            Toast.makeText(getContext(), "Failed to fetch feed.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Starts a PostActivity when user clicks on a post in the feed
     */
    private class PostActivityListener implements FeedAdapter.OnPostClickListener {
        @Override
        public void onPostClick(String postId) {
            Intent postActivityIntent = new Intent(getContext(), PostActivity.class);
            postActivityIntent.putExtra(getResources().getString(R.string.extra_post_id),
                    postId);
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
