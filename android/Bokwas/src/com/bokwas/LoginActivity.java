package com.bokwas;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.bokwas.apirequests.GetFriendsApi;
import com.bokwas.apirequests.GetPersonInfo;
import com.bokwas.apirequests.GetPosts;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.Friends;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.dialogboxes.GenericDialogOk;
import com.bokwas.dialogboxes.GenericDialogOk.DialogType;
import com.bokwas.util.GCMUtils;
import com.bokwas.util.GeneralUtil;
import com.bokwas.util.TrackerName;
import com.google.android.gms.analytics.Tracker;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.Permission.Type;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
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
		
		setupGoogleAnalytics();

	}
	
	private void setupGoogleAnalytics() {
		Tracker t = GeneralUtil.getTracker(TrackerName.APP_TRACKER,this);
		t.enableAutoActivityTracking(true);
	}

	private void setOnClickListeners() {
		findViewById(R.id.facebookButton).setOnClickListener(this);
		findViewById(R.id.whyFacebookLoginButton).setOnClickListener(this);
	}

	private void moveToProfileScreen() {
		try {
			if(pdia!=null) {
				pdia.cancel();
				pdia.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(this, ProfileChooserActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
		finish();
	}

	@SuppressWarnings("unused")
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
				for (Profile friend : friends) {
					UserDataStore.getStore().getFriends().add(new Friends(friend.getName(), friend.getId(), friend.getPicture(), null, null));
				}
				UserDataStore.getStore().save(LoginActivity.this);
				pdia.dismiss();
				moveToProfileScreen();
			}
		});
	}

	public void getUserData() {
		pdia = new ProgressDialog(this);
		pdia.setMessage("Fetching details...");
		pdia.setCancelable(false);
		pdia.setCanceledOnTouchOutside(false);
		pdia.show();
		Log.d("LoginActivity", "AccessToken : " + mSimpleFacebook.getSession().getAccessToken());
		UserDataStore.getStore().setUserAccessToken(mSimpleFacebook.getSession().getAccessToken());
		UserDataStore.getStore().save(this);

		mSimpleFacebook.getProfile(new OnProfileListener() {

			@Override
			public void onComplete(Profile profile) {
				Log.d(TAG, "FirstName :" + profile.getFirstName());
				Log.d(TAG, "LastName :" + profile.getLastName());
				Log.d(TAG, "Gender :" + profile.getGender());
				Log.d(TAG, "email :" + profile.getEmail());
				Log.d(TAG, "Facebook Id :" + profile.getId());
				getSharedPreferences(GeneralUtil.sharedPreferences, MODE_PRIVATE).edit().putString(GeneralUtil.userGender, profile.getGender()).commit();
				// getFriendsAndStore();
				UserDataStore.getStore().setUserId(profile.getId());
				UserDataStore.getStore().setFbName(profile.getName());
				UserDataStore.getStore().setFbPicLink(profile.getPicture());
				UserDataStore.getStore().save(LoginActivity.this);
				checkIfReturningUser();
			}

			@Override
			public void onFail(String reason) {
				pdia.dismiss();
				Toast.makeText(LoginActivity.this, "Something went wrongbecause " + reason + ". Try again", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onException(Throwable throwable) {
				pdia.dismiss();
				Toast.makeText(LoginActivity.this, "Something went wrongbecause " + throwable.getMessage() + ". Try again", Toast.LENGTH_SHORT).show();
			}

		});
	}

	protected void checkIfReturningUser() {
		new GetPersonInfo(this, UserDataStore.getStore().getUserId(), new com.bokwas.apirequests.GetPersonInfo.APIListener() {
			
			@Override
			public void onAPIStatus(boolean status,String extraMessage) {
				
				if(!status && extraMessage!=null) {
					try {
						if(pdia!=null) {
							pdia.cancel();
							pdia.dismiss();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					GenericDialogOk dialog = new GenericDialogOk(LoginActivity.this, "Bokwas", extraMessage,"Invite",DialogType.DIALOG_INVITE);
					dialog.show();
					return;
				}
				if(status) {
					new GetPosts(LoginActivity.this, UserDataStore.getStore().getUserAccessToken(), UserDataStore.getStore().getBokwasName(), String.valueOf(UserDataStore.getStore().getAvatarId()), UserDataStore
							.getStore().getGcmRegId(), true, new APIListener() {

						@Override
						public void onAPIStatus(boolean status) {
							if (status) {
//								moveToHomePage();
								new GetFriendsApi(LoginActivity.this, UserDataStore.getStore().getAccessKey(), UserDataStore.getStore().getUserId(), new APIListener() {
									
									@Override
									public void onAPIStatus(boolean status) {
										SharedPreferences.Editor editor = getSharedPreferences(GeneralUtil.sharedPreferences, MODE_PRIVATE).edit();
										editor.putBoolean(GeneralUtil.isLoggedInKey, true);
										editor.commit();
										GeneralUtil.setRecurringAlarm(LoginActivity.this);
										moveToHomePage();
									}
								}).execute("");
							} else {
								try {
									if(pdia!=null) {
										pdia.cancel();
										pdia.dismiss();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
//								moveToProfileScreen();
//								Toast.makeText(LoginActivity.this, "Something went wrong. Try again after a while", Toast.LENGTH_SHORT).show();
							}
						}

					}).execute("");
				}else {
					moveToProfileScreen();
				}
			}
		}).execute("");
	}

	@Override
	public void onResume() {
		super.onResume();
		mSimpleFacebook = SimpleFacebook.getInstance(this);
	}
	
	private void moveToHomePage() {
		try {
			if(pdia!=null) {
				pdia.cancel();
				pdia.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		new GetFriendsApi(this, UserDataStore.getStore().getAccessKey(), UserDataStore.getStore().getUserId(), null).execute("");
		Intent intent = new Intent(this, HomescreenActivity.class);
		intent.putExtra("fromSplashscreen", true);
		UserDataStore.getStore().setInit(true);
		UserDataStore.getStore().save(this);
		UserDataStore.getStore().sortPosts();
		startActivity(intent);
		overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
		finish();
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
			Permission[] permissions = new Permission[] { Permission.USER_PHOTOS, Permission.EMAIL, Permission.READ_STREAM, };
			SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder().setAppId("282335065256850").setNamespace("bokwas_android").setPermissions(permissions).build();
			SimpleFacebook.setConfiguration(configuration);
			mSimpleFacebook.login(new OnLoginListener() {

				@Override
				public void onFail(String reason) {
					pdia.dismiss();
					Toast.makeText(LoginActivity.this, "Something went wrong because " + reason + ". Try again", Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onException(Throwable throwable) {
					pdia.dismiss();

					Toast.makeText(LoginActivity.this, "Something went wrongbecause " + throwable.getMessage() + ". Try again", Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onThinking() {

				}

				@Override
				public void onNotAcceptingPermissions(Type type) {
					pdia.dismiss();
					Toast.makeText(LoginActivity.this, "Permissions not given", Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onLogin() {
					pdia.dismiss();
					getUserData();
				}
			});

		} else if (view.getId() == R.id.whyFacebookLoginButton) {
			GenericDialogOk dialog = new GenericDialogOk(this, "Why connect to facebook?",
					"We retrieve only posts from facebook so that you can talk about it on Bokwas. We do NOT post anything back on facebook.","OK",DialogType.DIALOG_GENERIC);
			dialog.show();
		}
	}

}
