package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.Friends;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.GetFriendsResponse;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

public class GetFriendsApi extends AsyncTask<String, Void, Boolean> {
	private Context context;
	private String accessKey;
	private String personId;
	private APIListener listener;
	
	public GetFriendsApi(Context context, String accessKey, String personId, APIListener listener) {
		super();
		this.context = context;
		this.accessKey = accessKey;
		this.personId = personId;
		this.listener = listener;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair> apiParams = new ArrayList<BasicNameValuePair>();
		apiUrl = AppData.getBaseURL() + "/getfriends";
		apiParams.add(new BasicNameValuePair("access_key", accessKey));
		apiParams.add(new BasicNameValuePair("person_id", personId));
		try {
			String response = BokwasHttpClient.postData(apiUrl, apiParams);
			if (response != null) {
				GetFriendsResponse getPostsResponse = new Gson().fromJson(response, GetFriendsResponse.class);
				
				if(getPostsResponse.bokwasfriends!= null) {
					for(Friends friend : getPostsResponse.bokwasfriends) {
						Log.d("BokwasFriends","friendName : "+friend.bokwasName);
					}
					UserDataStore.getStore().setFriends(getPostsResponse.bokwasfriends);
					UserDataStore.getStore().save(context);
				}else {
					return false;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (result) {
			if (listener != null) {
				listener.onAPIStatus(true);
			}
		} else {
			if (listener != null) {
				listener.onAPIStatus(false);
			}
		}
	}

}
