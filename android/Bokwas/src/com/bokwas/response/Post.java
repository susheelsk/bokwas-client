package com.bokwas.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Post {
	@SerializedName("post_id")
	private String postId;
	@SerializedName("created_time")
	private long timestamp;
	@SerializedName("updated_time")
	private long updatedTime;
	@SerializedName("message")
	private String postText;
	@SerializedName("likes")
	private String likes;
	@SerializedName("posted_by")
	private String postedBy;
	@SerializedName("isBokwasPost")
	private boolean isBokwasPost;
	@SerializedName("comments")
	private List<Comment> comments = new ArrayList<Comment>();

	public long getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(long updatedTime) {
		this.updatedTime = updatedTime;
	}
	
	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getPostText() {
		return postText;
	}

	public void setPostText(String postText) {
		this.postText = postText;
	}

	public String getLikes() {
		return likes;
	}

	public void setLikes(String likes) {
		this.likes = likes;
	}

	public String getPostedBy() {
		return postedBy;
	}

	public void setPostedBy(String postedBy) {
		this.postedBy = postedBy;
	}

	public boolean isBokwasPost() {
		return isBokwasPost;
	}

	public void setBokwasPost(boolean isBokwasPost) {
		this.isBokwasPost = isBokwasPost;
	}
	
	public Comment getComment(String commentId) {
		for(Comment comment : comments) {
			if(comment.getCommentId().equals(commentId)) {
				return comment;
			}
		}
		return null;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
	public void addComment(Comment comment) {
		comments.add(comment);
	}
	
	public void addLikes(String personId) {
		String likes = getLikes();
		if(likes.contains(personId)) {
			return;
		}
		likes += personId + ",";
		setLikes(likes);
	}

	public Post(String postId, long timestamp,long updatedTime, String postText,
			String likes, String postedBy, boolean isBokwasPost,
			List<Comment> comments) {
		super();
		this.postId = postId;
		this.timestamp = timestamp;
		this.updatedTime = updatedTime;
		this.postText = postText;
		this.likes = likes;
		this.postedBy = postedBy;
		this.isBokwasPost = isBokwasPost;
		this.comments = comments;
	}

}