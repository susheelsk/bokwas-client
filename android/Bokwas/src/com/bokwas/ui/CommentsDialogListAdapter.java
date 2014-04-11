package com.bokwas.ui;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokwas.R;
import com.bokwas.apirequests.AddLikesApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Comment;
import com.bokwas.response.Post;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;

public class CommentsDialogListAdapter extends ArrayAdapter<Comment> {

	private Activity activity;
	private List<Comment> comments;
	private Post post;

	public CommentsDialogListAdapter(Activity activity, List<Comment> comments,Post post) {
		super(activity, R.layout.comment_dialog, comments);
		this.activity = activity;
		this.comments = comments;
		this.post = post;
	}

	static class ViewHolder {
		public TextView name;
		public TextView postText;
		public TextView time;
		public RelativeLayout commentLayout;
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
			rowView = inflater.inflate(R.layout.post_list_item, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) rowView.findViewById(R.id.post_name);
			viewHolder.postText = (TextView) rowView
					.findViewById(R.id.post_content);
			viewHolder.time = (TextView) rowView.findViewById(R.id.post_time);
			viewHolder.commentLayout = (RelativeLayout) rowView
					.findViewById(R.id.post_comment_button);
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

		// Timestamp stamp = new Timestamp(comment.getTimestamp());
		Date date = new Date(comment.getTimestamp());
		String dateString = DateUtil.formatToYesterdayOrToday(date);
		holder.time.setText(dateString);

		holder.commentLine.setVisibility(View.INVISIBLE);
		holder.commentLayout.setVisibility(View.INVISIBLE);

		String temp = comment.getLikes();
		String[] likes = null;
		if (temp != null && !temp.trim().equals("") && !temp.trim().equals(",")) {
			temp = method(temp);
			likes = temp.split(",");
			holder.likeSize.setText(String.valueOf(likes.length));
		} else {
			holder.likeSize.setText(String.valueOf(0));
		}
		
		if(comment.getLikes().contains(UserDataStore.getStore().getUserId())) {
			holder.likeLayout.setClickable(false);
			holder.likeLayout.setEnabled(false);
			holder.likeLayout.findViewById(R.id.like_image).setBackgroundResource(R.drawable.facebook_icon_enable);
		}

		if (comment.getCommentedBy().equals(
				UserDataStore.getStore().getUserId())) {
			holder.name.setText(UserDataStore.getStore().getBokwasName());
			String avatarId = String.valueOf(UserDataStore.getStore().getAvatarId());
			holder.picture.setImageBitmap(GeneralUtil.getImageBitmap(
					GeneralUtil.getAvatarResourceId(avatarId), activity));
		} else {
			holder.name.setText(UserDataStore.getStore()
					.getFriend(comment.getCommentedBy()).getBokwasName());
			String avatarId = UserDataStore.getStore()
					.getFriend(comment.getCommentedBy()).getBokwasAvatarId();
			holder.picture.setImageBitmap(GeneralUtil.getImageBitmap(
					GeneralUtil.getAvatarResourceId(avatarId), activity));
		}
		
		holder.likeLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final ProgressDialog pdia = new ProgressDialog(activity);
				pdia.setMessage("Liking the post");
				pdia.setCancelable(false);
				pdia.show();
				new AddLikesApi(activity, UserDataStore.getStore()
						.getUserAccessToken(), post.getPostId(), UserDataStore
						.getStore().getUserId(), post.getPostedBy(), comment.getCommentId(),
						new APIListener() {

							@Override
							public void onAPIStatus(boolean status) {
								if (pdia.isShowing()) {
									pdia.dismiss();
								}
								if (status) {
									Toast.makeText(activity, "Post liked!",
											Toast.LENGTH_SHORT).show();
									notifyDataSetChanged();
								} else {
									Toast.makeText(
											activity,
											"Post couldn't be liked. Try again",
											Toast.LENGTH_SHORT).show();
								}
							}
						}).execute("");
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