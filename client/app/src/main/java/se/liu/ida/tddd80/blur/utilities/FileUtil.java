package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {
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

}
