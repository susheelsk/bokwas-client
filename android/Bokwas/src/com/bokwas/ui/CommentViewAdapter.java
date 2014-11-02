package com.bokwas.ui;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokwas.ProfileActivityExperimental;
import com.bokwas.R;
import com.bokwas.apirequests.AddLikesApi;
import com.bokwas.apirequests.DeleteApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.apirequests.ReportAbuseApi;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.dialogboxes.LikesDialog;
import com.bokwas.response.Comment;
import com.bokwas.response.Likes;
import com.bokwas.response.Post;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CommentViewAdapter {

	private Activity activity;
	private Comment comment;
	private Post post;
	private boolean isLike = true;
	private CommentViewListener listener;

	public CommentViewAdapter(Activity activity, Comment comment, Post post, CommentViewListener listener) {
		this.activity = activity;
		this.comment = comment;
		this.post = post;
		this.listener = listener;
	}
	
	public interface CommentViewListener {
		public void onUpdateUI();
	}

	public View getView() {
		View rowView;

		LayoutInflater inflater = activity.getLayoutInflater();
		rowView = inflater.inflate(R.layout.comment_list_item, null);
		rowView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
		TextView name = (TextView) rowView.findViewById(R.id.post_name);
		TextView postText = (TextView) rowView.findViewById(R.id.post_content);
		TextView time = (TextView) rowView.findViewById(R.id.post_time);
		RelativeLayout deleteLayout = (RelativeLayout) rowView.findViewById(R.id.post_delete_button);
		RelativeLayout likeLayout = (RelativeLayout) rowView.findViewById(R.id.post_like_button);
		TextView likeSize = (TextView) rowView.findViewById(R.id.post_like_number);
		ImageView picture = (ImageView) rowView.findViewById(R.id.post_profile_pic);
		ImageView optionsButton = (ImageView) rowView.findViewById(R.id.overflowButton);

		postText.setText(comment.getCommentText());

		Date date = new Date(comment.getTimestamp());
		String dateString = DateUtil.getSimpleTime(date);
		time.setText(dateString);

		optionsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View view) {
				PopupMenu popup = new PopupMenu(activity, view);
				popup.getMenuInflater().inflate(R.menu.comment_menu, popup.getMenu());
				MenuItem deleteItem = popup.getMenu().findItem(R.id.comment_delete);
				MenuItem reportAbuseItem = popup.getMenu().findItem(R.id.comment_report_abuse);
				MenuItem showLikesItem = popup.getMenu().findItem(R.id.comment_show_like);
				if (comment.getCommentedBy().equals(UserDataStore.getStore().getUserId())) {
					deleteItem.setVisible(true);
					reportAbuseItem.setVisible(false);
					
				} else {
					deleteItem.setVisible(false);
				}
				if (comment.getLikes().size() < 1) {
					showLikesItem.setVisible(false);
				}
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						switch (item.getItemId()) {
						case R.id.comment_delete:
							deleteComment(comment, post);
							break;
						case R.id.comment_like:
							likeComment(comment, post);
							break;
						case R.id.comment_show_like:
							showLikes(comment);
							break;
						case R.id.comment_report_abuse:
							reportAbuse(comment,post);
						}
						return true;
					}

				});
				popup.show();
			}
		});

		List<Likes> likes = comment.getLikes();
		if (likes.size() > 0) {
			likeSize.setText(String.valueOf(likes.size()));
		} else {
			likeSize.setText(String.valueOf(0));
		}

		if (comment.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			likeLayout.findViewById(R.id.like_image).setBackgroundResource(R.drawable.facebook_icon_enable);
		} else {
			likeLayout.findViewById(R.id.like_image).setBackgroundResource(R.drawable.like_icon);
		}

		name.setText(comment.getBokwasName());

		name.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, ProfileActivityExperimental.class);
				intent.putExtra("profileId", comment.getCommentedBy());
				intent.putExtra("name", comment.getBokwasName());
				intent.putExtra("avatarId", Integer.valueOf(comment.getAvatarId()));

				activity.startActivity(intent);
				activity.finish();
				activity.overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);

			}
		});

		picture.setImageBitmap(GeneralUtil.getImageBitmap(GeneralUtil.getAvatarResourceId(comment.getAvatarId()), activity));

		picture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, ProfileActivityExperimental.class);
				intent.putExtra("profileId", comment.getCommentedBy());
				intent.putExtra("name", comment.getBokwasName());
				intent.putExtra("avatarId", Integer.valueOf(comment.getAvatarId()));

				activity.startActivity(intent);
				activity.finish();
				activity.overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);

			}
		});

		deleteLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteComment(comment, post);
			}
		});

		likeLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				likeComment(comment, post);
			}
		});

		return rowView;
	}

	private void showLikes(Comment comment) {
		LikesDialog likesDialog = new LikesDialog(activity, comment.getLikes());
		likesDialog.show();
	}

	private void likeComment(Comment comment, Post post) {
		final SuperActivityToast superActivityToast = new SuperActivityToast(activity, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);

		if (comment.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			isLike = false;
		} else {
		}
		new AddLikesApi(activity, UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), post.getPostedBy(), comment.getCommentId(), new APIListener() {

			@Override
			public void onAPIStatus(boolean status) {
				if (status) {
					if (isLike) {
						Crouton.makeText(activity, "Comment liked!", Style.INFO).show();
					} else {
						Crouton.makeText(activity, "Comment unliked!", Style.INFO).show();
					}
					listener.onUpdateUI();
				} else {
					Crouton.makeText(activity, "Comment couldn't be liked. Try again", Style.ALERT).show();
				}
			}
		}).execute("");

	}

	private void deleteComment(final Comment comment, final Post post) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			private SuperActivityToast superActivityToast;

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					superActivityToast = new SuperActivityToast(activity, SuperToast.Type.PROGRESS);
					superActivityToast.setText("Deleting comment");
					superActivityToast.setIndeterminate(true);
					superActivityToast.setProgressIndeterminate(true);
					superActivityToast.show();
					new DeleteApi(UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), comment.getCommentId(), activity, new APIListener() {

						@Override
						public void onAPIStatus(boolean status) {
							superActivityToast.dismiss();
							if (status) {
								Crouton.makeText(activity, "Comment deleted!", Style.INFO).show();
							} else {
								Crouton.makeText(activity, "Comment couldn't be deleted. Try again", Style.ALERT).show();
							}
							listener.onUpdateUI();
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

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Delete the comment?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
		// new DeleteApi(UserDataStore.getStore().getAccessKey(),
		// post.getPostId(), UserDataStore.getStore().getUserId(),
		// comment.getCommentId(), activity, new APIListener() {
		//
		// @Override
		// public void onAPIStatus(boolean status) {
		// NotificationProgress.clearNotificationProgress(GeneralUtil.NOTIFICATION_PROGRESS_DELETECOMMENT);
		// if (status) {
		// Crouton.makeText(activity, "Comment deleted!", Style.INFO).show();
		// notifyDataSetChanged();
		// } else {
		// Crouton.makeText(activity, "Comment couldn't be deleted. Try again",
		// Style.ALERT).show();
		// }
		// }
		// }).execute("");

	}
	
	private void reportAbuse(final Comment comment, final Post post) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			private SuperActivityToast superActivityToast;

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					superActivityToast = new SuperActivityToast(activity, SuperToast.Type.PROGRESS);
					superActivityToast.setText("Reporting");
					superActivityToast.setIndeterminate(true);
					superActivityToast.setProgressIndeterminate(true);
					superActivityToast.show();
					new ReportAbuseApi(UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), comment.getCommentId(), activity, new APIListener() {

						@Override
						public void onAPIStatus(boolean status) {
							superActivityToast.dismiss();
							if (status) {
								Crouton.makeText(activity, "Reported!", Style.INFO).show();
							} else {
								Crouton.makeText(activity, "Something went wrong", Style.ALERT).show();
							}
							listener.onUpdateUI();
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

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Do you want to report the comment as abuse?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
	}

	public String method(String str) {
		if (str.length() > 0 && str.charAt(str.length() - 1) == 'x') {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

}
