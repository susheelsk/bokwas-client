package com.bokwas.datasets;

public class Message {
	
	private String fromId;
	private String toId;
	private long timestamp;
	private String message;
	private String messageId;
	private boolean isSeen;
	
	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
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

	public Message(String fromId, String toId, long timestamp, String message,String messageId,boolean isSeen) {
		super();
		this.fromId = fromId;
		this.toId = toId;
		this.timestamp = timestamp;
		this.message = message;
		this.messageId = messageId;
		this.isSeen = isSeen;
	}

}
