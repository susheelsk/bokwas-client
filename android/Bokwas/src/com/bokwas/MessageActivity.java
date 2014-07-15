package com.bokwas;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.apirequests.SendMessageApi;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.ui.MessageListAdapter;
import com.bokwas.util.GeneralUtil;
import com.bokwas.util.NotificationProgress;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MessageActivity extends Activity implements OnClickListener {

	private EditText editText;
	private String receiverId;
	private SuperActivityToast superActivityToast;
	private ListView listView;
	private MessageListAdapter adapter;
	private BroadcastReceiver receiver;
	private Button messageButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.message_page);

		receiverId = getIntent().getStringExtra("receiverId");
		Log.d("BokwasNotifications", "Opening MessageActivity");
		if (getIntent().getBooleanExtra("fromNoti", false)) {
			Log.d("BokwasNotifications", "fromNoti : " + true);
			UserDataStore.initData(this);
			NotificationProgress.clearNotification(this, GeneralUtil.GENERAL_NOTIFICATIONS);
		}

		if (receiverId == null || receiverId.equals("")) {
			onBackPressed();
		}

		setOnClickListeners();

		setupUI();

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

	}
	
	@Override
	protected void onDestroy() {
	  super.onDestroy();
	  unregisterReceiver(receiver);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MessageFriendsActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_right);
		finish();
	}

	private void setupUI() {
		findViewById(R.id.messageHeaderButton).setVisibility(View.GONE);
		((TextView) findViewById(R.id.titlebar)).setText(UserDataStore.getStore().getFriend(receiverId).getBokwasName());

		editText = (EditText) findViewById(R.id.message_edittext);
		listView = (ListView) findViewById(R.id.message_list);
		adapter = new MessageListAdapter(this, UserDataStore.getStore().getMessagesForPerson(receiverId));
		listView.setAdapter(adapter);
	}

	private void setOnClickListeners() {
		messageButton = (Button) findViewById(R.id.messageButton);
		findViewById(R.id.messageButton).setOnClickListener(this);
		findViewById(R.id.overflowButton).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.messageButton) {
			if (editText.getText().toString().trim() != null && !editText.getText().toString().trim().equals("")) {
				superActivityToast = new SuperActivityToast(MessageActivity.this, SuperToast.Type.PROGRESS);
				messageButton.setClickable(false);
				superActivityToast.setText("Sending message");
				superActivityToast.setIndeterminate(true);
				superActivityToast.setProgressIndeterminate(true);
				superActivityToast.show();
				new SendMessageApi(this, UserDataStore.getStore().getUserId(), receiverId, UserDataStore.getStore().getAccessKey(), editText.getText().toString(), new APIListener() {

					@Override
					public void onAPIStatus(boolean status) {
						superActivityToast.dismiss();
						setupUI();
						messageButton.setClickable(true);
						editText.setText("");
						if (status) {

						} else {
							Crouton.makeText(MessageActivity.this, "Something went wrong. Try again", Style.ALERT).show();
						}
					}
				}).execute("");
			}
		} else if (view.getId() == R.id.overflowButton) {
			PopupMenu popup = new PopupMenu(this, view);

			popup.getMenu().add(4, 4, 4, "Home");
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {
					case 4:
						Intent intent1 = new Intent(MessageActivity.this, HomescreenActivity.class);
						startActivity(intent1);
						overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
						finish();
						break;
					}
					return true;
				}
			});
			popup.show();
		}
	}

}
