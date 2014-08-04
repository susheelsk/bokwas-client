package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.ApiResponse;
import com.bokwas.response.GetNotificationResponse;
import com.bokwas.response.Notification;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class GetNotificationsApi extends AsyncTask<String, Void, Boolean>{

	private String accessKey;
	private String personId;
	private APIListener listener;

	public GetNotificationsApi(String accessKey, String personId, APIListener listener) {
		super();
		this.accessKey = accessKey;
		this.personId = personId;
		this.listener = listener;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair>  apiParams = new ArrayList<BasicNameValuePair>();
		apiUrl = AppData.getBaseURL() + "/getnotification";
		apiParams.add(new BasicNameValuePair("access_key", accessKey));
		apiParams.add(new BasicNameValuePair("person_id", personId));
		try {
			String response = BokwasHttpClient.postData(apiUrl, apiParams);
			GetNotificationResponse getNotificationResponse = new Gson().fromJson(
					response, GetNotificationResponse.class);
			ApiResponse apiResponse = getNotificationResponse.status;
			if(apiResponse.statusCode == 200) {
				Log.d("BokwasNotification", "Response : "+response);
				for(Notification notif : getNotificationResponse.notification) {
					notif.setTimestamp(System.currentTimeMillis());
					Log.d("BokwasNotification", "Notification Data : "+notif.getNotification_data().get("postId"));
					UserDataStore.getStore().addNotification(notif);
					UserDataStore.getStore().removeOldNotifications();
				}
			}
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if(listener!=null) {
			listener.onAPIStatus(result);
		}
	}
	
	
}
