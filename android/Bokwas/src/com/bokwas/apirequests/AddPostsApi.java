package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.AsyncTask;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.AddPostResponse;
import com.bokwas.response.Comment;
import com.bokwas.response.Likes;
import com.bokwas.response.Post;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

public class AddPostsApi extends AsyncTask<String, Void, Boolean> {

	private String accessKey;
	private String personId;
	private String postText;
	private Activity activity;
	private APIListener listener;

	public AddPostsApi(Activity activity, String accessKey, String personId,
			String postText, APIListener listener) {
		super();
		this.activity = activity;
		this.accessKey = accessKey;
		this.personId = personId;
		this.postText = postText;
		this.listener = listener;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair> apiParams = new ArrayList<BasicNameValuePair>();
		apiUrl = AppData.baseURL + "/addpost";
		apiParams.add(new BasicNameValuePair("access_key", accessKey));
		apiParams.add(new BasicNameValuePair("person_id", personId));
		apiParams.add(new BasicNameValuePair("post_text", postText));
		try {
			String response = BokwasHttpClient.postData(apiUrl, apiParams);
			AddPostResponse apiResponse = new Gson().fromJson(response,
					AddPostResponse.class);
			if (apiResponse.status.statusCode == 200) {
				List<Comment> comments = new ArrayList<Comment>();
				List<Likes> likes = new ArrayList<Likes>();
				Post post = new Post(apiResponse.postId,
						System.currentTimeMillis(), System.currentTimeMillis(),
						postText, likes, personId, true, comments,
						UserDataStore.getStore().getBokwasName(),String.valueOf(UserDataStore.getStore().getAvatarId()),"");
				UserDataStore.getStore().addNewPost(post);
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
