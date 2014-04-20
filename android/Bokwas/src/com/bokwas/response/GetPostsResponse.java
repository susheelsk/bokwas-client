package com.bokwas.response;

import java.util.List;

public class GetPostsResponse {

	private List<Post> posts;
	private List<BokwasUser> users;
	private ApiResponse status;
	private String access_key;

	public String getAccess_key() {
		return access_key;
	}

	public List<BokwasUser> getUsers() {
		return users;
	}
	
	public List<Post> getPosts() {
		return posts;
	}

	public ApiResponse getStatus() {
		return status;
	}

}
