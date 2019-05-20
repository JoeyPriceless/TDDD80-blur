package se.liu.ida.tddd80.blur.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.activities.PostActivity;
import se.liu.ida.tddd80.blur.fragments.CommentFragment;
import se.liu.ida.tddd80.blur.models.Comment;
import se.liu.ida.tddd80.blur.models.CommentList;
import se.liu.ida.tddd80.blur.utilities.StringUtil;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView authorImage;
        public TextView authorName;
        public TextView timestamp;
        public TextView location;
        public TextView content;
        public ImageView attachment;
        public TextView scoreText;
        public Button upvButton;
        public Button downvButton;

        public ViewHolder(@NonNull View v) {
            super(v);

            authorName = v.findViewById(R.id.textview_comment_author);
            timestamp = v.findViewById(R.id.textview_comment_time);
            location = v.findViewById(R.id.textview_comment_location);
            content = v.findViewById(R.id.textview_comment_content);
            scoreText = v.findViewById(R.id.textview_score);
            upvButton = v.findViewById(R.id.button_upvote);
            downvButton = v.findViewById(R.id.button_downvote);
        }

        private String getCommentId() {
            return comments.get(getAdapterPosition()).getId();
        }
    }



    private String postId;
    private CommentList comments;
    private CommentFragment fragment;
    private FragmentManager fragmentManager;

    public CommentsAdapter(String postId, CommentList comments, CommentFragment fragment, FragmentManager fragmentManager) {
        this.postId = postId;
        this.comments = comments;
        this.fragment = fragment;
        this.fragmentManager = fragmentManager;
    }

    public void setCommentScore(int position, int score) {
        comments.get(position).setScore(score);
        // It's important not to update a post's views directly in a RecyclerView. Rather, update
        // the model and notify the adapter.
        // Useful resource: https://stackoverflow.com/a/48959184/4400799
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_comment, parent, false);
        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder vh, int i) {
        final Comment comment = comments.get(i);
        vh.authorName.setText(PostActivity.AUTHOR_SPACE_PADDING + comment.getAuthor().getUsername());
        vh.timestamp.setText(StringUtil.formatDateTimeShort(comment.getTimeCreated()));
        vh.content.setText(comment.getContent());

        //ViewUtil.refreshPostViews(vh.reactButton, post, vh.authorName, vh.location, vh.authorImage);
    }

    @Override
    public int getItemCount() {
        return comments.getComments().size();
    }

    public void replaceComments(CommentList comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }
}
