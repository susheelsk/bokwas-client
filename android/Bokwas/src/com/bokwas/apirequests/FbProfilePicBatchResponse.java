package com.bokwas.apirequests;

import java.util.List;

public class FbProfilePicBatchResponse {
	public String code;
	List<Header> headers;
	public String body;

}

class Header {
	public String name;
	public String value;
}

class Body {
	public String id;
	public String name;
	public Picture picture;

}

class Picture {
	public Data data;
}

class Data {
	public boolean is_silhouette;
	public String url;

}
