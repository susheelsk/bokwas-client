package com.bokwas;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import com.bokwas.apirequests.GetPosts;
import com.bokwas.apirequests.GetPosts.APIListener;
import com.bokwas.apirequests.UpdateProfileInfoApi;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.util.AppData;
import com.bokwas.util.GeneralUtil;
import com.bokwas.util.TrackerName;
import com.google.android.gms.analytics.Tracker;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

@SuppressWarnings("deprecation")
public class SettingsActivity extends Activity implements OnClickListener {

	private Integer[] imageIds;

	private Integer[] maleImageIds = { R.drawable.avatar_16, R.drawable.avatar_17, R.drawable.avatar_18, R.drawable.avatar_19, R.drawable.avatar_20, R.drawable.avatar_21, R.drawable.avatar_22,
			R.drawable.avatar_23, R.drawable.avatar_24, R.drawable.avatar_25, R.drawable.avatar_26, R.drawable.avatar_27, R.drawable.avatar_28, R.drawable.avatar_29, R.drawable.avatar_30 };
	private Integer[] femaleImageIds = { R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3, R.drawable.avatar_4, R.drawable.avatar_5, R.drawable.avatar_6, R.drawable.avatar_7,
			R.drawable.avatar_8, R.drawable.avatar_9, R.drawable.avatar_10, R.drawable.avatar_11, R.drawable.avatar_12, R.drawable.avatar_13, R.drawable.avatar_14, R.drawable.avatar_15 };
	private Gallery avatarChooserList;
	private AvatarChooser avatarChooser;
	private EditText nameText;
	protected String nicknameText;
	private int MAX_NUM_BATCHES = 15;
	private String gender;
	private ProgressDialog pdia;
	protected int newAvatarId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_avatar_page);
		
		if(AppData.isReset) {
			Toast.makeText(this, "Please restart the app", Toast.LENGTH_SHORT).show();
			finish();
		}

		gender = getSharedPreferences(GeneralUtil.sharedPreferences, MODE_PRIVATE).getString(GeneralUtil.userGender, "male");
		if (gender.equals("male")) {
			imageIds = maleImageIds;
		} else {
			imageIds = femaleImageIds;
		}

		setupUI();
		
		setupGoogleAnalytics();

	}
	
	private void setupGoogleAnalytics() {
		Tracker t = GeneralUtil.getTracker(TrackerName.APP_TRACKER,this);
		t.enableAutoActivityTracking(true);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (!UserDataStore.isInitialized()) {
			try {
				UserDataStore.initData(this);
				onBackPressed();
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setupUI();
	}

	private void setupUI() {
		
		setupAvatarChooser();

		setupNicknameTextbox();

		setOnClickListeners();
		
		pdia = new ProgressDialog(this);
		int position = UserDataStore.getStore().getAvatarId() - 1;
		if (gender.equals("male")) {
			position = position % 15;
		}

		avatarChooserList.setSelection(position);
		showNextButton();
	}

	private void setOnClickListeners() {
		findViewById(R.id.nextButton).setOnClickListener(this);
	}

	private void setupAvatarChooser() {
		avatarChooserList = (Gallery) findViewById(R.id.avatar_chooser_view);
		List<Integer> badgesList = new ArrayList<Integer>();
		for (int i = 0; i < MAX_NUM_BATCHES; i++) {
			badgesList.add(Integer.valueOf(1));
		}
		Log.d("AvatarChooser", "Width : " + avatarChooserList.getLayoutParams().width);
		Log.d("AvatarChooser", "Height : " + avatarChooserList.getLayoutParams().height);
		avatarChooser = new AvatarChooser(this);
		avatarChooserList.setAdapter(avatarChooser);
		avatarChooserList.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				try {
					// avatarChooser.refreshList();
					for (int i = 0; i < parent.getChildCount(); i++) {
						View childView = parent.getChildAt(i);
						childView.setLayoutParams(new Gallery.LayoutParams(avatarChooserList.getHeight() / 2, avatarChooserList.getHeight() / 2));
						childView.setBackgroundResource(imageIds[i]);
					}
					view.setLayoutParams(new Gallery.LayoutParams(avatarChooserList.getHeight(), avatarChooserList.getHeight()));
					view.setBackgroundResource(imageIds[position]);
					if (gender.equals("male")) {
						position = 15 + position;
					} else {

					}
					newAvatarId = position + 1;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.d("AvatarChooser", "Nothing is selected");
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(this, HomescreenActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_right);
		finish();
	}

	private class AvatarChooser extends BaseAdapter {
		private Context mContext;

		public AvatarChooser(Context context) {
			mContext = context;
		}

		public int getCount() {
			return imageIds.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		/*
		 * public void refreshList() { Log.d("AvatarChooser",
		 * "Refreshing List"); notifyDataSetChanged(); }
		 */

		// Override this method according to your need
		public View getView(int index, View view, ViewGroup viewGroup) {
			// TODO Auto-generated method stub
			ImageView i = new ImageView(mContext);
			i.setImageResource(imageIds[index]);
			i.setLayoutParams(new Gallery.LayoutParams(avatarChooserList.getHeight() / 2, avatarChooserList.getHeight() / 2));

			i.setScaleType(ImageView.ScaleType.FIT_XY);
			return i;
		}
	}

	private void setupNicknameTextbox() {
		nameText = (EditText) findViewById(R.id.nameText);
		nameText.setText(UserDataStore.getStore().getBokwasName());
		InputFilter filter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				// TODO Auto-generated method stub
				for (int i = start; i < end; i++) {
					if (!Character.isLetterOrDigit(source.charAt(i)) && source.charAt(i) != ' ') {
						Toast.makeText(SettingsActivity.this, "Please do-not enter special charaters.", Toast.LENGTH_SHORT).show();
						return "";
					}
				}
				return null;
			}

		};

		InputFilter lengthFilter = new InputFilter.LengthFilter(24);
		nameText.setFilters(new InputFilter[] { filter, lengthFilter });
		hideNextButton();
		nameText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus)
					nameText.setHint("Pick an epic display name");
				else if (nameText.getText().length() == 0) {
					nameText.setHint("Pick an epic display name");
				}
			}
		});

		nameText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {

				nicknameText = s.toString();

				// you can call or do what you want with your EditText here
				if (nicknameText.length() > 0 && nicknameText.replaceAll("\\s", "").length() > 0) {
					showNextButton();
				} else {
					hideNextButton();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

		});
		nameText.setSelected(false);
	}

	protected void hideNextButton() {
		findViewById(R.id.nextButton).setVisibility(View.INVISIBLE);
	}

	protected void showNextButton() {
		findViewById(R.id.nextButton).setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.nextButton) {
			// make api call to update the users chosen avatar id and username
			if (nameText.getText().toString().length() > 1) {
				pdia.setMessage("Loading");
				pdia.setCancelable(false);
				pdia.setCanceledOnTouchOutside(false);
				pdia.show();
				new UpdateProfileInfoApi(this, UserDataStore.getStore().getUserId(), UserDataStore.getStore().getAccessKey(), nameText.getEditableText().toString(), String.valueOf(newAvatarId),
						new APIListener() {

							@Override
							public void onAPIStatus(boolean status) {
								if(status) {
									UserDataStore.getStore().resetPosts();
									UserDataStore.getStore().save(SettingsActivity.this);
									new GetPosts(SettingsActivity.this, UserDataStore.getStore().getUserAccessToken(), UserDataStore.getStore().getUserId(), new APIListener() {

										@Override
										public void onAPIStatus(boolean status) {
											if (status) {
												Crouton.makeText(SettingsActivity.this, "Profile information updated", Style.INFO).show();
												pdia.cancel();
											}
										}
									}).execute("");
								}else {
									pdia.cancel();
								}
								
							}
						}).execute("");
			} else {
				Toast.makeText(SettingsActivity.this, "Please enter an appropriate username.", Toast.LENGTH_SHORT).show();
			}
		}
	}

}
