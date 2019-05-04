package se.liu.ida.tddd80.blur.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.fragments.FeedFragment;
import se.liu.ida.tddd80.blur.models.FeedType;
import se.liu.ida.tddd80.blur.utilities.NetworkUtil;

public class FeedActivity extends AppCompatActivity
        implements FeedFragment.OnFragmentInteractionListener {
    private Menu menu;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);

		// Initiate feed fragments.
        // TODO TabLayout which 3 fragments
        FeedType feedType = FeedType.HOT;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.framelayout_fragmentholder,
                FeedFragment.newInstance(feedType.toString()));
        transaction.commit();

        // Setup ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar_feed);
        toolbar.setTitle(feedType.toString());
        setSupportActionBar(toolbar);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	    this.menu = menu;
	    NetworkUtil netUtil = NetworkUtil.getInstance(this);
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
                    NetworkUtil netUtil = NetworkUtil.getInstance(FeedActivity.this);
                    menu.removeItem(item.getItemId());
                    netUtil.logout();
                    setLoginMenuItem();
                    Toast.makeText(FeedActivity.this, "Logged out successfully",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
    }

    @Override public void onFragmentInteraction(final Uri uri) {
   	}
}