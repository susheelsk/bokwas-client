package com.bokwas.ui;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokwas.R;
import com.bokwas.response.Likes;
import com.bokwas.util.GeneralUtil;

public class LikesDialogListAdapter extends ArrayAdapter<Likes>{
	
	private Activity activity;
	private List<Likes> likes;

	public LikesDialogListAdapter(Activity activity, List<Likes> likes) {
		super(activity, R.layout.like_list_item, likes);
		this.likes = likes;
		this.activity = activity;
	}
	
	static class ViewHolder {
		public TextView name;
		public ImageView picture;
	}

	@Override
	public int getCount() {
		return likes.size();
	}

	@Override
	public Likes getItem(int position) {
		return likes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		Likes like = likes.get(position);
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.like_list_item, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) rowView.findViewById(R.id.likes_name);
			viewHolder.picture = (ImageView) rowView
					.findViewById(R.id.likes_profile_pic);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		
		holder.name.setText(like.getName());
		holder.picture.setImageBitmap(GeneralUtil.getImageBitmap(
				GeneralUtil.getAvatarResourceId(like.getId()),
				activity));
		return rowView;
	}

}
