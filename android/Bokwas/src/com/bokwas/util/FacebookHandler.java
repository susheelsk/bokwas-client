package com.bokwas.util;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

public class FacebookHandler {
	private Activity activity;
	private String TAG = "Facebook";
	
	public Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	    } else if (state.isClosed()) {
	        Log.i(TAG, "Logged out...");
	    }
	}

	public FacebookHandler(Activity activity) {
		this.activity = activity;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 Session.getActiveSession().onActivityResult(activity, requestCode, resultCode, data);
	}

	public void loginToFacebook() {
		Session.openActiveSession(activity, true, new Session.StatusCallback() {
			public void call(Session session, SessionState state,
					Exception exception) {
				if (session.isOpened()) {
					UserDetails.setFbAccessToken(activity, session.getAccessToken());
					Request.executeMeRequestAsync(session,
							new Request.GraphUserCallback() {
								@Override
								public void onCompleted(GraphUser user,
										Response response) {
									if (user != null) {
										Log.d(TAG,"User Name :"+user.getName());
										Log.d(TAG,"User Id :"+user.getId());
										Log.d(TAG,"User Gender :"+user.asMap().get("gender").toString());
										Log.d(TAG,"User email :"+user.asMap().get("email").toString());
										
										UserDetails.setUserEmail(activity, user.asMap().get("email").toString());
										UserDetails.setUserGender(activity, user.asMap().get("gender").toString());
										UserDetails.setUserId(activity, user.getId());
									}
								}
							});
				}
			}
		});
	}
	
	public void getFriendsList() {
		Session.openActiveSession(activity, true, new Session.StatusCallback() {

			@Override
			public void call(Session session, SessionState state,
					Exception exception) {
				if (state.isOpened()) {
			        Log.i(TAG, "Logged in...");
			        Request.executeMyFriendsRequestAsync(session,
			                new GraphUserListCallback() {

			                    @Override
			                    public void onCompleted(List<GraphUser> users,
			                            Response response) {
			                        Log.i("Response JSON", response.toString());
			                        for (int i=0; i<users.size();i++){
			                        	Log.d(TAG,"Friend Name : "+users.get(i).getName());
			                        	Log.d(TAG,"Friend Id : "+users.get(i).getId());
//			                        	Log.d(TAG,"Friend Id : "+users.get(i).getId());
			                        }                           
			                    }
			                });
			    } else if (state.isClosed()) {
			        Log.i(TAG, "Logged out...");
			    }
			}
			
		});
	}
}
