package com.bokwas.ui;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokwas.R;
import com.bokwas.apirequests.AddLikesApi;
import com.bokwas.apirequests.DeleteApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Comment;
import com.bokwas.response.Likes;
import com.bokwas.response.Post;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;
import com.bokwas.util.NotificationProgress;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CommentsDialogListAdapter extends ArrayAdapter<Comment> {

	private Activity activity;
	private List<Comment> comments;
	private Post post;

	public CommentsDialogListAdapter(Activity activity, List<Comment> comments,
			Post post) {
		super(activity, R.layout.comment_dialog, comments);
		this.activity = activity;
		this.comments = comments;
		this.post = post;
	}

	static class ViewHolder {
		public TextView name;
		public TextView postText;
		public TextView time;
		public RelativeLayout deleteLayout;
		public RelativeLayout likeLayout;
		public View commentLine;
		public TextView likeSize;
		public ImageView picture;
	}

	@Override
	public int getCount() {
		return comments.size();
	}

	@Override
	public Comment getItem(int position) {
		return comments.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.comment_list_item, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) rowView.findViewById(R.id.post_name);
			viewHolder.postText = (TextView) rowView
					.findViewById(R.id.post_content);
			viewHolder.time = (TextView) rowView.findViewById(R.id.post_time);
			viewHolder.deleteLayout = (RelativeLayout) rowView
					.findViewById(R.id.post_delete_button);
			viewHolder.likeLayout = (RelativeLayout) rowView
					.findViewById(R.id.post_like_button);
			viewHolder.commentLine = (View) rowView
					.findViewById(R.id.comment_line_seperator);
			viewHolder.likeSize = (TextView) rowView
					.findViewById(R.id.post_like_number);
			viewHolder.picture = (ImageView) rowView
					.findViewById(R.id.post_profile_pic);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		final Comment comment = comments.get(position);

		holder.postText.setText(comment.getCommentText());

		Date date = new Date(comment.getTimestamp());
		String dateString = DateUtil.formatToYesterdayOrToday(date);
		holder.time.setText(dateString);

		if (comment.getCommentedBy().equals(
				UserDataStore.getStore().getUserId())) {
			holder.commentLine.setVisibility(View.VISIBLE);
			holder.deleteLayout.setVisibility(View.VISIBLE);
		}else {
//			holder.commentLine.setVisibility(View.GONE);
//			holder.deleteLayout.setVisibility(View.GONE);
		}

		List<Likes> likes = comment.getLikes();
		if (likes.size() > 0) {
			holder.likeSize.setText(String.valueOf(likes.size()));
		} else {
			holder.likeSize.setText(String.valueOf(0));
		}

		if (comment.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			holder.likeLayout.findViewById(R.id.like_image)
					.setBackgroundResource(R.drawable.facebook_icon_enable);
		} else {
			holder.likeLayout.findViewById(R.id.like_image)
					.setBackgroundResource(R.drawable.like_icon);
		}

		// if (comment.getCommentedBy().equals(
		// UserDataStore.getStore().getUserId())) {
		// holder.name.setText(UserDataStore.getStore().getBokwasName());
		// String avatarId = String.valueOf(UserDataStore.getStore()
		// .getAvatarId());
		// holder.picture.setImageBitmap(GeneralUtil.getImageBitmap(
		// GeneralUtil.getAvatarResourceId(avatarId), activity));
		// } else {
		// holder.name.setText(UserDataStore.getStore()
		// .getFriend(comment.getCommentedBy()).getBokwasName());
		// String avatarId = UserDataStore.getStore()
		// .getFriend(comment.getCommentedBy()).getBokwasAvatarId();
		// holder.picture.setImageBitmap(GeneralUtil.getImageBitmap(
		// GeneralUtil.getAvatarResourceId(avatarId), activity));
		// }

		holder.name.setText(comment.getBokwasName());
		holder.picture.setImageBitmap(GeneralUtil.getImageBitmap(
				GeneralUtil.getAvatarResourceId(comment.getAvatarId()),
				activity));

		holder.deleteLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteComment(comment, post);
			}
		});

		holder.likeLayout.setOnClickListener(new OnClickListener() {

			private boolean isLike = true;

			@Override
			public void onClick(View v) {
				final SuperActivityToast superActivityToast = new SuperActivityToast(
						activity, SuperToast.Type.PROGRESS);
				superActivityToast.setIndeterminate(true);
				superActivityToast.setProgressIndeterminate(true);

				if (comment
						.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
					isLike = false;
					NotificationProgress.showNotificationProgress(activity,
							"Unliking the post",
							GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
				} else {
					NotificationProgress.showNotificationProgress(activity,
							"Liking the post",
							GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
				}
				new AddLikesApi(activity, UserDataStore.getStore()
						.getAccessKey(), post.getPostId(), UserDataStore
						.getStore().getUserId(), post.getPostedBy(), comment
						.getCommentId(), new APIListener() {

					@Override
					public void onAPIStatus(boolean status) {
						NotificationProgress
								.clearNotificationProgress(GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
						if (status) {
							if (isLike) {
								Crouton.makeText(activity, "Comment liked!",
										Style.INFO).show();
							} else {
								Crouton.makeText(activity, "Comment unliked!",
										Style.INFO).show();
							}
							notifyDataSetChanged();
						} else {
							Crouton.makeText(activity,
									"Comment couldn't be liked. Try again",
									Style.ALERT).show();
						}
					}
				}).execute("");
			}
		});

		return rowView;
	}

	private void deleteComment(Comment comment, Post post) {
		final SuperActivityToast superActivityToast = new SuperActivityToast(
				activity, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);
		NotificationProgress.showNotificationProgress(activity,
				"Deleting the comment",
				GeneralUtil.NOTIFICATION_PROGRESS_DELETECOMMENT);
		new DeleteApi(UserDataStore.getStore().getAccessKey(),
				post.getPostId(), UserDataStore.getStore().getUserId(),
				comment.getCommentId(), activity, new APIListener() {

					@Override
					public void onAPIStatus(boolean status) {
						NotificationProgress
								.clearNotificationProgress(GeneralUtil.NOTIFICATION_PROGRESS_DELETECOMMENT);
						if (status) {
							Crouton.makeText(activity, "Comment deleted!",
									Style.INFO).show();
							notifyDataSetChanged();
						} else {
							Crouton.makeText(activity,
									"Comment couldn't be deleted. Try again",
									Style.ALERT).show();
						}
					}
				}).execute("");

	}

	public String method(String str) {
		if (str.length() > 0 && str.charAt(str.length() - 1) == 'x') {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

}
