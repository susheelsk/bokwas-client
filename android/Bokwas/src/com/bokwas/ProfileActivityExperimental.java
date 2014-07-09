package com.bokwas;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.achep.header2actionbar.FadingActionBarHelper;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.apirequests.GetPostsOfPersonApi;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Post;
import com.bokwas.ui.ProfilePageFragment;

public class ProfileActivityExperimental extends Activity implements OnClickListener {

	private FadingActionBarHelper mFadingActionBarHelper;
	private String profileId;
	private String name;
	private int avatarId;
	private String fbProfilePic;
	private boolean isBokwasPost = false;
	private Bundle savedInstanceState;
	private ProgressDialog pdia;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_activity);
		this.savedInstanceState = savedInstanceState;
		pdia = new ProgressDialog(this);
		
		profileId = getIntent().getStringExtra("profileId");
		name = getIntent().getStringExtra("name");
		avatarId = getIntent().getIntExtra("avatarId", -1);
		fbProfilePic = getIntent().getStringExtra("fbProfilePic");
		if (profileId == null || profileId.equals("") || name == null || name.equals("") || (avatarId == -1 && (fbProfilePic == null || fbProfilePic.equals("")))) {
			Toast.makeText(this, "Can't find the person. Please try again", Toast.LENGTH_SHORT).show();
			onBackPressed();
			return;
		}
		
		if(fbProfilePic == null || fbProfilePic.equals("")) {
			isBokwasPost = true;
		}else {
			isBokwasPost = false;
		}
		
		if(isBokwasPost) {
			if(UserDataStore.getStore().getBokwasPostsOfPerson(profileId).size()<0) {
				pdia.setCancelable(false);
				pdia.setMessage("Loading");
				pdia.show();
				refreshPosts();
			}
		}else {
			if(UserDataStore.getStore().getFbPostsOfPerson(profileId).size()<0) {
				pdia.setCancelable(false);
				pdia.setMessage("Loading");
				pdia.show();
				refreshPosts();
			}
		}

		setupUI();

	}

	private void setupUI() {
		getActionBar().setIcon(null);
		getActionBar().setTitle("bokwas");
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F99238")));
		mFadingActionBarHelper = new FadingActionBarHelper(getActionBar(), getResources().getDrawable(R.drawable.actionbar_bg));

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new ProfilePageFragment()).commit();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(this, HomescreenActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_right);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile_overflow, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.overflow_refresh) { // open repo at GitHub
			refreshPosts();
			return true;
		}else if(id == R.id.oveflow_home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void refreshPosts() {
		Toast.makeText(this, "Loading more posts", Toast.LENGTH_SHORT).show();
		long since;
		List<Post> posts = new ArrayList<Post>();
		if(isBokwasPost) {
			posts = UserDataStore.getStore().getBokwasPostsOfPerson(profileId);
			since = posts.get(posts.size()-1).getTimestamp();
		}else {
			posts = UserDataStore.getStore().getFbPostsOfPerson(profileId);
			since = posts.get(posts.size()-1).getTimestamp();
		}
		new GetPostsOfPersonApi(this, UserDataStore.getStore().getAccessKey(), String.valueOf(since), isBokwasPost, UserDataStore.getStore().getUserId(),profileId, new APIListener() {
			
			@Override
			public void onAPIStatus(boolean status) {
				if(status) {
//					setupUI();
					if(pdia!=null && pdia.isShowing()) {
						pdia.dismiss();
					}
					getFragmentManager().findFragmentById(R.id.container).onAttach(ProfileActivityExperimental.this);
				}
			}
		}).execute("");
	}

	public FadingActionBarHelper getFadingActionBarHelper() {
		return mFadingActionBarHelper;
	}

	@Override
	public void onClick(View view) {
		
	}

}
