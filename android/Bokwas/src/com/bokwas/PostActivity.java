package com.bokwas;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokwas.apirequests.AddCommentsApi;
import com.bokwas.apirequests.AddLikesApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
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

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class PostActivity extends Activity implements OnClickListener{
	
	private Post post;
	private EditText editText;
	private CommentsDialogListAdapter adapter;
	private List<Comment> comments;
	private ListView listView;
	private boolean isLike = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.post_screen);
		
		String postId = getIntent().getStringExtra("postId");
		post = UserDataStore.getStore().getPost(postId);

		setOnClickListeners();

		setupUI();
		ArrayList<Post> postList = new ArrayList<Post>();
		postList.add(post);
//		new FbProfilePicBatchApi(
//				postList,
//				new com.bokwas.apirequests.FbProfilePicBatchApi.ProfilePicDownload() {
//
//					@Override
//					public void onDownloadComplete() {
//						setupUI();
//					}
//				}).execute("");

	}

	private void setupUI() {
		editText = (EditText) findViewById(R.id.comment_edittext);
		comments = post.getComments();
		adapter = new CommentsDialogListAdapter(PostActivity.this, comments, post);
		listView = (ListView) findViewById(R.id.comment_list);
		listView.setAdapter(adapter);
		
		TextView postText = (TextView) findViewById(R.id.post_content);
		postText.setText(post.getPostText());
		TextView time = (TextView) findViewById(R.id.post_time);
		Date date = new Date(post.getTimestamp());
		String dateString = DateUtil.formatToYesterdayOrToday(date);
		time.setText(dateString);
		TextView commentSize = (TextView) findViewById(R.id.post_comment_number);
		commentSize.setText(String.valueOf(post.getComments().size()));
		if(comments==null || comments.size()<1) {
			listView.setBackgroundResource(android.R.color.transparent);
		}
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
				String avatarId = String.valueOf(UserDataStore.getStore()
						.getAvatarId());
				picture.setImageBitmap(GeneralUtil.getImageBitmap(
						GeneralUtil.getAvatarResourceId(avatarId), this));
			} else {
				name.setText(post.getName());
				String avatarId = post.getAvatarId();
				picture.setImageBitmap(GeneralUtil.getImageBitmap(
						GeneralUtil.getAvatarResourceId(avatarId), this));
			}

		} else {
			name.setText(post.getName());
			UrlImageViewHelper.setUrlDrawable(picture, post.getProfilePicture(), null, 60000 * 100);
		}
	}

	private void setOnClickListeners() {
//		findViewById(R.id.post_comment_button).setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				CommentsDialog commentsDialog = new CommentsDialog(PostActivity.this,
//						post.getComments(), post);
//				commentsDialog.setOnDismissListener(new OnDismissListener() {
//					
//					@Override
//					public void onDismiss(DialogInterface dialog) {
//						setupUI();
//					}
//				});
//				commentsDialog.show();
//			}
//		});
		
		findViewById(R.id.overflowButton).setOnClickListener(this);
		
		findViewById(R.id.commentButton).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				if (editText.getText().toString().trim() != null
						&& !editText.getText().toString().trim().equals("")) {
					
					NotificationProgress.showNotificationProgress(PostActivity.this, "Adding a new comment", GeneralUtil.NOTIFICATION_PROGRESS_NEWCOMMENT);
					
					new AddCommentsApi(UserDataStore.getStore().getAccessKey(),
							post.getPostId(), post.getPostedBy(), editText
									.getText().toString(), UserDataStore.getStore()
									.getUserId(), PostActivity.this, new APIListener() {

								@Override
								public void onAPIStatus(boolean status) {
									NotificationProgress.clearNotificationProgress(GeneralUtil.NOTIFICATION_PROGRESS_NEWCOMMENT);
									if (status) {
										Toast.makeText(PostActivity.this, "Comment added!",
												Toast.LENGTH_SHORT).show();
										editText.setText("");
										adapter.notifyDataSetChanged();
									} else {
										Toast.makeText(PostActivity.this,
												"Comment couldn't be added!",
												Toast.LENGTH_SHORT).show();
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
		if(post.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			isLike = false;
			NotificationProgress.showNotificationProgress(PostActivity.this, "Unliking the post", GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
		}else {
			NotificationProgress.showNotificationProgress(PostActivity.this, "Liking the post", GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
		}
		new AddLikesApi(PostActivity.this, UserDataStore.getStore()
				.getAccessKey(), post.getPostId(), UserDataStore
				.getStore().getUserId(), post.getPostedBy(), null,
				new APIListener() {

					@Override
					public void onAPIStatus(boolean status) {
						NotificationProgress.clearNotificationProgress(GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
						if (status) {
							if(isLike) {
							Crouton.makeText(PostActivity.this, "Post liked!",
									Style.INFO).show();
							}else {
								Crouton.makeText(PostActivity.this, "Post unliked!",
										Style.INFO).show();
							}
							setupUI();
						} else {
							Crouton.makeText(
									PostActivity.this,
									"Post couldn't be liked. Try again",
									Style.ALERT).show();
						}
					}
				}).execute("");
	
	
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(this, HomescreenActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_slide_in_right,
				R.anim.activity_slide_out_right);
		finish();
	}

	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.overflowButton) {
			PopupMenu popup = new PopupMenu(PostActivity.this, view);
			popup.getMenuInflater().inflate(R.menu.post_activity_menu,
					popup.getMenu());
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
					}
					return true;
				}
			});
			popup.show();
		}
	}

	protected void sharePost() {
		final View view = getWindow().getDecorView().findViewById(android.R.id.content);
		view.findViewById(R.id.comment_edittext).setVisibility(
				View.INVISIBLE);
		view.findViewById(R.id.commentButton).setVisibility(
				View.INVISIBLE);
		view.layout(0, 0, getWindow().getDecorView().getWidth(), getWindow().getDecorView().getHeight());
		try {
			Bitmap bitmap = GeneralUtil
					.loadBitmapFromView(view);
			GeneralUtil.sharePhotoIntent(
					PostActivity.this, bitmap,
					"Check out what I saw on Bokwas");

		} catch (Exception e) {
			Crouton.makeText(PostActivity.this,
					"Post couldn't be shared. Try again",
					Style.ALERT).show();
			e.printStackTrace();
		}
		view.findViewById(R.id.comment_edittext).setVisibility(
				View.VISIBLE);
		view.findViewById(R.id.commentButton).setVisibility(
				View.VISIBLE);
	}
	
}
