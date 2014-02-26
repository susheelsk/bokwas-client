package com.bokwas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.bokwas.util.UserDetails;

public class SplashScreen extends Activity {

	private int SPLASH_TIME_OUT = 3000;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spash_screen);
		context = this;
		moveToNextPage();
	}

	private void moveToNextPage() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				if (UserDetails.getUserId(context) == null) {
					Intent i = new Intent(SplashScreen.this,
							LoginActivity.class);
					 startActivity(i);
				} else {
					Intent i = new Intent(SplashScreen.this,
							LoginActivity.class);
					 startActivity(i);
					// Intent i = new Intent(SplashScreen.this,
					// MainActivity.class);
					// startActivity(i);
				}
				finish();
			}
		}, SPLASH_TIME_OUT);
	}

}
