package com.bokwas.dialogboxes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bokwas.R;

public class GenericDialogOk extends Dialog implements android.view.View.OnClickListener{

	private Activity activity;
	private String title;
	private String description;
	private DialogType type;
	
	public enum DialogType {
		DIALOG_GENERIC,
		DIALOG_UPDATE_APP
	}
	
	public GenericDialogOk(Activity activity, String title, String description,DialogType type) {
		super(activity);
		this.activity = activity;
		this.title = title;
		this.description = description;
		this.type = type;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_generic_singlebutton);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		Display display =((WindowManager)activity.getSystemService(Activity.WINDOW_SERVICE)).getDefaultDisplay();
	    int width = display.getWidth();
	    int height=display.getHeight();
	    Window window = getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.CENTER_VERTICAL;
		wlp.width = width;
		window.setAttributes(wlp);
		getWindow().setLayout(width,height);
		this.setCanceledOnTouchOutside(true);
		((TextView)findViewById(R.id.dialog_title)).setText(title);
		((TextView)findViewById(R.id.dialog_description)).setText(description);
		findViewById(R.id.dialog_button_ok).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.dialog_button_ok) {
			switch(type) {
			case DIALOG_GENERIC :
				
				break;
			case DIALOG_UPDATE_APP :
				String url = "";
				String my_package_name = "com.bokwas";
				try {
					// Check whether Google Play store is installed or not:
					activity.getPackageManager().getPackageInfo("com.android.vending", 0);
					url = "market://details?id=" + my_package_name;
				} catch (final Exception e) {
					url = "https://play.google.com/store/apps/details?id=" + my_package_name;
				}
				// Open the app page in Google Play store:
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				activity.startActivity(intent);
				break;
			}
			dismiss();
		}
	}

}
