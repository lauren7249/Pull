package com.Pull.pullapp.model;

public class Comment {

	private long date;
	private String message;
	private String sender;
	

	public Comment(String body, String address, long date) {
		this.message = body;
		this.sender = address;
		this.date = date;
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
