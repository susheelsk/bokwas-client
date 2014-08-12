package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.AsyncTask;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.Message;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.GetPrivateMessagesResponse;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

public class GetPrivateMessagesApi extends AsyncTask<String, Void, Boolean> {

	private String personId;
	private String accessKey;
	private APIListener listener;
	private Activity activity;

	public GetPrivateMessagesApi(Activity activity, String personId, String accessKey, APIListener listener) {
		super();
		this.personId = personId;
		this.accessKey = accessKey;
		this.listener = listener;
		this.activity = activity;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair> apiParams = new ArrayList<BasicNameValuePair>();
		apiUrl = AppData.getBaseURL() + "/getprivatemessage";
		apiParams.add(new BasicNameValuePair("access_key", accessKey));
		apiParams.add(new BasicNameValuePair("person_id", personId));
		try {
			String response = BokwasHttpClient.postData(apiUrl, apiParams);
			GetPrivateMessagesResponse apiResponse = new Gson().fromJson(response, GetPrivateMessagesResponse.class);
			if (apiResponse.status.statusCode == 200) {
				for (GetPrivateMessagesResponse.Message privateMessage : apiResponse.messages) {
					Message message = new Message(privateMessage.messageFromId, UserDataStore.getStore().getUserId(), Long.valueOf(privateMessage.messageTime), privateMessage.messageText,
							privateMessage.messageId, false);
					UserDataStore.getStore().addMessageToPerson(privateMessage.messageFromId, message);
				}
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
