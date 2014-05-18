package com.bokwas.util;

public class AppData {

	private static AppData instance = null;
	private boolean isDevMode = true;
	
	public static String baseURL = "http://162.243.118.127:8080"; 
//	public static String baseURL = "http://10.0.0.8:8080/BokwasServer";

	public boolean isDevMode() {
		return isDevMode;
	}

	public void setDevMode(boolean isDevMode) {
		this.isDevMode = isDevMode;
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
