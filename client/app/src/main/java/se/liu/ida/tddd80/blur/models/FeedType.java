package se.liu.ida.tddd80.blur.models;

import org.apache.commons.lang3.StringUtils;

public enum FeedType {
    TOP,
    FUNNY,
    INTERESTING,
    EDUCATIONAL;

    /**
     * Returns a capitalized version of the enum. Eg TOP -> Top
     */
    public String toStringCapitalized() {
        return StringUtils.capitalize(name().toLowerCase());
    }
}
