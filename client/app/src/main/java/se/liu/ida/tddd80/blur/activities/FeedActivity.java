package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.adapters.SpinnerAdapter;
import se.liu.ida.tddd80.blur.fragments.FeedFragment;
import se.liu.ida.tddd80.blur.fragments.ProfileDialogFragment;
import se.liu.ida.tddd80.blur.models.FeedType;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;

public class FeedActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Menu menu;
    FloatingActionButton fab;
    private NetworkUtil netUtil = NetworkUtil.getInstance(this);


    @Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);

        FeedType feedType = FeedType.TOP;

        setupFAB();
        setupActionBar();

		// Initiate feed fragment
        replaceFragment(feedType);
	}

	private void replaceFragment(FeedType type) {
        FeedFragment feedFragment = FeedFragment.newInstance(type.toString());
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.framelayout_fragmentholder, feedFragment);
        transaction.commit();
    }

	private void setupFAB() {
        // Can only submit post if logged in.
        if (!netUtil.isUserLoggedIn()) return;
        fab = findViewById(R.id.fab_feed);
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent submitPostIntent = new Intent(FeedActivity.this, SubmitPostActivity.class);
                startActivity(submitPostIntent);
            }
        });
    }

    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_feed);
        setSupportActionBar(toolbar);

        // Replace the title with a spinner which controls feed type.
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Spinner toolbarSpinner = findViewById(R.id.spinner_toolbar_feed);
        String[] strings = getResources().getStringArray(R.array.spinner_items);
        Integer[] drawables = new Integer[] { R.drawable.top, R.drawable.upvote_0,
                R.drawable.upvote_1, R.drawable.upvote_2 };
        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.item_toolbar_spinner, strings,
                drawables);
        adapter.setDropDownViewResource(R.layout.item_toolbar_spinner);
        toolbarSpinner.setAdapter(adapter);
        toolbarSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	    this.menu = menu;
	    // Creates a different options menu depending on if user is logged in or not.
	    if (netUtil.isUserLoggedIn())
	        setLoggedInMenu();
        else
            setLoggedOutMenu();
        return true;
    }

    private void setLoggedOutMenu() {
        menu.add("Login").setOnMenuItemClickListener(
            new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent loginIntent = new Intent(FeedActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                    return true;
                }
            }
        );
    }

    private void setLoggedInMenu() {
        menu.clear();
        menu.add("Choose profile picture").setOnMenuItemClickListener(
            new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    ProfileDialogFragment dialog = new ProfileDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(ProfileDialogFragment.KEY_IMAGE_URL,
                            netUtil.getUserPictureUri());
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), getClass().getSimpleName());
                    return true;
                }
            }
        );

        menu.add("Logout").setOnMenuItemClickListener(
            new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    netUtil = NetworkUtil.getInstance(FeedActivity.this);
                    netUtil.logout(new LogoutResponse(), new LogoutErrorResponse());
                    return true;
                }
            }
        );
    }

    private class LogoutResponse implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            netUtil.clearUser();
            invalidateOptionsMenu();
            fab.hide();
            Toast.makeText(FeedActivity.this, "Logged out successfully",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class LogoutErrorResponse implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(FeedActivity.this, "Failed to log out",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Listener for toolbar spinner
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        FeedType selectedType = FeedType.values()[position];
        replaceFragment(selectedType);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}