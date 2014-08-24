package com.bokwas;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bokwas.apirequests.AddPostsApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.util.AppData;
import com.bokwas.util.GeneralUtil;
import com.bokwas.util.NotificationProgress;
import com.bokwas.util.TrackerName;
import com.google.android.gms.analytics.Tracker;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class NewPostActivity extends FragmentActivity implements OnClickListener, EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {

	private EditText editText;
	private boolean isShareIntent = false;
	private FragmentManager manager;
	private FragmentTransaction transaction;
	private EmojiconsFragment emojiFragment;
	private boolean isEmojiShown = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.newpost_dialog);
		
		if(AppData.isReset) {
			Toast.makeText(this, "Please restart the app", Toast.LENGTH_SHORT).show();
			finish();
		}

		setOnClickListeners();

		manager = getSupportFragmentManager();
		transaction = manager.beginTransaction();

		setupUI();
		
		setupGoogleAnalytics();

	}
	
	private void setupGoogleAnalytics() {
		Tracker t = GeneralUtil.getTracker(TrackerName.APP_TRACKER,this);
		t.enableAutoActivityTracking(true);
	}

	private void setupUI() {
		String shareableText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
		if (shareableText != null && !shareableText.equals("")) {
			editText.setText(shareableText);
			isShareIntent = true;
			if (!getSharedPreferences(GeneralUtil.sharedPreferences, MODE_PRIVATE).getBoolean(GeneralUtil.isLoggedInKey, false)) {
				Toast.makeText(this, "You're not logged in. Please login and then try", Toast.LENGTH_SHORT).show();
				finish();
			}
			UserDataStore.initData(this);
		}
		findViewById(R.id.overflowButton).setVisibility(View.GONE);

		findViewById(R.id.messageHeaderButton).setVisibility(View.GONE);
		editText = (EditText) findViewById(R.id.post_content);
		ImageView profilePic = (ImageView) findViewById(R.id.post_profile_pic);
		TextView nameTextView = (TextView) findViewById(R.id.post_name);
		nameTextView.setText(UserDataStore.getStore().getBokwasName());
		String avatarId = String.valueOf(UserDataStore.getStore().getAvatarId());
		profilePic.setImageBitmap(GeneralUtil.getImageBitmap(GeneralUtil.getAvatarResourceId(avatarId), this));

		emojiFragment = (EmojiconsFragment) manager.findFragmentById(R.id.emojicons);
		transaction.hide(emojiFragment);
		transaction.commit();

		editText.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isEmojiShown) {
					hideEmojis();
				}
				return false;
			}
		});

	}

	private void setOnClickListeners() {
		findViewById(R.id.post_like_button).setOnClickListener(this);
		findViewById(R.id.post_emoji_button).setOnClickListener(this);
		findViewById(R.id.titlebar).setOnClickListener(this);
	}

	protected void setupNotificationBar() {
		try {
			TextView notificationButton = (TextView) findViewById(R.id.messageHeaderButton);
			notificationButton.setText(String.valueOf(UserDataStore.getStore().getUnseenNotifications().size()));

			if (UserDataStore.getStore().getNotifications().size() >= 1) {
				notificationButton.setBackgroundResource(R.drawable.circle_red);
			} else {
				notificationButton.setOnClickListener(null);
				notificationButton.setBackgroundResource(R.drawable.circle_grey);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		if (isEmojiShown) {
			hideEmojis();
			return;
		}
		if (!isShareIntent) {
			Intent intent = new Intent(this, HomescreenActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_right);
		}
		finish();
	}

	@Override
	public void onClick(final View view) {
		if (view.getId() == R.id.post_like_button && editText.getText().toString().trim() != null && !editText.getText().toString().trim().equals("")) {
			// final SuperActivityToast superActivityToast = new
			// SuperActivityToast(NewPostActivity.this,
			// SuperToast.Type.PROGRESS);
			// superActivityToast.setIndeterminate(true);
			// superActivityToast.setProgressIndeterminate(true);
			// superActivityToast.setText("Adding new post...");
			// superActivityToast.show();
			
			view.setClickable(false);

			NotificationProgress.showNotificationProgress(this, "Adding a new post", GeneralUtil.NOTIFICATION_PROGRESS_NEWPOST);
			new AddPostsApi(this, UserDataStore.getStore().getAccessKey(), UserDataStore.getStore().getUserId(), editText.getText().toString(), new APIListener() {

				@Override
				public void onAPIStatus(boolean status) {
					// if (superActivityToast.isShowing()) {
					// superActivityToast.dismiss();
					// }
					view.setClickable(true);
					NotificationProgress.clearNotificationProgress(GeneralUtil.NOTIFICATION_PROGRESS_NEWPOST);
					if (status) {
						Crouton.makeText(NewPostActivity.this, "Post added!", Style.INFO).show();
						Intent intent = new Intent(NewPostActivity.this, HomescreenActivity.class);
						startActivity(intent);
						overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_right);
						finish();
					} else {
						Crouton.makeText(NewPostActivity.this, "Couldn't add post. Try again", Style.ALERT).show();
					}
				}
			}).execute("");
		} else if (view.getId() == R.id.post_emoji_button) {
			hideKeyboard();
		} else if (view.getId() == R.id.titlebar) {
			// Intent intent;
			// intent = new Intent(this, HomescreenActivity.class);
			// startActivity(intent);
			// overridePendingTransition(R.anim.activity_slide_in_right,
			// R.anim.activity_slide_out_right);
			// finish();

			Toast.makeText(this, editText.getEditableText().toString(), Toast.LENGTH_SHORT).show();

			try {
				Log.d("Emoticon", "Text : " + decodeUTF8(editText.getEditableText().toString().getBytes("US-ASCII")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	private String decodeUTF8(byte[] bytes) {
		return new String(bytes, Charset.forName("UTF-8"));
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		isEmojiShown = true;
		findViewById(R.id.overflowButton).postDelayed(new Runnable() {

			@Override
			public void run() {
				manager = getSupportFragmentManager();
				transaction = manager.beginTransaction();
				transaction.show(emojiFragment);
				transaction.commit();
			}
		}, 250);
	}

	private void hideEmojis() {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, 0);
		isEmojiShown = false;
		manager = getSupportFragmentManager();
		transaction = manager.beginTransaction();
		transaction.hide(emojiFragment);
		transaction.commit();
	}

	protected void moveToHomescreen() {
		onBackPressed();
	}

	@Override
	public void onEmojiconBackspaceClicked(View v) {
		EmojiconsFragment.backspace(editText);
	}

	@Override
	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiconsFragment.input(editText, emojicon);
	}

}
