package com.bokwas.response;

import java.util.List;

public class GetPostsResponse {

	private List<Post> posts;
	private ApiResponse status;

	public List<Post> getPosts() {
		return posts;
	}

	public ApiResponse getStatus() {
		return status;
	}

}
