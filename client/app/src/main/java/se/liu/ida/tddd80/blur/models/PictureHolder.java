package se.liu.ida.tddd80.blur.models;

import com.google.gson.annotations.SerializedName;

public abstract class PictureHolder {
    @SerializedName("picture_path")
    protected String picturePath = null;
    protected String localPicturePath;
}
