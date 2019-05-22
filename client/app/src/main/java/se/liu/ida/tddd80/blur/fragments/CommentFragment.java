package se.liu.ida.tddd80.blur.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
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

import java.util.ArrayList;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.adapters.CommentsAdapter;
import se.liu.ida.tddd80.blur.models.Comment;
import se.liu.ida.tddd80.blur.models.CommentList;
import se.liu.ida.tddd80.blur.utilities.GsonUtil;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.StringUtil;

public class CommentFragment extends Fragment implements Response.Listener<JSONObject>, Response.ErrorListener,
        SwipeRefreshLayout.OnRefreshListener{
    private final String TAG = getClass().getSimpleName();
    private static final String POSTID = "postid";

    private NetworkUtil networkUtil;
    private RecyclerView rv;
    private SwipeRefreshLayout swipeLayout;

    public static CommentFragment newInstance(String postId) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putString(POSTID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkUtil = NetworkUtil.getInstance(getContext());
        if (getArguments() != null) {
            networkUtil.getComments((String)getArguments().get(POSTID), this, this);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_comments, container, false);

        swipeLayout = inflatedView.findViewById(R.id.swiperefreshlayout_comments);
        swipeLayout.setOnRefreshListener(this);

        rv = inflatedView.findViewById(R.id.recyclerview_comments);
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(lm);
        rv.addItemDecoration(new DividerItemDecoration(rv.getContext(), lm.getOrientation()));
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        CommentList commentList = new CommentList();
        commentList.setComments(new ArrayList<Comment>());

        return inflatedView;
    }

    @Override
    public void onRefresh() {
        if (getArguments() != null) {
            networkUtil.getComments((String)getArguments().get(POSTID), this, this);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        try {
            int statusCode = error.networkResponse.statusCode;
            Log.e(TAG, StringUtil.parsePlainJsonResponse(error));
            Toast.makeText(getContext(), statusCode + " Failed to fetch comments.", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            // Ignore
        }
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void onResponse(JSONObject response) {
        CommentList comments = GsonUtil.getInstance().parseComments(response);
        if (getArguments() != null) {
            CommentsAdapter adapter = new CommentsAdapter((String) getArguments().get(POSTID), comments,
                    this, getFragmentManager());
            rv.setAdapter(adapter);
        }
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        swipeLayout.setRefreshing(false);
    }
}
