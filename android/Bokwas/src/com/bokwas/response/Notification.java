package com.bokwas.response;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.annotations.Expose;

public class Notification {
	@Expose
	private String notification_id;
	@Expose
	private String notification_data;
	private long timestamp;
	private boolean isViewed = false;
	
	public void setViewed(boolean isViewed) {
		this.isViewed = isViewed;
	}
	
	public boolean isViewed() {
		return isViewed;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getNotification_id() {
		return notification_id;
	}

	public void setNotification_id(String notification_id) {
		this.notification_id = notification_id;
	}

	public Map<String, String> getNotification_data() {
		Map<String,String> map = new HashMap<String, String>();
		try {
			JSONObject jsonObject = new JSONObject(notification_data);
			Iterator<?> keys = jsonObject.keys();
			while( keys.hasNext() ){
			    String key = (String)keys.next();
			    if( jsonObject.get(key) instanceof JSONObject ){
			    	map.put(key, jsonObject.get(key).toString());
			    }else {
			    	map.put(key, jsonObject.get(key).toString());
			    }
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return map;
	}

	public Notification(String notification_id, String notification_data,long timestamp) {
		super();
		this.notification_id = notification_id;
		this.notification_data = notification_data;
		this.timestamp = timestamp;
	}
}
