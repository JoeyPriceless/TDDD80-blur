package se.liu.ida.tddd80.blur.utilities;

import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.widget.Button;

import se.liu.ida.tddd80.blur.R;
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
}
