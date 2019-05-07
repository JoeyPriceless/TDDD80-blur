package se.liu.ida.tddd80.blur.models;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.gson.annotations.SerializedName;

import se.liu.ida.tddd80.blur.R;

public enum ReactionType {
    @SerializedName("null")
    NEUTRAL(R.drawable.neutral_face),

    /**
     * Funny
     * Icon: laughing face
     */
    @SerializedName("0")
    UPVOTE_0(R.drawable.upvote_0),

    /**
     * Intereresting / Wow
     * Icon:
     */
    @SerializedName("1")
    UPVOTE_1(R.drawable.upvote_1),

    /**
     * Informational / Educational
     * Icon: Student hat / student with hat
     */
    @SerializedName("2")
    UPVOTE_2(R.drawable.upvote_2),

    /**
     * Hateful
     * Icon: Angry face
     */
    @SerializedName("3")
    DOWNVOTE_0(R.drawable.downvote_0),

    /**
     * Misleading / Disagree
     * Icon: Confused face / face with question marks
     */
    @SerializedName("4")
    DOWNVOTE_(R.drawable.downvote_1),

    /**
     * Irritated / Unamused
     */
    @SerializedName("5")
    DOWNVOTE_2(R.drawable.downvote_2);

    private int resourceId;

    ReactionType(int resourceId) {
        this.resourceId = resourceId;
    }

    public Drawable getDrawable(Context context) {
        if  (this.equals(ReactionType.NEUTRAL))
            return context.getDrawable(R.drawable.neutral_face);
        else
            return context.getDrawable(resourceId);
    }
}
