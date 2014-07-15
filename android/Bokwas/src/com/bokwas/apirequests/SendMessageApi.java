package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.Message;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.ApiResponse;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

public class SendMessageApi extends AsyncTask<String, Void, Boolean> {

	private String personId;
	private String receiverId;
	private String accessKey;
	private String message;
	private APIListener listener;
	private Activity activity;

	public SendMessageApi(Activity activity, String personId, String receiverId, String accessKey, String message, APIListener listener) {
		super();
		this.personId = personId;
		this.receiverId = receiverId;
		this.accessKey = accessKey;
		this.message = message;
		this.activity = activity;
		this.listener = listener;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair> apiParams = new ArrayList<BasicNameValuePair>();
		apiUrl = AppData.baseURL + "/sendgcm";
		apiParams.add(new BasicNameValuePair("access_key", accessKey));
		apiParams.add(new BasicNameValuePair("person_id", personId));
		apiParams.add(new BasicNameValuePair("receiver_id", receiverId));
		apiParams.add(new BasicNameValuePair("message", message));
		try {
			String response = BokwasHttpClient.postData(apiUrl, apiParams);
			Log.d("SendMessageApi","Response : "+response);			
			ApiResponse apiResponse = new Gson().fromJson(response, ApiResponse.class);
			if (apiResponse.statusCode == 200) {
				Message messageData = new Message(personId, receiverId, System.currentTimeMillis(), message);
				UserDataStore.getStore().addMessageToPerson(receiverId, messageData);
				UserDataStore.getStore().save(activity);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (listener != null) {
			listener.onAPIStatus(result);
		}
	}

}
