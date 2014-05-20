package com.bokwas.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
	private List<Likes> likes = new ArrayList<Likes>();
	@SerializedName("posted_by")
	private String postedBy;
	@SerializedName("isBokwasPost")
	private boolean isBokwasPost;
	@SerializedName("name")
	private String name;
	@SerializedName("avatar_id")
	private String avatarId;
	@SerializedName("comments")
	private List<Comment> comments = new ArrayList<Comment>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvatarId() {
		return avatarId;
	}

	public void setAvatarId(String avatarId) {
		this.avatarId = avatarId;
	}
	
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

	public List<Likes> getLikes() {
		return likes;
	}

	public void setLikes(List<Likes> likes) {
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
		Collections.sort(comments, new CommentComparator());
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
	public void addComment(Comment comment) {
		comments.add(comment);
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

	public Post(String postId, long timestamp,long updatedTime, String postText,
			List<Likes> likes, String postedBy, boolean isBokwasPost,
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
	
	private class CommentComparator implements Comparator<Comment> {
		public int compare(Comment a, Comment b) {
			Date dateA = new Date(a.getTimestamp());
			Date dateB = new Date(b.getTimestamp());
			return dateA.compareTo(dateB);
		}
	}

}