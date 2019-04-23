package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.SerializedName;

public enum ReactionType {
    @SerializedName("0")
    UPVOTE_0,
    @SerializedName("1")
    UPVOTE_1,
    @SerializedName("2")
    UPVOTE_2,
    @SerializedName("3")
    DOWNVOTE_0,
    @SerializedName("4")
    DOWNVOTE_1,
    @SerializedName("5")
    DOWNVOTE_2
}
