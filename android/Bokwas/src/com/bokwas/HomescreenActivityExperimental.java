//package com.bokwas;
//
//import android.app.ActionBar;
//import android.app.Activity;
//import android.content.Intent;
//import android.content.res.Resources;
//import android.graphics.Color;
//import android.graphics.RectF;
//import android.os.Bundle;
//import android.text.Spannable;
//import android.text.SpannableString;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.Window;
//import android.view.animation.AccelerateDecelerateInterpolator;
//import android.widget.AbsListView;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.bokwas.apirequests.GetPosts;
//import com.bokwas.apirequests.GetPosts.APIListener;
//import com.bokwas.datasets.UserDataStore;
//import com.bokwas.ui.AlphaForegroundColorSpan;
//import com.bokwas.ui.HomescreenPostsListAdapter;
//import com.bokwas.ui.HomescreenPostsListAdapter.PostShare;
//import com.handmark.pulltorefresh.library.PullToRefreshBase;
//import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
//import com.handmark.pulltorefresh.library.PullToRefreshListView;
//
//public class HomescreenActivityExperimental extends Activity implements OnClickListener,PostShare {
//
//	private PullToRefreshListView listView;
//	private View mFakeHeader;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);	
//		setContentView(R.layout.home_screen_experimental);
//
//		setOnClickListeners();
//
//		setupUI();
//
//	}
//	
//	@Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.overflow_menu, menu);
// 
//        return super.onCreateOptionsMenu(menu);
//    }
//
//	private void setupUI() {
//		ActionBar actionBar = getActionBar();
//		actionBar.setIcon(R.drawable.bokwas_icon);
//
//		final HomescreenPostsListAdapter adapter = new HomescreenPostsListAdapter(
//				this, UserDataStore.getStore().getPosts(),this);
//		getActionBarTitleView().setAlpha(0f);
//		listView = (PullToRefreshListView) findViewById(R.id.feed_list);
//		mFakeHeader = getLayoutInflater().inflate(R.layout.header_main,
//				listView, false);
//		listView.setAdapter(adapter);
//		listView.getRefreshableView().addHeaderView(mFakeHeader);
//		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//			}
//
//			@Override
//			public void onScroll(AbsListView view, int firstVisibleItem,
//					int visibleItemCount, int totalItemCount) {
//				float ratio = clamp(mFakeHeader.getTranslationY() / 24, 0.0f,
//						1.0f);
//				// actionbar title alpha
//				setTitleAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
//				AccelerateDecelerateInterpolator mAccelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
//				float interpolation = mAccelerateDecelerateInterpolator
//						.getInterpolation(ratio);
//
//				View actionBarIconView = getActionBarIconView();
//
//				RectF mRect1 = new RectF();
//				RectF mRect2 = new RectF();
//
//				View mHeaderLogo = HomescreenActivityExperimental.this
//						.findViewById(R.id.header_logo);
//				getOnScreenRect(mRect1, mHeaderLogo);
//				getOnScreenRect(mRect2, actionBarIconView);
//
//				float scaleX = 1.0F + interpolation
//						* (mRect2.width() / mRect1.width() - 1.0F);
//				float scaleY = 1.0F + interpolation
//						* (mRect2.height() / mRect1.height() - 1.0F);
//				float translationX = 0.5F * (interpolation * (mRect2.left
//						+ mRect2.right - mRect1.left - mRect1.right));
//				float translationY = 0.5F * (interpolation * (mRect2.top
//						+ mRect2.bottom - mRect1.top - mRect1.bottom));
//
//				mHeaderLogo.setTranslationX(translationX);
//				mHeaderLogo.setTranslationY(translationY
//						- mFakeHeader.getTranslationY());
//				mHeaderLogo.setScaleX(scaleX);
//				mHeaderLogo.setScaleY(scaleY);
//			}
//		});
//		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
//			@Override
//			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
//				new GetPosts(HomescreenActivityExperimental.this, UserDataStore
//						.getStore().getUserAccessToken(), UserDataStore
//						.getStore().getUserId(), new APIListener() {
//
//					@Override
//					public void onAPIStatus(boolean status) {
//						listView.onRefreshComplete();
//						if (status) {
//							adapter.setPosts(UserDataStore.getStore()
//									.getPosts());
//							adapter.notifyDataSetChanged();
//						}
//					}
//				}).execute("");
//			}
//		});
//		if (getIntent().getBooleanExtra("fromSplashscreen", false)) {
//			listView.setRefreshing(true);// pdia.show
//		}
//	}
//
//	private RectF getOnScreenRect(RectF rect, View view) {
//		rect.set(view.getLeft(), view.getTop(), view.getRight(),
//				view.getBottom());
//		return rect;
//	}
//
//	public static float clamp(float value, float max, float min) {
//		return Math.max(Math.min(value, min), max);
//	}
//
//	private void setTitleAlpha(float alpha) {
//		AlphaForegroundColorSpan mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(
//				Color.parseColor("#F99238"));
//		mAlphaForegroundColorSpan.setAlpha(alpha);
//		SpannableString mSpannableString = new SpannableString("bokwas");
//		mSpannableString.setSpan(mAlphaForegroundColorSpan, 0,
//				mSpannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		getActionBar().setTitle(mSpannableString);
//	}
//
//	private TextView getActionBarTitleView() {
//		int id = Resources.getSystem().getIdentifier("action_bar_title", "id",
//				"android");
//		return (TextView) findViewById(id);
//	}
//
//	private void setOnClickListeners() {
////		findViewById(R.id.newPostButton).setOnClickListener(this);
////		findViewById(R.id.overflowButton).setOnClickListener(this);
//	}
//
//	private ImageView getActionBarIconView() {
//		return (ImageView) findViewById(android.R.id.home);
//	}
//
////	private int getScrollY() {
////		View c = listView.getChildAt(0);
////		if (c == null) {
////			return 0;
////		}
////
////		int firstVisiblePosition = listView.getChildAt(0).getTop();
////		int top = c.getTop();
////
////		int headerHeight = 0;
////		if (firstVisiblePosition >= 1) {
////			headerHeight = mFakeHeader.getHeight();
////		}
////
////		return -top + firstVisiblePosition * c.getHeight() + headerHeight;
////	}
//
//	@Override
//	public void onClick(View view) {
//		if (view.getId() == R.id.newPostButton) {
//			Intent intent = new Intent(this, NewPostActivity.class);
//			startActivity(intent);
//			overridePendingTransition(R.anim.activity_slide_in_left,
//					R.anim.activity_slide_out_left);
//			finish();
//		} else if (view.getId() == R.id.overflowButton) {
//
//		}
//	}
//
//@Override
//public void onPostShare(int position) {
//	// TODO Auto-generated method stub
//	
//}
//
//}
