package com.bokwas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.bokwas.datasets.Friends;
import com.bokwas.datasets.Message;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.ui.MessageFriendsListAdapter;
import com.bokwas.util.AppData;
import com.bokwas.util.GeneralUtil;
import com.bokwas.util.NotificationProgress;
import com.bokwas.util.TrackerName;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class MessageFriendsActivity extends Activity {

	private ListView listview;
	private MessageFriendsListAdapter adapter;
	private BroadcastReceiver receiver;
	private EditText searchText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_friends_page);

		if (AppData.isReset) {
			Toast.makeText(this, "Please restart the app", Toast.LENGTH_SHORT).show();
			finish();
		}

		IntentFilter filter = new IntentFilter("NEW_MESSAGE");
		filter.addAction("NEW_MESSAGE");
		filter.addAction("SOME_OTHER_ACTION");
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				setupUI();
			}
		};
		registerReceiver(receiver, filter);

		setupUI();

		setupGoogleAnalytics();
	}

	private void setupGoogleAnalytics() {
		Tracker t = GeneralUtil.getTracker(TrackerName.APP_TRACKER, this);
		t.enableAutoActivityTracking(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!UserDataStore.isInitialized()) {
			try {
				UserDataStore.initData(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setupUI();
	}

	@Override
	protected void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}

	private void setupUI() {
		NotificationProgress.clearNotification(this, GeneralUtil.GENERAL_NOTIFICATIONS);
		listview = (ListView) findViewById(R.id.friend_list);
		searchText = (EditText) findViewById(R.id.friend_search_edittext);
		searchText.setSelected(false);
		final List<Friends> friendsList = UserDataStore.getStore().getFriends();
		searchText.addTextChangedListener(new TextWatcher() {

			@SuppressLint("DefaultLocale")
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				int textlength = cs.length();

				List<Friends> tempFriendList = new ArrayList<Friends>();
				for (Friends f : friendsList) {
					if (textlength <= f.getBokwasName().length()) {
						if (f.getBokwasName().toLowerCase().contains(cs.toString().toLowerCase())) {
							tempFriendList.add(f);
						}
					}
				}
				adapter = new MessageFriendsListAdapter(MessageFriendsActivity.this, tempFriendList);
				listview.setAdapter(adapter);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		List<Friends> friends = UserDataStore.getStore().getFriends();
		Collections.sort(friends, new MessageFriendsComparator());
		adapter = new MessageFriendsListAdapter(this, friends);
		listview.setAdapter(adapter);

		if (friends.size() < 1) {
			listview.setVisibility(View.GONE);
			findViewById(R.id.inviteFriendsTextView).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, HomescreenActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_right);
		finish();
	}

	private class MessageFriendsComparator implements Comparator<Friends> {
		public int compare(Friends a, Friends b) {
			List<Message> messageA = UserDataStore.getStore().getMessagesForPerson(a.id);
			List<Message> messageB = UserDataStore.getStore().getMessagesForPerson(b.id);
			Date dateA = new Date();
			Date dateB = new Date();
			if (messageA != null && messageA.size() > 0) {
				dateA = new Date(messageA.get(messageA.size() - 1).getTimestamp());
			} else {
				dateA = new Date(0);
			}
			if (messageB != null && messageB.size() > 0) {
				dateB = new Date(messageB.get(messageB.size() - 1).getTimestamp());
			} else {
				dateB = new Date(0);
			}

			return dateB.compareTo(dateA);
		}
	}

}
