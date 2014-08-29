package com.bokwas.ui;

import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
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
import com.bokwas.dialogboxes.MessageDialog;
import com.bokwas.util.DateUtil;
import com.bokwas.util.GeneralUtil;
import com.rockerhieu.emojicon.emoji.Emojicon;

public class MessageFriendsListAdapter extends ArrayAdapter<Friends> {

	private List<Friends> friends;
	private FragmentActivity activity;
	protected MessageDialog dialog;

	public MessageFriendsListAdapter(FragmentActivity activity, List<Friends> friends) {
		super(activity, R.layout.message_friends_listitem, friends);
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
		public TextView messageCount;
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
			rowView = inflater.inflate(R.layout.message_friends_listitem, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) rowView.findViewById(R.id.post_name);
			viewHolder.picture = (ImageView) rowView.findViewById(R.id.post_profile_pic);
			viewHolder.time = (TextView) rowView.findViewById(R.id.post_time);
			viewHolder.message = (TextView) rowView.findViewById(R.id.post_content);
			viewHolder.messageCount = (TextView) rowView.findViewById(R.id.messageCount);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		String name = "", message = "", avatarId = "";
		long time = 0;

		name = friend.getBokwasName();
		avatarId = friend.getBokwasAvatarId();

		List<Message> messages = UserDataStore.getStore().getMessagesForPerson(friend.getId());

		if (messages != null && messages.size() > 0) {
			time = messages.get(messages.size() - 1).getTimestamp();
			message = messages.get(messages.size() - 1).getMessage();
		}
		
		holder.time.setTextColor(Color.parseColor("#777777"));
		if(messages.size()>0) {
			holder.time.setText(DateUtil.getSimpleTime(new Date(time)));
		}else {
			holder.time.setText("");
		}

		int unSeenMessageSize = 0;
		for (Message unseenMessage : messages) {
			if (!unseenMessage.isSeen()) {
				unSeenMessageSize++;
			}
		}

		if (unSeenMessageSize > 0) {
			holder.messageCount.setVisibility(View.VISIBLE);
			holder.messageCount.setText(String.valueOf(unSeenMessageSize));
		}
		holder.message.setText(message);
		holder.name.setText(name);
		holder.picture.setImageResource(GeneralUtil.getAvatarResourceId(avatarId));

		rowView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, MessageActivity.class);
				intent.putExtra("receiverId", friend.id);
				activity.overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
				activity.startActivity(intent);
				
//				dialog = new MessageDialog(activity, friend.id);
//				dialog.show();
			}
		});

		return rowView;
	}
	
	public void onEmojiBackClicked() {
		if(dialog!=null) {
			dialog.onEmojiconBackspaceClicked();
		}
	}
	
	public void onEmojiClicked(Emojicon emojicon) {
		if(dialog!=null) {
			dialog.onEmojiconClicked(emojicon);
		}
	}

}
