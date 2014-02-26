package com.bokwas.util;
/**
 * 
 * @author sk
 * 
 * This class has all the data points concerning the specifics about a user and his attributes
 * It is a singleton class which has to be initialized at the beginning of the application. 
 * The instance of the class is serialized and stored in the database after every write/set operation on the same. 
 * There is no requirement to set into db after every read. 
 *
 */
public class UserDataStore {
	private static UserDataStore instance = null;
	
	private String userId;
	private String userAccessToken;
	private String gender;
	private String email;
	private String bokwasName;
	private int avatarId;

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
	
	public UserDataStore getStore() {
		if(instance == null) {
			return new UserDataStore();
		}
		return instance;
	}
	
}
