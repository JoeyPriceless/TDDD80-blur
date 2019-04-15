package se.liu.ida.tddd80.blur.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.adapters.FeedAdapter;
import se.liu.ida.tddd80.blur.models.Feed;
import se.liu.ida.tddd80.blur.models.FeedType;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment {
    private static final String ARG_FEED_NAME = "feedName";

    private FeedType mFeedType;
    private FeedAdapter mAdapter;
    private NetworkUtil mNetUtil;
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
            mFeedType = FeedType.valueOf((String)getArguments().get(ARG_FEED_NAME));
        }
        mNetUtil = NetworkUtil.getInstance(getContext());
        mNetUtil.getFeed(mFeedType, new ResponseListener(), new ResponseErrorListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_feed, container, false);
        rv = inflatedView.findViewById(R.id.recyclerview_feed);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        return inflatedView;
    }

    // TODO: Rename method, update argument and hook method into UI event
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

    private class ResponseListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            mAdapter = new FeedAdapter(Feed.fromJson(response));
            rv.setAdapter(mAdapter);
        }
    }

    private class ResponseErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(getContext(), "Failed to fetch feed.\n" + error,
                    Toast.LENGTH_LONG).show();
        }
    }
}
