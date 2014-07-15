package com.bokwas.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bokwas.R;

public class GeneralUtil {

	public static final int NOTIFICATION_PROGRESS_NEWPOST = 501;
	public static final int NOTIFICATION_PROGRESS_NEWCOMMENT = 502;
	public static final int NOTIFICATION_PROGRESS_ADDLIKES = 503;
	public static final int NOTIFICATION_PROGRESS_DELETEPOST = 504;
	public static final int NOTIFICATION_PROGRESS_DELETECOMMENT = 504;
	public static final int GENERAL_NOTIFICATIONS = 500;
	public static final String sharedPreferences = "bokwasSharePreferences";
	public static final String isLoggedInKey = "isLoggedIn";
	public static final String userGender = "userGender";
	
	public static Parcelable listSavedInstanceHomeScreen = null;
	
	public static Bitmap loadBitmapFromView(View view) throws Exception{
		try {
			int width = view.getWidth();
		    int height = view.getHeight();

		    int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
		    int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

		    //Cause the view to re-layout
		    view.measure(measuredWidth, measuredHeight);
		    view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

		    //Create a bitmap backed Canvas to draw the view into
		    Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		    Canvas c = new Canvas(b);

		    //Now that the view is laid out and we have a canvas, ask the view to draw itself into the canvas
		    view.draw(c);

		    return b;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static Bitmap getImageBitmap(int id, Context context) {
		Bitmap srcBmp = BitmapFactory
				.decodeResource(context.getResources(), id);
		Bitmap modBmp = Bitmap.createBitmap(srcBmp, 0, 0, srcBmp.getWidth(),
				  srcBmp.getHeight() );
		return modBmp;
	}

	public static void showPopupMenu(final Activity activity, View v,int menuId) {
		PopupMenu popupMenu = new PopupMenu(activity, v);
		popupMenu.getMenuInflater().inflate(menuId,
				popupMenu.getMenu());

		popupMenu
				.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						Toast.makeText(activity, item.toString(),
								Toast.LENGTH_LONG).show();
						return true;
					}
				});

		popupMenu.show();
	}
	
	public static void sharePhotoIntent(Activity activity, Bitmap img, String text) {
		String path = saveImageLocally(activity,img);
		Log.d("SharePhoto","path : "+path);
		img = BitmapFactory.decodeFile(path);
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/png");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		java.io.File f = new java.io.File(path);
		
		img.compress(Bitmap.CompressFormat.PNG, 100, bytes);
		
		try {
			f.createNewFile();
			FileOutputStream fo = new FileOutputStream(f);
			fo.write(bytes.toByteArray());
			fo.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Native email client doesn't currently support HTML, but it doesn't
		// hurt to try in case they fix it
		share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + path));
		share.putExtra(Intent.EXTRA_SUBJECT, activity.getResources()
				.getString(R.string.app_name));
		share.setType("message/rfc822");

		PackageManager pm = activity.getPackageManager();
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("text/plain");

		Intent openInChooser = Intent.createChooser(share, "Share via");

		List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
		List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
		for (int i = 0; i < resInfo.size(); i++) {
			// Extract the label, append it, and repackage it in a LabeledIntent
			ResolveInfo ri = resInfo.get(i);
			String packageName = ri.activityInfo.packageName;
			Log.d("Share", "PackageName : " + packageName);
			if (packageName.contains("gm")) {
				share.setPackage(packageName);
			} else if (packageName.contains("twitter")
					|| packageName.contains("facebook")
					|| packageName.contains("whatsapp")
					|| packageName.contains("hike")
					|| packageName.contains("plus")
					|| packageName.contains("naver")
					|| packageName.contains("tencent")) {
				Intent intent = new Intent();
				intent.setComponent(new ComponentName(packageName,
						ri.activityInfo.name));
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
				if (packageName.contains("twitter")) {
					intent.putExtra(Intent.EXTRA_STREAM,
							Uri.parse("file:///" + path));
					intent.putExtra(Intent.EXTRA_TEXT, text);
				} else if (packageName.contains("facebook")) {
					intent.setType("image/*");
					intent.putExtra(Intent.EXTRA_STREAM,
							Uri.parse("file:///" + path));
					intent.putExtra(Intent.EXTRA_TEXT, text);
				} else if (packageName.contains("mms")) {
					intent.putExtra(Intent.EXTRA_STREAM,
							Uri.parse("file:///" + path));
				} else if (packageName.contains("android.gm")) {
					intent.putExtra(Intent.EXTRA_STREAM,
							Uri.parse("file:///" + path));
					intent.putExtra(Intent.EXTRA_SUBJECT, activity
							.getResources().getString(R.string.app_name));
					intent.setType("message/rfc822");
				} else if (packageName.contains("whatsapp")) {
					intent.putExtra(Intent.EXTRA_STREAM,
							Uri.parse("file:///" + path));
//					intent.putExtra(Intent.EXTRA_TEXT, text);
					intent.setType("message/rfc822");
				} else if (packageName.contains("hike")) {
					intent.putExtra(Intent.EXTRA_STREAM,
							Uri.parse("file:///" + path));
					intent.putExtra(Intent.EXTRA_TEXT, text);
				} else if (packageName.contains("plus")) {
					intent.putExtra(Intent.EXTRA_STREAM,
							Uri.parse("file:///" + path));
					intent.putExtra(Intent.EXTRA_TEXT, text);
				} else if (packageName.contains("naver")) {
					intent.putExtra(Intent.EXTRA_STREAM,
							Uri.parse("file:///" + path));
					intent.putExtra(Intent.EXTRA_TEXT, text);
				} else if (packageName.contains("tencent")) {
					intent.putExtra(Intent.EXTRA_STREAM,
							Uri.parse("file:///" + path));
					intent.putExtra(Intent.EXTRA_TEXT, text);
				}

				intentList.add(new LabeledIntent(intent, packageName, ri
						.loadLabel(pm), ri.icon));
			}
		}

		// convert intentList to array
		LabeledIntent[] extraIntents = intentList
				.toArray(new LabeledIntent[intentList.size()]);

		openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);

		activity.startActivity(Intent.createChooser(openInChooser,
				"Share Image"));
	}
	
	private static String saveImageLocally(Activity activity, Bitmap _bitmap) {
		java.io.File outputFile = null;
		String state = Environment.getExternalStorageState();
		java.io.File filesDir;
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    filesDir = activity.getExternalFilesDir(null);
		} else {
		    // Load another directory, probably local memory
		    filesDir = activity.getFilesDir();
		}
		outputFile = new java.io.File(filesDir,
				"temp.png");
		try {
			FileOutputStream out = new FileOutputStream(outputFile);
			_bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
		} catch (Exception e) {
			// handle exception
			e.printStackTrace();
		}
		return outputFile.getAbsolutePath();
	}
	
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
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return R.drawable.avatar_13;
	}

}
