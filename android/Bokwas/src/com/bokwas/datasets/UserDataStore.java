package com.bokwas.datasets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

import com.bokwas.response.Notification;
import com.bokwas.response.Post;
import com.bokwas.util.AppData;
import com.bokwas.util.DateUtil;
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
	private List<Notification> notificationList = new ArrayList<Notification>();
	private String gcmRegId;
	private boolean gcmUpdated = false;
	private HashMap<String, List<Message>> messageMap = new HashMap<String, List<Message>>();
	private boolean isInit = false;

	public boolean isInit() {
		return isInit;
	}

	public void setInit(boolean isInit) {
		this.isInit = isInit;
	}

	public List<Message> getMessagesForPerson(String personId) {
		List<Message> messages = messageMap.get(personId);
		if (messages != null) {
			Collections.sort(messages, new MessageComparator());
		} else {
			messages = new ArrayList<Message>();
		}
		return messages;
	}
	
	public List<Message> getTodaysMessagesForPerson(String personId) {
		List<Message> messages = new ArrayList<Message>();
		for(Message message : getMessagesForPerson(personId)) {
			if(DateUtil.getDateDiff(new Date(), new Date(message.getTimestamp()), TimeUnit.DAYS)<1) {
				messages.add(message);
			}else {
				break;
			}
		}
		Collections.sort(messages, new MessageComparator());
		return messages;
	}

	public void addMessageToPerson(String personId, Message message) {
		if (personId != null) {
			List<Message> messages = messageMap.get(personId);
			if (messages == null) {
				messages = new ArrayList<Message>();
			}
			if (getMessage(personId, message.getMessageId()) == null) {
				messages.add(message);
			}
			messageMap.put(personId, messages);
		}
	}

	private Message getMessage(String personId, String messageId) {
		List<Message> messages = messageMap.get(personId);
		if (messages != null) {
			Collections.sort(messages, new MessageComparator());
		} else {
			messages = new ArrayList<Message>();
		}
		for (Message message : messages) {
			if (message.getMessageId().equals(messageId)) {
				return message;
			}
		}
		return null;
	}

	private void removeOldMessages() {
		for (Friends friend : getFriends()) {
			if (getMessagesForPerson(friend.getId()) != null) {
				if (getMessagesForPerson(friend.getId()).size() > 200) {
//					messageMap.put(friend.getId(), getMessagesForPerson(friend.getId()).subList(0, 200));
					messageMap.put(friend.getId(), new ArrayList<Message>());
				}
				if(getMessagesForPerson(friend.getId()).size()<1) {
					continue;
				}
				Date lastMessageDate = new Date(getMessagesForPerson(friend.getId()).get(getMessagesForPerson(friend.getId()).size()-1).getTimestamp());
				Date nowMessageDate = new Date();
				long daysDiff = DateUtil.getDateDiff(lastMessageDate, nowMessageDate, TimeUnit.DAYS);
				if(daysDiff >= 1) {
					messageMap.put(friend.getId(), new ArrayList<Message>());
				}
			}
		}
	}

	public boolean isGcmUpdated() {
		return gcmUpdated;
	}

	public void setGcmUpdated(boolean gcmUpdated) {
		this.gcmUpdated = gcmUpdated;
	}

	public String getGcmRegId() {
		return gcmRegId;
	}

	public int getUnSeenMessagesSize() {
		int unseenMessagesSize = 0;
		for (Friends friend : getFriends()) {
			for (Message message : getMessagesForPerson(friend.getId())) {
				if (!message.isSeen()) {
					unseenMessagesSize++;
				}
			}
		}
		return unseenMessagesSize;
	}

	public void addNotification(Notification newNotification) {
		Post post = getPost(newNotification.getNotification_data().get("postId"));
		Date today = new Date();
		Date notificationDate = new Date(newNotification.getTimestamp());
		int diffInMins = (int) DateUtil.getDateDiff(today, notificationDate, TimeUnit.MINUTES);
		for (Notification notif : notificationList) {
			if (notif.getNotification_id().equals(newNotification.getNotification_id())) {
				// duplicate notification. Already exists
				return;
			}
			if (notif.getNotification_data().get("postId").equals(newNotification.getNotification_data().get("postId")) && diffInMins < 20) {
				// one notification for this post alreay exists and it came
				// within 2 days
				return;
			}
		}
		if (post != null) {
			notificationList.add(newNotification);
		} else {
			Log.d("BokwasNotification", "Post not found concerning notification");
		}
	}

	public List<Notification> getNotifications() {
		Collections.sort(notificationList, new NotificationComparator());
		return notificationList;
	}

	public List<Notification> getUnseenNotifications() {
		List<Notification> notifications = new ArrayList<Notification>();
		for (Notification notif : notificationList) {
			if (!notif.isViewed()) {
				notifications.add(notif);
			}
		}
		return notifications;
	}

	public Notification getNotification(String notificationId) {
		for (Notification notif : notificationList) {
			if (notificationId.equals(notif.getNotification_id())) {
				return notif;
			}
		}
		return null;
	}

	public void removeNotification(String notificationId) {
		notificationList.remove(getNotification(notificationId));
	}

	@SuppressWarnings("unused")
	public void removeOldNotifications() {
		List<Integer> removeIds = new ArrayList<Integer>();
		List<Notification> removeNotifications = new ArrayList<Notification>();
		int i = 0;
		Date today = new Date();
		for (Notification notification : notificationList) {
			Date notificationDate = new Date(notification.getTimestamp());
			int diffInDays = (int) DateUtil.getDateDiff(today, notificationDate, TimeUnit.DAYS);
			Post post = getPost(notification.getNotification_data().get("postId"));
			if (post == null || diffInDays > 15) {
				removeIds.add(i);
				removeNotifications.add(notification);
			}
			i++;
		}
		i = 0;
		if (removeIds.size() > 0) {
			for (Integer removalId : removeIds) {
				Log.d("BokwasNotifications", "Removing notification");
				notificationList.remove(removeNotifications.get(i));
				i++;
			}
		}
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
		String postId;
		for (Post post : getPosts()) {
			Log.d("BokwasPosts", "post.getPostedBy : " + post.getPostedBy() + " , userId : " + personId + " , isBokwasPost : " + post.isBokwasPost());
			postId = post.getPostedBy();
			if (postId.trim().equals(personId.trim()) && post.isBokwasPost()) {
				Log.d("BokwasPosts", "Found user's post");
				userPosts.add(post);
			}
		}
		return userPosts;
	}

	public List<Post> getFbPostsOfPerson(String personId) {
		List<Post> userPosts = new ArrayList<Post>();
		String postId;
		for (Post post : getPosts()) {
			postId = post.getPostedBy();
			Log.d("BokwasPosts", "post.getPostedBy : " + post.getPostedBy() + " , userId : " + personId + " , isBokwasPost : " + post.isBokwasPost());
			if (postId.trim().equals(personId.trim()) && !post.isBokwasPost()) {
				Log.d("BokwasPosts", "Found user's post");
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
		posts.add(0, newPost);
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
		// Collections.sort(posts, new PostComparator());
		return posts;
	}

	public void setPosts(List<Post> post) {
		this.posts = post;
	}

	public List<Friends> getFriends() {
		return friends;
	}

	public boolean isPostFromFriendOrMe(Post post) {
		if (post.getPostedBy().equals(getUserId())) {
			return true;
		}
		for (Friends friend : getFriends()) {
			if (post.getPostedBy().equals(friend.getId())) {
				return true;
			}
		}
		return false;
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

	private UserDataStore() {

	}

	public void save(Context context) {
		if (getStore() != null && getStore().bokwasName != null)
			Log.d("LocalStorage", "Name: " + getStore().getBokwasName());
		LocalStorage.storeObj(context, instance);
	}

	public void resetData(Context context) {
		setInstance(null);
		save(context);
		AppData.isReset = true;
	}

	public void resetPosts() {
		posts = new ArrayList<Post>();
	}

	public static synchronized void setInstance(UserDataStore userDataStore) {
		instance = userDataStore;
	}

	public static void initData(Context context) {
		setInstance(LocalStorage.getObj(context, UserDataStore.class));
		getStore().sortPosts();
		if (getStore().getPosts().size() > AppData.MAX_POSTS_LIMIT) {
			for (int i = 100; i < getStore().getPosts().size(); i++) {
				getStore().getPosts().remove(i);
			}
			getStore().save(context);
		}
		getStore().removeOldNotifications();
		getStore().removeOldMessages();
	}

	public static synchronized boolean isInitialized() {
		if (instance == null || !instance.isInit()) {
			Log.d("Bokwas","UserDataStore : null");
			return false;
		}
		return true;
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

	private class MessageComparator implements Comparator<Message> {
		public int compare(Message a, Message b) {
			Date dateA = new Date(a.getTimestamp());
			Date dateB = new Date(b.getTimestamp());
			return dateA.compareTo(dateB);
		}
	}

	private class NotificationComparator implements Comparator<Notification> {
		public int compare(Notification a, Notification b) {
			Date dateA = new Date(a.getTimestamp());
			Date dateB = new Date(b.getTimestamp());
			return dateB.compareTo(dateA);
		}
	}

}
