package se.liu.ida.tddd80.blur.models;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.EnumMap;

public class Reactions {
    private EnumMap<ReactionType, Integer> map = new EnumMap<>(ReactionType.class);

    public Reactions(Pair<ReactionType, Integer>... pairs) {
        for (Pair<ReactionType, Integer> pair : pairs) {
            map.put(pair.first, pair.second);
        }
    }

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