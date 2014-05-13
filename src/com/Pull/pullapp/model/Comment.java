package com.Pull.pullapp.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Comment")
public class Comment extends ParseObject {

	private long date;
	private String message;
	private String sender;
	private boolean isProposal = false;
	public boolean failedToDeliver;

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
		//return this.date;
		return getLong("date");
	}

	public String getMessage() {
		//return this.message;
		return getString("message");
	}

	public String getSender() {
		//return this.sender;
		return getString("sender");
	}
	
	public void setProposal(boolean p) {
		this.isProposal = p;
		put("isProposal",isProposal);
	}
	public boolean isProposal(){
		return getBoolean("isProposal");
	}	
}
