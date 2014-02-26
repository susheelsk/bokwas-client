package com.bokwas.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserDetails {
	// UserId (facebookId)
	public static void setUserId(Context context, String userId) {
		setData(context, "user_id", userId);
	}

	public static String getUserId(Context context) {
		return getData(context, "user_id");
	}

	// facebook accessToken
	public static void setFbAccessToken(Context context, String accessToken) {
		setData(context, "access_token", accessToken);
	}

	public static String getFbAccessToken(Context context) {
		return getData(context, "access_token");
	}

	// user gender(Male or Female) stored as String
	public static void setUserGender(Context context, String gender) {
		setData(context, "gender", gender);
	}

	public static String getUserGender(Context context) {
		return getData(context, "gender");
	}

	// user email
	public static void setUserEmail(Context context, String email) {
		setData(context, "email", email);
	}

	public static String getUserEmail(Context context) {
		return getData(context, "email");
	}

	private static String getData(Context context, String keyName) {
		SharedPreferences pref = context.getSharedPreferences("user_details",
				Context.MODE_PRIVATE);
		String userId = pref.getString(keyName, "");
		return userId;
	}

	private static void setData(Context context, String keyName, String value) {
		SharedPreferences.Editor editor = context.getSharedPreferences(
				"user_details", Context.MODE_PRIVATE).edit();
		editor.putString(keyName, value);
		editor.commit();
	}

}
