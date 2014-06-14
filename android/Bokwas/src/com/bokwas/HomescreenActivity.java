package com.bokwas;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokwas.apirequests.GetPosts;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Likes;
import com.bokwas.response.Post;
import com.bokwas.ui.HomescreenPostsListAdapter;
import com.bokwas.ui.HomescreenPostsListAdapter.PostShare;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class HomescreenActivity extends Activity implements OnClickListener,
		PostShare {

	private PullToRefreshListView listView;
	private HomescreenPostsListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		adapter = new HomescreenPostsListAdapter(this, UserDataStore.getStore()
				.getPosts(), this);
		listView = (PullToRefreshListView) findViewById(R.id.feed_list);
		listView.setAdapter(adapter);
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
			// listView.setRefreshing(true);// pdia.show
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
			PopupMenu popup = new PopupMenu(HomescreenActivity.this, view);
			popup.getMenuInflater().inflate(R.menu.overflow_menu,
					popup.getMenu());
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {
					case R.id.overflow_home:

						break;
					case R.id.overflow_profile:
						break;
					case R.id.oveflow_newpost:
						Intent intent1 = new Intent(HomescreenActivity.this,
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
							GeneralUtil.sharePhotoIntent(
									HomescreenActivity.this, bitmap,
									"Check out what I saw on Bokwas");

						} catch (Exception e) {
							Crouton.makeText(HomescreenActivity.this,
									"Post couldn't be shared. Try again",
									Style.ALERT).show();
							e.printStackTrace();
						}
					}

				});
	}

	private View getViewToShare(int position) {
		final View view = LayoutInflater.from(this).inflate(
				R.layout.post_list_item_new, null);
		// Do some stuff to the view, like add an ImageView, etc.
		Post post = adapter.getItem(position);
		((TextView) view.findViewById(R.id.post_content)).setText(post
				.getPostText());

		Date date = new Date(post.getTimestamp());
		String dateString = DateUtil.formatToYesterdayOrToday(date);
		((TextView) view.findViewById(R.id.post_time)).setText(dateString);
		((TextView) view.findViewById(R.id.post_comment_number)).setText(String
				.valueOf(post.getComments().size()));
		List<Likes> likes = post.getLikes();
		if (likes.size() > 0) {
			((TextView) view.findViewById(R.id.post_like_number))
					.setText(String.valueOf(likes.size()));
		} else {
			((TextView) view.findViewById(R.id.post_like_number))
					.setText(String.valueOf(0));
		}

		if (post.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			((RelativeLayout) view.findViewById(R.id.post_like_button))
					.findViewById(R.id.like_image).setBackgroundResource(
							R.drawable.facebook_icon_enable);
		} else {
			((RelativeLayout) view.findViewById(R.id.post_like_button))
					.findViewById(R.id.like_image).setBackgroundResource(
							R.drawable.like_icon);
		}

		if (post.isBokwasPost()) {
			((TextView) view.findViewById(R.id.post_name)).setText(post
					.getName());
			String avatarId = post.getAvatarId();
			((ImageView) view.findViewById(R.id.post_profile_pic))
					.setImageBitmap(GeneralUtil.getImageBitmap(
							GeneralUtil.getAvatarResourceId(avatarId), this));
		} else {
			String url = post.getProfilePicture();
			((TextView) view.findViewById(R.id.post_name)).setText(post
					.getName());
			UrlImageViewHelper.setUrlDrawable(
					((ImageView) view.findViewById(R.id.post_profile_pic)),
					url, null, 60000 * 100);
		}
		view.measure(view.getWidth(), view.getHeight());
		View childView = adapter.getView(position, null, listView);
		childView.measure(
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		childView.layout(0, 0, listView.getWidth(),
				childView.getMeasuredHeight());
		return childView;
	}

}
