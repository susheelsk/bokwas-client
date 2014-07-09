package com.bokwas.datasets;

import com.google.gson.annotations.SerializedName;

public class Friends {
	
	@SerializedName("fbname")
	public String fbName;
	@SerializedName("fbid")
	public String id;
	@SerializedName("bokwas_name")
	public String bokwasName;
	@SerializedName("avatar_id")
	public String bokwasAvatarId;
	
	private String fbPicLink;

	public String getFbName() {
		return fbName;
	}

	public void setFbName(String fbName) {
		this.fbName = fbName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFbPicLink() {
		return fbPicLink;
	}

	public void setFbPicLink(String fbPicLink) {
		this.fbPicLink = fbPicLink;
	}

	public String getBokwasName() {
		return bokwasName;
	}

	public void setBokwasName(String bokwasName) {
		this.bokwasName = bokwasName;
	}

	public String getBokwasAvatarId() {
		return bokwasAvatarId;
	}

	public void setBokwasAvatarId(String bokwasAvatarId) {
		this.bokwasAvatarId = bokwasAvatarId;
	}

	public Friends(String fbName, String id, String fbPicLink, String bokwasName, String bokwasAvatarId) {
		super();
		this.fbName = fbName;
		this.id = id;
		this.fbPicLink = fbPicLink;
		this.bokwasName = bokwasName;
		this.bokwasAvatarId = bokwasAvatarId;
	}

}
