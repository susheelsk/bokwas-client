package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.AsyncTask;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.ApiResponse;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

public class UpdateGcmRegId extends AsyncTask<String, Void, Boolean> {

	private String personId;
	private String accessKey;
	private String gcmRegId;
	private APIListener listener;
	private Context context;

	public UpdateGcmRegId(Context context, String personId, String accessKey, String gcmRegId,
			APIListener listener) {
		super();
		this.context = context;
		this.personId = personId;
		this.accessKey = accessKey;
		this.gcmRegId = gcmRegId;
		this.listener = listener;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair> apiParams = new ArrayList<BasicNameValuePair>();
		apiUrl = AppData.baseURL + "/addgcmregid";
		apiParams.add(new BasicNameValuePair("access_key", accessKey));
		apiParams.add(new BasicNameValuePair("person_id", personId));
		apiParams.add(new BasicNameValuePair("gcmregid", gcmRegId));
		try {
			String response = BokwasHttpClient.postData(apiUrl, apiParams);
			ApiResponse apiResponse = new Gson().fromJson(response,
					ApiResponse.class);
			if (apiResponse.statusCode == 200) {
				UserDataStore.getStore().setGcmUpdated(true);
				UserDataStore.getStore().save(context);
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
		if(listener!=null) {
			listener.onAPIStatus(result);
		}
	}

}
