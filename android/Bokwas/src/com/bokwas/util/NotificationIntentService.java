package com.bokwas.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bokwas.MessageActivity;
import com.bokwas.PostActivity;
import com.bokwas.R;
import com.bokwas.SplashScreen;
import com.bokwas.datasets.Friends;
import com.bokwas.datasets.Message;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Comment;
import com.bokwas.response.Likes;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class NotificationIntentService extends IntentService {

	private String TAG = "NotificationIntentService";
	public static final int NOTIFICATION_ID = GeneralUtil.GENERAL_NOTIFICATIONS;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public NotificationIntentService(String name) {
		super(name);
	}

	public NotificationIntentService() {
		super("NotificationIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		Log.d(TAG, "MessageType : " + messageType);

		if (!UserDataStore.isInitialized()) {
			UserDataStore.initData(this);
		}

		logReceivedNotification(extras);

		sendNotification(extras);

		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void logReceivedNotification(Bundle bundle) {
		try {
			JSONObject json = new JSONObject();
			Set<String> keys = bundle.keySet();
			for (String key : keys) {
				try {
					json.put(key, bundle.get(key));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			Log.d(TAG, "NotificationData : " + json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendNotification(Bundle bundle) {
		try {
			mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
			Intent intent = new Intent(this, SplashScreen.class);
			if (bundle.getString("type").equals("ADDLIKES_NOTI")) {
				addLikes(bundle);
				intent = new Intent(this, PostActivity.class);
				intent.putExtra("postId", bundle.getString("postId"));
				Log.d("PostActivity", "PostId : " + bundle.getString("postId"));
				intent.putExtra("fromNoti", true);
				buildNotification(bundle.getString("title", "Bokwas"), bundle.getString("message"), intent, null);
				return;
			} else if (bundle.getString("type").equals("ADDCOMMENT_NOTI")) {
				addCommentToPost(bundle);
				intent = new Intent(this, PostActivity.class);
				intent.putExtra("postId", bundle.getString("postId"));
				Log.d("PostActivity", "PostId : " + bundle.getString("postId"));
				intent.putExtra("fromNoti", true);
				String commentMessage = bundle.getString("message");
				// If comment
				if (UserDataStore.getStore().getPost(bundle.getString("postId")).getPostedBy().equals(UserDataStore.getStore().getUserId())) {
					commentMessage = bundle.getString("commentPersonBokwasName") + " has commented on your post";
				}
				buildNotification(bundle.getString("title", "Bokwas"), commentMessage, intent, null);
			} else if (bundle.getString("type").equals("PRIVATE_MESSAGE_NOTI")) {
				addMessage(bundle);
				String fromId = bundle.getString("fromId");
				intent = new Intent(this, MessageActivity.class);
				intent.putExtra("receiverId", fromId);
				intent.putExtra("fromNoti", true);
				Friends friend = UserDataStore.getStore().getFriend(fromId);
				if (!isActivityRunning("MessageActivity")) {
					buildNotification(friend.bokwasName, bundle.getString("message"), intent, GeneralUtil.getImageBitmap(GeneralUtil.getAvatarResourceId(friend.getBokwasAvatarId()), this));
				} else {
					intent = new Intent("NEW_MESSAGE");
					intent.setAction("NEW_MESSAGE");
					sendBroadcast(intent);
				}
				return;
			} else if (bundle.getString("type").equals("UPDATE_MESSAGE_NOTI")) {
				String url = "";
				String my_package_name = "com.bokwas";
				try {
					// Check whether Google Play store is installed or not:
					this.getPackageManager().getPackageInfo("com.android.vending", 0);
					url = "market://details?id=" + my_package_name;
				} catch (final Exception e) {
					url = "https://play.google.com/store/apps/details?id=" + my_package_name;
				}
				// Open the app page in Google Play store:
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				buildNotification(bundle.getString("title", "Bokwas"), bundle.getString("message"), intent, null);
			} else {
				intent = new Intent(this, SplashScreen.class);
				buildNotification(bundle.getString("title", "Bokwas"), bundle.getString("message"), intent, null);
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addMessage(Bundle bundle) {
		String fromId = bundle.getString("fromId");
		String time = bundle.getString("time");
		String message = bundle.getString("message");
		String messageId = bundle.getString("messageId");

		Friends friend = UserDataStore.getStore().getFriend(fromId);
		if (friend == null) {
			return;
		}

		Message messageData = new Message(fromId, UserDataStore.getStore().getUserId(), Long.valueOf(time), message, messageId, false);
		UserDataStore.getStore().addMessageToPerson(fromId, messageData);
		UserDataStore.getStore().save(this);
	}

	private boolean isActivityRunning(String className) {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);

		if (services.get(0).topActivity.getClassName().toString().contains(className)) {
			return true;
		}
		return false;
	}

	private void addLikes(Bundle extras) {
		try {
			String commentId = extras.getString("likesCommentId", "");
			String postId = extras.getString("postId", "");
			String likesPersonName = extras.getString("likesPersonName", "");
			String likesPersonId = extras.getString("likesPersonId", "");
			String avatarId = extras.getString("avatarId", "");
			if (commentId.equals("")) {
				UserDataStore.getStore().getPost(postId).addLikes(likesPersonId, likesPersonName, avatarId);
			} else {
				UserDataStore.getStore().getPost(postId).getComment(commentId).addLikes(likesPersonId, likesPersonName, avatarId);
			}
			UserDataStore.getStore().save(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addCommentToPost(Bundle extras) {
		try {
			Comment comment = new Comment(extras.getString("commentId"), Long.valueOf(extras.getString("commentTime")), extras.getString("commentText"), new ArrayList<Likes>(),
					extras.getString("commentPersonId"), extras.getString("commentPersonBokwasName"), extras.getString("avatarId"));
			UserDataStore.getStore().getPost(extras.getString("postId")).addComment(comment);
			UserDataStore.getStore().save(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void buildNotification(String title, String message, Intent intent, Bitmap bitmap) {
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.bokwas_icon).setContentTitle(title)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setContentText(message);
		mBuilder.setContentIntent(contentIntent);
		if (bitmap != null) {
			mBuilder.setLargeIcon(bitmap);
		}
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

}
