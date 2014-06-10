package com.bokwas.ui;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokwas.R;
import com.bokwas.PostActivity;
import com.bokwas.apirequests.AddLikesApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.dialogboxes.CommentsDialog;
import com.bokwas.response.Likes;
import com.bokwas.response.Post;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;
import com.bokwas.util.NotificationProgress;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class HomescreenPostsListAdapter extends ArrayAdapter<Post> {

	private Activity activity;
	private List<Post> posts;

	public void setPosts(List<Post> posts) {
		this.posts = posts;
		notifyDataSetChanged();
	}

	public HomescreenPostsListAdapter(Activity activity, List<Post> posts) {
		super(activity, R.layout.post_list_item, posts);
		this.activity = activity;
		this.posts = posts;
	}

	static class ViewHolder {
		public TextView name;
		public TextView postText;
		public TextView time;
		public TextView commentSize;
		public TextView likeSize;
		public ImageView picture;
		public RelativeLayout commentButton;
		public RelativeLayout likeButton;
	}

	@Override
	public int getCount() {
		return posts.size();
	}

	@Override
	public Post getItem(int position) {
		return posts.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.post_list_item_new, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) rowView.findViewById(R.id.post_name);
			viewHolder.postText = (TextView) rowView
					.findViewById(R.id.post_content);
			viewHolder.time = (TextView) rowView.findViewById(R.id.post_time);
			viewHolder.commentSize = (TextView) rowView
					.findViewById(R.id.post_comment_number);
			viewHolder.likeSize = (TextView) rowView
					.findViewById(R.id.post_like_number);
			viewHolder.picture = (ImageView) rowView
					.findViewById(R.id.post_profile_pic);
			viewHolder.commentButton = (RelativeLayout) rowView
					.findViewById(R.id.post_comment_button);
			viewHolder.likeButton = (RelativeLayout) rowView
					.findViewById(R.id.post_like_button);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		final Post post = posts.get(position);

		holder.postText.setText(post.getPostText());

		// Timestamp stamp = new Timestamp(post.getTimestamp());
		Date date = new Date(post.getTimestamp());
		String dateString = DateUtil.formatToYesterdayOrToday(date);
		holder.time.setText(dateString);
		holder.commentSize.setText(String.valueOf(post.getComments().size()));
		List<Likes> likes = post.getLikes();
		if (likes.size() > 0) {
			holder.likeSize.setText(String.valueOf(likes.size()));
		} else {
			holder.likeSize.setText(String.valueOf(0));
		}

		if (post.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			// holder.likeButton.setBackgroundColor(Color.GRAY);
			// holder.likeButton.setClickable(false);
			// holder.likeButton.setEnabled(false);
			holder.likeButton.findViewById(R.id.like_image)
					.setBackgroundResource(R.drawable.facebook_icon_enable);
		} else {
			holder.likeButton.findViewById(R.id.like_image)
					.setBackgroundResource(R.drawable.like_icon);
		}

		if (post.isBokwasPost()) {
			holder.name.setText(post.getName());
			String avatarId = post.getAvatarId();
			holder.picture.setImageBitmap(GeneralUtil.getImageBitmap(
					GeneralUtil.getAvatarResourceId(avatarId), activity));
		} else {
			String url = post.getProfilePicture();
			holder.name.setText(post.getName());
			UrlImageViewHelper.setUrlDrawable(holder.picture, url, null,
					60000 * 100);
		}

		holder.commentButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CommentsDialog commentsDialog = new CommentsDialog(activity,
						posts.get(position).getComments(), post);
				commentsDialog.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						notifyDataSetChanged();
					}
				});
				commentsDialog.show();
			}
		});
		holder.likeButton.setOnClickListener(new OnClickListener() {
			private boolean isLike = true;

			@Override
			public void onClick(View v) {
				final SuperActivityToast superActivityToast = new SuperActivityToast(
						activity, SuperToast.Type.PROGRESS);
				superActivityToast.setIndeterminate(true);
				superActivityToast.setProgressIndeterminate(true);
				if (post.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
					isLike = false;
//					superActivityToast.setText("Unliking the post");
					NotificationProgress.showNotificationProgress(activity, "Unliking the post", GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
				} else {
//					superActivityToast.setText("Liking the post");
					NotificationProgress.showNotificationProgress(activity, "Liking the post", GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
				}
//				superActivityToast.show();
				new AddLikesApi(activity, UserDataStore.getStore()
						.getAccessKey(), post.getPostId(), UserDataStore
						.getStore().getUserId(), post.getPostedBy(), null,
						new APIListener() {

							@Override
							public void onAPIStatus(boolean status) {
//								if (superActivityToast.isShowing()) {
//									superActivityToast.dismiss();
//								}
								NotificationProgress.clearNotificationProgress(GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
								if (status) {
									if (isLike) {
										Crouton.makeText(activity,
												"Post liked!", Style.INFO)
												.show();
									} else {
										Crouton.makeText(activity,
												"Post unliked!", Style.INFO)
												.show();
									}
									notifyDataSetChanged();
								} else {
									Crouton.makeText(
											activity,
											"Post couldn't be liked. Try again",
											Style.ALERT).show();
								}
							}
						}).execute("");
			}
		});

		rowView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, PostActivity.class);
				intent.putExtra("postId", post.getPostId());
				activity.startActivity(intent);
				activity.finish();
				activity.overridePendingTransition(
						R.anim.activity_slide_in_left,
						R.anim.activity_slide_out_left);
			}
		});

		return rowView;
	}

	public String method(String str) {
		if (str.length() > 0 && str.charAt(str.length() - 1) == 'x') {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

}
