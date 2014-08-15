package com.bokwas;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
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
import com.bokwas.apirequests.GetPrivateMessagesApi;
import com.bokwas.datasets.Friends;
import com.bokwas.datasets.Message;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.dialogboxes.NotificationDialog;
import com.bokwas.response.Likes;
import com.bokwas.response.Post;
import com.bokwas.ui.HomescreenPostsListAdapter;
import com.bokwas.ui.HomescreenPostsListAdapter.PostShare;
import com.bokwas.util.AppData;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;
import com.bokwas.util.NotificationProgress;
import com.bokwas.util.TrackerName;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
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
	private ImageView floatButton;
	private boolean isAnimRunning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_screen);
		
		if(AppData.isReset) {
			Toast.makeText(this, "Please restart the app", Toast.LENGTH_SHORT).show();
			finish();
		}

		setOnClickListeners();

		setupUI();

		setupNotificationBar();

		setupGoogleAnalytics();

		if (!UserDataStore.isInitialized()) {
			try {
				UserDataStore.initData(this);
				NotificationProgress.clearNotification(this, GeneralUtil.GENERAL_NOTIFICATIONS);
				isRefreshed = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		updateFriendsAndMessagesApi();
		
		NotificationProgress.clearNotification(this, GeneralUtil.GENERAL_NOTIFICATIONS);
		NotificationProgress.clearNotification(this, GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
		NotificationProgress.clearNotification(this, GeneralUtil.NOTIFICATION_PROGRESS_DELETECOMMENT);
		NotificationProgress.clearNotification(this, GeneralUtil.NOTIFICATION_PROGRESS_DELETEPOST);
		NotificationProgress.clearNotification(this, GeneralUtil.NOTIFICATION_PROGRESS_NEWCOMMENT);
		NotificationProgress.clearNotification(this, GeneralUtil.NOTIFICATION_PROGRESS_NEWPOST);

	}

	private void setupGoogleAnalytics() {
		Tracker t = GeneralUtil.getTracker(TrackerName.APP_TRACKER, this);
		t.enableAutoActivityTracking(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	
	private void updateFriendsAndMessagesApi() {
		if (getIntent().getBooleanExtra("fromSplashscreen", false) && isRefreshed == false) {

			new GetFriendsApi(this, UserDataStore.getStore().getAccessKey(), UserDataStore.getStore().getUserId(), null).execute("");

			new GetNotificationsApi(UserDataStore.getStore().getAccessKey(), UserDataStore.getStore().getUserId(), new APIListener() {

				@Override
				public void onAPIStatus(boolean status) {
					setupNotificationBar();
				}
			}).execute("");

			new GetPrivateMessagesApi(this, UserDataStore.getStore().getUserId(), UserDataStore.getStore().getAccessKey(), new APIListener() {

				@Override
				public void onAPIStatus(boolean status) {
					int unSeenMessageSize = 0;
					for (Friends friend : UserDataStore.getStore().getFriends()) {
						for (Message message : UserDataStore.getStore().getMessagesForPerson(friend.getId())) {
							if (!message.isSeen()) {
								unSeenMessageSize++;
							}
						}
					}
					if (unSeenMessageSize > 0) {
						findViewById(R.id.messageHeaderCount).setVisibility(View.VISIBLE);
						((TextView) findViewById(R.id.messageHeaderCount)).setText("" + unSeenMessageSize);
					}
				}
			}).execute("");

			// listView.setRefreshing();
			isRefreshed = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("HomescreenActivity", "onResume()");
		if (!UserDataStore.isInitialized()) {
			try {
				Log.d("HomescreenActivity", "onResume() isInit : false");
				UserDataStore.initData(this);
				isRefreshed = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setupUI();
		setupNotificationBar();
	}

	private int getPixelsInDp(int sizeInDp) {
		float scale = getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (sizeInDp * scale + 0.5f);
		return dpAsPixels;
	}

	private void setupUI() {
		floatButton = (ImageView) findViewById(R.id.newPostFloat);

		findViewById(R.id.newPostButton).setVisibility(View.GONE);
		floatButton.setVisibility(View.VISIBLE);

		int pixelsInDp = getPixelsInDp(12);
		findViewById(R.id.newPostButton).setPadding(pixelsInDp, pixelsInDp, pixelsInDp, pixelsInDp);

		adapter = new HomescreenPostsListAdapter(this, UserDataStore.getStore().getPosts(), this);
		listView = (PullToRefreshListView) findViewById(R.id.feed_list);
		listView.setAdapter(adapter);
		int unSeenMessageSize = 0;
		for (Friends friend : UserDataStore.getStore().getFriends()) {
			for (Message message : UserDataStore.getStore().getMessagesForPerson(friend.getId())) {
				if (!message.isSeen()) {
					unSeenMessageSize++;
				}
			}
		}
		if (unSeenMessageSize > 0) {
			findViewById(R.id.messageHeaderCount).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.messageHeaderCount)).setText("" + unSeenMessageSize);
		}

		setupListViewListeners();

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
					hideFloatButton();
				}
				if (mLastFirstVisibleItem > firstVisibleItem) {
					showFloatButton();
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
							UserDataStore.getStore().sortPosts();
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
			// if (UserDataStore.getStore().getUnseenNotifications().size() > 0)
			// {
			findViewById(R.id.notificationButtonLayout).setVisibility(View.VISIBLE);
			TextView notificationButton = (TextView) findViewById(R.id.notificationButton);
			notificationButton.setText(String.valueOf(UserDataStore.getStore().getUnseenNotifications().size()));
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showFloatButton() {
		if (floatButton.getVisibility() == View.GONE && !isAnimRunning) {
			floatButton.setVisibility(View.VISIBLE);
			Animation.AnimationListener animListener = new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					isAnimRunning = true;
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					isAnimRunning = false;
				}
			};
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.activity_slide_in_bottom);
			animation.setAnimationListener(animListener);
			floatButton.startAnimation(animation);
		}
	}

	private void hideFloatButton() {
		if (floatButton.getVisibility() == View.VISIBLE && !isAnimRunning) {
			Animation.AnimationListener animListener = new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					isAnimRunning = true;
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					floatButton.setVisibility(View.GONE);
					isAnimRunning = false;
				}
			};
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.activity_slide_out_top);
			animation.setAnimationListener(animListener);
			floatButton.startAnimation(animation);
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
		} else if (view.getId() == R.id.overflowButton) {
			PopupMenu popup = new PopupMenu(HomescreenActivity.this, view);
			popup.getMenuInflater().inflate(R.menu.overflow_menu, popup.getMenu());
			// MenuItem settingsItem =
			// popup.getMenu().findItem(R.id.overflow_settings);
			// settingsItem.setVisible(false);
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
					case R.id.overflow_invite:
						GeneralUtil
								.shareIntent(
										HomescreenActivity.this,
										"Hi! I'm using Bokwas, a cool social networking app where we interact in a universe of alter-egos. We can comment on Facebook posts, chat with friends and a lot more, while in complete anonymity! Learn more about it, and get a chance to download the early preview at http://bokwas.com");
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

	@SuppressWarnings("unused")
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
