package com.bokwas.util;

public class AppData {

	private static boolean isDevMode = false;
	public static int MAX_POSTS_LIMIT = 100;
	private static String devUrl = "http://162.243.118.127:8080";
	private static String prodUrl = "http://23.254.115.105:8080";
	
	private static AppData instance = null;
//	public static String devUrl = "http://10.0.0.3:8080/BokwasServer";

	public static String getBaseURL() {
		if(isDevMode) {
			return devUrl;
		}else {
			return prodUrl;
		}
	}

	public boolean isDevMode() {
		return isDevMode;
	}

	public void setDevMode(boolean isDevMode) {
		AppData.isDevMode = isDevMode;
	}
	
	private AppData() {
		
	}
	
	public static AppData getAppData() {
		if(instance == null) {
			return new AppData();
		}
		return instance;
	}
	
}
