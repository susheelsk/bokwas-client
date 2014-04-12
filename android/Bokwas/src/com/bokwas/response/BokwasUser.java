package com.bokwas.response;

import com.google.gson.annotations.SerializedName;

public class BokwasUser {
	@SerializedName("user_id")
	public String userId;
	@SerializedName("user_bokwas_name")
	public String userBokwasName;
	@SerializedName("user_bokwas_avatar_id")
	public String userBokwasAvatarId;
}
