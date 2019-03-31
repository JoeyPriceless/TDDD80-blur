package se.liu.ida.tddd80.blur.models;

import android.util.Pair;

import java.util.EnumMap;
import java.util.HashMap;

public class Reactions {
    private EnumMap<ReactionType, Integer> map = new EnumMap<>(ReactionType.class);

    public Reactions(Pair<ReactionType, Integer>... pairs) {
        for (Pair<ReactionType, Integer> pair : pairs) {
            map.put(pair.first, pair.second);
        }
    }

    public int getReactionCount(ReactionType type) {
        return map.get(type);
    }
}