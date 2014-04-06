package com.Pull.pullapp.model;

import java.util.ArrayList;

public class SharedConversation {
	
	private int id;
	private String confidante, original_recipient, date_shared, hashtags;
	private ArrayList<SMSMessage> conversation = new ArrayList<SMSMessage>();
	
	
	public SharedConversation(int id, String date, String confidante,
			String original_recipient, String hashtags) {
		this.id = id;
		this.date_shared = date;
		this.confidante = confidante;
		this.original_recipient = original_recipient;
		this.hashtags = hashtags;
	}

	public SharedConversation() {
	}

	public SharedConversation(String date, String shared_with,
			String shared_from, String hashtags) {
		this.date_shared = date;
		this.confidante = shared_with;
		this.original_recipient = shared_from;
		this.hashtags = hashtags;
	}

	public String getDate() {
		return date_shared;
	}

	public String getConfidante() {
		return confidante;
	}

	public String getOriginalRecipient() {
		return original_recipient;
	}

	public String getHashtags() {
		return hashtags;
	}

	public void setId(int parseInt) {
		this.id = parseInt;
	}

	public void setDate(String string) {
		this.date_shared= string;
		
	}

	public void setConfidante(String string) {
		this.confidante = string;
		
	}

	public void setOriginalRecipient(String string) {
		this.original_recipient = string;
		
	}

	public void setHashtags(String string) {
		this.hashtags = string;
		
	}

	public int getId() {
		return this.id;
	}
}
