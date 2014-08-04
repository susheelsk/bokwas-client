package com.bokwas.datasets;

public class Message {
	
	private String fromId;
	private String toId;
	private long timestamp;
	private String message;
	private boolean isSeen;
	
	public boolean isSeen() {
		return isSeen;
	}

	public void setSeen(boolean isSeen) {
		this.isSeen = isSeen;
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Message(String fromId, String toId, long timestamp, String message,boolean isSeen) {
		super();
		this.fromId = fromId;
		this.toId = toId;
		this.timestamp = timestamp;
		this.message = message;
		this.isSeen = isSeen;
	}

}
