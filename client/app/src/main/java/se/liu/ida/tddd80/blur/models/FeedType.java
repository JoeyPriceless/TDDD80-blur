package se.liu.ida.tddd80.blur.models;

import org.apache.commons.lang3.StringUtils;

public enum FeedType {
    TOP("top"),
    FUNNY("0"),
    INTERESTING("1"),
    EDUCATIONAL("2");

    private String typeString;

    FeedType(String typeString) {
        this.typeString = typeString;
    }

    /**
     * Returns a capitalized version of the enum. Eg TOP -> Top
     */
    public String toStringCapitalized() {
        return StringUtils.capitalize(name().toLowerCase());
    }

    /**
     * Used by server to determine which feed type to send.
     */
    public String getTypeString() {
        return typeString;
    }
}
