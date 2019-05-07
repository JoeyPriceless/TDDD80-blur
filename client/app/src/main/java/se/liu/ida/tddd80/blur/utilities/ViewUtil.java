package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;

import jp.wasabeef.blurry.Blurry;
import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.fragments.FeedFragment;
import se.liu.ida.tddd80.blur.fragments.ReactDialogFragment;
import se.liu.ida.tddd80.blur.models.Post;
import se.liu.ida.tddd80.blur.models.ReactionType;
import se.liu.ida.tddd80.blur.models.Reactions;

public class ViewUtil {
    /**
     * Update a given reaction button according to a post's reactions.
     * Sets the color of the icon and text according to the user's vote.
     * Also blurs/unblurs author's name and image.
     * @param button Button which drawable/text should be updated
     */
    public static void refreshPostViews(Button button, Post post, TextView tvAuthor,
                                        UserImageView ivAuthor) {
        Context context = button.getContext();
        Reactions reactions = post.getReactions();
        button.setText(String.valueOf(reactions.getScore()));
        int colorId = R.color.neutralColor;
        Drawable buttonDrawable = reactions.getOwnReaction().getDrawable(context);
        button.setCompoundDrawablesWithIntrinsicBounds(buttonDrawable, null, null, null);
        ReactionType ownReaction = reactions.getOwnReaction();
        if (post.hasBlur()) {
            blurText(tvAuthor);
        } else {
            colorId = ownReaction.ordinal() < ReactionType.DOWNVOTE_0.ordinal()
                    ? R.color.positiveColor
                    : R.color.negativeColor;
            unblurTextView(tvAuthor);

        }
        ivAuthor.setBlur(post.hasBlur());
        setImageByUrl(ivAuthor, post.getAuthorPictureUrl());

        int color = ContextCompat.getColor(context, colorId);
        button.setTextColor(color);
    }

    /**
     * Private message run by the public methods.
     */
    private static void pShowReactionDialog(Context context, FragmentManager fragmentManager,
                                          String postId, ReactionType ownReaction,
                                            @Nullable FeedFragment targetFragment,
                                            @Nullable Integer adapterPosition) {
        if (!NetworkUtil.getInstance(context).isUserLoggedIn()) {
            Toast.makeText(context, "You must be logged in to react",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ReactDialogFragment dialog = new ReactDialogFragment();
        Bundle args = new Bundle();
        args.putString(ReactDialogFragment.KEY_POST_ID, postId);
        args.putInt(ReactDialogFragment.KEY_CURRENT_SELECTION, ownReaction.ordinal());
        if (targetFragment != null && adapterPosition != null) {
            args.putInt(ReactDialogFragment.KEY_ADAPTER_POSITION, adapterPosition);
            dialog.setTargetFragment(targetFragment, FeedFragment.DIALOG_FRAGENT);
        }
        dialog.setArguments(args);
        dialog.show(fragmentManager, context.getClass().getSimpleName());
    }

    /**
     * Shows an ReactDialog displaying each of the votes a user can select for a post. This overload
     * is used by FeedFragments which need to specify the post's adapter position.
     */
    public static void showReactionDialog(Context context, FragmentManager fragmentManager,
                                          String postId, ReactionType ownReaction,
                                          FeedFragment targetFragment, Integer adapterPosition) {
        pShowReactionDialog(context, fragmentManager, postId, ownReaction, targetFragment,
                adapterPosition);
    }

    /**
     * Shows an ReactDialog displaying each of the votes a user can select for a post in a
     * PostActivity
     */
    public static void showReactionDialog(Context context, FragmentManager fragmentManager,
                                          String postId, ReactionType ownReaction) {
        pShowReactionDialog(context, fragmentManager, postId, ownReaction, null, null);
    }

    public static void blurText(TextView tv) {
        tv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        float radius = tv.getTextSize() / (float)3.5;
        BlurMaskFilter filter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
        tv.getPaint().setMaskFilter(filter);
    }

    public static void unblurTextView(TextView tv) {
        tv.getPaint().setMaskFilter(null);
        tv.invalidate();
    }

    public static void setImageByUrl(final UserImageView iv, final String url) {
        NetworkUtil.getInstance(iv.getContext()).setImageUrl(iv, url);
    }

    public static void blurImageView(final NetworkImageView iv) {
        // The Blurry library cannot be run on the UI thread as it often fails a race condition
        // with the ImageView. Use iv.post() to delay it.
        Blurry.with(iv.getContext()).radius(iv.getWidth() / 50).sampling(10).capture(iv).into(iv);
    }

    public static Bitmap blurBitmap(Bitmap bitmap) {
        // TODO
        return bitmap;
    }
}
