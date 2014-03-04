package com.bokwas;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.bokwas.datasets.UserDataStore;
import com.bokwas.dialogboxes.GenericDialogOk;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

public class LoginActivity extends Activity implements OnClickListener {

	private String TAG = "LoginActivity";
	private UiLifecycleHelper uiHelper;
	private LoginButton authButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(this, callback);
	    uiHelper.onCreate(savedInstanceState);
		setContentView(R.layout.login_page);

		authButton = (LoginButton) findViewById(R.id.facebookButton);
		authButton.setBackgroundResource(R.drawable.fb_login_main_button);
		authButton.setText("");
		authButton.setReadPermissions(Arrays.asList("email", "user_about_me"));
		setOnClickListeners();

	}

	private void setOnClickListeners() {
		// findViewById(R.id.facebookButton).setOnClickListener(this);
		findViewById(R.id.whyFacebookLoginButton).setOnClickListener(this);
	}
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (session.isOpened()) {
			Log.d(TAG,"AccessToken :"+session.getAccessToken());
			UserDataStore.getStore().setUserAccessToken(session.getAccessToken());
			UserDataStore.getStore().save(this);
			Toast.makeText(this, "Logged in...", Toast.LENGTH_SHORT).show();
			authButton.setText("");
			getUserData();
			moveToNextScreen();
		}

	}

	private void moveToNextScreen() {
		Intent intent = new Intent(this, ProfileChooserActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_slide_in_left,
				R.anim.activity_slide_out_left);
		finish();
	}

	public void getUserData() {
		Request.executeMeRequestAsync(Session.getActiveSession(),
				new GraphUserCallback() {

					@Override
					public void onCompleted(GraphUser user, Response response) {

						Log.d(TAG, "FirstName :"+ user.getFirstName());
						Log.d(TAG, "LastName :"+ user.getLastName());
						Log.d(TAG, "Gender :"+ user.getProperty("gender").toString());
						Log.d(TAG, "email :"+ user.getProperty("email").toString());
						Log.d(TAG, "Facebook Id :"+ user.getId());
						UserDataStore.getStore().setUserId(user.getId());
						UserDataStore.getStore().save(LoginActivity.this);
					}
				});
	}

	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.facebookButton) {

		} else if (view.getId() == R.id.whyFacebookLoginButton) {
			GenericDialogOk dialog = new GenericDialogOk(
					this,
					"Why connect to facebook?",
					"We retrieve only posts from facebook so that you can talk about it on Bokwas. We do NOT post anything back on facebook.");
			dialog.show();
		}
	}

}
