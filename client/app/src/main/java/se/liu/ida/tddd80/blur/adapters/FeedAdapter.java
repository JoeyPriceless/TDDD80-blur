package se.liu.ida.tddd80.blur.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.models.Feed;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.utilities.StringUtil;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder>{
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
            commentButton = v.findViewById(R.id.button_feeditem_comment);
            favoriteButton = v.findViewById(R.id.button_feeditem_favorite);
        }
    }

    private Feed mFeed;

    public FeedAdapter(Feed feed) {
        this.mFeed = feed;
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
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Post post = mFeed.get(i);

        viewHolder.authorName.setText(post.getAuthor().getUsername());
        viewHolder.timestamp.setText(StringUtil.formatDateTimeShort(post.getTimeCreated()));
        viewHolder.content.setText(post.getContent());
    }

    @Override
    public int getItemCount() {
        return mFeed.size();
    }
}
