package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;

import com.bokwas.datasets.Friends;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Post;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

public class FbProfilePicBatchApi extends AsyncTask<String, Void, Boolean> {

	private List<Post> posts;
	private List<String> appscopedIds;
	private ProfilePicDownload profilePicDownload;
	
	public interface ProfilePicDownload {
		public void onDownloadComplete();
	}

	public FbProfilePicBatchApi(List<Post> posts,ProfilePicDownload profilePicDownload) {
		super();
		this.posts = posts;
		this.profilePicDownload = profilePicDownload;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		appscopedIds = new ArrayList<String>();
		for (Post post : posts) {
			Log.d("FbProfilePicBatchApi","PostedBy : "+post.getPostedBy());
			Friends friend = UserDataStore.getStore().getFriend(
					post.getPostedBy());
			int i = 0;
			if (friend == null || friend.getFbPicLink() == null || friend.getFbPicLink().equals("")) {
				appscopedIds.add(post.getPostedBy());
				Log.d("FbProfilePicBatchApi","count : "+i++);
			}
		}

		List<RequestData> requestDatas = new ArrayList<FbProfilePicBatchApi.RequestData>();
		for (String appscopeId : appscopedIds) {
			RequestData requestData = new RequestData();
			requestData.method = "GET";
			requestData.relative_url = appscopeId + "?fields=id,picture";
			requestDatas.add(requestData);
		}

		String requestDataJson = new Gson().toJson(requestDatas);
		Log.d("FbProfilePicBatchApi", "requestDataJson : " + requestDataJson);
		List<BasicNameValuePair>  apiParams = new ArrayList<BasicNameValuePair>();
		apiParams.add(new BasicNameValuePair("access_token",UserDataStore.getStore().getUserAccessToken()));
		apiParams.add(new BasicNameValuePair("batch",requestDataJson));
		try {
			String response = BokwasHttpClient.getData(
					"https://graph.facebook.com/v2.0/",
					apiParams);
			Log.d("FbProfilePicBatchApi", "Response : "+response);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		profilePicDownload.onDownloadComplete();
	}
	
	@SuppressWarnings("unused")
	private class RequestData {
		public String method;
		public String relative_url;
	}

}
