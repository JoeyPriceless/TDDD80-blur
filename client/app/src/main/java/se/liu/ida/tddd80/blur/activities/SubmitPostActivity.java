package se.liu.ida.tddd80.blur.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.utilities.GsonUtil;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;
import se.liu.ida.tddd80.blur.utilities.ResponseListeners;
import se.liu.ida.tddd80.blur.utilities.StringUtil;

public class SubmitPostActivity extends AppCompatActivity implements Response.Listener<JSONObject>,
        OnSuccessListener<Location> {
    private static final int IMAGE_REQUEST_CODE = 78;
    private static final int LOCATION_REQUEST_CODE = 99;
    private String unknownLocation;

    private Intent ImageCaptureIntent;
    private FusedLocationProviderClient fusedLocation;

    private NetworkUtil netUtil;
    private EditText etContent;
    private Editable contentEditable;
    private TextView tvCharCount;
    private int maxLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_post);

        setupActionBar();

        unknownLocation = getString(R.string.location_unknown);
        fusedLocation = LocationServices.getFusedLocationProviderClient(this);
        netUtil = NetworkUtil.getInstance(this);
        maxLength = getResources().getInteger(R.integer.post_max_length);

        etContent = findViewById(R.id.edittext_content_submit);
        tvCharCount = findViewById(R.id.textview_charcount_submit);
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

    public void onClickImageButton(View v) {

    }

    public void onClickSetLocation(View v) {
        getLocationifPermitted();
    }

    public void onClickRemoveLocation(View v) {
        Button btnLocation = findViewById(R.id.button_location_submit);
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

    public void getLocationifPermitted() {
        String locationPermission = Manifest.permission.ACCESS_COARSE_LOCATION;
        if (checkSelfPermission(locationPermission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { locationPermission }, LOCATION_REQUEST_CODE);
        } else {
            fusedLocation.getLastLocation().addOnSuccessListener(this);
        }
    }

    @Override public void onRequestPermissionsResult(final int requestCode,
            @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        int result = grantResults[0];
        if (result == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case LOCATION_REQUEST_CODE:
                    getLocationifPermitted();
            }
        } else if (result == PackageManager.PERMISSION_DENIED) {
            switch (requestCode) {
                case LOCATION_REQUEST_CODE:
                    Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean submitPost() {
        if (!netUtil.isUserLoggedIn()) {
            Toast.makeText(this, "Submission unsuccessful. You are not logged in.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        int length = contentEditable.length();
        if (length > maxLength || length == 0) {
            Toast.makeText(this, "Submission must be between 1 and 240 characters.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        netUtil.createPost(contentEditable.toString(), netUtil.getUserId(), this,
                new ResponseListeners.DefaultError(this));
        return true;
    }

    private void refreshCharCount(Editable editable) {
        tvCharCount.setText(String.format("%d/%d", editable.length(), maxLength));
    }

    /**
     * Called by fusedLocation.getLastLocation() upon success.
     */
    @Override
    public void onSuccess(Location location) {
        if (location == null) return;
        Geocoder geo = new Geocoder(this, Locale.getDefault());
        new LocationFetchTask(this).execute(location, geo);
    }

    /**
     * Volley ResponseListener. Get the PostId and launch a PostActivity with the new post.
     */
    @Override
    public void onResponse(JSONObject response) {
        String postId = GsonUtil.getInstance().parseString(response);
        Intent postIntent = new Intent(SubmitPostActivity.this, PostActivity.class);
        postIntent.putExtra(PostActivity.EXTRA_POST_ID, postId);
        // TODO remove this Activity from history so back button doesn't navigate to
        //  SubmitPostActivity
        startActivity(postIntent);
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

    private static class LocationFetchTask extends AsyncTask<Object, Void, String> {
        // Using static class together with WeakReference to avoid memory leaks.
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
            } catch (IOException ex) {
                return activity.getString(R.string.location_unknown);
            }
        }

        @Override protected void onPostExecute(final String s) {
            // Get a strong reference to activity. Don't update button if activity was closed before
            // execution was finished.
            final SubmitPostActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            Button btnLocation = activity.findViewById(R.id.button_location_submit);
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
}