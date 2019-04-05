package se.liu.ida.tddd80.blur.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import se.liu.ida.tddd80.blur.R;
import se.liu.ida.tddd80.blur.models.Post;


public class PostActivity extends AppCompatActivity {
	private Post post;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);
	}
}