package com.bokwas.response;

import com.google.gson.annotations.SerializedName;

public class Comment {
	@SerializedName("comment_id")
	private String commentId;
	@SerializedName("created_time")
	private long timestamp;
	@SerializedName("message")
	private String commentText;
	@SerializedName("likes")
	private String likes;
	@SerializedName("commented_by")
	private String commentedBy;

	public Comment(String commentId, long timestamp, String commentText,
			String likes, String commentedBy) {
		super();
		this.commentId = commentId;
		this.timestamp = timestamp;
		this.commentText = commentText;
		this.likes = likes;
		this.commentedBy = commentedBy;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getCommentText() {
		return commentText;
	}

	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}

	public String getLikes() {
		return likes;
	}

	public void setLikes(String likes) {
		this.likes = likes;
	}

	public String getCommentedBy() {
		return commentedBy;
	}

	public void setCommentedBy(String commentedBy) {
		this.commentedBy = commentedBy;
	}

	public void addLikes(String personId) {
		String likes = getLikes();
		if(likes.contains(personId)) {
			return;
		}
		likes += personId + ",";
		setLikes(likes);
	}

}