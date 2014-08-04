package com.bokwas.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bokwas.HomescreenActivity;
import com.bokwas.MessageActivity;
import com.bokwas.PostActivity;
import com.bokwas.R;
import com.bokwas.SplashScreen;
import com.bokwas.datasets.Message;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Comment;
import com.bokwas.response.Likes;
import com.bokwas.response.Notification;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

public class GCMIntentService extends IntentService {
	public static final int NOTIFICATION_ID = GeneralUtil.GENERAL_NOTIFICATIONS;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	private String TAG = "GCMIntentService";

	public GCMIntentService() {
		super("GCMIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		Log.d(TAG, "MessageType : " + messageType);

		logReceivedNotification(extras);

		sendNotification(extras);

		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void logReceivedNotification(Bundle bundle) {
		JSONObject json = new JSONObject();
		Set<String> keys = bundle.keySet();
		for (String key : keys) {
			try {
				json.put(key, bundle.get(key));
			} catch (JSONException e) {
				// Handle exception here
			}
		}
		Log.d("BokwasNotification", "NotificationData : " + json.toString());
	}

	private void addLikes(Bundle extras) {
		try {
			if (!isAppRunning()) {
				UserDataStore.initData(this);
			}
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
			if (!isAppRunning()) {
				UserDataStore.initData(this);
			}
			Comment comment = new Comment(extras.getString("commentId"), Long.valueOf(extras.getString("commentTime")), extras.getString("commentText"), new ArrayList<Likes>(),
					extras.getString("commentPersonId"), extras.getString("commentPersonBokwasName"), extras.getString("avatarId"));
			UserDataStore.getStore().getPost(extras.getString("postId")).addComment(comment);
			UserDataStore.getStore().save(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isAppRunning() {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);

		if (services.get(0).topActivity.getPackageName().toString().equalsIgnoreCase(getPackageName().toString())) {
			return true;
		}
		return false;
	}

	private boolean isActivityRunning(String className) {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);

		if (services.get(0).topActivity.getPackageName().toString().contains(className)) {
			return true;
		}
		return false;
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(Bundle bundle) {
		try {
			mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
			Intent intent = new Intent(this, SplashScreen.class);
			String notificationId = bundle.getString("notificationId");
			if (bundle.getString("type").equals("ADDLIKES_NOTI")) {
				addLikes(bundle);
				intent = new Intent(this, PostActivity.class);
				intent.putExtra("postId", bundle.getString("postId"));
			} else if (bundle.getString("type").equals("ADDCOMMENT_NOTI")) {
				addCommentToPost(bundle);
				intent = new Intent(this, PostActivity.class);
				intent.putExtra("postId", bundle.getString("postId"));
				// If comment
				if (UserDataStore.getStore().getPost(bundle.getString("postId")).getPostedBy().equals(UserDataStore.getStore().getUserId())) {
					PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.bokwas_icon).setContentTitle(bundle.getString("title"))
							.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
							.setStyle(new NotificationCompat.BigTextStyle().bigText(bundle.getString("commentPersonBokwasName") + " has commented on your post"))
							.setContentText(bundle.getString("message"));
					mBuilder.setContentIntent(contentIntent);
					mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
					return;
				}
			} else if (bundle.getString("type").equals("PRIVATE_MESSAGE_NOTI")) {
				addMessage(bundle);
				return;
			} else {
				intent = new Intent(this, HomescreenActivity.class);
			}
			Map<String, String> map = new HashMap<String, String>();
			for (String key : bundle.keySet()) {
				String value = bundle.get(key).toString();
				map.put(key, value);
			}
			Notification notification = new Notification(notificationId, new Gson().toJson(map), System.currentTimeMillis());
			UserDataStore.getStore().addNotification(notification);
			intent.putExtra("fromNoti", true);

			buildNotification(intent, bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addMessage(Bundle bundle) {
		String fromId = bundle.getString("fromId");
		String time = bundle.getString("time");
		String message = bundle.getString("message");

		boolean isAppRunning = false;
		if (!isAppRunning()) {
			Log.d("BokwasNotification", "App is not running");
			UserDataStore.initData(this);
		} else {
			Log.d("BokwasNotification", "App is running");
			isAppRunning = true;
		}
		Message messageData = new Message(fromId, UserDataStore.getStore().getUserId(), Long.valueOf(time), message,false);
		UserDataStore.getStore().addMessageToPerson(fromId, messageData);
		UserDataStore.getStore().save(this);
		Intent intent = new Intent(this, MessageActivity.class);
		intent.putExtra("receiverId", fromId);
		intent.putExtra("fromNoti", true);
		if (!isAppRunning) {
			buildNotification(intent, bundle);
		} else if (isActivityRunning("MessageActivity")) {
			buildNotification(intent, bundle);
		} else {
			intent = new Intent("NEW_MESSAGE");
			intent.setAction("NEW_MESSAGE");
			sendBroadcast(intent);
		}
	}

	private void buildNotification(Intent intent, Bundle bundle) {
		int requestID = (int) System.currentTimeMillis();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		String title = bundle.getString("title");
		if (title == null || title.equals("")) {
			title = "Bokwas";
		}
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.bokwas_icon).setContentTitle(title)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setStyle(new NotificationCompat.BigTextStyle().bigText(bundle.getString("message")))
				.setContentText(bundle.getString("message"));

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
