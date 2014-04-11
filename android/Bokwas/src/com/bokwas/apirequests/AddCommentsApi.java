package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.AsyncTask;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.AddCommentResponse;
import com.bokwas.response.Comment;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

public class AddCommentsApi extends AsyncTask<String, Void, Boolean>{
	
	private String accessToken;
	private String postId;
	private String postPersonId;
	private String commentText;
	private String personId;
	private Activity activity;
	private APIListener listener;

	public AddCommentsApi(String accessToken, String postId,
			String postPersonId, String commentText, String personId,Activity activity,APIListener listener) {
		super();
		this.accessToken = accessToken;
		this.postId = postId;
		this.postPersonId = postPersonId;
		this.commentText = commentText;
		this.personId = personId;
		this.activity = activity;
		this.listener = listener;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair>  apiParams = new ArrayList<BasicNameValuePair>();
		apiUrl = AppData.baseURL + "/addcomment";
		apiParams.add(new BasicNameValuePair("access_token", accessToken));
		apiParams.add(new BasicNameValuePair("person_id", personId));
		apiParams.add(new BasicNameValuePair("post_text", commentText));
		apiParams.add(new BasicNameValuePair("post_id", postId));
		apiParams.add(new BasicNameValuePair("comment_text",commentText));
		apiParams.add(new BasicNameValuePair("post_person_id", postPersonId));
		try {
			String response = BokwasHttpClient.postData(apiUrl, apiParams);
			AddCommentResponse apiResponse = new Gson().fromJson(
					response, AddCommentResponse.class);
			if(apiResponse.status.statusCode == 200) {
				UserDataStore.getStore().getPost(postId).setUpdatedTime(System.currentTimeMillis());
				Comment comment = new Comment(apiResponse.commentId, System.currentTimeMillis(), commentText, "", personId);
				UserDataStore.getStore().getPost(postId).addComment(comment);
				UserDataStore.getStore().save(activity);
				return true;
			}
		}catch(Exception e) {
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
