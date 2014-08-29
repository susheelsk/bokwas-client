/*
 * Copyright (C) 2013 AChep@xda <artemchep@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bokwas.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.achep.header2actionbar.HeaderFragment;
import com.bokwas.R;
import com.bokwas.apirequests.GetPostsOfPersonApi;
import com.bokwas.apirequests.GetPostsOfPersonApi.OnGetPostsOfPerson;
import com.bokwas.datasets.UserDataStore;
import com.bokwas.response.Post;
import com.bokwas.ui.HomescreenPostsListAdapter.PostShare;
import com.bokwas.util.GeneralUtil;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

/**
 * Created by Artem on 06.12.13.
 */
public class ProfilePageFragment extends HeaderFragment {

	private ListView mListView;
	private FrameLayout mContentOverlay;
	private AsyncLoadSomething mAsyncLoadSomething;
	private boolean mLoaded;
	private String profileId;
	private String name;
	private int avatarId;
	private String fbProfilePic;
	private boolean isBokwasPost;
	private Activity activity;
	private List<Post> posts;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
		profileId = activity.getIntent().getStringExtra("profileId");
		name = activity.getIntent().getStringExtra("name");
		avatarId = activity.getIntent().getIntExtra("avatarId", -1);
		fbProfilePic = activity.getIntent().getStringExtra("fbProfilePic");
		posts = new ArrayList<Post>();
		if (fbProfilePic == null || fbProfilePic.equals("")) {
			isBokwasPost = true;
		} else {
			isBokwasPost = false;
		}
		setHeaders();
		refreshPosts();
	}

	private void setHeaders() {

		setHeaderBackgroundScrollMode(HEADER_BACKGROUND_SCROLL_PARALLAX);
		setOnHeaderScrollChangedListener(new OnHeaderScrollChangedListener() {
			@Override
			public void onHeaderScrollChanged(float progress, int height, int scroll) {
				try {
					height -= getActivity().getActionBar().getHeight();
					progress = (float) scroll / height;
					if (progress > 1f)
						progress = 1f;

					progress = (1 - (float) Math.cos(progress * Math.PI)) * 0.5f;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mAsyncLoadSomething = new AsyncLoadSomething(this);
		mAsyncLoadSomething.execute();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public View onCreateHeaderView(LayoutInflater inflater, ViewGroup container) {
		View view = inflater.inflate(R.layout.fragment_header, container, false);
		if (name != null && name.length() > 18) {
			name = name.split(" ")[0];
		}
		((TextView) view.findViewById(R.id.name)).setText(name);
		if (avatarId != -1) {
			((ImageView) view.findViewById(R.id.profile_pic)).setImageResource(GeneralUtil.getAvatarResourceId(String.valueOf(avatarId)));
		} else {
			ImageView profPic = ((ImageView) view.findViewById(R.id.profile_pic));
			profPic.setBackgroundResource(android.R.color.transparent);
			UrlImageViewHelper.setUrlDrawable(profPic, fbProfilePic, null, 60000 * 100);
		}
		return view;
	}

	@Override
	public View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
		mListView = (ListView) inflater.inflate(R.layout.fragment_listview, container, false);
		if (mLoaded)
			setListViewTitles(posts);
		return mListView;
	}

	@Override
	public View onCreateContentOverlayView(LayoutInflater inflater, ViewGroup container) {
		ProgressBar progressBar = new ProgressBar(getActivity());
		mContentOverlay = new FrameLayout(getActivity());
		mContentOverlay.addView(progressBar, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		if (mLoaded)
			mContentOverlay.setVisibility(View.GONE);
		return mContentOverlay;
	}

	public void refreshPosts() {
		long since;
		if (posts.size() > 0) {
			if (isBokwasPost) {
				since = posts.get(posts.size() - 1).getTimestamp();
			} else {
				since = posts.get(posts.size() - 1).getTimestamp();
			}
		} else {
			since = System.currentTimeMillis();
		}

		new GetPostsOfPersonApi(activity, UserDataStore.getStore().getAccessKey(), String.valueOf(since), isBokwasPost, UserDataStore.getStore().getUserId(), profileId, new OnGetPostsOfPerson() {

			@Override
			public void onGetPostsOfPerson(List<Post> newPosts) {
				for (Post post : newPosts) {
					if (posts.contains(post) == false) {
						posts.add(post);
					}
				}
				setListViewTitles(posts);
			}
		}).execute("");
	}

	private void setListViewTitles(List<Post> posts) {
		if (posts == null || posts.size() < 1) {
			return;
		}
		mLoaded = true;
		if (mListView == null)
			return;

		mListView.setVisibility(View.VISIBLE);

		if (avatarId != -1) {
			setListViewAdapter(mListView, new HomescreenPostsListAdapter(getActivity(), posts, new PostShare() {

				@Override
				public void onPostShare(int position) {

				}
			}));
		} else {
			setListViewAdapter(mListView, new HomescreenPostsListAdapter(getActivity(), posts, new PostShare() {

				@Override
				public void onPostShare(int position) {

				}
			}));
		}

	}

	private static class AsyncLoadSomething extends AsyncTask<Void, Void, String[]> {

		final WeakReference<ProfilePageFragment> weakFragment;

		public AsyncLoadSomething(ProfilePageFragment fragment) {
			this.weakFragment = new WeakReference<ProfilePageFragment>(fragment);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			final ProfilePageFragment audioFragment = weakFragment.get();
			if (audioFragment.mListView != null)
				audioFragment.mListView.setVisibility(View.INVISIBLE);
			if (audioFragment.mContentOverlay != null)
				audioFragment.mContentOverlay.setVisibility(View.VISIBLE);
		}

		@Override
		protected String[] doInBackground(Void... voids) {

			try {
				// Emulate long downloading
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return new String[] { "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder",
					"Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder",
					"Placeholder" };
		}

		@Override
		protected void onPostExecute(String[] titles) {
			super.onPostExecute(titles);
			final ProfilePageFragment audioFragment = weakFragment.get();
			if (audioFragment == null) {
				return;
			}

			if (audioFragment.mContentOverlay != null)
				audioFragment.mContentOverlay.setVisibility(View.GONE);
		}
	}

}
