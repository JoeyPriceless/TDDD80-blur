package se.liu.ida.tddd80.blur.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import se.liu.ida.tddd80.blur.R;

public class HotFeedFragment extends Fragment {
	@Override public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable @Override public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
												 @Nullable final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_feed_hot, container, true);
	}
}
