package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;
import com.vansuita.gaussianblur.GaussianBlur;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
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
                                        TextView tvLocation, ImageView ivAuthor) {
        Context context = button.getContext();
        Reactions reactions = post.getReactions();
        button.setText(String.valueOf(reactions.getScore()));
        int colorId = R.color.neutralColor;
        Drawable buttonDrawable = reactions.getOwnReaction().getDrawable(context);
        button.setCompoundDrawablesWithIntrinsicBounds(buttonDrawable, null, null, null);
        ReactionType ownReaction = reactions.getOwnReaction();
        int errorRes;
        if (post.hasBlur()) {
            blurTextView(tvAuthor);
            blurTextView(tvLocation);
            errorRes = R.mipmap.img_profile_default_blurred_fore;
        } else {
            colorId = ownReaction.ordinal() < ReactionType.DOWNVOTE_0.ordinal()
                    ? R.color.positiveColor
                    : R.color.negativeColor;
            unblurTextView(tvAuthor);
            unblurTextView(tvLocation);
            errorRes = R.mipmap.img_profile_default_fore;
        }

         RequestCreator picasso = Picasso.get().load(post.getAuthorPictureUrl())
                .noFade()
                .error(errorRes);
        if (post.hasBlur())
            picasso.transform(new BlurTransformation(context));
        picasso.into(ivAuthor);

        int color = ContextCompat.getColor(context, colorId);
        button.setTextColor(color);
    }

    public static class BlurTransformation implements Transformation {
        private Context context;

        public BlurTransformation(Context context) {
            this.context = context;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap blurred = blurBitmap(context, source);

            if (blurred != source)
                source.recycle();
            return blurred;
        }

        @Override
        public String key() {
            return "blur()";
        }
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

    public static void blurTextView(TextView tv) {
        TextPaint paint = tv.getPaint();
        if (paint.getMaskFilter() != null) return;
        tv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        float radius = tv.getTextSize() / (float)3;
        BlurMaskFilter filter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
        paint.setMaskFilter(filter);
    }

    public static void unblurTextView(TextView tv) {
        TextPaint paint = tv.getPaint();
        if (paint.getMaskFilter() == null) return;
        tv.getPaint().setMaskFilter(null);
        tv.invalidate();
    }


    public static Bitmap blurBitmap(Context context, Bitmap bitmap) {
        Bitmap blurred = GaussianBlur.with(context).size(200).radius(25).render(bitmap);
        return blurred;
    }
}
