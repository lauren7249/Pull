package com.Pull.pullapp.model;

import java.util.ArrayList;

public class SharedConversation {
	
	private int id;
	private String confidante, original_recipient, date_shared;
	private ArrayList<SMSMessage> conversation = new ArrayList<SMSMessage>();
	private ArrayList<Comment> comments = new ArrayList<Comment>();
	public String original_hashtags;
	
	public SharedConversation(int id, String date, String confidante,
			String original_recipient) {
		this.id = id;
		this.date_shared = date;
		this.confidante = confidante;
		this.original_recipient = original_recipient;
	}

	public SharedConversation() {
	}

	public SharedConversation(String date, String shared_with,
			String shared_from) {
		this.date_shared = date;
		this.confidante = shared_with;
		this.original_recipient = shared_from;
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

	public int getId() {
		return this.id;
	}
	
	public void addMessage(SMSMessage sms) {
		conversation.add(sms);
	}
	public void addComment(Comment comment) {
		comments.add(comment);
	}	
	
	public ArrayList<SMSMessage> getMessages(){
		return conversation;
	}

	public ArrayList<Comment> getComments() {
		return this.comments;
	}

	public String getHashtags() {
		String hashtags = "";
		for(Comment c : comments) {
			if(c.getHashtagID()!=-1) hashtags = hashtags + " " + c.getMessage();
		}
		return hashtags;
	}

	public void setComments(ArrayList<Comment> comments) {
		this.comments = comments;
		
	}

	public void setMessages(ArrayList<SMSMessage> messages) {
		this.conversation = messages;
	}
}
