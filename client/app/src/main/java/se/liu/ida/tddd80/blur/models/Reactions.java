package se.liu.ida.tddd80.blur.models;

import android.util.Pair;

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
    private ReactionType sentiment;

    public Reactions(Pair<ReactionType, Integer>... pairs) {
        for (Pair<ReactionType, Integer> pair : pairs) {
            map.put(pair.first, pair.second);
        }
    }

    public ReactionType getOwnReaction() {
        return ownReaction;
    }

    public ReactionType getSentiment() {
        // TODO implement in response and remove placeholder value
        return sentiment == null ? ReactionType.NEUTRAL : sentiment;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * @param type The type of reaction
     * @return The number of reactions of type
     */
    public int getReactionCount(ReactionType type) {
        Integer value = map.get(type);
        if (value == null) {
            map.put(type, 0);
            return 0;
        } else {
            return value;
        }
    }
}