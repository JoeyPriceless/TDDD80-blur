package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.SerializedName;

import java.util.EnumMap;

/**
 * An EnumMap between ReactionTypes and the number of reactions registered.
 */
public class Reactions {
    private EnumMap<ReactionType, Integer> map = new EnumMap<>(ReactionType.class);
    private int score;
    @SerializedName("own_reaction")
    private ReactionType ownReaction;

    public ReactionType getOwnReaction() {
        return ownReaction;
    }

    public int getScore() {
        return score;
    }
}