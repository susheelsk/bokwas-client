package com.bokwas;

import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bokwas.apirequests.AddLikesApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.dialogboxes.CommentsDialog;
import com.bokwas.response.Post;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class PostActivity extends Activity{
	
	private Post post;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.post_screen);
		
		String postId = getIntent().getStringExtra("postId");
		post = UserDataStore.getStore().getPost(postId);

		setOnClickListeners();

		setupUI();

	}

	private void setupUI() {
		TextView postText = (TextView) findViewById(R.id.post_content);
		postText.setText(post.getPostText());
		TextView time = (TextView) findViewById(R.id.post_time);
		Date date = new Date(post.getTimestamp());
		String dateString = DateUtil.formatToYesterdayOrToday(date);
		time.setText(dateString);
		TextView commentSize = (TextView) findViewById(R.id.post_comment_number);
		commentSize.setText(String.valueOf(post.getComments().size()));
		TextView likeSize = (TextView) findViewById(R.id.post_like_number);
		String temp = post.getLikes();
		String[] likes = null;
		if (temp != null && !temp.trim().equals("") && !temp.trim().equals(",")) {
			temp = method(temp);
			likes = temp.split(",");
			likeSize.setText(String.valueOf(likes.length));
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
				name.setText(UserDataStore.getStore()
						.getFriend(post.getPostedBy()).getBokwasName());
				String avatarId = UserDataStore.getStore()
						.getFriend(post.getPostedBy()).getBokwasAvatarId();
				picture.setImageBitmap(GeneralUtil.getImageBitmap(
						GeneralUtil.getAvatarResourceId(avatarId), this));
			}

		} else {
			if (post.getPostedBy().equals(UserDataStore.getStore().getUserId())) {
				name.setText(UserDataStore.getStore().getFbName());
				UrlImageViewHelper.setUrlDrawable(picture, UserDataStore
						.getStore().getFbPicLink(), null, 60000 * 100);
			} else {

				name.setText(UserDataStore.getStore()
						.getFriend(post.getPostedBy()).getFbName());
				UrlImageViewHelper.setUrlDrawable(picture, UserDataStore
						.getStore().getFriend(post.getPostedBy())
						.getFbPicLink(), null, 60000 * 100);
			}
		}
	}

	private void setOnClickListeners() {
		findViewById(R.id.post_comment_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CommentsDialog commentsDialog = new CommentsDialog(PostActivity.this,
						post.getComments(), post);
				commentsDialog.show();
			}
		});
		
		findViewById(R.id.post_like_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				final ProgressDialog pdia = new ProgressDialog(PostActivity.this);
				pdia.setMessage("Liking the post");
				pdia.setCancelable(false);
				pdia.show();
				new AddLikesApi(PostActivity.this, UserDataStore.getStore()
						.getUserAccessToken(), post.getPostId(), UserDataStore
						.getStore().getUserId(), post.getPostedBy(), null,
						new APIListener() {

							@Override
							public void onAPIStatus(boolean status) {
								if (pdia.isShowing()) {
									pdia.dismiss();
								}
								if (status) {
									Toast.makeText(PostActivity.this, "Post liked!",
											Toast.LENGTH_SHORT).show();
									setupUI();
								} else {
									Toast.makeText(
											PostActivity.this,
											"Post couldn't be liked. Try again",
											Toast.LENGTH_SHORT).show();
								}
							}
						}).execute("");
			
			}
		});
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
	
	private String method(String str) {
		if (str.length() > 0 && str.charAt(str.length() - 1) == 'x') {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

}