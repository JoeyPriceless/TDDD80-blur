package se.liu.ida.tddd80.blur.models;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

public class Comment extends RecyclerView.ViewHolder {

    private String id;
    private User author;
    private String content;
    @SerializedName("time_created")
    private DateTime timeCreated; // Check json compatibility
    private int score;
    @SerializedName("own_score")
    private int ownScore;

    public Comment(@NonNull View itemView) {
        super(itemView);
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public DateTime getTimeCreated() {
        return timeCreated;
    }
}
