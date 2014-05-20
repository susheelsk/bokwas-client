package com.bokwas.apirequests;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;

import com.bokwas.datasets.Friends;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Post;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FbProfilePicBatchApi extends AsyncTask<String, Void, Boolean> {

	private List<Post> posts;
	private List<String> appscopedIds;
	private ProfilePicDownload profilePicDownload;

	public interface ProfilePicDownload {
		public void onDownloadComplete();
	}

	public FbProfilePicBatchApi(List<Post> posts,
			ProfilePicDownload profilePicDownload) {
		super();
		this.posts = posts;
		this.profilePicDownload = profilePicDownload;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		appscopedIds = new ArrayList<String>();
		int i = 0;
		for (Post post : posts) {
			Log.d("FbProfilePicBatchApi", "PostedBy : " + post.getPostedBy());
			Friends friend = UserDataStore.getStore().getFriend(
					post.getPostedBy());
			if (friend == null || friend.getFbPicLink() == null
					|| friend.getFbPicLink().equals("")) {
				appscopedIds.add(post.getPostedBy());
				i++;
				Log.d("FbProfilePicBatchApi", "count : " + i);
			}
		}

		List<RequestData> requestDatas = new ArrayList<FbProfilePicBatchApi.RequestData>();
		for (String appscopeId : appscopedIds) {
			RequestData requestData = new RequestData();
			requestData.method = "GET";
			requestData.relative_url = appscopeId + "?fields=id,picture,name";
			requestDatas.add(requestData);
		}

		if (requestDatas.size() < 1) {
			return null;
		}

		String requestDataJson = new Gson().toJson(requestDatas);
		Log.d("FbProfilePicBatchApi", "requestDataJson : " + requestDataJson);
		List<BasicNameValuePair> apiParams = new ArrayList<BasicNameValuePair>();
		apiParams.add(new BasicNameValuePair("access_token", UserDataStore
				.getStore().getUserAccessToken()));
		apiParams.add(new BasicNameValuePair("method", "POST"));
		String paramString = URLEncodedUtils.format(apiParams, "utf-8");
		String url = "https://graph.facebook.com/v2.0/?";
		url += paramString;
		try {
			url += "&batch=" + URLEncoder.encode(requestDataJson, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		Log.d("FbProfilePicBatchApi", "Url : " + url);
		try {
			String responseString = BokwasHttpClient.getData(url, null);
			Log.d("FbProfilePicBatchApi", "Response : " + responseString);
			TypeToken<List<FbProfilePicBatchResponse>> token = new TypeToken<List<FbProfilePicBatchResponse>>() {
			};
			List<FbProfilePicBatchResponse> responseItems = new Gson()
					.fromJson(responseString, token.getType());

			for (FbProfilePicBatchResponse responseItem : responseItems) {
				Body body = new Gson().fromJson(responseItem.body, Body.class);
				Log.d("FbProfilePicBatchApi", "id : " + body.id + " ; "
						+ "url : " + body.picture.data.url);
				if (UserDataStore.getStore().getFriend(body.id) == null) {
					UserDataStore
							.getStore()
							.getFriends()
							.add(new Friends(body.name, body.id,
									body.picture.data.url, "", ""));
				} else {
					UserDataStore.getStore().getFriend(body.id)
							.setFbName(body.name);
					UserDataStore.getStore().getFriend(body.id)
							.setFbPicLink(body.picture.data.url);
				}
			}

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
