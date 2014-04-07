package com.Pull.pullapp.model;

public class Comment {

	private String date;
	private String message;
	private String sender;
	private int hashtagID;

	public Comment(String body, String address, String date, int i) {
		this.message = body;
		this.sender = address;
		this.date = date;
		this.hashtagID = i;
	}

	public String getDate() {
		return this.date;
	}

	public String getMessage() {
		return this.message;
	}

	public String getSender() {
		return this.sender;
	}

	public int getHashtagID() {
		return this.hashtagID;
	}

}
