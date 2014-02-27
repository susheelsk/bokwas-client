package com.bokwas.datasets;

public class Comment {

	private String commentUpdaterName;
	private String commentUpdaterAvatarId;
	private String timestamp;
	private String commentText;
	
	public String getCommentUpdaterName() {
		return commentUpdaterName;
	}
	public void setCommentUpdaterName(String commentUpdaterName) {
		this.commentUpdaterName = commentUpdaterName;
	}
	public String getCommentUpdaterAvatarId() {
		return commentUpdaterAvatarId;
	}
	public void setCommentUpdaterAvatarId(String commentUpdaterAvatarId) {
		this.commentUpdaterAvatarId = commentUpdaterAvatarId;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getCommentText() {
		return commentText;
	}
	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}
	
}
