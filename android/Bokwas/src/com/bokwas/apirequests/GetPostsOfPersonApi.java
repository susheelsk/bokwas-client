package com.bokwas.apirequests;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.GetPostsResponse;
import com.bokwas.response.Post;
import com.bokwas.util.AppData;
import com.bokwas.util.BokwasHttpClient;
import com.google.gson.Gson;

public class GetPostsOfPersonApi extends AsyncTask<String, Void, Boolean> {
	private Context context;
	private String accessKey;
	private String personId;
	private String since;
	private String isbokwaspost;
	private String postPersonId;
	private APIListener postsListener;
	private OnGetPostsOfPerson onGetPostsOfPerson;
	private List<Post> posts;
	
	public interface OnGetPostsOfPerson {
		public void onGetPostsOfPerson(List<Post> posts);
	}

	public GetPostsOfPersonApi(Context context, String accessKey, String since, boolean isBokwasPost, String personId, String postPersonId, APIListener postsListener) {
		this.context = context;
		this.accessKey = accessKey;
		this.personId = personId;
		this.postPersonId = postPersonId;
		this.since = since;
		if (isBokwasPost) {
			this.isbokwaspost = "true";
		} else {
			this.isbokwaspost = "false";
		}

		this.postsListener = postsListener;
	}
	
	public GetPostsOfPersonApi(Activity context, String accessKey, String since, boolean isBokwasPost, String personId, String postPersonId, OnGetPostsOfPerson onGetPostsOfPerson) {
		this.context = context;
		this.accessKey = accessKey;
		this.personId = personId;
		this.postPersonId = postPersonId;
		this.since = since;
		if (isBokwasPost) {
			this.isbokwaspost = "true";
		} else {
			this.isbokwaspost = "false";
		}

		this.onGetPostsOfPerson = onGetPostsOfPerson;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String apiUrl = null;
		List<BasicNameValuePair> apiParams = new ArrayList<BasicNameValuePair>();
		apiUrl = AppData.getBaseURL() + "/getpostsperson";
		apiParams.add(new BasicNameValuePair("access_key", accessKey));
		apiParams.add(new BasicNameValuePair("person_id", personId));
		apiParams.add(new BasicNameValuePair("since", since));
		apiParams.add(new BasicNameValuePair("isbokwaspost", isbokwaspost));
		apiParams.add(new BasicNameValuePair("post_person_id", postPersonId));
		try {
			String response = BokwasHttpClient.postData(apiUrl, apiParams);
			if (response != null) {
				GetPostsResponse getPostsResponse = new Gson().fromJson(response, GetPostsResponse.class);
				if (getPostsResponse.getAccess_key() != null) {
					UserDataStore.getStore().setAccessKey(getPostsResponse.getAccess_key());
					Log.d("GetPostsOfPerson", "Access_Key : " + getPostsResponse.getAccess_key());
				}
				List<Post> posts = getPostsResponse.getPosts();
				this.posts = posts;
				Log.d("GetPostsOfPerson", "Posts size : " + posts.size());
				for (Post post : posts) {
					UserDataStore.getStore().addPost(post);
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
			if (postsListener != null) {
				postsListener.onAPIStatus(true);
			}
			if(onGetPostsOfPerson != null) {
				onGetPostsOfPerson.onGetPostsOfPerson(posts);
			}
		} else {
			if (postsListener != null) {
				postsListener.onAPIStatus(false);
			}
		}
	}
}
