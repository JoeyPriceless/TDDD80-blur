package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.BlurMaskFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import jp.wasabeef.blurry.Blurry;
import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.fragments.ReactDialogFragment;
import se.liu.ida.tddd80.blur.models.ReactionType;
import se.liu.ida.tddd80.blur.models.Reactions;

public class ViewUtil {

    /**
     * Update a given reaction button according to a post's reactions. Sets the color of the icon
     * and text according to the user's vote.
     * @param button Button which drawable/text should be updated
     * @param reactions The reactions belonging to the post which was voted on.
     */
    public static void onReactionUpdateViews(Button button, Reactions reactions, TextView tvAuthor,
                                             ImageView ivAuthor) {
        button.setText(String.valueOf(reactions.getScore()));
        int colorId = R.color.colorReactionNeutral;
        ReactionType ownReaction = reactions.getOwnReaction();
        if (ownReaction != null) {
            colorId = ownReaction.ordinal() < 3 ? R.color.colorReactionPositive :
                    R.color.colorReactionNegative;
            ViewUtil.unBlurPost(tvAuthor, ivAuthor);
        } else {
            ViewUtil.blurText(tvAuthor);
            ViewUtil.blurImage(ivAuthor);
        }
        int color = ContextCompat.getColor(button.getContext(), colorId);
        button.setCompoundDrawableTintList(ColorStateList.valueOf(color));
        button.setTextColor(color);
    }

    public static void showReactionDialog(Context context, FragmentManager fragmentManager,
                                          String postId) {
        showReactionDialog(context, fragmentManager, postId, 0, 0, 0);
    }

    /**
     * Shows an ReactDialog displaying each of the votes a user can select for a post.
     * @param context
     * @param fragmentManager
     * @param postId
     * @param buttonId
     */
    public static void showReactionDialog(Context context, FragmentManager fragmentManager,
                                          String postId, int buttonId, int authorTextViewId,
                                          int authorImageViewId) {
        if (!NetworkUtil.getInstance(context).isUserLoggedIn()) {
            Toast.makeText(context, "You must be logged in to react",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ReactDialogFragment dialog = new ReactDialogFragment();
        Bundle args = new Bundle();
        args.putString(ReactDialogFragment.KEY_POST_ID, postId);
        args.putInt(ReactDialogFragment.KEY_BUTTON_ID, buttonId);
        args.putInt(ReactDialogFragment.KEY_AUTHOR_TEXT_ID, authorTextViewId);
        args.putInt(ReactDialogFragment.KEY_IMAGE_VIEW_ID, authorImageViewId);
        dialog.setArguments(args);
        dialog.show(fragmentManager, context.getClass().getSimpleName());
    }

    public static void blurText(TextView tv) {
        tv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        float radius = tv.getTextSize() / (float)3.5;
        BlurMaskFilter filter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
        tv.getPaint().setMaskFilter(filter);
    }

    public static void blurImage(final ImageView iv) {
        // TODO set actual image
        // The Blurry library cannot be run on the UI thread as it often fails a race condition
        // with the ImageView.
        iv.post(new Runnable() {
            @Override
            public void run() {
                Context context = iv.getContext();
                iv.setImageDrawable(context.getDrawable(R.mipmap.img_profile_default));
                Blurry.with(context).radius(iv.getWidth() / 50).sampling(10).capture(iv).into(iv);
            }
        });
    }

    public static void unBlurPost(TextView tv, ImageView iv) {
        // TODO include custom image
        // Can't find any info on how to unblur an ImageView through Blurry so have to reset the
        // drawable. Look into overlay blur on image?
        iv.setImageDrawable(tv.getContext().getDrawable(R.mipmap.img_profile_default_round));
        tv.getPaint().setMaskFilter(null);
        tv.invalidate();
    }
}
