package com.bokwas;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.bokwas.datasets.UserDataStore;
import com.bokwas.util.GCMUtils;
import com.bokwas.util.GeneralUtil;
import com.bokwas.util.LocalStorage;
import com.bokwas.util.NotificationProgress;
import com.sromku.simple.fb.Permission.Type;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLoginListener;

public class SplashScreen extends Activity {

	private int SPLASH_TIME_OUT = 100;
	private String TAG = "SplashScreen";
	private SimpleFacebook mSimpleFacebook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spash_screen);
		mSimpleFacebook = SimpleFacebook.getInstance(this);
		logKeyHash();
		initAppData();
		init();
	}

	private void init() {
		if (UserDataStore.getStore().getBokwasName() != null) {
			mSimpleFacebook.login(new OnLoginListener() {

				@Override
				public void onFail(String reason) {
					Toast.makeText(getParent(), "Can't login to facebook",
							Toast.LENGTH_SHORT).show();
					SplashScreen.this.finish();
				}

				@Override
				public void onException(Throwable throwable) {

				}

				@Override
				public void onThinking() {

				}

				@Override
				public void onNotAcceptingPermissions(Type type) {

				}

				@Override
				public void onLogin() {
					UserDataStore.getStore().setUserAccessToken(
							mSimpleFacebook.getSession().getAccessToken());
					UserDataStore.getStore().save(SplashScreen.this);
					Log.d("SplashScreen", "AccessToken : "
							+ mSimpleFacebook.getSession().getAccessToken());
					// new GetPosts(SplashScreen.this,
					// mSimpleFacebook.getSession()
					// .getAccessToken(), UserDataStore.getStore()
					// .getUserId(), null).execute("");
					initAppData();
					moveToNextPage();
				}
			});
		} else {
			SPLASH_TIME_OUT = 3000;
			moveToNextPage();
		}
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

	private void initAppData() {
		NotificationProgress.clearNotificationProgress(GeneralUtil.GENERAL_NOTIFICATIONS);
		if (LocalStorage.getObj(this, UserDataStore.class) != null) {
			UserDataStore.setInstance(LocalStorage.getObj(this,
					UserDataStore.class));
			Log.d(TAG, "UserId: " + UserDataStore.getStore().getUserId());
			Log.d(TAG, "BokwasName: "
					+ UserDataStore.getStore().getBokwasName());
			Log.d(TAG, "accessToken: "
					+ UserDataStore.getStore().getUserAccessToken());
			UserDataStore.getStore().sortPosts();
			if (UserDataStore.getStore().isGcmUpdated() == false) {
				GCMUtils.getRegistrationId(this);
			}
		}
	}

	private void logKeyHash() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					"com.facebook.samples.loginhowto",
					PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("KeyHash:",
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {

		} catch (NoSuchAlgorithmException e) {

		}
	}

	private void moveToNextPage() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				if (UserDataStore.getStore().getPosts().size() > 0) {
					Intent i = new Intent(SplashScreen.this,
							HomescreenActivity.class);
					i.putExtra("fromSplashscreen", true);
					startActivity(i);
				} else {
					Intent i = new Intent(SplashScreen.this,
							LoginActivity.class);
					startActivity(i);
					overridePendingTransition(R.anim.activity_slide_in_left,
							R.anim.activity_slide_out_left);
				}
				finish();
			}
		}, SPLASH_TIME_OUT);
	}

}
