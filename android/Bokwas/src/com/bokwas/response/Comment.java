package com.bokwas.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Comment {
	@SerializedName("comment_id")
	private String commentId;
	@SerializedName("bokwas_name")
	private String bokwasName;
	@SerializedName("avatar_id")
	private String avatarId;
	@SerializedName("created_time")
	private long timestamp;
	@SerializedName("message")
	private String commentText;
	@SerializedName("likes")
	private List<Likes> likes = new ArrayList<Likes>();
	@SerializedName("commented_by")
	private String commentedBy;

	public Comment(String commentId, long timestamp, String commentText,
			List<Likes> likes, String commentedBy,String bokwasName,String avatarId) {
		super();
		this.commentId = commentId;
		this.timestamp = timestamp;
		this.commentText = commentText;
		this.likes = likes;
		this.commentedBy = commentedBy;
		this.bokwasName = bokwasName;
		this.avatarId = avatarId;
	}

	public String getBokwasName() {
		return bokwasName;
	}

	public void setBokwasName(String bokwasName) {
		this.bokwasName = bokwasName;
	}

	public String getAvatarId() {
		return avatarId;
	}

	public void setAvatarId(String avatarId) {
		this.avatarId = avatarId;
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

	public List<Likes> getLikes() {
		return likes;
	}

	public void setLikes(List<Likes> likes) {
		this.likes = likes;
	}

	public String getCommentedBy() {
		return commentedBy;
	}

	public void setCommentedBy(String commentedBy) {
		this.commentedBy = commentedBy;
	}
	
	public boolean isAlreadyLiked(String id) {
		for(Likes like : getLikes()) {
			if(like.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	public Likes getLikes(String id) {
		for(Likes like : getLikes()) {
			if(like.getId().equals(id)) {
				return like;
			}
		}
		return null;
	}

	public void addLikes(String personId,String name) {
		if(isAlreadyLiked(personId)) {
			getLikes().remove(getLikes(personId));
			return;
		}
		List<Likes>likes = getLikes();
		likes.add(new Likes(personId, name));
		setLikes(likes);
	}

}