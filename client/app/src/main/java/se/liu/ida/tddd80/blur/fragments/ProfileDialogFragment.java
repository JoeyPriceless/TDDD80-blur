package se.liu.ida.tddd80.blur.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.utilities.FileUtil;
import se.liu.ida.tddd80.blur.utilities.ImageUtil;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.ViewUtil;

import static android.app.Activity.RESULT_OK;

public class ProfileDialogFragment extends DialogFragment implements Response.Listener<JSONObject>,
        Response.ErrorListener {
    public static String KEY_IMAGE_URL = "IMAGE_URL";

    private static final int IMAGE_REQUEST_CODE = 78;

    private Intent chooserIntent;
    private Intent imageCaptureIntent;
    private NetworkUtil netUtil;
    private Picasso picasso = Picasso.get();
    private String imageUrl;
    private Uri imageUri;
    private Bitmap imageBitmap;


    private ImageView ivBlurred;
    private ImageView ivUnblurred;

    @Override
    public void setArguments(@Nullable Bundle args) {
        if (args != null) {
            imageUrl = args.getString(KEY_IMAGE_URL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_profile_picture, container);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        netUtil = NetworkUtil.getInstance(getContext());

        // Initiate camera/gallery intent
        imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = FileUtil.generateImageUri(getContext(), imageCaptureIntent);
        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        chooserIntent = Intent.createChooser(galleryIntent, "Choose photo app");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { imageCaptureIntent });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivUnblurred = view.findViewById(R.id.imageview_profiledialog_unblurred);
        ivBlurred = view.findViewById(R.id.imageview_profiledialog_blurred);
        ViewUtil.loadProfileImage(picasso, imageUrl, false, ivUnblurred);
        ViewUtil.loadProfileImage(picasso, imageUrl, true, ivBlurred);

        Button pictureButton = view.findViewById(R.id.button_profiledialog_choose);
        pictureButton.setOnClickListener(new OnClickChoosePicture());

        Button positiveButton = view.findViewById(R.id.button_profiledialog_positive);
        positiveButton.setOnClickListener(new OnClickPositive());

        Button negativeButton = view.findViewById(R.id.button_profiledialog_negative);
        negativeButton.setOnClickListener(new OnClickNegative());

    }

    private void startIntent(Intent intent) {
        // Check if there is any activity registered to open the camera intent.
        Context context = getContext();
        if (chooserIntent.resolveActivity(context.getPackageManager()) == null)
            Toast.makeText(context, "Could not find a camera app to launch.",
                    Toast.LENGTH_SHORT).show();

        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    private class OnClickPositive implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (imageBitmap == null) dismiss();
            netUtil.sendProfilePicture(imageBitmap, ProfileDialogFragment.this,
                    ProfileDialogFragment.this);
            dismiss();
        }
    }

    private class OnClickNegative implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    }

    private class OnClickChoosePicture implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startIntent(chooserIntent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Context context = getContext();
        if (context == null) return;
        String cameraPermission = Manifest.permission.CAMERA;

        if (requestCode == IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = null;
                    if (data != null && data.getData() != null) {
                        uri = data.getData();
                    } else if (imageUri != null) {
                        uri = imageUri;
                    }
                    imageBitmap = ImageUtil.getImageAndRotate(getContext(), uri);
                } catch (IOException ex) {
                    Toast.makeText(getContext(), "Failed to load image\n" + ex.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (imageBitmap != null) {
                    setImage(imageBitmap, ivBlurred, true);
                    setImage(imageBitmap, ivUnblurred, false);
                } else {
                    Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            } else if (context.checkSelfPermission(cameraPermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { cameraPermission }, IMAGE_REQUEST_CODE);
            }
        }
    }

    private void setImage(Bitmap image, ImageView target, boolean applyBlur) {
        if (applyBlur)
            image = ViewUtil.blurBitmap(target.getContext(), image);
        target.setImageBitmap(image);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        int result = grantResults[0];
        if (result == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == IMAGE_REQUEST_CODE) {
                startIntent(imageCaptureIntent);
            }
        } else if (result == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getContext(), "Camera permission denied.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResponse(JSONObject response) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(getContext(), "Image upload failed.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Force dialog width to match parent.
        Window window = getDialog().getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
