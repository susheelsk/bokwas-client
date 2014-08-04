package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.AsyncTask;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.GetPersonInfoResponse;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

public class GetPersonInfo extends AsyncTask<String, Void, Boolean>{
	
	private Context context;
	private String personId;
	private APIListener listener;

	public GetPersonInfo(Context context, String personId, APIListener listener) {
		super();
		this.context = context;
		this.personId = personId;
		this.listener = listener;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair> apiParams = new ArrayList<BasicNameValuePair>();
		apiUrl = AppData.getBaseURL() + "/getpersoninfo";
		apiParams.add(new BasicNameValuePair("person_id", personId));
		try {
			String response = BokwasHttpClient.postData(apiUrl, apiParams);
			if (response != null) {
				GetPersonInfoResponse getPersonInfoResponse = new Gson().fromJson(response, GetPersonInfoResponse.class);
				if (getPersonInfoResponse.status.statusCode == 200) {
					UserDataStore.getStore().setBokwasName(getPersonInfoResponse.person_details.bokwas_name);
					UserDataStore.getStore().setAvatarId(Integer.valueOf(getPersonInfoResponse.person_details.bokwas_avatar_id));
				}else {
					return false;
				}
				
				UserDataStore.getStore().save(context);
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
