package com.bokwas.util;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bokwas.apirequests.UpdateGcmRegId;
import com.bokwas.datasets.UserDataStore;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMUtils {

	public static boolean checkPlayServices(Activity activity) {
//		int resultCode = GooglePlayServicesUtil
//				.isGooglePlayServicesAvailable(activity);
//		if (resultCode != ConnectionResult.SUCCESS) {
//			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
////				GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
////						9000).show();
//			} else {
//				Log.d("GCMRegistrar", "This device is not supported.");
//				return false;
//			}
//			return false;
//		}
		return true;
	}

	public static void getRegistrationId(final Context context) {
		new AsyncTask<String, Void, Boolean>() {

			private String regId = null;

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if (regId != null) {
					if (UserDataStore.getStore().isGcmUpdated() == false
							&& UserDataStore.getStore().getAccessKey() != null) {
						new UpdateGcmRegId(context, UserDataStore.getStore()
								.getUserId(), UserDataStore.getStore()
								.getAccessKey(), regId, null).execute("");
					}
				}
			}

			@Override
			protected Boolean doInBackground(String... params) {
				GoogleCloudMessaging gcm = GoogleCloudMessaging
						.getInstance(context);
				String regId = null;
				try {
					regId = gcm.register("6305380231");
					this.regId = regId;
					Log.d("GCMRegistrar", "GCM RegId : " + regId);
					if (regId != null) {
						UserDataStore.getStore().setGcmRegId(regId);
						UserDataStore.getStore().save(context);
					}
				} catch (IOException e) {
					e.printStackTrace();
					Log.d("GCMRegistrar", "Error : " + e.getMessage());
					return null;
				}
				return null;
			}
		}.execute("");
	}
}
