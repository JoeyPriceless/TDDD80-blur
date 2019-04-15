package se.liu.ida.tddd80.blur.activities;

import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.fragments.FeedFragment;
import se.liu.ida.tddd80.blur.models.FeedType;

public class FeedActivity extends AppCompatActivity
        implements FeedFragment.OnFragmentInteractionListener{

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.framelayout_fragmentholder,
                FeedFragment.newInstance(FeedType.HOT.toString()));
        transaction.commit();
	}

    @Override public void onFragmentInteraction(final Uri uri) {
   	}
}
