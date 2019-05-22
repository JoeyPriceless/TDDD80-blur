package se.liu.ida.tddd80.blur.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.models.Comment;
import se.liu.ida.tddd80.blur.models.CommentList;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.ResponseListeners;
import se.liu.ida.tddd80.blur.utilities.StringUtil;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView authorName;
        private TextView timestamp;
        public TextView content;
        private TextView scoreText;
        private Button upvButton;
        private Button downvButton;

        private ViewHolder(@NonNull View v) {
            super(v);

            authorName = v.findViewById(R.id.textview_comment_author);
            timestamp = v.findViewById(R.id.textview_comment_time);
            content = v.findViewById(R.id.textview_comment_content);
            scoreText = v.findViewById(R.id.textview_score);
            upvButton = v.findViewById(R.id.button_upvote);
            downvButton = v.findViewById(R.id.button_downvote);
        }
    }


    private CommentList comments;

    public CommentsAdapter(CommentList comments) {
        this.comments = comments;
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
    public void onBindViewHolder(@NonNull final ViewHolder vh, @SuppressLint("RecyclerView") final int i) {
        final Comment comment = comments.get(i);
        vh.authorName.setText(comment.getAuthor().getUsername());
        vh.timestamp.setText(StringUtil.formatDateTimeShort(comment.getTimeCreated()));
        vh.content.setText(comment.getContent());
        vh.scoreText.setText(String.valueOf(comment.getScore()));
        setArrowColors(comment, vh);
        vh.upvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comment.getOwnScore() == 2)
                    comment.setOwnScore(0);
                else
                    comment.setOwnScore(2);
                NetworkUtil.getInstance(v.getContext()).reactToComment(comment.getId(), 2,
                        new ResponseListeners.CommentReactionSuccess(CommentsAdapter.this, i),
                        new ResponseListeners.DefaultError(v.getContext()));
            }
        });
        vh.downvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comment.getOwnScore() == 1)
                    comment.setOwnScore(0);
                else
                    comment.setOwnScore(1);
                NetworkUtil.getInstance(v.getContext()).reactToComment(comment.getId(), 1,
                        new ResponseListeners.CommentReactionSuccess(CommentsAdapter.this, i),
                        new ResponseListeners.DefaultError(v.getContext()));
            }
        });
    }

    private void setArrowColors(Comment comment, ViewHolder vh) {
        Drawable upArrow = comment.getOwnScore() == 2?vh.itemView.getContext().getResources()
                .getDrawable(R.drawable.ic_keyboard_arrow_up_blue_24dp, null):
                vh.itemView.getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp, null);
        Drawable downArrow = comment.getOwnScore() == 1?vh.itemView.getContext().getResources()
                .getDrawable(R.drawable.ic_keyboard_arrow_down_orange_24dp, null):
                vh.itemView.getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_black_24dp, null);
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
        return comments.getComments().size();
    }
}
