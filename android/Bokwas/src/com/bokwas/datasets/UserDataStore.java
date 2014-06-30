package com.bokwas.datasets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.bokwas.response.Post;
import com.bokwas.util.LocalStorage;

/**
 * 
 * @author sk
 * 
 *         This class has all the data points concerning the specifics about a
 *         user and his attributes It is a singleton class which has to be
 *         initialized at the beginning of the application. The instance of the
 *         class is serialized and stored in the database after every write/set
 *         operation on the same. There is no requirement to set into db after
 *         every read.
 * 
 */
public class UserDataStore {
	private static UserDataStore instance = null;

	private String userId;
	private String userAccessToken;
	private String gender;
	private String email;
	private String bokwasName;
	private String fbName;
	private String fbPicLink;
	private int avatarId;
	private String accessKey;
	private List<Post> posts = new ArrayList<Post>();
	private List<Friends> friends = new ArrayList<Friends>();
	private String gcmRegId;
	private boolean gcmUpdated = false;

	public boolean isGcmUpdated() {
		return gcmUpdated;
	}

	public void setGcmUpdated(boolean gcmUpdated) {
		this.gcmUpdated = gcmUpdated;
	}

	public String getGcmRegId() {
		return gcmRegId;
	}

	public void setGcmRegId(String gcmRegId) {
		this.gcmRegId = gcmRegId;
	}

	public Friends getFriend(String friendFbId) {
		for (Friends friend : friends) {
			if (friend.getId().equals(friendFbId)) {
				return friend;
			}
		}
		return null;
	}
	
	public List<Post> getBokwasPostsOfPerson(String personId) {
		List<Post> userPosts = new ArrayList<Post>();
		for (Post post : getPosts()) {
			if (post.getPostedBy().equals(personId) && post.isBokwasPost()) {
				userPosts.add(post);
			}
		}
		return userPosts;
	}
	
	public List<Post> getPostsOfPerson(String personId) {
		List<Post> userPosts = new ArrayList<Post>();
		for (Post post : getPosts()) {
			if (post.getPostedBy().equals(personId)) {
				userPosts.add(post);
			}
		}
		return userPosts;
	}

	public Post getPost(String postId) {
		for (Post post : posts) {
			if (post.getPostId().equals(postId)) {
				return post;
			}
		}
		return null;
	}
	
	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getFbName() {
		return fbName;
	}

	public void setFbName(String fbName) {
		this.fbName = fbName;
	}

	public String getFbPicLink() {
		return fbPicLink;
	}

	public void setFbPicLink(String fbPicLink) {
		this.fbPicLink = fbPicLink;
	}

	public void addPost(Post newPost) {
		for (Post post : posts) {
			if (post.getPostId().equals(newPost.getPostId())) {
				updatePost(newPost);
				return;
			}
		}
		posts.add(newPost);
	}
	
	public void addNewPost(Post newPost) {
		for (Post post : posts) {
			if (post.getPostId().equals(newPost.getPostId())) {
				updatePost(newPost);
				return;
			}
		}
		posts.add(0,newPost);
	}
	
	public void sortPosts() {
		Collections.sort(posts, new PostComparator());
	}

	public void updatePost(Post newPost) {
		for (int i = 0; i < posts.size(); i++) {
			if (posts.get(i).getPostId().equals(newPost.getPostId())) {
				posts.get(i).setBokwasPost(newPost.isBokwasPost());
				posts.get(i).setComments(newPost.getComments());
				posts.get(i).setLikes(newPost.getLikes());
				posts.get(i).setUpdatedTime(newPost.getUpdatedTime());
			}
		}
	}

	public void deletePost(Post post) {
		try {
			posts.remove(post);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Post> getPosts() {
//		Collections.sort(posts, new PostComparator());
		return posts;
	}

	public void setPosts(List<Post> post) {
		this.posts = post;
	}

	public List<Friends> getFriends() {
		return friends;
	}

	public void setFriends(List<Friends> friends) {
		this.friends = friends;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserAccessToken() {
		return userAccessToken;
	}

	public void setUserAccessToken(String userAccessToken) {
		this.userAccessToken = userAccessToken;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBokwasName() {
		return bokwasName;
	}

	public void setBokwasName(String bokwasName) {
		this.bokwasName = bokwasName;
	}

	public int getAvatarId() {
		return avatarId;
	}

	public void setAvatarId(int avatarId) {
		this.avatarId = avatarId;
	}

	protected UserDataStore() {

	}

	public void save(Context context) {
		if (getStore() != null && getStore().bokwasName != null)
			Log.d("LocalStorage", "Name: " + getStore().getBokwasName());
		LocalStorage.storeObj(context, instance);
	}

	public static synchronized void setInstance(UserDataStore userDataStore) {
		instance = userDataStore;
	}
	
	public static void initData(Context context) {
		setInstance(LocalStorage.getObj(context,
				UserDataStore.class));
	}

	public static synchronized UserDataStore getStore() {
		if (instance == null) {
			return instance = new UserDataStore();
		}
		return instance;
	}

	private class PostComparator implements Comparator<Post> {
		public int compare(Post a, Post b) {
			Date dateA = new Date(a.getUpdatedTime());
			Date dateB = new Date(b.getUpdatedTime());
			return dateB.compareTo(dateA);
		}
	}

}
