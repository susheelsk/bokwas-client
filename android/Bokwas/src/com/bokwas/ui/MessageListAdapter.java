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
import android.widget.TextView;

import com.bokwas.R;
import com.bokwas.datasets.Friends;
import com.bokwas.datasets.Message;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;

public class MessageListAdapter extends ArrayAdapter<Message> {

	private List<Message> messages;
	private Activity activity;

	public MessageListAdapter(Activity activity, List<Message> message) {
		super(activity, R.layout.notification_list_item, message);
		this.messages = message;
		this.activity = activity;
	}

	static class ViewHolder {
		public TextView time;
		public TextView message;
		public ImageView picture;
	}
	
	@Override
	public int getItemViewType(int position) {
		Message message = messages.get(position);
		if(message.getFromId().equals(UserDataStore.getStore().getUserId())) {
			return 0;
		}else {
			return 1;
		}
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getCount() {
		return messages.size();
	}

	@Override
	public Message getItem(int position) {
		return messages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		final Message message = messages.get(position);
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			if (getItemViewType(position) == 0) {
				rowView = inflater.inflate(R.layout.message_list_item_ours, null);
			}else {
				rowView = inflater.inflate(R.layout.message_list_item_theirs, null);
			}
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.picture = (ImageView) rowView.findViewById(R.id.post_profile_pic);
			viewHolder.time = (TextView) rowView.findViewById(R.id.post_time);
			viewHolder.message = (TextView) rowView.findViewById(R.id.post_content);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		String messageText = "", avatarId = "";
		long time = 0;

		if(message.getFromId().equals(UserDataStore.getStore().getUserId())) {
			avatarId = String.valueOf(UserDataStore.getStore().getAvatarId());
		}else {
			Friends friend = UserDataStore.getStore().getFriend(message.getFromId());
			avatarId = friend.getBokwasAvatarId();
		}
		messageText = message.getMessage();
		time = message.getTimestamp();
		
		holder.message.setText(messageText);
		holder.time.setText(DateUtil.getSimpleTime(new Date(time)));
		holder.picture.setImageResource(GeneralUtil.getAvatarResourceId(avatarId));

		rowView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
			}
		});

		return rowView;
	}

}
