package com.bokwas.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.bokwas.R;

public class NotificationProgress {
	
	private static NotificationManager mNotifyManager = null;
	private static Builder mBuilder = null;

	public static void showNotificationProgress(Activity activity, String tickerText, int newTask) {
		mNotifyManager  = (NotificationManager) activity
                .getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder  = new NotificationCompat.Builder(activity);
		mBuilder.setContentTitle("Bokwas")
		    .setContentText(tickerText)
		    .setSmallIcon(R.drawable.bokwas_icon);
		mBuilder.setProgress(0, 0, true);
        mNotifyManager.notify(newTask, mBuilder.build());
	}
	
	public static void clearNotificationProgress(int newTask) {
		try {
			mNotifyManager.cancel(newTask);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
