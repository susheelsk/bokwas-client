package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.ApiResponse;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class UpdateProfileInfoApi extends AsyncTask<String, Void, Boolean> {

	private String personId;
	private String accessKey;
	private String newName;
	private String newAvatarId;
	private APIListener listener;
	private Activity activity;
	
	private String responseMessage;

	public UpdateProfileInfoApi(Activity activity, String personId, String accessKey, String newName, String newAvatarId, APIListener listener) {
		super();
		this.personId = personId;
		this.accessKey = accessKey;
		this.newName = newName;
		this.newAvatarId = newAvatarId;
		this.listener = listener;
		this.activity = activity;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair> apiParams = new ArrayList<BasicNameValuePair>();
		apiUrl = AppData.getBaseURL() + "/updateprofileinfo";
		apiParams.add(new BasicNameValuePair("access_key", accessKey));
		apiParams.add(new BasicNameValuePair("person_id", personId));
		apiParams.add(new BasicNameValuePair("bokwas_name", newName));
		apiParams.add(new BasicNameValuePair("bokwas_avatar_id", newAvatarId));
		try {
			String response = BokwasHttpClient.postData(apiUrl, apiParams);
			ApiResponse apiResponse = new Gson().fromJson(response, ApiResponse.class);
			if (apiResponse.statusCode == 200) {
				UserDataStore.getStore().setBokwasName(newName);
				UserDataStore.getStore().setAvatarId(Integer.parseInt(newAvatarId));
				UserDataStore.getStore().save(activity);
				return true;
			}else {
				responseMessage = apiResponse.message;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if(listener!=null) {
			if(!result && responseMessage!=null) {
				Log.d("UpdateProfileInfoApi","responseMessage : "+responseMessage);
				Crouton.makeText(activity, responseMessage, Style.ALERT).show();
			}
			listener.onAPIStatus(result);
		}
	}

}
