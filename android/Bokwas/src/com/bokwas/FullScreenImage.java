package com.bokwas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class FullScreenImage extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fullscreen_image);
		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		ImageView fullScreenImage = (ImageView) findViewById(R.id.fullscreen_imageview);
		Picasso.with(this).load(url).placeholder(R.drawable.placeholder).into(fullScreenImage);

	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent;
		if(getIntent().getStringExtra("activity")!=null && getIntent().getStringExtra("activity").contains("ProfileActivityExperimental")) {
			intent = new Intent(this, ProfileActivityExperimental.class);
			intent.putExtra("profileId", getIntent().getStringExtra("profileId"));
			intent.putExtra("name", getIntent().getStringExtra("name"));
			if (getIntent().getStringExtra("avatarId")!=null) {
				intent.putExtra("avatarId", getIntent().getStringExtra("avatarId"));
			} else {
				intent.putExtra("fbProfilePic", getIntent().getStringExtra("fbProfilePic"));
			}
		}else {
			intent = new Intent(this, HomescreenActivity.class);
		}
		startActivity(intent);
		overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_right);
		finish();
	}
}