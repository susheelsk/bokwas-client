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

import com.bokwas.R;
import com.bokwas.response.Likes;
import com.bokwas.ui.LikesDialogListAdapter;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

public class LikesDialog extends Dialog{
	
	private Activity activity;
	private List<Likes> likes;
	private SuperActivityToast superActivityToast;
	private ListView listView;
	private LikesDialogListAdapter adapter;
	
	public LikesDialog(Activity activity, List<Likes> likes) {
		super(activity);
		this.activity = activity;
		this.likes = likes;
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

		superActivityToast = new SuperActivityToast(activity, SuperToast.Type.PROGRESS);
		superActivityToast.setIndeterminate(true);
		superActivityToast.setProgressIndeterminate(true);
		this.setCanceledOnTouchOutside(true);
		adapter = new LikesDialogListAdapter(activity, likes);
		listView = (ListView) findViewById(R.id.like_list);
		listView.setAdapter(adapter);
	}

}
