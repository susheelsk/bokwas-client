package com.bokwas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.bokwas.apirequests.GetPosts;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.ui.HomescreenPostsListAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class HomescreenActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_screen);

		setOnClickListeners();

		setupUI();

	}

	private void setupUI() {

		final HomescreenPostsListAdapter adapter = new HomescreenPostsListAdapter(
				this, UserDataStore.getStore().getPosts());
		final PullToRefreshListView listView = (PullToRefreshListView) findViewById(R.id.feed_list);
		listView.setAdapter(adapter);
		// new FbProfilePicBatchApi(
		// UserDataStore.getStore().getPosts(),
		// new com.bokwas.apirequests.FbProfilePicBatchApi.ProfilePicDownload()
		// {
		//
		// @Override
		// public void onDownloadComplete() {
		// adapter.notifyDataSetChanged();
		// }
		// }).execute("");
		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				new GetPosts(HomescreenActivity.this, UserDataStore.getStore()
						.getUserAccessToken(), UserDataStore.getStore()
						.getUserId(), new APIListener() {

					@Override
					public void onAPIStatus(boolean status) {
						listView.onRefreshComplete();
						if (status) {
							adapter.setPosts(UserDataStore.getStore()
									.getPosts());
							adapter.notifyDataSetChanged();
						}
					}
				}).execute("");
			}
		});
		if (getIntent().getBooleanExtra("fromSplashscreen", false)) {
			listView.setRefreshing(true);// pdia.show
		}
	}

	private void setOnClickListeners() {
		findViewById(R.id.newPostButton).setOnClickListener(this);
		findViewById(R.id.overflowButton).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.newPostButton) {
			Intent intent = new Intent(this, NewPostActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.activity_slide_in_left,
					R.anim.activity_slide_out_left);
			finish();
		} else if (view.getId() == R.id.overflowButton) {
			
		}
	}

}
