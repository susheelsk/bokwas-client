package com.bokwas.dialogboxes;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.bokwas.R;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.apirequests.SendMessageApi;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.ui.MessageListAdapter;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MessageDialog extends Dialog implements OnClickListener{
	
	private FragmentActivity activity;
	private MessageListAdapter adapter;
	private EmojiconsFragment emojiFragment;
	private String receiverId;
	private SuperActivityToast superActivityToast;
	private EditText editText;
	private ListView listView;
	private boolean isEmojiShown = false;
	private FragmentManager manager;
	private FragmentTransaction transaction;
	private Button messageButton;

	public MessageDialog(FragmentActivity activity,String receiverId) {
		super(activity);
		this.activity = activity;
		this.receiverId = receiverId;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_dialog);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		Display display = ((WindowManager) activity
				.getSystemService(Activity.WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		Window window = getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.CENTER;
		wlp.width = width;
		window.setAttributes(wlp);
		getWindow().setLayout(width, height);

		superActivityToast = new SuperActivityToast(activity, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);
		editText = (EditText) findViewById(R.id.message_edittext);

		messageButton = (Button) findViewById(R.id.messageButton);
		
		IntentFilter filter = new IntentFilter("NEW_MESSAGE");
		filter.addAction("NEW_MESSAGE");
		filter.addAction("SOME_OTHER_ACTION");

		findViewById(R.id.messageButton).setOnClickListener(this);
		findViewById(R.id.emojiButton).setOnClickListener(this);
		findViewById(R.id.message_edittext).setOnClickListener(this);
		
		setupUI();
	}
	
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Service.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		isEmojiShown  = true;
		findViewById(R.id.overflowButton).postDelayed(new Runnable() {

			@Override
			public void run() {
				manager = activity.getSupportFragmentManager();
				transaction = manager.beginTransaction();
				transaction.show(emojiFragment);
				transaction.commit();
			}
		}, 250);
	}
	
	private void hideEmojis() {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Service.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, 0);
		isEmojiShown = false;
		manager = activity.getSupportFragmentManager();
		transaction = manager.beginTransaction();
		transaction.hide(emojiFragment);
		transaction.commit();
	}

	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.messageButton) {
			if (editText.getText().toString().trim() != null && !editText.getText().toString().trim().equals("")) {
				superActivityToast = new SuperActivityToast(activity, SuperToast.Type.PROGRESS);
				messageButton.setClickable(false);
				superActivityToast.setText("Sending message");
				superActivityToast.setIndeterminate(true);
				superActivityToast.setProgressIndeterminate(true);
				superActivityToast.show();
				new SendMessageApi(activity, UserDataStore.getStore().getUserId(), receiverId, UserDataStore.getStore().getAccessKey(), editText.getText().toString(), new APIListener() {

					@Override
					public void onAPIStatus(boolean status) {
						superActivityToast.dismiss();
						setupUI();
						messageButton.setClickable(true);
						editText.setText("");
						if (status) {

						} else {
							Crouton.makeText(activity, "Something went wrong. Try again", Style.ALERT).show();
						}
					}
				}).execute("");
			}
		
		}else if(view.getId() == R.id.emojiButton) {
			hideKeyboard();
		}else if(view.getId() == R.id.message_edittext) {
			if (isEmojiShown) {
				hideEmojis();
			}
		}
	}

	protected void setupUI() {
		listView = (ListView) findViewById(R.id.message_list);
		adapter = new MessageListAdapter(activity, UserDataStore.getStore().getMessagesForPerson(receiverId));
		listView.setAdapter(adapter);
	}

	public void onEmojiconBackspaceClicked() {
		EmojiconsFragment.backspace(editText);
	}

	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiconsFragment.input(editText, emojicon);
	}

}
