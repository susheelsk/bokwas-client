package com.bokwas.datasets;

import java.util.ArrayList;
import java.util.List;

public class FacebookPosts {
	
	private static FacebookPosts instance = null;
	private String postUpdaterName;
	private long timestamp;
	private String postUpdaterImageLink;
	private String postText;
	private List<Comment> comment = new ArrayList<Comment>();
	
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
	public String getPostUpdaterImageLink() {
		return postUpdaterImageLink;
	}
	public void setPostUpdaterImageLink(String postUpdaterImageLink) {
		this.postUpdaterImageLink = postUpdaterImageLink;
	}
	public List<Comment> getComment() {
		return comment;
	}
	public void setComment(List<Comment> comment) {
		this.comment = comment;
	}
	
	private FacebookPosts() {
		
	}
	
	public static synchronized FacebookPosts getFbPosts() {
		if(instance == null) {
			return new FacebookPosts();
		}
		return instance;
	}
	
}
