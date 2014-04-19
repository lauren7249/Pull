package com.Pull.pullapp.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Comment")
public class Comment extends ParseObject {

	private long date;
	private String message;
	private String sender;
	
	public Comment(){
		
	}
	public Comment(String body, String sender, long date) {
		this.message = body;
		this.sender = sender;
		this.date = date;
		put("message",message);
		put("date",date);
		put("sender",sender);
	}

	public long getDate() {
		return this.date;
	}

	public String getMessage() {
		return this.message;
	}

	public String getSender() {
		return this.sender;
	}
}
