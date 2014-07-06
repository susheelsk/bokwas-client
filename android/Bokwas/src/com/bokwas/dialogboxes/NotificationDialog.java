package com.bokwas.dialogboxes;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.bokwas.R;
import com.bokwas.response.Notification;
import com.bokwas.ui.NotificationDialogListAdapter;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

public class NotificationDialog extends Dialog{
	
	private Activity activity;
	private List<Notification> notificationList;
	private SuperActivityToast superActivityToast;
	private ListView listView;
	private NotificationDialogListAdapter adapter;
	
	public NotificationDialog(Activity activity, List<Notification> notifications) {
		super(activity);
		this.activity = activity;
		this.notificationList = notifications;
	}
	
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.likes_dialog);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		Display display = ((WindowManager) activity
				.getSystemService(Activity.WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		Window window = getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.CENTER_VERTICAL;
		wlp.width = width;
		window.setAttributes(wlp);
		getWindow().setLayout(width, height);

		((TextView)findViewById(R.id.dialog_title)).setText("Notifications");
		superActivityToast = new SuperActivityToast(activity, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);

		adapter = new NotificationDialogListAdapter(activity, notificationList);
		listView = (ListView) findViewById(R.id.like_list);
		listView.setAdapter(adapter);
	}

}
