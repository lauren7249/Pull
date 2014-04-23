package com.Pull.pullapp.model;

import java.util.ArrayList;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("SharedConversation")
public class SharedConversation extends ParseObject {
	
	private String id, confidante, original_recipient;
	private int type;
	private long date_shared;
	private ArrayList<SMSMessage> conversation = new ArrayList<SMSMessage>();
	private ArrayList<Comment> comments = new ArrayList<Comment>();
	private ParseUser sharer;
	
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
		//return date_shared;
		return getLong("date_shared");
	}
	
	public String getConfidante() {
		//return confidante;
		return getString("confidante");
	}

	public String getOriginalRecipient() {
		//return original_recipient;
		return getString("original_recipient");
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
		//return this.id;
		return getString("id");
	}
	
	public void addMessage(SMSMessage sms) {
		conversation.add(sms);
        sms.put("parent", this);		
	}
	public void addComment(Comment comment) {
		comments.add(comment);
		comment.put("parent", this);
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
		for(Comment c : comments) {
			c.put("parent", this);
		}
		
	}

	public void setMessages(ArrayList<SMSMessage> messages) {
		this.conversation = messages;
		for(SMSMessage c : messages) {
			c.put("parent", this);
		}		
	}

	public int getType() {
		//return type;
		return getInt("type");
	}
	
	public void setType(int messageType) {
		this.type = messageType;
		put("type",type);
	}

	public ParseUser getSharer() {
		return (ParseUser) this.getParseObject("user");
	}

}
