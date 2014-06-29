package com.bokwas;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.bokwas.apirequests.GetPosts;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Post;
import com.bokwas.ui.HomescreenPostsListAdapter;
import com.bokwas.ui.HomescreenPostsListAdapter.PostShare;
import com.bokwas.util.GeneralUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ProfileActivity extends Activity implements OnClickListener,
		PostShare {

	private PullToRefreshListView listView;
	private HomescreenPostsListAdapter adapter;
	private String personId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		personId = getIntent().getStringExtra("personId");
		setContentView(R.layout.home_screen);

		setOnClickListeners();

		setupUI();

	}

	@Override
	protected void onResume() {
		super.onResume();
		setupUI();
	}

	private void setupUI() {
		List<Post> userPosts = UserDataStore.getStore().getBokwasPostsOfPerson(
				personId);
		Log.d("ProfileActivity","Posts : "+userPosts.size());
		adapter = new HomescreenPostsListAdapter(this, userPosts, this);
		listView = (PullToRefreshListView) findViewById(R.id.feed_list);
		listView.setAdapter(adapter);
		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});
		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				new GetPosts(ProfileActivity.this, UserDataStore.getStore()
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

//		listView.setOnLoadMoreListener(new OnLoadMoreListener() {
//
//			@Override
//			public void onLoadMore() {
//				long timestamp = UserDataStore.getStore().getPosts()
//						.get(UserDataStore.getStore().getPosts().size() - 1)
//						.getTimestamp();
//				new GetNewPosts(ProfileActivity.this, UserDataStore.getStore()
//						.getAccessKey(), UserDataStore.getStore()
//						.getUserAccessToken(), UserDataStore.getStore()
//						.getUserId(), timestamp, new APIListener() {
//
//					@Override
//					public void onAPIStatus(boolean status) {
//						listView.onLoadMoreComplete();
//						if (status) {
//							adapter.setPosts(UserDataStore.getStore()
//									.getPosts());
//							adapter.notifyDataSetChanged();
//						}
//					}
//				}).execute("");
//			}
//		});
		// listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
		// @Override
		// public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		// new GetPosts(HomescreenActivity.this, UserDataStore.getStore()
		// .getUserAccessToken(), UserDataStore.getStore()
		// .getUserId(), new APIListener() {
		//
		// @Override
		// public void onAPIStatus(boolean status) {
		// listView.onRefreshComplete();
		// if (status) {
		// adapter.setPosts(UserDataStore.getStore()
		// .getPosts());
		// adapter.notifyDataSetChanged();
		// }
		// }
		// }).execute("");
		// }
		// });
		if (getIntent().getBooleanExtra("fromSplashscreen", false)) {
			listView.setRefreshing(true);
		}
	}

	private void setOnClickListeners() {
		// findViewById(R.id.newPostButton).setOnClickListener(this);
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
			PopupMenu popup = new PopupMenu(ProfileActivity.this, view);
			popup.getMenuInflater().inflate(R.menu.overflow_menu,
					popup.getMenu());
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {
					case R.id.overflow_refresh:
						listView.setRefreshing(true);
						break;
					case R.id.overflow_profile:
						break;
					case R.id.oveflow_newpost:
						Intent intent1 = new Intent(ProfileActivity.this,
								NewPostActivity.class);
						startActivity(intent1);
						overridePendingTransition(
								R.anim.activity_slide_in_left,
								R.anim.activity_slide_out_left);
						finish();
						break;
					}
					return true;
				}
			});
			popup.show();
		}
	}

	@Override
	public void onPostShare(int position) {
		final View view = adapter.getView(position, null, listView);
		((RelativeLayout) findViewById(R.id.hidden_view)).addView(view);
		view.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						view.findViewById(R.id.overflowButton).setVisibility(
								View.INVISIBLE);
						view.layout(0, 0, listView.getWidth(), view.getHeight());
						view.getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
						view.setVisibility(View.GONE);
						try {
							Bitmap bitmap = GeneralUtil
									.loadBitmapFromView(view);
							GeneralUtil.sharePhotoIntent(ProfileActivity.this,
									bitmap, "Check out what I saw on Bokwas");

						} catch (Exception e) {
							Crouton.makeText(ProfileActivity.this,
									"Post couldn't be shared. Try again",
									Style.ALERT).show();
							e.printStackTrace();
						}
					}

				});
	}

}
