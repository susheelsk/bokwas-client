package com.bokwas.ui;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokwas.FullScreenImage;
import com.bokwas.PostActivity;
import com.bokwas.ProfileActivityExperimental;
import com.bokwas.R;
import com.bokwas.apirequests.AddLikesApi;
import com.bokwas.apirequests.DeleteApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.apirequests.ReportAbuseApi;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.dialogboxes.CommentsDialog;
import com.bokwas.dialogboxes.LikesDialog;
import com.bokwas.response.Likes;
import com.bokwas.response.Post;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.squareup.picasso.Picasso;

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
		super(activity, R.layout.post_list_item_new, posts);
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
		public ImageView profilePicture;
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
		Post post = posts.get(position);
		if (post.isBokwasPost()) {
			return 0;
		} else if (post.getType().equals("status")) {
			if (post.isBokwasPost() && !UserDataStore.getStore().isPostFromFriendOrMe(post)) {
				return 3;
			}
			return 1;
		} else {
			return 2;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public Post getItem(int position) {
		return posts.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public static float convertDpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}

	@Override
	public View getView(final int position, final View convertView, ViewGroup parent) {
		View rowView = convertView;
		final Post post = posts.get(position);
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			if (getItemViewType(position) == 0) {
				rowView = inflater.inflate(R.layout.post_list_item_bokwas, null);
			} else if (getItemViewType(position) == 1) {
				rowView = inflater.inflate(R.layout.post_list_item_new, null);
			} else if (getItemViewType(position) == 2) {
				rowView = inflater.inflate(R.layout.post_list_item_picture, null);
			} else if (getItemViewType(position) == 3) {
				rowView = inflater.inflate(R.layout.post_list_item_picture, null);
				rowView.findViewById(R.id.post_like_button).setVisibility(View.GONE);
				rowView.findViewById(R.id.post_comment_button).setVisibility(View.GONE);
			}

			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) rowView.findViewById(R.id.post_name);
			viewHolder.postText = (TextView) rowView.findViewById(R.id.post_content);
			viewHolder.time = (TextView) rowView.findViewById(R.id.post_time);
			viewHolder.commentSize = (TextView) rowView.findViewById(R.id.post_comment_number);
			viewHolder.likeSize = (TextView) rowView.findViewById(R.id.post_like_number);
			viewHolder.profilePicture = (ImageView) rowView.findViewById(R.id.post_profile_pic);
			viewHolder.optionsButton = (ImageView) rowView.findViewById(R.id.overflowButton);
			viewHolder.commentButton = (RelativeLayout) rowView.findViewById(R.id.post_comment_button);
			viewHolder.likeButton = (RelativeLayout) rowView.findViewById(R.id.post_like_button);
			viewHolder.picture = (ImageView) rowView.findViewById(R.id.post_picture);
			rowView.setTag(viewHolder);
		}
		
		ViewHolder holder = (ViewHolder) rowView.getTag();
		String postText = post.getPostText();
		if (postText.length() > 250) {
			holder.postText.setText(postText.subSequence(0, 250) + " ...");
		} else {
			holder.postText.setText(postText);
		}

		try {
			if (post.getType().equals("photo") && post.getPicture() != null && !post.getPicture().equals("")) {
//			UrlImageViewHelper.setUrlDrawable(holder.picture, post.getPicture(), R.drawable.placeholder, 60000 * 100);
				Picasso.with(activity).load(post.getPicture()).resize((int)convertDpToPixel(250, activity),(int) convertDpToPixel(250, activity)).centerCrop().placeholder(R.drawable.placeholder).into(holder.picture);
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		
		holder.picture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, FullScreenImage.class);
				intent.putExtra("url", post.getPicture());
				intent.putExtra("activity", activity.getClass().getSimpleName());
				intent.putExtra("profileId", post.getPostedBy());
				intent.putExtra("name", post.getName());
				if (post.isBokwasPost()) {
					intent.putExtra("avatarId", Integer.valueOf(post.getAvatarId()));
				} else {
					intent.putExtra("fbProfilePic", post.getProfilePicture());
				}
				activity.startActivity(intent);
				activity.finish();
			}
		});

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

		try {
			if (post.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
				holder.likeButton.findViewById(R.id.like_image).setBackgroundResource(R.drawable.facebook_icon_enable);
			} else {
				holder.likeButton.findViewById(R.id.like_image).setBackgroundResource(R.drawable.like_icon);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (post.isBokwasPost()) {
			holder.name.setText(post.getName());
			String avatarId = post.getAvatarId();
			holder.profilePicture.setImageBitmap(GeneralUtil.getImageBitmap(GeneralUtil.getAvatarResourceId(avatarId), activity));
		} else {
			String url = post.getProfilePicture();
			holder.name.setText(post.getName());
			UrlImageViewHelper.setUrlDrawable(holder.profilePicture, url, null, 60000 * 100);
		}

		holder.name.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, ProfileActivityExperimental.class);
				intent.putExtra("profileId", post.getPostedBy());
				intent.putExtra("name", post.getName());
				if (post.isBokwasPost()) {
					intent.putExtra("avatarId", Integer.valueOf(post.getAvatarId()));
				} else {
					intent.putExtra("fbProfilePic", post.getProfilePicture());
				}

				activity.startActivity(intent);
				activity.finish();
				activity.overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);

			}
		});

		holder.profilePicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, ProfileActivityExperimental.class);
				intent.putExtra("profileId", post.getPostedBy());
				intent.putExtra("name", post.getName());
				if (post.isBokwasPost()) {
					intent.putExtra("avatarId", Integer.valueOf(post.getAvatarId()));
				} else {
					intent.putExtra("fbProfilePic", post.getProfilePicture());
				}

				activity.startActivity(intent);
				activity.finish();
				activity.overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);

			}
		});

		holder.optionsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View view) {
				PopupMenu popup = new PopupMenu(activity, view);
				popup.getMenuInflater().inflate(R.menu.post_menu, popup.getMenu());
				MenuItem deleteItem = popup.getMenu().findItem(R.id.post_delete);
				MenuItem commentItem = popup.getMenu().findItem(R.id.post_comment);
				MenuItem reportAbuseItem = popup.getMenu().findItem(R.id.post_report_abuse);
				MenuItem likeItem = popup.getMenu().findItem(R.id.post_like);
				commentItem.setVisible(false);
				MenuItem showLikesItem = popup.getMenu().findItem(R.id.post_show_like);
				if(!post.isBokwasPost()) {
					reportAbuseItem.setVisible(false);
				}
				if (!UserDataStore.getStore().isPostFromFriendOrMe(post)) {
					likeItem.setVisible(false);
				}
				if (post.isBokwasPost() && post.getPostedBy().equals(UserDataStore.getStore().getUserId())) {
					deleteItem.setVisible(true);
					reportAbuseItem.setVisible(false);
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
						case R.id.post_report_abuse:
							reportAbuse(post);
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
				intent.putExtra("activity", activity.getClass().getSimpleName());
				activity.startActivity(intent);
				activity.finish();
				activity.overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
			}
		});
		holder.likeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				view.setClickable(false);
				likePost(post);
			}
		});

		rowView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, PostActivity.class);
				intent.putExtra("postId", post.getPostId());
				intent.putExtra("activity", activity.getClass().getSimpleName());
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

	@SuppressWarnings({ "deprecation", "unused" })
	private void scaleImage(ImageView view, int boundBoxInDp) {
		// Get the ImageView and its bitmap
		Drawable drawing = view.getDrawable();
		Bitmap bitmap = ((BitmapDrawable) drawing).getBitmap();

		// Get current dimensions
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		// Determine how much to scale: the dimension requiring less scaling is
		// closer to the its side. This way the image always stays inside your
		// bounding box AND either x/y axis touches it.
		float xScale = ((float) boundBoxInDp) / width;
		float yScale = ((float) boundBoxInDp) / height;
		float scale = (xScale <= yScale) ? xScale : yScale;

		// Create a matrix for the scaling and add the scaling data
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		// Create a new bitmap and convert it to a format understood by the
		// ImageView
		Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		BitmapDrawable result = new BitmapDrawable(scaledBitmap);
		width = scaledBitmap.getWidth();
		height = scaledBitmap.getHeight();

		// Apply the scaled bitmap
		view.setImageDrawable(result);

		// Now change ImageView's dimensions to match the scaled image
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
		params.width = width;
		params.height = height;
		view.setLayoutParams(params);
	}

	private void deletePost(final Post post) {

		final SuperActivityToast superActivityToast = new SuperActivityToast(activity, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);
		superActivityToast.show();
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					new DeleteApi(UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), null, activity, new APIListener() {

						@Override
						public void onAPIStatus(boolean status) {
							superActivityToast.dismiss();
							if (status) {
								Crouton.makeText(activity, "Post deleted!", Style.INFO).show();
								notifyDataSetChanged();
							} else {
								Crouton.makeText(activity, "Post couldn't be deleted. Try again", Style.ALERT).show();
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

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Delete the post?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();

	}
	
	private void reportAbuse(final Post post) {

		final SuperActivityToast superActivityToast = new SuperActivityToast(activity, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);
		superActivityToast.show();
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					new ReportAbuseApi(UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), null, activity, new APIListener() {

						@Override
						public void onAPIStatus(boolean status) {
							superActivityToast.dismiss();
							if (status) {
								Crouton.makeText(activity, "Reported!", Style.INFO).show();
								notifyDataSetChanged();
							} else {
								Crouton.makeText(activity, "Something went wrong", Style.ALERT).show();
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

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Do you want to report this post as abuse?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();

	}

	private void likePost(Post post) {
		final SuperActivityToast superActivityToast = new SuperActivityToast(activity, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);
		if (post.isAlreadyLiked(UserDataStore.getStore().getUserId())) {
			isLike = false;
			// superActivityToast.setText("Unliking the post");
		} else {
			// superActivityToast.setText("Liking the post");
		}
		// superActivityToast.show();
		new AddLikesApi(activity, UserDataStore.getStore().getAccessKey(), post.getPostId(), UserDataStore.getStore().getUserId(), post.getPostedBy(), null, new APIListener() {

			@Override
			public void onAPIStatus(boolean status) {
				// if (superActivityToast.isShowing()) {
				// superActivityToast.dismiss();
				// }
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
