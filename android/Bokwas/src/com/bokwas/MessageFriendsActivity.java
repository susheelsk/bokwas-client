package com.bokwas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.achep.header2actionbar.FadingActionBarHelper;
import com.bokwas.apirequests.GetFriendsApi;
import com.bokwas.apirequests.GetPosts.APIListener;
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

public class MessageFriendsActivity extends FragmentActivity {

	private ListView listview;
	private MessageFriendsListAdapter adapter;
	private BroadcastReceiver receiver;
	private boolean isMessageDialogShown;

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
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_friends_overflow, menu);
 
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.overflow_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				List<Friends> tempFriendList = new ArrayList<Friends>();
				List<Friends> friendsList = UserDataStore.getStore().getFriends();
				int textlength = newText.length();
				for (Friends f : friendsList) {
					if (textlength <= f.getBokwasName().length()) {
						if (f.getBokwasName().toLowerCase().contains(newText.toString().toLowerCase())) {
							tempFriendList.add(f);
						}
					}
				}
				adapter = new MessageFriendsListAdapter(MessageFriendsActivity.this, tempFriendList);
				listview.setAdapter(adapter);
				return false;
			}
		});
        return true;
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
			setupUI();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}

	private void setupUI() {
		NotificationProgress.clearNotification(this, GeneralUtil.GENERAL_NOTIFICATIONS);
		listview = (ListView) findViewById(R.id.friend_list);
		getActionBar().setIcon(null);
		getActionBar().setTitle("bokwas");
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setBackgroundDrawable(new ColorDrawable(R.color.dark_color));
		new FadingActionBarHelper(getActionBar(), getResources().getDrawable(R.drawable.actionbar_bg));
//		searchText = (EditText) findViewById(R.id.friend_search_edittext);
//		searchText.setSelected(false);
//		final List<Friends> friendsList = UserDataStore.getStore().getFriends();
//		searchText.addTextChangedListener(new TextWatcher() {
//
//			@SuppressLint("DefaultLocale")
//			@Override
//			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
//				int textlength = cs.length();
//
//				List<Friends> tempFriendList = new ArrayList<Friends>();
//				for (Friends f : friendsList) {
//					if (textlength <= f.getBokwasName().length()) {
//						if (f.getBokwasName().toLowerCase().contains(cs.toString().toLowerCase())) {
//							tempFriendList.add(f);
//						}
//					}
//				}
//				adapter = new MessageFriendsListAdapter(MessageFriendsActivity.this, tempFriendList);
//				listview.setAdapter(adapter);
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//				// TODO Auto-generated method stub
//
//			}
//		});
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
		if(isMessageDialogShown) {
			return;
		}
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.overflow_refresh) { 
			new GetFriendsApi(this, UserDataStore.getStore().getAccessKey(), UserDataStore.getStore().getUserId(), new APIListener() {
				
				@Override
				public void onAPIStatus(boolean status) {
					if(status) {
						setupUI();
					}
				}
			}).execute("");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
//	private void showMessageDialog(String receiverId) {
//		isMessageDialogShown = true;
//		messageFragment.setReceiverId(receiverId);
//		listview.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				manager = getSupportFragmentManager();
//				transaction = manager.beginTransaction();
//				transaction.show(messageFragment);
//				transaction.commit();
//				messageFragment.setupUI();
//			}
//		}, 250);
//	}
//	
//	private void setupFragmentManager() {
//		manager = getSupportFragmentManager();
//		transaction = manager.beginTransaction();
//		emojiFragment = (EmojiconsFragment) manager.findFragmentById(R.id.emojicons);
//		messageFragment = (MessageDialogFragment) manager.findFragmentById(R.id.messageFragment);
//		transaction.hide(emojiFragment);
//		transaction.hide(messageFragment);
//		transaction.commit();
//	}
//
//	@Override
//	public void onEmojiconBackspaceClicked(View v) {
//		messageFragment.onEmojiconBackspaceClicked();
//	}
//
//	@Override
//	public void onEmojiconClicked(Emojicon emojicon) {
//		messageFragment.onEmojiconClicked(emojicon);
//	}
//
//	@Override
//	public void showEmojis(View view) {
//		InputMethodManager imm = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
//		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//		listview.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				manager = getSupportFragmentManager();
//				transaction = manager.beginTransaction();
//				transaction.show(emojiFragment);
//				transaction.commit();
//			}
//		}, 250);
//	}
//
//	@Override
//	public void hideEmojis(View view) {
//		InputMethodManager imm = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
//		imm.showSoftInput(view, 0);
//		manager = getSupportFragmentManager();
//		transaction = manager.beginTransaction();
//		transaction.hide(emojiFragment);
//		transaction.commit();
//	}
//
//	@Override
//	public void onDissmiss() {
//		isMessageDialogShown = false;
//		manager = getSupportFragmentManager();
//		transaction = manager.beginTransaction();
//		transaction.hide(messageFragment);
//		transaction.commit();
//	}

}
