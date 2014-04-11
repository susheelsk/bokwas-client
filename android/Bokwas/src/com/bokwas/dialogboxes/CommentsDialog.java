package com.bokwas.dialogboxes;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.bokwas.R;
import com.bokwas.apirequests.AddCommentsApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Comment;
import com.bokwas.response.Post;
import com.bokwas.ui.CommentsDialogListAdapter;

public class CommentsDialog extends Dialog implements OnClickListener {

	private Activity activity;
	private List<Comment> comments;
	private EditText editText;
	private Post post;
	private ProgressDialog pdia;
	private ListView listView;
	private CommentsDialogListAdapter adapter;

	public CommentsDialog(Activity activity, List<Comment> comments, Post post) {
		super(activity);
		this.activity = activity;
		this.comments = comments;
		this.post = post;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.comment_dialog);
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

		pdia = new ProgressDialog(activity);
		editText = (EditText) findViewById(R.id.comment_edittext);
		findViewById(R.id.commentButton).setOnClickListener(this);

		adapter = new CommentsDialogListAdapter(
				activity, comments,post);
		listView = (ListView) findViewById(R.id.comment_list);
		listView.setAdapter(adapter);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.commentButton) {
			if (editText.getText().toString().trim() != null) {
				pdia.setMessage("Adding comment");
				pdia.setCancelable(false);
				pdia.show();
				new AddCommentsApi(UserDataStore.getStore()
						.getUserAccessToken(), post.getPostId(),
						post.getPostedBy(), editText.getText().toString(),
						UserDataStore.getStore().getUserId(), activity,
						new APIListener() {

							@Override
							public void onAPIStatus(boolean status) {
								if(pdia.isShowing()) {
									pdia.dismiss();
								}
								if (status) {
									Toast.makeText(activity, "Comment added!",
											Toast.LENGTH_SHORT).show();
									editText.setText("");
									adapter.notifyDataSetChanged();
								} else {
									Toast.makeText(activity,
											"Comment couldn't be added!",
											Toast.LENGTH_SHORT).show();
								}
							}
						}).execute("");
			}
		}
	}

}