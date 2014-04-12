package com.bokwas.response;

import java.util.List;

public class GetPostsResponse {

	private List<Post> posts;
	private List<BokwasUser> users;
	private ApiResponse status;

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
