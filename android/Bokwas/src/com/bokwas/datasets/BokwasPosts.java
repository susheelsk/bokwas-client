package com.bokwas.datasets;

import java.util.ArrayList;
import java.util.List;

public class BokwasPosts {
	
	private static BokwasPosts instance;
	private String postUpdaterName;
	private long timestamp;
	private int postUpdaterAvatarId;
	private String postText;
	private int numUpvotes;
	private List<Comment> comment = new ArrayList<Comment>();
	
	public int getNumUpvotes() {
		return numUpvotes;
	}
	public void setNumUpvotes(int numUpvotes) {
		this.numUpvotes = numUpvotes;
	}
	
	public String getPostText() {
		return postText;
	}
	public void setPostText(String postText) {
		this.postText = postText;
	}


	public String getPostUpdaterName() {
		return postUpdaterName;
	}

	public void setPostUpdaterName(String postUpdaterName) {
		this.postUpdaterName = postUpdaterName;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getPostUpdaterAvatarId() {
		return postUpdaterAvatarId;
	}

	public void setPostUpdaterAvatarId(int postUpdaterAvatarId) {
		this.postUpdaterAvatarId = postUpdaterAvatarId;
	}

	public List<Comment> getComment() {
		return comment;
	}

	public void setComment(List<Comment> comment) {
		this.comment = comment;
	}

	private BokwasPosts() {

	}

	public static synchronized BokwasPosts getFbPosts() {
		if (instance == null) {
			return new BokwasPosts();
		}
		return instance;
	}
}
