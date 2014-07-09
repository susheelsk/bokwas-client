package com.bokwas.response;

import com.google.gson.annotations.SerializedName;

public class BokwasUser {
	@SerializedName("fbid")
	public String userId;
	@SerializedName("bokwas_name")
	public String userBokwasName;
	@SerializedName("avatar_id")
	public String userBokwasAvatarId;
	@SerializedName("fbname")
	public String fbName;
}
