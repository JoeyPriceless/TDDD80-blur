package se.liu.ida.tddd80.blur.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.squareup.picasso.Picasso;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.utilities.FileUtil;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.ViewUtil;

import static android.app.Activity.RESULT_OK;

public class ProfileDialogFragment extends DialogFragment {
    public static String KEY_IMAGE_URL = "IMAGE_URL";

    private static final int IMAGE_REQUEST_CODE = 78;

    private Intent imageCaptureIntent;
    private ProfileDialogListener listener;
    private NetworkUtil netUtil;
    private Picasso picasso = Picasso.get();
    private String imageUrl;
    private Uri imageUri;

    public interface ProfileDialogListener {
        void onSetPicture(ProfileDialogFragment dialog);
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        if (args != null) {
            imageUrl = args.getString(KEY_IMAGE_URL);
        }
    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        LayoutInflater inflater = requireActivity().getLayoutInflater();
//        builder.setView(inflater.inflate(R.layout.dialog_profile_picture, null))
//                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dismiss();
//                    }
//                })
//                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dismiss();
//                    }
//                });
//        return builder.create();
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_profile_picture, container);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView ivUnblurred = view.findViewById(R.id.imageview_profiledialog_unblurred);
        ImageView ivBlurred = view.findViewById(R.id.imageview_profiledialog_blurred);
        ViewUtil.loadProfileImage(picasso, imageUrl, false, ivUnblurred);
        ViewUtil.loadProfileImage(picasso, imageUrl, true, ivBlurred);

        Button pictureButton = view.findViewById(R.id.button_profiledialog_choose);
        pictureButton.setOnClickListener(new OnClickChoosePicture());
    }

    private class OnClickChoosePicture implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Intent chooserIntent = Intent.createChooser(galleryIntent, "Choose photo app");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { imageCaptureIntent });

            // Check if there is any activity registered to open the camera intent.
            Context context = getContext();
            if (chooserIntent.resolveActivity(context.getPackageManager()) == null)
                Toast.makeText(context, "Could not find a camera app to launch.",
                        Toast.LENGTH_SHORT).show();
            imageUri = FileUtil.generateImageUri(context, imageCaptureIntent);
            imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(chooserIntent, IMAGE_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Context context = getContext();
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

            } else if (context.checkSelfPermission(cameraPermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { cameraPermission }, IMAGE_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Force dialog width to match parent.
        Window window = getDialog().getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ProfileDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.getClass().getSimpleName()
                    + " must implement ReactDialogListener");
        }
    }
}
