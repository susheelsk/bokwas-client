package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.AsyncTask;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.ApiResponse;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

public class AddLikesApi extends AsyncTask<String, Void, Boolean>{
	
	private String accessKey;
	private String postId;
	private String personId;
	private String postedBy;
	private String commentId;
	private Activity activity;
	private APIListener listener;
	
	public AddLikesApi(Activity activity, String accessKey, String postId, String personId, String postedBy, String commentId,APIListener listener) {
		this.accessKey = accessKey;
		this.postId = postId;
		this.personId = personId;
		this.postedBy = postedBy;
		this.commentId = commentId;
		this.activity = activity;
		this.listener = listener;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair>  apiParams = new ArrayList<BasicNameValuePair>();
		apiUrl = AppData.baseURL + "/addlikes";
		apiParams.add(new BasicNameValuePair("access_key", accessKey));
		apiParams.add(new BasicNameValuePair("post_id", postId));
		apiParams.add(new BasicNameValuePair("person_id", personId));
		apiParams.add(new BasicNameValuePair("post_person_id", postedBy));
		apiParams.add(new BasicNameValuePair("comment_id", commentId));
		
		try {
			String response = BokwasHttpClient.postData(apiUrl, apiParams);
			ApiResponse apiResponse = new Gson().fromJson(
					response, ApiResponse.class);
			if(apiResponse.statusCode == 200) {
				if (commentId == null) {
					UserDataStore.getStore().getPost(postId).setUpdatedTime(System.currentTimeMillis());
					UserDataStore.getStore().getPost(postId).addLikes(String.valueOf(UserDataStore.getStore().getUserId()),UserDataStore.getStore().getBokwasName(),String.valueOf(UserDataStore.getStore().getAvatarId()));
					UserDataStore.getStore().save(activity);
				}else {
					UserDataStore.getStore().getPost(postId).setUpdatedTime(System.currentTimeMillis());
					UserDataStore.getStore().getPost(postId).getComment(commentId).addLikes(String.valueOf(UserDataStore.getStore().getUserId()),UserDataStore.getStore().getBokwasName(),String.valueOf(UserDataStore.getStore().getAvatarId()));
					UserDataStore.getStore().save(activity);
				}
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
