package com.bokwas;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.bokwas.datasets.Friends;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.dialogboxes.GenericDialogOk;
import com.bokwas.util.GCMUtils;
import com.sromku.simple.fb.Permission.Type;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.entities.Profile.Properties;
import com.sromku.simple.fb.listeners.OnFriendsListener;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;

public class LoginActivity extends Activity implements OnClickListener {

	private String TAG = "LoginActivity";
	private SimpleFacebook mSimpleFacebook;
	private ProgressDialog pdia;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_page);

		setOnClickListeners();

	}

	private void setOnClickListeners() {
		findViewById(R.id.facebookButton).setOnClickListener(this);
		findViewById(R.id.whyFacebookLoginButton).setOnClickListener(this);
	}

	private void moveToNextScreen() {
		Intent intent = new Intent(this, ProfileChooserActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_slide_in_left,
				R.anim.activity_slide_out_left);
		finish();
	}

	private void getFriendsAndStore() {
		pdia = new ProgressDialog(this);
		pdia.setCancelable(false);
		pdia.setMessage("Syncing..");
		pdia.show();
		Properties properties = new Properties.Builder().add(Properties.ID).add(Properties.NAME).add(Properties.PICTURE).build();
		mSimpleFacebook.getFriends(properties, new OnFriendsListener() {
			@Override
		    public void onComplete(List<Profile> friends) {
		        Log.i(TAG, "Number of friends = " + friends.size());
		        for(Profile friend : friends) {
		        	UserDataStore.getStore().getFriends().add(new Friends(friend.getName(), friend.getId(), friend.getPicture(), null, null));
		        }
		        UserDataStore.getStore().save(LoginActivity.this);
		        pdia.dismiss();
		        moveToNextScreen();
		    }
		});
	}

	public void getUserData() {
		final ProgressDialog pdia = new ProgressDialog(this);
		pdia.setMessage("Fetching details...");
		pdia.setCancelable(false);
		pdia.setCanceledOnTouchOutside(false);

		UserDataStore.getStore().setUserAccessToken(
				mSimpleFacebook.getSession().getAccessToken());
		UserDataStore.getStore().save(this);

		mSimpleFacebook.getProfile(new OnProfileListener() {

			@Override
			public void onComplete(Profile profile) {
				Log.d(TAG, "FirstName :" + profile.getFirstName());
				Log.d(TAG, "LastName :" + profile.getLastName());
				Log.d(TAG, "Gender :" + profile.getGender());
				Log.d(TAG, "email :" + profile.getEmail());
				Log.d(TAG, "Facebook Id :" + profile.getId());
				getFriendsAndStore();
				UserDataStore.getStore().setUserId(profile.getId());
				UserDataStore.getStore().setFbName(profile.getName());
				UserDataStore.getStore().setFbPicLink(profile.getPicture());
				UserDataStore.getStore().save(LoginActivity.this);
//				moveToNextScreen();
			}

			@Override
			public void onFail(String reason) {
				pdia.dismiss();
				Toast.makeText(LoginActivity.this,
						"Something went wrong. Try again", Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onException(Throwable throwable) {
				pdia.dismiss();
				Toast.makeText(LoginActivity.this,
						"Something went wrong. Try again", Toast.LENGTH_SHORT)
						.show();
			}

		});
	}

	@Override
	public void onResume() {
		super.onResume();
		mSimpleFacebook = SimpleFacebook.getInstance(this);
	}

	//
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.facebookButton) {
			final ProgressDialog pdia = new ProgressDialog(this);
			pdia.setMessage("Logging in...");
			pdia.setCancelable(false);
			pdia.setCanceledOnTouchOutside(false);
			if (GCMUtils.checkPlayServices(this)) {
	            GCMUtils.getRegistrationId(this);
	        } else {
	            Log.d(TAG, "No valid Google Play Services APK found.");
	        }
			mSimpleFacebook.login(new OnLoginListener() {

				@Override
				public void onFail(String reason) {
					pdia.dismiss();
					Toast.makeText(LoginActivity.this,
							"Something went wrong. Try again",
							Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onException(Throwable throwable) {
					pdia.dismiss();
					Toast.makeText(LoginActivity.this,
							"Something went wrong. Try again",
							Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onThinking() {

				}

				@Override
				public void onNotAcceptingPermissions(Type type) {
					pdia.dismiss();
					Toast.makeText(LoginActivity.this, "Permissions not given",
							Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onLogin() {
					pdia.dismiss();
					getUserData();
				}
			});

		} else if (view.getId() == R.id.whyFacebookLoginButton) {
			GenericDialogOk dialog = new GenericDialogOk(
					this,
					"Why connect to facebook?",
					"We retrieve only posts from facebook so that you can talk about it on Bokwas. We do NOT post anything back on facebook.");
			dialog.show();
		}
	}

}
