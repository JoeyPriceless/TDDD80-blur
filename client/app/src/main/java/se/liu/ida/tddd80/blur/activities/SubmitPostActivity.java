package se.liu.ida.tddd80.blur.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.fragments.SubmitImageDialogFragment;
import se.liu.ida.tddd80.blur.utilities.FileUtil;
import se.liu.ida.tddd80.blur.utilities.GsonUtil;
import se.liu.ida.tddd80.blur.utilities.ImageRotator;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.ResponseListeners;
import se.liu.ida.tddd80.blur.utilities.StringUtil;
import se.liu.ida.tddd80.blur.utilities.ViewUtil;

public class SubmitPostActivity extends AppCompatActivity implements Response.Listener<JSONObject>,
        OnSuccessListener<Location>, SubmitImageDialogFragment.SubmitImageDialogListener {
    private static final int IMAGE_REQUEST_CODE = 78;
    private static final int LOCATION_REQUEST_CODE = 99;
    private static final int THUMBNAIL_HEIGHT = 400;

    private Intent imageCaptureIntent;
    private Uri imageUri = null;
    private Bitmap bmFullsize = null;
    private FusedLocationProviderClient fusedLocation;
    private LocationCallback locationCallback;
    private static final String[] locationPermissions = new String[] {
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE
    };

    private NetworkUtil netUtil;
    private EditText etContent;
    private Editable contentEditable;
    private TextView tvCharCount;
    private int maxLength;
    private ImageView ivThumbnail;
    private Button btnLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_post);

        setupActionBar();

        imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fusedLocation = LocationServices.getFusedLocationProviderClient(this);
        netUtil = NetworkUtil.getInstance(this);
        maxLength = getResources().getInteger(R.integer.post_max_length);

        etContent = findViewById(R.id.edittext_submit_content);
        tvCharCount = findViewById(R.id.textview_submit_charcount);
        ivThumbnail = findViewById(R.id.imageview_submit_thumbnail);
        btnLocation = findViewById(R.id.button_submit_location);
        etContent.addTextChangedListener(new ContentWatcher());
        // Set content box as focused automatically.
        etContent.requestFocus();
        contentEditable = etContent.getText();

        refreshCharCount(contentEditable);
    }

    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_submit);
        setSupportActionBar(toolbar);
        // Enable ActionBar back button.
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setHomeAsUpIndicator(getDrawable(R.drawable.ic_close_black_24dp));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Submit").setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return submitPost();
                    }
        })
        .setIcon(getDrawable(R.drawable.ic_submit_black_24dp))
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    /**
     * Neccessary for Actionbar back button to work.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override public void onRequestPermissionsResult(final int requestCode,
             @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        int result = grantResults[0];
        if (result == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case LOCATION_REQUEST_CODE:
                    getLocationIfPermitted();
                    break;
                case IMAGE_REQUEST_CODE:
                    openCameraIfPermitted();
            }
        } else if (result == PackageManager.PERMISSION_DENIED) {
            String toastText = "";
            switch (requestCode) {
                case LOCATION_REQUEST_CODE:
                    toastText = "Location permission denied.";
                    break;
                case IMAGE_REQUEST_CODE:
                    toastText = "Camera permission denied.";
                    break;
            }
            if (!toastText.isEmpty())
                Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickImageButton(View v) {
        openCameraIfPermitted();
    }

    /**
     * Starts a camera activity for result if app has camera persmission. Otherwise, ask for
     * permission.
     */
    private void openCameraIfPermitted() {
        String cameraPermission = Manifest.permission.CAMERA;
        if (checkSelfPermission(cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { cameraPermission }, IMAGE_REQUEST_CODE);
        } else {
            // Check if there is any activity registered to open the camera intent.
            if (imageCaptureIntent.resolveActivity(getPackageManager()) == null)
                Toast.makeText(this, "Could not find a camera app to launch.",
                        Toast.LENGTH_SHORT).show();

            // ACTION_IMAGE_CAPTURE returns a low quality thumbnail by default. In order to get the
            // image in full quality, EXTRA_OUTPUT has to be saved on the device.
            imageUri = FileUtil.generateImageUri(this, imageCaptureIntent);
            imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(imageCaptureIntent, IMAGE_REQUEST_CODE);
        }
    }

    /**
     * Handles the picture from the camera activity on finish.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                 bmFullsize = ImageRotator.getImageAndRotate(this, imageUri);
            } catch (IOException ex) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
            // Scale down and maintain aspect ratio
            double aspectRatio = (double)bmFullsize.getWidth() / bmFullsize.getHeight();
            int width = (int)Math.round(THUMBNAIL_HEIGHT * aspectRatio);
            Bitmap bmThumbnail = ThumbnailUtils.extractThumbnail(bmFullsize, width,
                    THUMBNAIL_HEIGHT);
            ivThumbnail.setImageDrawable(ViewUtil.roundCorners(this, bmThumbnail, 15));
        }
    }

    public void onClickImageThumbnail(View v) {
        SubmitImageDialogFragment fragment = new SubmitImageDialogFragment();
        fragment.show(getSupportFragmentManager(), this.getClass().getSimpleName());
    }

    private boolean submitPost() {
        if (!netUtil.isUserLoggedIn()) {
            Toast.makeText(this, "Submission unsuccessful. You are not logged in.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        int length = contentEditable.length();
        if (length > maxLength || length == 0) {
            Toast.makeText(this, "Text must be between 1 and 240 characters.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        String locationString = btnLocation.getText().toString();
        // Return null rather than empty string.
        locationString = locationString.isEmpty() ? null : locationString;
        netUtil.createPost(contentEditable.toString(), netUtil.getUserId(), locationString, this,
                new ResponseListeners.DefaultError(this));
        return true;
    }

    private void refreshCharCount(Editable editable) {
        tvCharCount.setText(String.format("%d/%d", editable.length(), maxLength));
    }

    /**
     * If permitted, starts looking for location as soon as activity is resumed in order to speed up
     * eventual geotagging.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates(false);
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private boolean hasLocationPermissions() {
        for (String permission : locationPermissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public void onClickSetLocation(View v) {
        getLocationIfPermitted();
    }

    @SuppressLint("MissingPermission")
    private void getLocationIfPermitted() {
        if (hasLocationPermissions()) {
            fusedLocation.getLastLocation().addOnSuccessListener(this);
        } else {
            requestPermissions(locationPermissions, LOCATION_REQUEST_CODE);
        }
    }

    public void onClickRemoveLocation(View v) {
        Button btnLocation = findViewById(R.id.button_submit_location);
        btnLocation.setText("");
        Drawable drawableLocAdd = getDrawable(R.drawable.ic_add_location_black_24dp);
        btnLocation.setCompoundDrawablesWithIntrinsicBounds(drawableLocAdd, null, null, null);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSetLocation(v);
            }
        });
    }

    /**
     * Tries to find current location. If successful, post is geotagged.
     * @param actOnUpdates True if the UI/post should be updated when location is found. False is
     *                     to simply get a location for future use.
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates(final boolean actOnUpdates) {
        if (hasLocationPermissions()) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (!actOnUpdates) return;
                    if (locationResult == null || locationResult.getLocations().isEmpty()) {
                        Toast.makeText(SubmitPostActivity.this, "Location unknown",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startLocationProcessingTask(locationResult.getLastLocation());
                }
            };
            LocationRequest locationRequest = LocationRequest.create().setNumUpdates(1);
            fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private void stopLocationUpdates() {
        if (locationCallback != null)
            fusedLocation.removeLocationUpdates(locationCallback);
    }

    /**
     * Called by fusedLocation.getLastLocation() upon success.
     * Last known location may be null, in which case it requests the current location.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onSuccess(Location location) {
        if (location == null) {
            startLocationUpdates(true);
            return;
        }
        startLocationProcessingTask(location);
    }



    private void startLocationProcessingTask(Location location) {
        Geocoder geo = new Geocoder(this, Locale.getDefault());
        new LocationFetchTask(this).execute(location, geo);
    }

    private static class LocationFetchTask extends AsyncTask<Object, Void, String> {
        // Using WeakReference to avoid memory leaks.
        // See https://stackoverflow.com/a/46166223/4400799
        private WeakReference<SubmitPostActivity> activityReference;

        public LocationFetchTask(SubmitPostActivity context) {
            this.activityReference = new WeakReference<>(context);
        }

        /**
         * @param bundle Includes Location and Geocoder, in that order.
         */
        @Override protected String doInBackground(final Object... bundle) {
            final SubmitPostActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return "";

            Location location = (Location) bundle[0];
            Geocoder geo = (Geocoder) bundle[1];
            try {
                List<Address> addresses = geo.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                return StringUtil.getLocationString(activity, addresses);
            } catch (Exception ex) {
                return null;
            }
        }

        @Override protected void onPostExecute(final String s) {
            // Get a strong reference to activity. Don't update button if activity was closed before
            // execution was finished.
            final SubmitPostActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            if (s == null || s.isEmpty()) {
                Toast.makeText(activity, activity.getString(R.string.location_unknown), Toast.LENGTH_SHORT).show();
                return;
            }
            Button btnLocation = activity.findViewById(R.id.button_submit_location);
            btnLocation.setText(s);
            Drawable drawableLocOff = activity.getDrawable(R.drawable.ic_location_off_black_24dp);
            btnLocation.setCompoundDrawablesWithIntrinsicBounds(drawableLocOff, null, null, null);
            btnLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onClickRemoveLocation(v);
                }
            });
        }
    }

    /**
     * Volley ResponseListener. Get the PostId and launch a PostActivity with the new post.
     */
    @Override
    public void onResponse(JSONObject response) {
        final String postId = GsonUtil.getInstance().parseString(response);

        if (bmFullsize == null) {
            continueToPost(postId);
        } else {
            netUtil.sendPostAttachment(postId, bmFullsize, imageUri.getPath(),
                    new ImageResponseListener(postId), new ImageErrorListener(postId));
        }
    }

    private void continueToPost(String postId) {
        Intent postIntent = new Intent(SubmitPostActivity.this, PostActivity.class);
        postIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        postIntent.putExtra(PostActivity.EXTRA_POST_ID, postId);
        startActivity(postIntent);
        finish();
    }

    private class ImageResponseListener implements Response.Listener<String> {
        private String postId;

        public ImageResponseListener(String postId) {
            this.postId = postId;
        }

        @Override
        public void onResponse(String response) {
            continueToPost(postId);
        }
    }

    private class ImageErrorListener implements Response.ErrorListener {
        private String postId;

        public ImageErrorListener(String postId) {
            this.postId = postId;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            continueToPost(postId);
        }
    }
    /**
     * Watches changes to etContent in order to update character counter
     */
    private class ContentWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            refreshCharCount(s);
        }
    }

    /**
     * Listener for SubmitImageDialogFragment
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        imageUri = null;
        ivThumbnail.setImageBitmap(null);
    }
}
