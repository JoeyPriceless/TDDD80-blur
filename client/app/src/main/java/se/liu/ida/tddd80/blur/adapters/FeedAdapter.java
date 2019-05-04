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
import se.liu.ida.tddd80.blur.models.Feed;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.utilities.StringUtil;
import se.liu.ida.tddd80.blur.utilities.ViewUtil;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView authorImage;
        public TextView authorName;
        public TextView timestamp;
        public TextView content;
        public Button reactButton;
        public Button commentButton;
        public Button favoriteButton;

        public ViewHolder(@NonNull View v) {
            super(v);

            authorImage = v.findViewById(R.id.imageview_feeditem_author);
            authorName = v.findViewById(R.id.textview_feeditem_author);
            timestamp = v.findViewById(R.id.textview_feeditem_time);
            content = v.findViewById(R.id.textview_feeditem_content);
            reactButton = v.findViewById(R.id.button_feeditem_react);
            reactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewUtil.showReactionDialog(v.getContext(), fragmentManager, getPostId(),
                            reactButton.getId(), authorName.getId(), authorImage.getId());
                }
            });
            commentButton = v.findViewById(R.id.button_feeditem_comment);
            favoriteButton = v.findViewById(R.id.button_feeditem_favorite);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onPostClick(getPostId());
                    }
                }
            });
        }

        private String getPostId() {
            return feed.get(getAdapterPosition()).getId();
        }
    }

    private Feed feed;
    private FragmentManager fragmentManager;
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(String postId);
    }

    public FeedAdapter(Feed feed, FragmentManager fragmentManager, OnPostClickListener listener) {
        this.feed = feed;
        this.fragmentManager = fragmentManager;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_feed, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int i) {
        Post post = feed.get(i);
        // TODO
        vh.authorName.setText(PostActivity.AUTHOR_SPACE_PADDING + post.getAuthor().getUsername());
        vh.timestamp.setText(StringUtil.formatDateTimeShort(post.getTimeCreated()));
        vh.content.setText(post.getContent());
        ViewUtil.onReactionUpdateViews(vh.reactButton, post.getReactions(), vh.authorName,
                vh.authorImage);
        // TODO
        vh.commentButton.setText("1024");
        // TODO
        vh.favoriteButton.setText("2048");
    }

    @Override
    public int getItemCount() {
        return feed.size();
    }
}
