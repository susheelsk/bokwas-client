package com.bokwas.ui;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokwas.PostActivity;
import com.bokwas.R;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Notification;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;

public class NotificationDialogListAdapter extends ArrayAdapter<Notification> {

	private List<Notification> notifications;
	private Activity activity;

	public NotificationDialogListAdapter(Activity activity, List<Notification> notifications) {
		super(activity, R.layout.notification_list_item, notifications);
		this.notifications = notifications;
		this.activity = activity;
		for (int i = 0; i < UserDataStore.getStore().getNotifications().size(); i++) {
			UserDataStore.getStore().getNotifications().get(i).setViewed(true);
		}
		UserDataStore.getStore().save(activity);
	}

	static class ViewHolder {
		public TextView name;
		public TextView time;
		public TextView message;
		public ImageView picture;
	}

	@Override
	public int getCount() {
		return notifications.size();
	}

	@Override
	public Notification getItem(int position) {
		return notifications.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		final Notification notification = notifications.get(position);
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.notification_list_item, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) rowView.findViewById(R.id.post_name);
			viewHolder.picture = (ImageView) rowView.findViewById(R.id.post_profile_pic);
			viewHolder.time = (TextView) rowView.findViewById(R.id.post_time);
			viewHolder.message = (TextView) rowView.findViewById(R.id.post_content);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		String name = "", message = "", avatarId = "";
		long time = 0;

		final String postId = notification.getNotification_data().get("postId");
		if (notification.getNotification_data().get("type").equals("ADDCOMMENT_NOTI")) {
			name = notification.getNotification_data().get("commentPersonBokwasName");
			avatarId = notification.getNotification_data().get("avatarId");
			message = notification.getNotification_data().get("message");

			time = notification.getTimestamp();
		} else if (notification.getNotification_data().get("type").equals("ADDLIKES_NOTI")) {
			name = notification.getNotification_data().get("likesPersonName");
			avatarId = notification.getNotification_data().get("avatarId");
			message = notification.getNotification_data().get("message");
			time = notification.getTimestamp();
		}
		holder.message.setText(message);
		holder.name.setText(name);
		holder.time.setText(DateUtil.getSimpleTime(new Date(time)));
		holder.picture.setImageResource(GeneralUtil.getAvatarResourceId(avatarId));

		rowView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (postId == null || postId.equals("")) {
					return;
				} else {
					// UserDataStore.getStore().removeNotification(notification.getNotification_id());
					// UserDataStore.getStore().save(activity);
					Intent i = new Intent(activity, PostActivity.class);
					i.putExtra("postId", postId);
					activity.startActivity(i);
					activity.overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
					activity.finish();
				}
			}
		});

		return rowView;
	}

}
