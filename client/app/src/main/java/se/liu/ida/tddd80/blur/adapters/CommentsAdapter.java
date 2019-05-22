package se.liu.ida.tddd80.blur.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.fragments.CommentFragment;
import se.liu.ida.tddd80.blur.models.Comment;
import se.liu.ida.tddd80.blur.models.CommentList;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.ResponseListeners;
import se.liu.ida.tddd80.blur.utilities.StringUtil;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView authorName;
        public TextView timestamp;
        public TextView content;
        public TextView scoreText;
        public Button upvButton;
        public Button downvButton;

        public ViewHolder(@NonNull View v) {
            super(v);

            authorName = v.findViewById(R.id.textview_comment_author);
            timestamp = v.findViewById(R.id.textview_comment_time);
            content = v.findViewById(R.id.textview_comment_content);
            scoreText = v.findViewById(R.id.textview_score);
            upvButton = v.findViewById(R.id.button_upvote);
            downvButton = v.findViewById(R.id.button_downvote);
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
    public void onBindViewHolder(@NonNull final ViewHolder vh, final int i) {
        final Comment comment = comments.get(i);
        vh.authorName.setText(comment.getAuthor().getUsername());
        vh.timestamp.setText(StringUtil.formatDateTimeShort(comment.getTimeCreated()));
        vh.content.setText(comment.getContent());
        vh.scoreText.setText(String.valueOf(comment.getScore()));
        setArrowColors(comment, vh);
        vh.upvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comment.getOwnReaction() == 2)
                    comment.setOwnReaction(0);
                else
                    comment.setOwnReaction(2);
                NetworkUtil.getInstance(v.getContext()).reactToComment(comment.getId(), 2,
                        new ResponseListeners.CommentReactionSuccess(CommentsAdapter.this, i),
                        new ResponseListeners.DefaultError(v.getContext()));
            }
        });
        vh.downvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comment.getOwnReaction() == 1)
                    comment.setOwnReaction(0);
                else
                    comment.setOwnReaction(1);
                NetworkUtil.getInstance(v.getContext()).reactToComment(comment.getId(), 1,
                        new ResponseListeners.CommentReactionSuccess(CommentsAdapter.this, i),
                        new ResponseListeners.DefaultError(v.getContext()));
            }
        });
    }

    public void setArrowColors(Comment comment, ViewHolder vh) {
        Drawable upArrow = comment.getOwnReaction() == 2?vh.itemView.getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_blue_24dp):
                vh.itemView.getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp);
        Drawable downArrow = comment.getOwnReaction() == 1?vh.itemView.getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_orange_24dp):
                vh.itemView.getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_black_24dp);
        upArrow.setBounds( 0, 0, 100, 100 );
        downArrow.setBounds( 0, 0, 100, 100 );
        vh.upvButton.setCompoundDrawables(upArrow, null, null, null);
        vh.downvButton.setCompoundDrawables(downArrow, null, null, null);
    }

    public void setCommentReactions(int position, int score) {
        Comment comment = comments.get(position);
        comment.setScore(score);
        // It's important not to update a post's views directly in a RecyclerView. Rather, update
        // the model and notify the adapter.
        // Useful resource: https://stackoverflow.com/a/48959184/4400799
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        int test = comments.getComments().size();
        return test;
    }
}
