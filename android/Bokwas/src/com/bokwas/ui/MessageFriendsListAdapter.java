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

import com.bokwas.MessageActivity;
import com.bokwas.R;
import com.bokwas.datasets.Friends;
import com.bokwas.datasets.Message;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;

public class MessageFriendsListAdapter extends ArrayAdapter<Friends> {

	private List<Friends> friends;
	private Activity activity;

	public MessageFriendsListAdapter(Activity activity, List<Friends> friends) {
		super(activity, R.layout.notification_list_item, friends);
		this.friends = friends;
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
		return friends.size();
	}

	@Override
	public Friends getItem(int position) {
		return friends.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		final Friends friend = friends.get(position);
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

		name = friend.getBokwasName();
		avatarId = friend.getBokwasAvatarId();
		
		List<Message> messages = UserDataStore.getStore().getMessagesForPerson(friend.getId());
		
		if(messages!=null && messages.size()>0) {
			time = messages.get(messages.size()-1).getTimestamp();
			message = messages.get(messages.size()-1).getMessage();
		}
		
		holder.message.setText(message);
		holder.name.setText(name);
		holder.time.setText(DateUtil.getSimpleTime(new Date(time)));
		holder.picture.setImageResource(GeneralUtil.getAvatarResourceId(avatarId));

		rowView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity,MessageActivity.class);
				intent.putExtra("receiverId", friend.id);
				activity.overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
				activity.startActivity(intent);
				activity.finish();
			}
		});

		return rowView;
	}

}
