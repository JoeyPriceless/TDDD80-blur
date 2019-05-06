package se.liu.ida.tddd80.blur.models;

import org.apache.commons.lang3.StringUtils;

public enum FeedType {
    HOT;

    /**
     * Returns a capitalized version of the enum. Eg HOT -> Hot
     */
    public String toStringCapitalized() {
        return StringUtils.capitalize(name().toLowerCase());
    }
}
