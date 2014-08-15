package com.bokwas.util;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import com.bokwas.MessageActivity;
import com.bokwas.R;
import com.bokwas.apirequests.GetFriendsApi;
import com.bokwas.apirequests.GetNotificationsApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.apirequests.GetPrivateMessagesApi;
import com.bokwas.datasets.Friends;
import com.bokwas.datasets.Message;
import com.bokwas.datasets.UserDataStore;

public class BackgroundDataService extends IntentService{

	public BackgroundDataService(String name) {
		super(name);
	}
	
	public BackgroundDataService() {
		super("BackgroundDataService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if(UserDataStore.isInitialized()) {
			return;
		}
		if (!getSharedPreferences(GeneralUtil.sharedPreferences, MODE_PRIVATE).getBoolean(GeneralUtil.isLoggedInKey, false)) {
			return;
		}
		UserDataStore.initData(this);
		new GetFriendsApi(this, UserDataStore.getStore().getAccessKey(), UserDataStore.getStore().getUserId(), null).execute("");

		new GetNotificationsApi(UserDataStore.getStore().getAccessKey(), UserDataStore.getStore().getUserId(), new APIListener() {

			@Override
			public void onAPIStatus(boolean status) {
				
			}
		}).execute("");

		new GetPrivateMessagesApi(this, UserDataStore.getStore().getUserId(), UserDataStore.getStore().getAccessKey(), new GetPrivateMessagesApi.APIMessageListener() {
			
			@Override
			public void onMessage(Message message) {
				Intent intent = new Intent(BackgroundDataService.this, MessageActivity.class);
				intent.putExtra("receiverId", message.getFromId());
				intent.putExtra("fromNoti", true);
				Friends friend = UserDataStore.getStore().getFriend(message.getFromId());
				buildMessageNotification(friend, intent, message.getMessage());
			}
		}).execute("");
	}
	
	private void buildMessageNotification(Friends friend, Intent intent,String message) {
		NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		int requestID = (int) System.currentTimeMillis();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.bokwas_icon).setContentTitle(friend.getBokwasName())
				.setLargeIcon(GeneralUtil.getImageBitmap(GeneralUtil.getAvatarResourceId(friend.getBokwasAvatarId()), this)).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				.setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setContentText(message);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(GeneralUtil.GENERAL_NOTIFICATIONS, mBuilder.build());
	}

}
