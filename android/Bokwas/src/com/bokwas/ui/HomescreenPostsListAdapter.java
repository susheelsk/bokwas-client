package com.bokwas.ui;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokwas.PostActivity;
import com.bokwas.R;
import com.bokwas.apirequests.AddLikesApi;
import com.bokwas.apirequests.DeleteApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.dialogboxes.CommentsDialog;
import com.bokwas.dialogboxes.LikesDialog;
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
	private boolean isLike = true;
	private PostShare postShare;

	public interface PostShare {
		public void onPostShare(int position);
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
		notifyDataSetChanged();
	}

	public HomescreenPostsListAdapter(Activity activity, List<Post> posts, PostShare postShare) {
		super(activity, R.layout.post_list_item, posts);
		this.activity = activity;
		this.posts = posts;
		this.postShare = postShare;
	}

	static class ViewHolder {
		public TextView name;
		public TextView postText;
		public TextView time;
		public TextView commentSize;
		public TextView likeSize;
		public ImageView picture;
		public ImageView optionsButton;
		public RelativeLayout commentButton;
		public RelativeLayout likeButton;
	}

	@Override
	public int getCount() {
		return posts.size();
	}

	@Override
	public int getItemViewType(int position) {
		if (posts.get(position).isBokwasPost()) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
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
	public View getView(final int position, final View convertView, ViewGroup parent) {
		View rowView = convertView;
		final Post post = posts.get(position);
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			if (getItemViewType(position) == 0) {
				rowView = inflater.inflate(R.layout.post_list_item_bokwas, null);
			} else {
				rowView = inflater.inflate(R.layout.post_list_item_new, null);
			}

			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) rowView.findViewById(R.id.post_name);
			viewHolder.postText = (TextView) rowView.findViewById(R.id.post_content);
			viewHolder.time = (TextView) rowView.findViewById(R.id.post_time);
			viewHolder.commentSize = (TextView) rowView.findViewById(R.id.post_comment_number);
			viewHolder.likeSize = (TextView) rowView.findViewById(R.id.post_like_number);
			viewHolder.picture = (ImageView) rowView.findViewById(R.id.post_profile_pic);
			viewHolder.optionsButton = (ImageView) rowView.findViewById(R.id.overflowButton);
			viewHolder.commentButton = (RelativeLayout) rowView.findViewById(R.id.post_comment_button);
			viewHolder.likeButton = (RelativeLayout) rowView.findViewById(R.id.post_like_button);
			rowView.setTag(viewHolder);
		}
		ViewHolder holder = (ViewHolder) rowView.getTag();

		String postText = post.getPostText();
		if (postText.length() > 250) {
			holder.postText.setText(postText.subSequence(0, 250) + " ...");
		} else {
			holder.postText.setText(postText);
		}

		Date date = new Date(post.getTimestamp());
		String dateString = DateUtil.getSimpleTime(date);
		holder.time.setText(dateString);
		holder.commentSize.setText(String.valueOf(post.getComments().size()));
		List<Likes> likes = post.getLikes();
		if (likes.size() > 0) {
			holder.likeSize.setText(String.valueOf(likes.size()));
		} else {
			holder.likeSize.setText(String.valueOf(0));
		}

		if (post.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			holder.likeButton.findViewById(R.id.like_image).setBackgroundResource(R.drawable.facebook_icon_enable);
		} else {
			holder.likeButton.findViewById(R.id.like_image).setBackgroundResource(R.drawable.like_icon);
		}

		if (post.isBokwasPost()) {
			holder.name.setText(post.getName());
			String avatarId = post.getAvatarId();
			holder.picture.setImageBitmap(GeneralUtil.getImageBitmap(GeneralUtil.getAvatarResourceId(avatarId), activity));
		} else {
			String url = post.getProfilePicture();
			holder.name.setText(post.getName());
			UrlImageViewHelper.setUrlDrawable(holder.picture, url, null, 60000 * 100);
		}

		holder.optionsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View view) {
				PopupMenu popup = new PopupMenu(activity, view);
				popup.getMenuInflater().inflate(R.menu.post_menu, popup.getMenu());
				MenuItem deleteItem = popup.getMenu().findItem(R.id.post_delete);
				MenuItem showLikesItem = popup.getMenu().findItem(R.id.post_show_like);
				if (post.isBokwasPost() && post.getPostedBy().equals(UserDataStore.getStore().getUserId())) {
					deleteItem.setVisible(true);
				} else {
					deleteItem.setVisible(false);
				}
				if (post.getLikes().size() < 1) {
					showLikesItem.setVisible(false);
				}
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						switch (item.getItemId()) {
						case R.id.post_share:

							try {
								if (postShare != null) {
									postShare.onPostShare(position);
								}
							} catch (Exception e) {
								Crouton.makeText(activity, "Post couldn't be shared. Try again", Style.ALERT).show();
								e.printStackTrace();
							}

							break;
						case R.id.post_like:
							likePost(post);
							break;
						case R.id.post_comment:
							CommentsDialog commentsDialog = new CommentsDialog(activity, posts.get(position).getComments(), post);
							commentsDialog.setOnDismissListener(new OnDismissListener() {

								@Override
								public void onDismiss(DialogInterface dialog) {
									notifyDataSetChanged();
								}
							});
							commentsDialog.show();
							break;
						case R.id.post_delete:
							deletePost(post);
							break;
						case R.id.post_show_like:
							showLikes(post);
							break;
						}
						return true;
					}

				});
				popup.show();
			}
		});

		holder.commentButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// CommentsDialog commentsDialog = new CommentsDialog(activity,
				// posts.get(position).getComments(), post);
				// commentsDialog.setOnDismissListener(new OnDismissListener() {
				//
				// @Override
				// public void onDismiss(DialogInterface dialog) {
				// notifyDataSetChanged();
				// }
				// });
				// commentsDialog.show();
				Intent intent = new Intent(activity, PostActivity.class);
				intent.putExtra("postId", post.getPostId());
				activity.startActivity(intent);
				activity.finish();
				activity.overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
			}
		});
		holder.likeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				likePost(post);
			}
		});

		rowView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, PostActivity.class);
				intent.putExtra("postId", post.getPostId());
				activity.startActivity(intent);
				activity.finish();
				activity.overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
			}
		});

		return rowView;
	}

	private void showLikes(Post post) {
		LikesDialog likesDialog = new LikesDialog(activity, post.getLikes());
		likesDialog.show();
	}

	private void deletePost(Post post) {

		final SuperActivityToast superActivityToast = new SuperActivityToast(activity, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);
		NotificationProgress.showNotificationProgress(activity, "Deleting the post", GeneralUtil.NOTIFICATION_PROGRESS_DELETEPOST);
		new DeleteApi(UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), null, activity, new APIListener() {

			@Override
			public void onAPIStatus(boolean status) {
				NotificationProgress.clearNotificationProgress(GeneralUtil.NOTIFICATION_PROGRESS_DELETEPOST);
				if (status) {
					Crouton.makeText(activity, "Post deleted!", Style.INFO).show();
					notifyDataSetChanged();
				} else {
					Crouton.makeText(activity, "Post couldn't be deleted. Try again", Style.ALERT).show();
				}
			}
		}).execute("");

	}

	private void likePost(Post post) {
		final SuperActivityToast superActivityToast = new SuperActivityToast(activity, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);
		if (post.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			isLike = false;
			// superActivityToast.setText("Unliking the post");
			NotificationProgress.showNotificationProgress(activity, "Unliking the post", GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
		} else {
			// superActivityToast.setText("Liking the post");
			NotificationProgress.showNotificationProgress(activity, "Liking the post", GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
		}
		// superActivityToast.show();
		new AddLikesApi(activity, UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), post.getPostedBy(), null, new APIListener() {

			@Override
			public void onAPIStatus(boolean status) {
				// if (superActivityToast.isShowing()) {
				// superActivityToast.dismiss();
				// }
				NotificationProgress.clearNotificationProgress(GeneralUtil.NOTIFICATION_PROGRESS_ADDLIKES);
				if (status) {
					if (isLike) {
						Crouton.makeText(activity, "Post liked!", Style.INFO).show();
					} else {
						Crouton.makeText(activity, "Post unliked!", Style.INFO).show();
					}
					notifyDataSetChanged();
				} else {
					Crouton.makeText(activity, "Post couldn't be liked. Try again", Style.ALERT).show();
				}
				isLike = true;
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
