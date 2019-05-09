package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.lang.reflect.Array;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.adapters.SpinnerAdapter;
import se.liu.ida.tddd80.blur.fragments.FeedFragment;
import se.liu.ida.tddd80.blur.models.Feed;
import se.liu.ida.tddd80.blur.models.FeedType;
import se.liu.ida.tddd80.blur.models.ReactionType;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;

public class FeedActivity extends AppCompatActivity
        implements FeedFragment.OnFragmentInteractionListener, AdapterView.OnItemSelectedListener {
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
	    if (netUtil.isUserLoggedIn())
	        setLogoutMenuItem();
        else
            setLoginMenuItem();
        return true;
    }

    private void setLoginMenuItem() {
        menu.add(0, 0, 0, "Login").setOnMenuItemClickListener(
            new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    menu.removeItem(item.getItemId());
                    Intent loginIntent = new Intent(FeedActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                    return true;
                }
            });
    }

    private void setLogoutMenuItem() {
        menu.add(0, 0, 0, "Logout").setOnMenuItemClickListener(
            new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    netUtil = NetworkUtil.getInstance(FeedActivity.this);
                    menu.removeItem(item.getItemId());
                    netUtil.logout();
                    setLoginMenuItem();
                    fab.hide();
                    Toast.makeText(FeedActivity.this, "Logged out successfully",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        FeedType selectedType = FeedType.values()[position];
        replaceFragment(selectedType);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override public void onFragmentInteraction(final Uri uri) {
   	}
}