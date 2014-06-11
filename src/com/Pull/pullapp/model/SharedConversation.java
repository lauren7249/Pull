package com.Pull.pullapp.model;

import java.util.ArrayList;

import org.json.JSONArray;

import com.Pull.pullapp.util.ContentUtils;
import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("SharedConversation")
public class SharedConversation extends ParseObject {
	
	private String confidante, original_recipient, sharer;
	private int type;
	private long date_shared;
	private ArrayList<SMSMessage> conversation = new ArrayList<SMSMessage>();
	private ArrayList<String> sms_ids = new ArrayList<String>();
	private ArrayList<Comment> comments = new ArrayList<Comment>();
	private String originalRecipientName;
	
	public SharedConversation() {
	}
	
	public SharedConversation(String id, long date, String confidante,
			String original_recipient) {
		this.date_shared = date;
		this.confidante = ContentUtils.addCountryCode(confidante);
		this.original_recipient = ContentUtils.addCountryCode(original_recipient);

		put("date_shared", date_shared);
		put("confidante",confidante);
		put("original_recipient",ContentUtils.addCountryCode(original_recipient));
	}

	public SharedConversation(long date, String shared_with,
			String shared_from) {
		this.date_shared = date;
		this.confidante = ContentUtils.addCountryCode(shared_with);
		this.original_recipient = ContentUtils.addCountryCode(shared_from);
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

	public void setDate(long date) {
		this.date_shared= date;
		put("date_shared",date_shared);
	}

	public void setConfidante(String string) {
		this.confidante = ContentUtils.addCountryCode(string);
		put("confidante",confidante);
	}

	public void setOriginalRecipient(String string) {
		this.original_recipient = ContentUtils.addCountryCode(string);
		put("original_recipient",original_recipient);
	}

	
	public void addMessage(SMSMessage sms) {
		sms.put("parent", this);
		conversation.add(sms);
        		
	}
	public void addComment(Comment comment) {
		comment.put("parent", this);
		comments.add(comment);
		
	}	
	
	public ArrayList<String> getMessageIDs(){
		for(SMSMessage m : conversation) {
			sms_ids.add(m.getObjectId());
		}
		return sms_ids;
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
			if(c.isHashtag()) hashtags = hashtags + " " + c.getMessage();
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
	public void setSharer(String sharer) {
		this.sharer = ContentUtils.addCountryCode(sharer);
		put("sharer",sharer);		
	}

	public String getSharer() {
		return getString("sharer");
	}

	public String getOriginalRecipientName() {
		// TODO Auto-generated method stub
		return getString("originalRecipientName");
	}
	public void setOriginalRecipientName(String originalRecipientName) {
		this.originalRecipientName = originalRecipientName;
		put("originalRecipientName",originalRecipientName);		
	}

	

}
