package com.bokwas.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bokwas.PostActivity;
import com.bokwas.R;
import com.bokwas.SplashScreen;
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

		sendNotification(extras);

		logReceivedNotification(extras);

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
		Log.d("BokwasNotification","NotificationData : "+json.toString());
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
		ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
		for (int i = 0; i < procInfos.size(); i++) {
			if (procInfos.get(i).processName.equals(getPackageName())) {
				return true;
			}
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
				intent.putExtra("fromNoti", true);
			} else if (bundle.getString("type").equals("ADDCOMMENT_NOTI")) {
				addCommentToPost(bundle);
				intent = new Intent(this, PostActivity.class);
				intent.putExtra("postId", bundle.getString("postId"));
				intent.putExtra("fromNoti", true);

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
			}
			Map<String, String> map = new HashMap<String, String>();
			for (String key : bundle.keySet()) {
				String value = bundle.get(key).toString();
				map.put(key, value);
			}
			Notification notification = new Notification(notificationId, new Gson().toJson(map), System.currentTimeMillis());
			UserDataStore.getStore().addNotification(notification);
			intent.putExtra("fromNoti", true);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.bokwas_icon).setContentTitle(bundle.getString("title"))
					.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setStyle(new NotificationCompat.BigTextStyle().bigText(bundle.getString("message")))
					.setContentText(bundle.getString("message"));

			mBuilder.setContentIntent(contentIntent);
			mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
