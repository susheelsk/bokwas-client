package com.bokwas;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokwas.apirequests.GetFriendsApi;
import com.bokwas.apirequests.GetNewPosts;
import com.bokwas.apirequests.GetNotificationsApi;
import com.bokwas.apirequests.GetPosts;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.dialogboxes.NotificationDialog;
import com.bokwas.response.Likes;
import com.bokwas.response.Post;
import com.bokwas.ui.HomescreenPostsListAdapter;
import com.bokwas.ui.HomescreenPostsListAdapter.PostShare;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class HomescreenActivity extends Activity implements OnClickListener, PostShare {

	private PullToRefreshListView listView;
	private HomescreenPostsListAdapter adapter;
	protected boolean isLoadingLastItems = false;
	private boolean isRefreshed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_screen);

		setOnClickListeners();

		setupUI();

		setupNotificationBar();

		new GetFriendsApi(this, UserDataStore.getStore().getAccessKey(), UserDataStore.getStore().getUserId(), null).execute("");

	}

	@Override
	protected void onResume() {
		super.onResume();
		setupUI();
	}

	private int getPixelsInDp(int sizeInDp) {
		float scale = getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (sizeInDp * scale + 0.5f);
		return dpAsPixels;
	}

	private void setupUI() {
		findViewById(R.id.newPostButton).setVisibility(View.GONE);
		findViewById(R.id.newPostFloat).setVisibility(View.VISIBLE);

		int pixelsInDp = getPixelsInDp(12);
		findViewById(R.id.newPostButton).setPadding(pixelsInDp, pixelsInDp, pixelsInDp, pixelsInDp);

		adapter = new HomescreenPostsListAdapter(this, UserDataStore.getStore().getPosts(), this);
		listView = (PullToRefreshListView) findViewById(R.id.feed_list);
		listView.setAdapter(adapter);

		setupListViewListeners();

		if (getIntent().getBooleanExtra("fromSplashscreen", false) && isRefreshed == false) {
			new GetNotificationsApi(UserDataStore.getStore().getAccessKey(), UserDataStore.getStore().getUserId(), new APIListener() {

				@Override
				public void onAPIStatus(boolean status) {
					 setupNotificationBar();
				}
			}).execute("");
			// listView.setRefreshing();
			isRefreshed = true;
		}

		if (GeneralUtil.listSavedInstanceHomeScreen != null) {
			listView.getRefreshableView().onRestoreInstanceState(GeneralUtil.listSavedInstanceHomeScreen);
		}
	}

	private void setupListViewListeners() {

		listView.setOnScrollListener(new OnScrollListener() {
			private int mLastFirstVisibleItem;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (mLastFirstVisibleItem < firstVisibleItem) {
					findViewById(R.id.newPostFloat).setVisibility(View.GONE);
				}
				if (mLastFirstVisibleItem > firstVisibleItem) {
					findViewById(R.id.newPostFloat).setVisibility(View.VISIBLE);
				}
				mLastFirstVisibleItem = firstVisibleItem;
				GeneralUtil.listSavedInstanceHomeScreen = listView.getRefreshableView().onSaveInstanceState();
			}
		});

		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				new GetPosts(HomescreenActivity.this, UserDataStore.getStore().getUserAccessToken(), UserDataStore.getStore().getUserId(), new APIListener() {

					@Override
					public void onAPIStatus(boolean status) {
						listView.onRefreshComplete();
						if (status) {
							adapter.setPosts(UserDataStore.getStore().getPosts());
							adapter.notifyDataSetChanged();
						}
					}
				}).execute("");
			}
		});

		listView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				if (isLoadingLastItems == false) {
					isLoadingLastItems = true;
					Toast.makeText(HomescreenActivity.this, "Getting more posts!", Toast.LENGTH_SHORT).show();
					long timestamp = UserDataStore.getStore().getPosts().get(UserDataStore.getStore().getPosts().size() - 1).getTimestamp();
					new GetNewPosts(HomescreenActivity.this, UserDataStore.getStore().getAccessKey(), UserDataStore.getStore().getUserAccessToken(), UserDataStore.getStore().getUserId(), timestamp,
							new APIListener() {

								@Override
								public void onAPIStatus(boolean status) {
									if (status) {
										isLoadingLastItems = false;
										adapter.setPosts(UserDataStore.getStore().getPosts());
										adapter.notifyDataSetChanged();
									}
								}

							}).execute("");
				}
			}
		});
	}

	protected void setupNotificationBar() {
		try {
			if (UserDataStore.getStore().getUnseenNotifications().size() > 0) {
				findViewById(R.id.notificationButtonLayout).setVisibility(View.VISIBLE);
				TextView notificationButton = (TextView) findViewById(R.id.notificationButton);
				notificationButton.setText(String.valueOf(UserDataStore.getStore().getUnseenNotifications().size()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setOnClickListeners() {
		findViewById(R.id.overflowButton).setOnClickListener(this);
		findViewById(R.id.messageHeaderButton).setOnClickListener(this);
		findViewById(R.id.newPostButton).setOnClickListener(this);
		findViewById(R.id.newPostFloat).setOnClickListener(this);
		findViewById(R.id.titlebar).setOnClickListener(this);
		findViewById(R.id.notificationButtonLayout).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.newPostButton) {
			Intent intent = new Intent(this, NewPostActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
			finish();
		} else if (view.getId() == R.id.newPostFloat) {
			Intent intent = new Intent(this, NewPostActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
			finish();
		}else if (view.getId() == R.id.overflowButton) {
			PopupMenu popup = new PopupMenu(HomescreenActivity.this, view);
			popup.getMenuInflater().inflate(R.menu.overflow_menu, popup.getMenu());
			MenuItem settingsItem = popup.getMenu().findItem(R.id.overflow_settings);
			settingsItem.setVisible(false);
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {
					case R.id.overflow_refresh:
						listView.setRefreshing(true);
						break;
					case R.id.overflow_profile:
						Intent intent3 = new Intent(HomescreenActivity.this, ProfileActivityExperimental.class);
						intent3.putExtra("profileId", UserDataStore.getStore().getUserId());
						intent3.putExtra("name", UserDataStore.getStore().getBokwasName());
						intent3.putExtra("avatarId", UserDataStore.getStore().getAvatarId());
						startActivity(intent3);
						overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
						finish();
						break;
					case R.id.overflow_settings:
						Intent intent2 = new Intent(HomescreenActivity.this, SettingsActivity.class);
						startActivity(intent2);
						overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
						finish();
						break;
					case R.id.oveflow_newpost:
						Intent intent1 = new Intent(HomescreenActivity.this, NewPostActivity.class);
						startActivity(intent1);
						overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
						finish();
						break;
					case R.id.overflow_feedback:
						sendEmail("susheel@bokwas.com", "Feedback");
						break;
					case R.id.overflow_notifications:
						if (UserDataStore.getStore().getNotifications().size() > 0) {
							NotificationDialog dialog = new NotificationDialog(HomescreenActivity.this, UserDataStore.getStore().getNotifications());
							dialog.show();
						} else {
							Crouton.makeText(HomescreenActivity.this, "No new notifications to show!", Style.INFO).show();
						}
					}
					return true;
				}
			});
			popup.show();
		} else if (view.getId() == R.id.messageHeaderButton) {
			Intent intent4 = new Intent(HomescreenActivity.this, MessageFriendsActivity.class);
			startActivity(intent4);
			overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
			finish();
		} else if (view.getId() == R.id.titlebar) {
			listView.getRefreshableView().smoothScrollToPosition(0);
		} else if (view.getId() == R.id.notificationButtonLayout) {
			NotificationDialog dialog = new NotificationDialog(HomescreenActivity.this, UserDataStore.getStore().getNotifications());
			dialog.show();
		}
	}

	public void sendEmail(String emailAddress, String subject) {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailAddress, null));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		startActivity(Intent.createChooser(emailIntent, "Send email..."));
	}

	@Override
	public void onPostShare(int position) {
		final View view = adapter.getView(position, null, listView);
		((RelativeLayout) findViewById(R.id.hidden_view)).addView(view);
		view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				view.findViewById(R.id.overflowButton).setVisibility(View.INVISIBLE);
				view.layout(0, 0, listView.getWidth(), view.getHeight());
				view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				view.setVisibility(View.GONE);
				try {
					Bitmap bitmap = GeneralUtil.loadBitmapFromView(view);
					GeneralUtil.sharePhotoIntent(HomescreenActivity.this, bitmap, "Check out what I saw on Bokwas");

				} catch (Exception e) {
					Crouton.makeText(HomescreenActivity.this, "Post couldn't be shared. Try again", Style.ALERT).show();
					e.printStackTrace();
				}
			}

		});
	}

	private View getViewToShare(int position) {
		final View view = LayoutInflater.from(this).inflate(R.layout.post_list_item_new, null);
		// Do some stuff to the view, like add an ImageView, etc.
		Post post = adapter.getItem(position);
		((TextView) view.findViewById(R.id.post_content)).setText(post.getPostText());

		Date date = new Date(post.getTimestamp());
		String dateString = DateUtil.formatToYesterdayOrToday(date);
		((TextView) view.findViewById(R.id.post_time)).setText(dateString);
		((TextView) view.findViewById(R.id.post_comment_number)).setText(String.valueOf(post.getComments().size()));
		List<Likes> likes = post.getLikes();
		if (likes.size() > 0) {
			((TextView) view.findViewById(R.id.post_like_number)).setText(String.valueOf(likes.size()));
		} else {
			((TextView) view.findViewById(R.id.post_like_number)).setText(String.valueOf(0));
		}

		if (post.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			((RelativeLayout) view.findViewById(R.id.post_like_button)).findViewById(R.id.like_image).setBackgroundResource(R.drawable.facebook_icon_enable);
		} else {
			((RelativeLayout) view.findViewById(R.id.post_like_button)).findViewById(R.id.like_image).setBackgroundResource(R.drawable.like_icon);
		}

		if (post.isBokwasPost()) {
			((TextView) view.findViewById(R.id.post_name)).setText(post.getName());
			String avatarId = post.getAvatarId();
			((ImageView) view.findViewById(R.id.post_profile_pic)).setImageBitmap(GeneralUtil.getImageBitmap(GeneralUtil.getAvatarResourceId(avatarId), this));
		} else {
			String url = post.getProfilePicture();
			((TextView) view.findViewById(R.id.post_name)).setText(post.getName());
			UrlImageViewHelper.setUrlDrawable(((ImageView) view.findViewById(R.id.post_profile_pic)), url, null, 60000 * 100);
		}
		view.measure(view.getWidth(), view.getHeight());
		View childView = adapter.getView(position, null, listView);
		childView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		childView.layout(0, 0, listView.getWidth(), childView.getMeasuredHeight());
		return childView;
	}

}
