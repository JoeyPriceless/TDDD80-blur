package se.liu.ida.tddd80.blur.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import se.liu.ida.tddd80.blur.R;

public class FileUtil {
    private static File createImageFile(Context context) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String extension = ".jpg";
        return createImageFile(context, timestamp, extension);
    }

    private static File createImageFile(Context context, String filename, String extension)
            throws IOException {
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(filename, extension, directory);
    }

    public static Uri generateImageUri(Context context, Intent imageCaptureIntent) {
        Uri imageUri = null;
        File imageFile = null;
        try {
            imageFile = FileUtil.createImageFile(context);
        } catch (IOException ex) {
            Toast.makeText(context, "Could not create image file", Toast.LENGTH_LONG).show();
        }

        if (imageFile != null) {
            String providerAuthority = context.getString(R.string.fileprovider_authority);
            imageUri = FileProvider.getUriForFile(context, providerAuthority, imageFile);

            // Have to add this to gain Uri access
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(
                    imageCaptureIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, imageUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }

        return imageUri;
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
