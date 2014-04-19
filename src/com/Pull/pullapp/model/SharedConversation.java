package com.Pull.pullapp.model;

import java.util.ArrayList;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("SharedConversation")
public class SharedConversation extends ParseObject {
	
	private String id, confidante, original_recipient;
	private int type;
	private long date_shared;
	private ArrayList<SMSMessage> conversation = new ArrayList<SMSMessage>();
	private ArrayList<Comment> comments = new ArrayList<Comment>();
	
	public SharedConversation() {
	}
	
	public SharedConversation(String id, long date, String confidante,
			String original_recipient) {
		this.id = id;
		this.date_shared = date;
		this.confidante = confidante;
		this.original_recipient = original_recipient;
		
		put("id", id);
		put("date_shared", date_shared);
		put("confidante",confidante);
		put("original_recipient",original_recipient);
	}

	public SharedConversation(long date, String shared_with,
			String shared_from) {
		this.date_shared = date;
		this.confidante = shared_with;
		this.original_recipient = shared_from;
		put("date_shared", date_shared);
		put("confidante",confidante);
		put("original_recipient",original_recipient);	
	}

	public long getDate() {
		return date_shared;
	}
	
	public String getConfidante() {
		return confidante;
	}

	public String getOriginalRecipient() {
		return original_recipient;
	}


	public void setId(String convo_id) {
		this.id = convo_id;
		put("id",id);
	}

	public void setDate(long date) {
		this.date_shared= date;
		put("date_shared",date_shared);
	}

	public void setConfidante(String string) {
		this.confidante = string;
		put("confidante",confidante);
	}

	public void setOriginalRecipient(String string) {
		this.original_recipient = string;
		put("original_recipient",original_recipient);
	}

	public String getId() {
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
		for(SMSMessage c : conversation) {
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

	public int getType() {
		return type;
	}
	
	public void setType(int messageType) {
		this.type = messageType;
	}

}
