package com.bokwas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bokwas.apirequests.AddPostsApi;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.util.GeneralUtil;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

public class NewPostActivity extends Activity implements OnClickListener {

	EditText editText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.newpost_dialog);

		setOnClickListeners();

		setupUI();

	}

	private void setupUI() {
		editText = (EditText) findViewById(R.id.post_content);
		ImageView profilePic = (ImageView) findViewById(R.id.post_profile_pic);
		TextView nameTextView = (TextView) findViewById(R.id.post_name);
		nameTextView.setText(UserDataStore.getStore().getBokwasName());
		String avatarId = String
				.valueOf(UserDataStore.getStore().getAvatarId());
		profilePic.setImageBitmap(GeneralUtil.getImageBitmap(
				GeneralUtil.getAvatarResourceId(avatarId), this));
	}

	private void setOnClickListeners() {
		findViewById(R.id.post_like_button).setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(this, HomescreenActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_slide_in_right,
				R.anim.activity_slide_out_right);
		finish();
	}

	@Override
	public void onClick(View view) {
		if (editText.getText().toString().trim() != null
				&& !editText.getText().toString().trim().equals("")) {
//			final ProgressDialog pdia = new ProgressDialog(NewPostActivity.this);
//			pdia.setMessage("Adding new post...");
//			pdia.setCancelable(false);
//			pdia.show();
			
			final SuperActivityToast superActivityToast = new SuperActivityToast(NewPostActivity.this, SuperToast.Type.PROGRESS);
			superActivityToast.setIndeterminate(true);
			superActivityToast.setProgressIndeterminate(true);
			superActivityToast.setText("Adding new post...");
			superActivityToast.show();
			new AddPostsApi(this, UserDataStore.getStore().getAccessKey(),
					UserDataStore.getStore().getUserId(), editText.getText()
							.toString(), new APIListener() {

						@Override
						public void onAPIStatus(boolean status) {
							if (superActivityToast.isShowing()) {
								superActivityToast.dismiss();
							}
							if (status) {
								Toast.makeText(NewPostActivity.this,
										"Post added!", Toast.LENGTH_SHORT)
										.show();
								moveToHomescreen();
							} else {
								Toast.makeText(NewPostActivity.this,
										"Couldn't add post. Try again",
										Toast.LENGTH_SHORT).show();
							}
						}
					}).execute("");
		}
	}

	protected void moveToHomescreen() {
		onBackPressed();
	}

}
