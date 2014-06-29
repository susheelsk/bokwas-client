package com.bokwas.dialogboxes;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
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
	
	public GenericDialogOk(Activity activity, String title, String description) {
		super(activity);
		this.activity = activity;
		this.title = title;
		this.description = description;
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
		
		((TextView)findViewById(R.id.dialog_title)).setText(title);
		((TextView)findViewById(R.id.dialog_description)).setText(description);
		findViewById(R.id.dialog_button_ok).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.dialog_button_ok) {
			dismiss();
		}
	}

}
