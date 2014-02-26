package com.bokwas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.bokwas.util.FacebookHandler;
import com.facebook.Session;

public class LoginActivity extends Activity{
	
	private FacebookHandler fbHandler;
	private String TAG = "LoginActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_page);
		
		findViewById(R.id.textView1).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				fbHandler = new FacebookHandler(LoginActivity.this);
				fbHandler.loginToFacebook();
			}
		});
		
//		fbHandler = new FacebookHandler(this);
//		fbHandler.loginToFacebook();
	}

	@Override
	protected void onPause() {
		Log.d(TAG ,"onPause()");
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d(TAG ,"onResume()");
		super.onResume();
	}

	@Override
	protected void onStart() {
		Log.d(TAG ,"onStart()");
		super.onStart();
	}

	@Override
	protected void onUserLeaveHint() {
		Log.d(TAG ,"onUserLeaveHint()");
		super.onUserLeaveHint();
	}

	@Override
	protected void onStop() {
		Log.d(TAG ,"onStop()");
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG ,"onActivityResult()");
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
//		if(fbHandler!=null) {
//			fbHandler.onActivityResult(requestCode, resultCode, data);
//		}
	}
	
}
