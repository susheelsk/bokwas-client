package com.bokwas.util;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bokwas.R;
import com.bokwas.SplashScreen;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Comment;
import com.bokwas.response.Likes;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
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
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);
		Log.d(TAG, "MessageType : " + messageType);

		if (extras.getString("type").equals("ADDLIKES_NOTI")) {
			addLikes(extras);
			sendNotification(extras);
		} else if (extras.getString("type").equals("ADDCOMMENT_NOTI")) {
			addCommentToPost(extras);
			sendNotification(extras);
		}

		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void addLikes(Bundle extras) {
		try {
			if (!isAppRunning()) {
				UserDataStore.initData(this);
			}
			String commentId = extras.getString("likesCommentId", "");
			String postId = extras.getString("likesPostId", "");
			String likesPersonName = extras.getString("likesPersonName", "");
			String likesPersonId = extras.getString("likesPersonId", "");
			if (commentId.equals("")) {
				UserDataStore.getStore().getPost(postId).addLikes(likesPersonId, likesPersonName);
			} else {
				UserDataStore.getStore().getPost(postId).getComment(commentId).addLikes(likesPersonId, likesPersonName);
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
			Comment comment = new Comment(extras.getString("commentId"),
					Long.valueOf(extras.getString("commentTime")),
					extras.getString("commentText"), new ArrayList<Likes>(),
					extras.getString("commentPersonId"),
					extras.getString("commentPersonBokwasName"),
					extras.getString("commentBokwasAvatarId"));
			UserDataStore.getStore().getPost(extras.getString("postId"))
					.addComment(comment);
			UserDataStore.getStore().save(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isAppRunning() {
		ActivityManager activityManager = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procInfos = activityManager
				.getRunningAppProcesses();
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
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		
		Intent intent = new Intent(this, SplashScreen.class);
//		if(bundle.getString("type").equals("ADDLIKES_NOTI")) {
//			intent = new Intent(this, PostActivity.class);
//			intent.putExtra("postId", bundle.getString("postId"));
//		}else if(bundle.getString("type").equals("ADDLIKES_NOTI")) {
//			intent = new Intent(this, PostActivity.class);
//			intent.putExtra("postId", bundle.getString("likesPostId"));
//		}else {
//			intent = new Intent(this, PostActivity.class);
//		}
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.bokwas_icon)
				.setContentTitle(bundle.getString("title"))
				.setStyle(
						new NotificationCompat.BigTextStyle().bigText(bundle
								.getString("message")))
				.setContentText(bundle.getString("message"));

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
