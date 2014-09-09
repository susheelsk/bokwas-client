package com.bokwas.ui;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.bokwas.R;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.apirequests.SendMessageApi;
import com.bokwas.datasets.UserDataStore;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MessageDialogFragment extends Fragment implements OnClickListener {

	private EditText editText;
	private ListView listView;
	private String receiverId;
	private Activity activity;
	private MessageDialogListener listener;
	private MessageListAdapter adapter;
	private Button messageButton;
	private SuperActivityToast superActivityToast;
	protected boolean isEmojiShown;

	public interface MessageDialogListener {
		public void showEmojis(View view);

		public void hideEmojis(View view);

		public void onDissmiss();
	}

	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiconsFragment.input(editText, emojicon);
	}
	
	public void onEmojiconBackspaceClicked() {
		EmojiconsFragment.backspace(editText);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.message_dialog, container, false);
		editText = (EditText) view.findViewById(R.id.message_edittext);
		listView = (ListView) view.findViewById(R.id.message_list);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
		if (activity instanceof MessageDialogListener) {
			listener = (MessageDialogListener) activity;
		} else {
			throw new ClassCastException(activity.toString() + " must implemenet MessageDialogFragment.MessageDialogListener");
		}
	}

	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}

	public void setupUI() {
		messageButton = (Button) getView().findViewById(R.id.messageButton);

		getView().setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_BACK:
					if (isEmojiShown) {
						hideEmojis();
						break;
					}
					listener.onDissmiss();
					break;
				}
				return false;
			}
		});
		if (receiverId != null) {
			adapter = new MessageListAdapter(activity, UserDataStore.getStore().getMessagesForPerson(receiverId));
			listView.setAdapter(adapter);
		}
		setOnClickListeners();
	}

	protected void hideEmojis() {
		isEmojiShown = false;
		listener.hideEmojis(editText);
	}

	private void showEmoji() {
		isEmojiShown = true;
		listener.showEmojis(editText);
	}

	private void setOnClickListeners() {
		editText.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isEmojiShown) {
					hideEmojis();
				}
				return false;
			}
		});

		getView().findViewById(R.id.messageButton).setOnClickListener(this);
		getView().findViewById(R.id.emojiButton).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.messageButton) {

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

		} else if (view.getId() == R.id.emojiButton) {
			showEmoji();
		} 
	}

}
