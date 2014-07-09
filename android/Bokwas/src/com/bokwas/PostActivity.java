package com.bokwas;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
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
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;
import com.bokwas.util.NotificationProgress;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.squareup.picasso.Picasso;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class PostActivity extends Activity implements OnClickListener {

	private Post post;
	private EditText editText;
	private CommentsDialogListAdapter adapter;
	private List<Comment> comments;
	private ListView listView;
	private boolean isLike = true;
	private ProgressDialog pdia;
	private boolean isOutsidePost = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.post_screen);
		
		String postId = getIntent().getStringExtra("postId");

		boolean isFromNoti = getIntent().getBooleanExtra("fromNoti", false);
		if (isFromNoti) {
			try {
				UserDataStore.initData(this);
				NotificationProgress.clearNotification(this, GeneralUtil.GENERAL_NOTIFICATIONS);
				refreshPost();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (postId == null) {
			Toast.makeText(this, "PostId not found", Toast.LENGTH_SHORT).show();
			onBackPressed();
			return;
		}
		post = UserDataStore.getStore().getPost(postId);
		comments = post.getComments();
		
		if(post.isBokwasPost() && !UserDataStore.getStore().isPostFromFriendOrMe(post)) {
			isOutsidePost = true;
			findViewById(R.id.post_like_button).setVisibility(View.GONE);
			findViewById(R.id.comment_edittext).setVisibility(View.GONE);
			findViewById(R.id.commentButton).setVisibility(View.GONE);
		}

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

		setupUI();
		ArrayList<Post> postList = new ArrayList<Post>();
		postList.add(post);
	}

	private void setupUI() {
		if (post.isBokwasPost()) {
			findViewById(R.id.post_relative_view).setBackgroundResource(R.drawable.rectangle_orange_stroke);
		}
		
		if (post.getType().equals("photo") && post.getPicture() != null && !post.getPicture().equals("")) {
			ImageView postPicture = (ImageView)findViewById(R.id.post_picture);
			postPicture.setVisibility(View.VISIBLE);
			Picasso.with(this).load(post.getPicture()).resize(250, 250).centerCrop().placeholder(R.drawable.placeholder).into(postPicture);
			findViewById(R.id.comment_list).setVisibility(View.GONE);
			findViewById(R.id.post_comment_button).setVisibility(View.VISIBLE);
		}else {
			adapter = new CommentsDialogListAdapter(PostActivity.this, comments, post);
			listView = (ListView) findViewById(R.id.comment_list);
			listView.setAdapter(adapter);
			if (comments == null || comments.size() < 1) {
				listView.setBackgroundResource(android.R.color.transparent);
			} else {
				listView.setBackgroundResource(R.drawable.rectangle_white_stroke);
			}
		}
		
		// setupNotificationBar();
		findViewById(R.id.notificationButton).setBackgroundResource(android.R.drawable.ic_menu_share);
//		findViewById(R.id.newPostButton).setVisibility(View.VISIBLE);
//		((ImageView)findViewById(R.id.newPostButton)).setImageResource(R.drawable.ic_menu_refresh);
		((TextView) findViewById(R.id.notificationButton)).setText("");
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
		
	}

	protected void setupNotificationBar() {
		try {
			TextView notificationButton = (TextView) findViewById(R.id.notificationButton);
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

	private void setOnClickListeners() {
		findViewById(R.id.overflowButton).setOnClickListener(this);
		findViewById(R.id.notificationButton).setOnClickListener(this);
		findViewById(R.id.newPostButton).setOnClickListener(this);
		findViewById(R.id.post_comment_button).setOnClickListener(this);
		findViewById(R.id.commentButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (editText.getText().toString().trim() != null && !editText.getText().toString().trim().equals("")) {

					NotificationProgress.showNotificationProgress(PostActivity.this, "Adding a new comment", GeneralUtil.NOTIFICATION_PROGRESS_NEWCOMMENT);

					new AddCommentsApi(UserDataStore.getStore().getAccessKey(), post.getPostId(), post.getPostedBy(), editText.getText().toString(), UserDataStore.getStore().getUserId(),
							PostActivity.this, new APIListener() {

								@Override
								public void onAPIStatus(boolean status) {
									NotificationProgress.clearNotificationProgress(GeneralUtil.NOTIFICATION_PROGRESS_NEWCOMMENT);
									if (status) {
										Toast.makeText(PostActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
										editText.setText("");
										if(adapter!=null) {
											adapter.notifyDataSetChanged();
										}
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
			public void onClick(View v) {
				likePost();
			}
		});
	}

	protected void likePost() {
		final SuperActivityToast superActivityToast = new SuperActivityToast(PostActivity.this, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);
		if (post.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			isLike = false;
			NotificationProgress.showNotificationProgress(PostActivity.this, "Unliking the post", GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
		} else {
			NotificationProgress.showNotificationProgress(PostActivity.this, "Liking the post", GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
		}
		new AddLikesApi(PostActivity.this, UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), post.getPostedBy(), null, new APIListener() {

			@Override
			public void onAPIStatus(boolean status) {
				NotificationProgress.clearNotificationProgress(GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
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
		super.onBackPressed();
		Intent intent;
		if(getIntent().getStringExtra("activity")!=null && getIntent().getStringExtra("activity").contains("ProfileActivityExperimental")) {
			intent = new Intent(this, ProfileActivityExperimental.class);
			intent.putExtra("profileId", post.getPostedBy());
			intent.putExtra("name", post.getName());
			if (post.isBokwasPost()) {
				intent.putExtra("avatarId", Integer.valueOf(post.getAvatarId()));
			} else {
				intent.putExtra("fbProfilePic", post.getProfilePicture());
			}
		}else {
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
			if(isOutsidePost) {
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
						likePost();
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
		}else if(view.getId() == R.id.notificationButton) {
			sharePost();
		}else if(view.getId() == R.id.newPostButton) {
			refreshPost();
		}else if(view.getId() == R.id.post_comment_button) {
			CommentsDialog commentsDialog = new CommentsDialog(this, post.getComments(), post);
			commentsDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					setupUI();
				}
			});
			commentsDialog.show();
		}
	}

	private void refreshPost() {
		Log.d("PostActivity","RefreshPost() called");
		new GetSinglePostApi(this, UserDataStore.getStore().getAccessKey(), post.getPostId(),UserDataStore.getStore().getUserId(), new APIListener() {
			
			@Override
			public void onAPIStatus(boolean status) {
				if(status) {
					setupUI();
				}
			}
		}).execute("");
	}
	
	private void refreshPost(final String postId) {
		Log.d("PostActivity","RefreshPost() called");
		new GetSinglePostApi(this, UserDataStore.getStore().getAccessKey(),postId,UserDataStore.getStore().getUserId(), new APIListener() {
			
			@Override
			public void onAPIStatus(boolean status) {
				if(pdia!=null && pdia.isShowing()) {
					pdia.cancel();
				}
				post = UserDataStore.getStore().getPost(postId);
				if(post==null) {
					Toast.makeText(PostActivity.this, "Can't find the post. Please try again", Toast.LENGTH_SHORT).show();
					onBackPressed();
					return;
				}
				if(status) {
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
		final SuperActivityToast superActivityToast = new SuperActivityToast(this, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);
		NotificationProgress.showNotificationProgress(this, "Deleting the post", GeneralUtil.NOTIFICATION_PROGRESS_DELETEPOST);
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	new DeleteApi(UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), null, PostActivity.this, new APIListener() {

		    			@Override
		    			public void onAPIStatus(boolean status) {
		    				NotificationProgress.clearNotificationProgress(GeneralUtil.NOTIFICATION_PROGRESS_DELETEPOST);
		    				if (status) {
		    					Crouton.makeText(PostActivity.this, "Post deleted!", Style.INFO).show();
		    					onBackPressed();
		    				} else {
		    					Crouton.makeText(PostActivity.this, "Post couldn't be deleted. Try again", Style.ALERT).show();
		    				}
		    			}
		    		}).execute("");
		            //Yes button clicked
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Delete the post?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
		
	}

	protected void sharePost() {
		final View view = getWindow().getDecorView().findViewById(android.R.id.content);
		view.findViewById(R.id.comment_edittext).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.commentButton).setVisibility(View.INVISIBLE);
		view.layout(0, 0, getWindow().getDecorView().getWidth(), getWindow().getDecorView().getHeight());
		try {
			Bitmap bitmap = GeneralUtil.loadBitmapFromView(view);
			GeneralUtil.sharePhotoIntent(PostActivity.this, bitmap, "Check out what I saw on Bokwas");

		} catch (Exception e) {
			Crouton.makeText(PostActivity.this, "Post couldn't be shared. Try again", Style.ALERT).show();
			e.printStackTrace();
		}
		view.findViewById(R.id.comment_edittext).setVisibility(View.VISIBLE);
		view.findViewById(R.id.commentButton).setVisibility(View.VISIBLE);
		setupUI();
	}

}
