package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.Toast;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.fragments.ReactDialogFragment;
import se.liu.ida.tddd80.blur.models.ReactionType;
import se.liu.ida.tddd80.blur.models.Reactions;

public class ViewUtil {
    public static void updateReactionButton(Button button, Reactions reactions) {
        button.setText(String.valueOf(reactions.getScore()));
        int colorId = R.color.colorReactionNeutral;
        ReactionType ownReaction = reactions.getOwnReaction();
        if (ownReaction != null) {
            colorId = ownReaction.ordinal() < 3 ? R.color.colorReactionPositive :
                    R.color.colorReactionNegative;
        }
        int color = ContextCompat.getColor(button.getContext(), colorId);
        button.setCompoundDrawableTintList(ColorStateList.valueOf(color));
        button.setTextColor(color);
    }

    public static void showReactionDialog(Context context, FragmentManager fragmentManager,
                                          String postId) {
        showReactionDialog(context, fragmentManager, postId, 0);
    }

    public static void showReactionDialog(Context context, FragmentManager fragmentManager,
                                          String postId, int buttonId) {
        if (!NetworkUtil.getInstance(context).isUserLoggedIn()) {
            Toast.makeText(context, "You must be logged in to react",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ReactDialogFragment dialog = new ReactDialogFragment();
        Bundle args = new Bundle();
        args.putString(ReactDialogFragment.KEY_POST_ID, postId);
        args.putInt(ReactDialogFragment.KEY_BUTTON_ID, buttonId);
        dialog.setArguments(args);
        dialog.show(fragmentManager, context.getClass().getSimpleName());
    }
}
