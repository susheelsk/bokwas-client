package com.bokwas;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
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

import com.bokwas.datasets.UserDataStore;

public class ProfileChooserActivity extends Activity implements OnClickListener {

	private Integer[] imageIds = { R.drawable.avatar_1, R.drawable.avatar_2,
			R.drawable.avatar_3, R.drawable.avatar_4, R.drawable.avatar_5,
			R.drawable.avatar_6, R.drawable.avatar_7, R.drawable.avatar_8,
			R.drawable.avatar_9, R.drawable.avatar_10, R.drawable.avatar_11,
			R.drawable.avatar_12, R.drawable.avatar_13, R.drawable.avatar_14,
			R.drawable.avatar_15, R.drawable.avatar_16, R.drawable.avatar_17,
			R.drawable.avatar_18, R.drawable.avatar_19, R.drawable.avatar_20,
			R.drawable.avatar_21, R.drawable.avatar_22, R.drawable.avatar_23,
			R.drawable.avatar_24, R.drawable.avatar_25, R.drawable.avatar_26,
			R.drawable.avatar_27, R.drawable.avatar_28, R.drawable.avatar_29,
			R.drawable.avatar_30, R.drawable.avatar_31, R.drawable.avatar_32,
			R.drawable.avatar_33, R.drawable.avatar_34, R.drawable.avatar_35 };
	private Gallery avatarChooserList;
	private AvatarChooser avatarChooser;
	private EditText nameText;
	protected String nicknameText;
	private int MAX_NUM_BATCHES = 35;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_avatar_page);

		setupAvatarChooser();

		setupNicknameTextbox();

		setOnClickListeners();
	}

	private void setOnClickListeners() {
		findViewById(R.id.nextButton).setOnClickListener(this);
	}

	private void setupNicknameTextbox() {
		nameText = (EditText) findViewById(R.id.nameText);
		InputFilter filter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				// TODO Auto-generated method stub
				for (int i = start; i < end; i++) {
					if (!Character.isLetterOrDigit(source.charAt(i))&&source.charAt(i)!=' ') {
						Toast.makeText(ProfileChooserActivity.this,
								"Please do-not enter special charaters.",
								Toast.LENGTH_SHORT).show();
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
					nameText.setHint("");
				else if (nameText.getText().length() == 0) {
					nameText.setHint("Enter yout name");
				}
			}
		});

		nameText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {

				nicknameText = s.toString();

				// you can call or do what you want with your EditText here
				if (nicknameText.length() > 0
						&& nicknameText.replaceAll("\\s", "").length() > 0) {
					showNextButton();
				} else {
					hideNextButton();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

		});
	}

	protected void hideNextButton() {
		findViewById(R.id.nextButton).setVisibility(View.INVISIBLE);
	}

	protected void showNextButton() {
		findViewById(R.id.nextButton).setVisibility(View.VISIBLE);
	}

	private void setupAvatarChooser() {
		avatarChooserList = (Gallery) findViewById(R.id.avatar_chooser_view);
		List<Integer> badgesList = new ArrayList<Integer>();
		for (int i = 0; i < MAX_NUM_BATCHES; i++) {
			badgesList.add(Integer.valueOf(1));
		}
		Log.d("AvatarChooser", "Width : "
				+ avatarChooserList.getLayoutParams().width);
		Log.d("AvatarChooser",
				"Height : " + avatarChooserList.getLayoutParams().height);
		avatarChooser = new AvatarChooser(this);
		avatarChooserList.setAdapter(avatarChooser);
		avatarChooserList
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// avatarChooser.refreshList();
						for (int i = 0; i < parent.getChildCount(); i++) {
							View childView = parent.getChildAt(i);
							childView.setLayoutParams(new Gallery.LayoutParams(
									avatarChooserList.getHeight() / 2,
									avatarChooserList.getHeight() / 2));
							childView.setBackgroundResource(imageIds[i]);
						}
						view.setLayoutParams(new Gallery.LayoutParams(
								avatarChooserList.getHeight(),
								avatarChooserList.getHeight()));
						view.setBackgroundResource(imageIds[position]);
						UserDataStore.getStore().setAvatarId(position + 1);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						Log.d("AvatarChooser", "Nothing is selected");
					}
				});
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

		/*public void refreshList() {
			Log.d("AvatarChooser", "Refreshing List");
			notifyDataSetChanged();
		}*/

		// Override this method according to your need
		public View getView(int index, View view, ViewGroup viewGroup) {
			// TODO Auto-generated method stub
			ImageView i = new ImageView(mContext);

			i.setImageResource(imageIds[index]);
			i.setLayoutParams(new Gallery.LayoutParams(avatarChooserList
					.getHeight() / 2, avatarChooserList.getHeight() / 2));

			i.setScaleType(ImageView.ScaleType.FIT_XY);
			return i;
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.nextButton) {
			UserDataStore.getStore().setBokwasName(nicknameText);
			Toast.makeText(this, "Thats it folks, for now", Toast.LENGTH_SHORT)
					.show();
		}
	}

}
