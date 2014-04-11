package com.bokwas.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.bokwas.R;

public class GeneralUtil {

	public static int getAvatarResourceId(String avatarId) {
		try {
			int id = Integer.parseInt(avatarId);
			switch (id) {
			case 1:
				return R.drawable.avatar_1;
			case 2:
				return R.drawable.avatar_2;
			case 3:
				return R.drawable.avatar_3;
			case 4:
				return R.drawable.avatar_4;
			case 5:
				return R.drawable.avatar_5;
			case 6:
				return R.drawable.avatar_6;
			case 7:
				return R.drawable.avatar_7;
			case 8:
				return R.drawable.avatar_8;
			case 9:
				return R.drawable.avatar_9;
			case 10:
				return R.drawable.avatar_10;
			case 11:
				return R.drawable.avatar_11;
			case 12:
				return R.drawable.avatar_12;
			case 13:
				return R.drawable.avatar_13;
			case 14:
				return R.drawable.avatar_14;
			case 15:
				return R.drawable.avatar_15;
			case 16:
				return R.drawable.avatar_16;
			case 17:
				return R.drawable.avatar_17;
			case 18:
				return R.drawable.avatar_18;
			case 19:
				return R.drawable.avatar_19;
			case 20:
				return R.drawable.avatar_20;
			case 21:
				return R.drawable.avatar_21;
			case 22:
				return R.drawable.avatar_22;
			case 23:
				return R.drawable.avatar_23;
			case 24:
				return R.drawable.avatar_24;
			case 25:
				return R.drawable.avatar_25;
			case 26:
				return R.drawable.avatar_26;
			case 27:
				return R.drawable.avatar_27;
			case 28:
				return R.drawable.avatar_28;
			case 29:
				return R.drawable.avatar_29;
			case 30:
				return R.drawable.avatar_30;
			case 31:
				return R.drawable.avatar_31;
			case 32:
				return R.drawable.avatar_32;
			case 33:
				return R.drawable.avatar_33;
			case 34:
				return R.drawable.avatar_34;
			case 35:
				return R.drawable.avatar_35;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return R.drawable.avatar_13;
	}

	public static Bitmap getImageBitmap(int id, Context context) {
		Bitmap srcBmp = BitmapFactory
				.decodeResource(context.getResources(), id);
		Bitmap modBmp = Bitmap.createBitmap(srcBmp, 0, 0,
				srcBmp.getWidth(), 3*srcBmp.getHeight()/4);
		return modBmp;
	}

}
