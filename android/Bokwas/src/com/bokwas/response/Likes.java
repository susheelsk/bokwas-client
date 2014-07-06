package com.bokwas.response;

public class Likes {

	private String fbid;
	private String name;
	private String avatar_id;

	public String getId() {
		return fbid;
	}

	public String getName() {
		return name;
	}

	public String getAvatarId() {
		return avatar_id;
	}

	public Likes(String id, String name, String avatarId) {
		super();
		this.fbid = id;
		this.name = name;
		this.avatar_id = avatarId;
	}

}
