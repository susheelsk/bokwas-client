package com.bokwas;

import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bokwas.apirequests.AddCommentsApi;
import com.bokwas.apirequests.AddLikesApi;
import com.bokwas.apirequests.DeleteApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.apirequests.GetSinglePostApi;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.dialogboxes.CommentsDialog;
import com.bokwas.dialogboxes.LikesDialog;
import com.bokwas.response.Comment;
import com.bokwas.response.Likes;
import com.bokwas.response.Post;
import com.bokwas.ui.CommentsDialogListAdapter;
import com.bokwas.ui.ExpandableHeightListView;
import com.bokwas.util.AppData;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;
import com.bokwas.util.NotificationProgress;
import com.bokwas.util.TrackerName;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.squareup.picasso.Picasso;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class PostActivity extends FragmentActivity implements OnClickListener, EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {

	private Post post;
	private EditText editText;
	private CommentsDialogListAdapter adapter;
	private List<Comment> comments;
	private ExpandableHeightListView listView;
	private boolean isLike = true;
	private ProgressDialog pdia;
	private boolean isOutsidePost = false;
	private FragmentManager manager;
	private FragmentTransaction transaction;
	private EmojiconsFragment emojiFragment;
	private boolean isEmojiShown = false;
	private BroadcastReceiver receiver;
	protected SuperActivityToast superActivityToast;
	private String TAG = "PostActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.post_screen);

		if (AppData.isReset) {
			Toast.makeText(this, "Please restart the app", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		manager = getSupportFragmentManager();
		transaction = manager.beginTransaction();

		String postId = getIntent().getStringExtra("postId");

		Log.d(TAG, "postId : " + postId);
		if (postId == null) {
			Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show();
			onBackPressed();
			return;
		}
		emojiFragment = (EmojiconsFragment) manager.findFragmentById(R.id.emojicons);
		transaction.hide(emojiFragment);
		transaction.commit();
		if (!UserDataStore.isInitialized()) {
			try {
				UserDataStore.initData(this);
				NotificationProgress.clearNotification(this, GeneralUtil.GENERAL_NOTIFICATIONS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		IntentFilter filter = new IntentFilter("NEW_MESSAGE");
		filter.addAction("NEW_MESSAGE");
		filter.addAction("SOME_OTHER_ACTION");
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				setupUI();
			}
		};
		registerReceiver(receiver, filter);

		NotificationProgress.clearNotification(this, GeneralUtil.GENERAL_NOTIFICATIONS);
		NotificationProgress.clearNotification(this, GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
		NotificationProgress.clearNotification(this, GeneralUtil.NOTIFICATION_PROGRESS_DELETECOMMENT);
		NotificationProgress.clearNotification(this, GeneralUtil.NOTIFICATION_PROGRESS_DELETEPOST);
		NotificationProgress.clearNotification(this, GeneralUtil.NOTIFICATION_PROGRESS_NEWCOMMENT);
		NotificationProgress.clearNotification(this, GeneralUtil.NOTIFICATION_PROGRESS_NEWPOST);

		post = UserDataStore.getStore().getPost(postId);

		setOnClickListeners();
		pdia = new ProgressDialog(this);
		if (post == null) {
			pdia.setCancelable(false);
			pdia.setCanceledOnTouchOutside(false);
			pdia.setMessage("Loading");
			pdia.show();
			refreshPost(postId);
			return;
		}
		comments = post.getComments();

		setupUI();
		if (post.isBokwasPost() && !UserDataStore.getStore().isPostFromFriendOrMe(post)) {
			isOutsidePost = true;
			findViewById(R.id.post_like_button).setVisibility(View.GONE);
			findViewById(R.id.comment_edittext).setVisibility(View.GONE);
			findViewById(R.id.commentButton).setVisibility(View.GONE);
			findViewById(R.id.emojiButton).setVisibility(View.GONE);
		}

		setupGoogleAnalytics();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!UserDataStore.isInitialized()) {
			try {
				UserDataStore.initData(this);
				onBackPressed();
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setupUI();
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
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		if (superActivityToast != null) {
			superActivityToast.dismiss();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}

	private void setupUI() {
		if (post.isBokwasPost()) {
			findViewById(R.id.post_relative_view).setBackgroundResource(R.drawable.rectangle_orange_stroke);
		}

		if (post.getType().equals("photo") && post.getPicture() != null && !post.getPicture().equals("")) {
			ImageView postPicture = (ImageView) findViewById(R.id.post_picture);
			postPicture.setBackgroundResource(android.R.color.transparent);
			postPicture.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(PostActivity.this, FullScreenImage.class);
					intent.putExtra("url", post.getPicture());
					intent.putExtra("activity", PostActivity.this.getClass().getSimpleName());
					intent.putExtra("postId", post.getPostId());
					intent.putExtra("name", post.getName());
					if (post.isBokwasPost()) {
						intent.putExtra("avatarId", Integer.valueOf(post.getAvatarId()));
					} else {
						intent.putExtra("fbProfilePic", post.getProfilePicture());
					}
					PostActivity.this.startActivity(intent);
				}
			});
			postPicture.setVisibility(View.VISIBLE);
			Picasso.with(this).load(post.getPicture()).resize(250, 250).centerCrop().placeholder(R.drawable.placeholder).into(postPicture);
			adapter = new CommentsDialogListAdapter(PostActivity.this, comments, post);
			listView = (ExpandableHeightListView) findViewById(R.id.comment_list);
			listView.setExpanded(true);
			listView.setAdapter(adapter);
			if (comments == null || comments.size() < 1) {
				listView.setBackgroundResource(android.R.color.transparent);
			} else {
				listView.setBackgroundResource(R.drawable.rectangle_white_stroke);
			}

		} else {
			adapter = new CommentsDialogListAdapter(PostActivity.this, comments, post);
			listView = (ExpandableHeightListView) findViewById(R.id.comment_list);
			listView.setExpanded(true);
			listView.setAdapter(adapter);
			if (comments == null || comments.size() < 1) {
				listView.setBackgroundResource(android.R.color.transparent);
			} else {
				listView.setBackgroundResource(R.drawable.rectangle_white_stroke);
			}
		}

		final ScrollView scrollView = (ScrollView) findViewById(R.id.post_screen_scrollview);
		scrollView.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				scrollView.fullScroll(ScrollView.FOCUS_UP);
			}
		});

		// setupNotificationBar();
		int pixelsInDp = getPixelsInDp(12);
		findViewById(R.id.messageHeaderButton).setPadding(pixelsInDp, pixelsInDp, pixelsInDp, pixelsInDp);
		((ImageView) findViewById(R.id.messageHeaderButton)).setImageResource(R.drawable.share_icon);
		// findViewById(R.id.newPostButton).setVisibility(View.VISIBLE);
		// ((ImageView)findViewById(R.id.newPostButton)).setImageResource(R.drawable.ic_menu_refresh);
		// ((TextView) findViewById(R.id.messageHeaderButton)).setText("");
		editText = (EditText) findViewById(R.id.comment_edittext);
		
		TextView postText = (TextView) findViewById(R.id.post_content);
		postText.setMovementMethod(new ScrollingMovementMethod());
		postText.setText(post.getPostText());
		TextView time = (TextView) findViewById(R.id.post_time);
		Date date = new Date(post.getTimestamp());
		String dateString = DateUtil.formatToYesterdayOrToday(date);
		time.setText(dateString);
		TextView commentSize = (TextView) findViewById(R.id.post_comment_number);
		commentSize.setText(String.valueOf(post.getComments().size()));

		TextView likeSize = (TextView) findViewById(R.id.post_like_number);
		List<Likes> likes = post.getLikes();
		if (likes.size() > 0) {
			likeSize.setText(String.valueOf(likes.size()));
		} else {
			likeSize.setText(String.valueOf(0));
		}

		if (post.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			findViewById(R.id.like_image).setBackgroundResource(R.drawable.facebook_icon_enable);
		} else {
			findViewById(R.id.like_image).setBackgroundResource(R.drawable.like_icon);
		}

		TextView name = (TextView) findViewById(R.id.post_name);
		ImageView picture = (ImageView) findViewById(R.id.post_profile_pic);
		if (post.isBokwasPost()) {
			if (post.getPostedBy().equals(UserDataStore.getStore().getUserId())) {
				name.setText(UserDataStore.getStore().getBokwasName());
				String avatarId = String.valueOf(UserDataStore.getStore().getAvatarId());
				picture.setImageBitmap(GeneralUtil.getImageBitmap(GeneralUtil.getAvatarResourceId(avatarId), this));
			} else {
				name.setText(post.getName());
				String avatarId = post.getAvatarId();
				picture.setImageBitmap(GeneralUtil.getImageBitmap(GeneralUtil.getAvatarResourceId(avatarId), this));
			}

		} else {
			name.setText(post.getName());
			UrlImageViewHelper.setUrlDrawable(picture, post.getProfilePicture(), null, 60000 * 100);
		}

		editText.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isEmojiShown) {
					hideEmojis();
				}
				return false;
			}
		});
	}

	private int getPixelsInDp(int sizeInDp) {
		float scale = getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (sizeInDp * scale + 0.5f);
		return dpAsPixels;
	}

	protected void setupNotificationBar() {
		try {
			TextView notificationButton = (TextView) findViewById(R.id.messageHeaderButton);
			notificationButton.setText(String.valueOf(UserDataStore.getStore().getUnseenNotifications().size()));

			if (UserDataStore.getStore().getNotifications().size() >= 1) {
				notificationButton.setBackgroundResource(R.drawable.circle_red);
			} else {
				notificationButton.setOnClickListener(null);
				notificationButton.setBackgroundResource(R.drawable.circle_grey);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		isEmojiShown = true;
		findViewById(R.id.overflowButton).postDelayed(new Runnable() {

			@Override
			public void run() {
				manager = getSupportFragmentManager();
				transaction = manager.beginTransaction();
				transaction.show(emojiFragment);
				transaction.commit();
			}
		}, 250);

	}

	private void hideEmojis() {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, 0);
		isEmojiShown = false;
		manager = getSupportFragmentManager();
		transaction = manager.beginTransaction();
		transaction.hide(emojiFragment);
		transaction.commit();
	}

	private void setOnClickListeners() {
		findViewById(R.id.overflowButton).setOnClickListener(this);
		findViewById(R.id.messageHeaderButton).setOnClickListener(this);
		findViewById(R.id.newPostButton).setOnClickListener(this);
		findViewById(R.id.post_comment_button).setOnClickListener(this);
		findViewById(R.id.titlebar).setOnClickListener(this);
		findViewById(R.id.post_profile_pic).setOnClickListener(this);
		findViewById(R.id.emojiButton).setOnClickListener(this);
		findViewById(R.id.commentButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View view) {

				if (editText.getText().toString().trim() != null && !editText.getText().toString().trim().equals("")) {
					view.setClickable(false);
					superActivityToast = new SuperActivityToast(PostActivity.this, SuperToast.Type.PROGRESS);
					superActivityToast.setText("Adding comment");
					superActivityToast.setIndeterminate(true);
					superActivityToast.setProgressIndeterminate(true);
					superActivityToast.show();

					new AddCommentsApi(UserDataStore.getStore().getAccessKey(), post.getPostId(), post.getPostedBy(), editText.getText().toString(), UserDataStore.getStore().getUserId(),
							PostActivity.this, new APIListener() {

								@Override
								public void onAPIStatus(boolean status) {
									view.setClickable(true);
									superActivityToast.dismiss();
									if (status) {
										Toast.makeText(PostActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
										editText.setText("");
										if (adapter != null) {
											adapter.notifyDataSetChanged();
										}
										comments = post.getComments();
										setupUI();
									} else {
										Toast.makeText(PostActivity.this, "Comment couldn't be added!", Toast.LENGTH_SHORT).show();
									}
								}
							}).execute("");
				}

			}
		});

		findViewById(R.id.post_like_button).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				likePost(view);
			}
		});
	}

	protected void likePost(final View view) {
		view.setClickable(false);
		final SuperActivityToast superActivityToast = new SuperActivityToast(PostActivity.this, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);
		if (post.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			isLike = false;
		} else {
		}
		new AddLikesApi(PostActivity.this, UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), post.getPostedBy(), null, new APIListener() {

			@Override
			public void onAPIStatus(boolean status) {
				view.setClickable(true);
				if (status) {
					if (isLike) {
						Crouton.makeText(PostActivity.this, "Post liked!", Style.INFO).show();
					} else {
						Crouton.makeText(PostActivity.this, "Post unliked!", Style.INFO).show();
					}
					setupUI();
				} else {
					Crouton.makeText(PostActivity.this, "Post couldn't be liked. Try again", Style.ALERT).show();
				}
			}
		}).execute("");

	}

	@Override
	public void onBackPressed() {
		if (isEmojiShown) {
			hideEmojis();
			return;
		}
		Intent intent;
		if (getIntent().getStringExtra("activity") != null && getIntent().getStringExtra("activity").contains("ProfileActivityExperimental")) {
			intent = new Intent(this, ProfileActivityExperimental.class);
			intent.putExtra("profileId", post.getPostedBy());
			intent.putExtra("name", post.getName());
			if (post.isBokwasPost()) {
				intent.putExtra("avatarId", Integer.valueOf(post.getAvatarId()));
			} else {
				intent.putExtra("fbProfilePic", post.getProfilePicture());
			}
		} else {
			intent = new Intent(this, HomescreenActivity.class);
		}
		startActivity(intent);
		overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_right);
		finish();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.overflowButton) {
			PopupMenu popup = new PopupMenu(PostActivity.this, view);
			popup.getMenuInflater().inflate(R.menu.post_activity_menu, popup.getMenu());
			MenuItem deleteItem = popup.getMenu().findItem(R.id.post_delete);
			MenuItem showLikesItem = popup.getMenu().findItem(R.id.post_show_like);
			MenuItem likesItem = popup.getMenu().findItem(R.id.post_like);
			if (isOutsidePost) {
				likesItem.setVisible(false);
			}
			if (post.isBokwasPost() && post.getPostedBy().equals(UserDataStore.getStore().getUserId())) {
				deleteItem.setVisible(true);
			} else {
				deleteItem.setVisible(false);
			}
			if (post.getLikes().size() < 1) {
				showLikesItem.setVisible(false);
			}
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {
					case R.id.post_home:
						onBackPressed();
						break;
					case R.id.post_like:
						likePost(findViewById(R.id.post_like_button));
						break;
					case R.id.post_share:
						sharePost();
						break;
					case R.id.post_delete:
						deletePost();
						break;
					case R.id.post_show_like:
						showLikes();
						break;
					case R.id.post_show_refresh:
						refreshPost();
						break;
					}
					return true;
				}
			});
			popup.show();
		} else if (view.getId() == R.id.messageHeaderButton) {
			sharePost();
		} else if (view.getId() == R.id.newPostButton) {
			refreshPost();
		} else if (view.getId() == R.id.post_comment_button) {
			CommentsDialog commentsDialog = new CommentsDialog(this, post.getComments(), post);
			commentsDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					setupUI();
				}
			});
			commentsDialog.show();
		} else if (view.getId() == R.id.titlebar) {
			Intent intent;
			intent = new Intent(this, HomescreenActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_right);
			finish();
		} else if (view.getId() == R.id.post_profile_pic) {
			Intent intent = new Intent(this, ProfileActivityExperimental.class);
			intent.putExtra("profileId", post.getPostedBy());
			intent.putExtra("name", post.getName());
			if (post.isBokwasPost()) {
				intent.putExtra("avatarId", Integer.valueOf(post.getAvatarId()));
			} else {
				intent.putExtra("fbProfilePic", post.getProfilePicture());
			}
			startActivity(intent);
			overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
			finish();
		} else if (view.getId() == R.id.emojiButton) {
			hideKeyboard();
		}
	}

	private void refreshPost() {
		Log.d("PostActivity", "RefreshPost() called");
		new GetSinglePostApi(this, UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), new APIListener() {

			@Override
			public void onAPIStatus(boolean status) {
				if (status) {
					comments = post.getComments();
					setupUI();
				} else {
					Toast.makeText(PostActivity.this, "Can't find post", Toast.LENGTH_SHORT).show();
					onBackPressed();
				}
			}
		}).execute("");
	}

	private void refreshPost(final String postId) {
		Log.d("PostActivity", "RefreshPost() called");
		new GetSinglePostApi(this, UserDataStore.getStore().getAccessKey(), postId, UserDataStore.getStore().getUserId(), new APIListener() {

			@Override
			public void onAPIStatus(boolean status) {
				if (pdia != null && pdia.isShowing()) {
					pdia.cancel();
				}
				post = UserDataStore.getStore().getPost(postId);
				if (post == null) {
					Toast.makeText(PostActivity.this, "Can't find the post. Please try again", Toast.LENGTH_SHORT).show();
					onBackPressed();
					return;
				}
				if (status) {
					setupUI();
				}
			}
		}).execute("");
	}

	private void showLikes() {
		LikesDialog likesDialog = new LikesDialog(this, post.getLikes());
		likesDialog.show();
	}

	private void deletePost() {

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					superActivityToast = new SuperActivityToast(PostActivity.this, SuperToast.Type.PROGRESS);
					superActivityToast.setText("Deleting post");
					superActivityToast.setIndeterminate(true);
					superActivityToast.setProgressIndeterminate(true);
					superActivityToast.show();
					new DeleteApi(UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), null, PostActivity.this, new APIListener() {

						@Override
						public void onAPIStatus(boolean status) {
							superActivityToast.dismiss();
							if (status) {
								Crouton.makeText(PostActivity.this, "Post deleted!", Style.INFO).show();
								onBackPressed();
							} else {
								Crouton.makeText(PostActivity.this, "Post couldn't be deleted. Try again", Style.ALERT).show();
							}
						}
					}).execute("");
					// Yes button clicked
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Delete the post?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();

	}

	protected void sharePost() {
//		final View view = getWindow().getDecorView().findViewById(android.R.id.content);
		final View view = findViewById(R.id.post_activiy_root_layout);
		view.findViewById(R.id.comment_edittext).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.commentButton).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.emojiButton).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.overflowButton).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.messageHeaderButton).setVisibility(View.INVISIBLE);
		view.layout(0, 0, getWindow().getDecorView().getWidth(), getWindow().getDecorView().getHeight());
		try {
			Bitmap bitmap = GeneralUtil.loadFullBitmapFromView(view);
			GeneralUtil.sharePhotoIntent(PostActivity.this, bitmap, "Check out what I saw on Bokwas");

		} catch (Exception e) {
			Crouton.makeText(PostActivity.this, "Post couldn't be shared. Try again", Style.ALERT).show();
			e.printStackTrace();
		}
		view.findViewById(R.id.comment_edittext).setVisibility(View.VISIBLE);
		view.findViewById(R.id.commentButton).setVisibility(View.VISIBLE);
		view.findViewById(R.id.emojiButton).setVisibility(View.VISIBLE);
		view.findViewById(R.id.overflowButton).setVisibility(View.VISIBLE);
		view.findViewById(R.id.messageHeaderButton).setVisibility(View.VISIBLE);
		setupUI();
	}

	@Override
	public void onEmojiconBackspaceClicked(View v) {
		EmojiconsFragment.backspace(editText);
	}

	@Override
	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiconsFragment.input(editText, emojicon);
	}

}
