package com.bokwas;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.bokwas.datasets.Friends;
import com.bokwas.datasets.Message;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.ui.MessageFriendsListAdapter;
import com.bokwas.util.GeneralUtil;
import com.bokwas.util.TrackerName;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class MessageFriendsActivity extends Activity{
	
	private ListView listview;
	private MessageFriendsListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_friends_page);
		
		setupUI();
		
		setupGoogleAnalytics();
	}
	
	private void setupGoogleAnalytics() {
		Tracker t = GeneralUtil.getTracker(TrackerName.APP_TRACKER,this);
		t.enableAutoActivityTracking(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
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
		listview = (ListView) findViewById(R.id.friend_list);
		List<Friends> friends = UserDataStore.getStore().getFriends();
		Collections.sort(friends,new MessageFriendsComparator());
		adapter = new MessageFriendsListAdapter(this, friends);
		listview.setAdapter(adapter);
		
		findViewById(R.id.overflowButton).setVisibility(View.GONE);
		findViewById(R.id.messageHeaderButton).setVisibility(View.GONE);
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this,HomescreenActivity.class);
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
			if(messageA!= null && messageA.size()>0) {
				dateA = new Date(messageA.get(messageA.size()-1).getTimestamp());
			}else {
				dateA = new Date(0);
			}
			if(messageB!= null && messageB.size()>0) {
				dateB = new Date(messageB.get(messageB.size()-1).getTimestamp());
			}else {
				dateB = new Date(0);
			}
			
			return dateB.compareTo(dateA);
		}
	}

}
