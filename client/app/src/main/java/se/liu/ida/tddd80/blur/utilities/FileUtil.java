package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {
    public static final int IMAGE_QUALITY = 70;

    public static File createImageFile(Context context) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String extension = ".jpg";
        return createImageFile(context, timestamp, extension);
    }

    public static File createImageFile(Context context, String filename, String extension)
            throws IOException {
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(filename, extension, directory);
    }

    public static String encodeImageFile(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}
