package com.bokwas.response;

import java.util.ArrayList;

public class GetPrivateMessagesResponse {
	
	public ArrayList<Message> messages = new ArrayList<Message>();
	public ApiResponse status;

	public class Message {
		public String messageId;
		public String messageText;
		public String messageFromId;
		public String messageTime;
	}
	
}
