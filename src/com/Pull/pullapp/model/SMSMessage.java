
package com.Pull.pullapp.model;

import android.provider.Telephony.TextBasedSmsColumns;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("SMSMessage")
public class SMSMessage extends ParseObject {
	private int smsId;
	private long smsDate;
	private String smsSender, smsMessage, smsRecipient;
    public boolean sentByMe, box, isDelayed;
    private boolean isHashtag;
	public long futureSendTime;
	public long launchedOn;
	private int hashtagID = -1;
 
    // Constructors
    public SMSMessage() {
    }

    public SMSMessage(String message, boolean sentByMe) {
        this.smsMessage = message;
        this.sentByMe = sentByMe;
        put("smsMessage",smsMessage);
    }
    
    public SMSMessage(String message, String sender, boolean sentByMe) {
        this.smsMessage = message;
        this.smsSender = sender;
        this.sentByMe = sentByMe;
        put("smsMessage",smsMessage);
        put("smsSender",smsSender);
    }    
    public SMSMessage(String message, String sender, int type) {
        this.smsMessage = message;
        this.smsSender = sender;
        this.sentByMe = (type != TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
        this.isDelayed = (type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX);
        put("smsMessage",smsMessage);
        put("smsSender",smsSender);        
    }  
    
    public SMSMessage(int id, long date, String sender, String recipient, String message) {
        this.smsId = id;
        this.smsDate = date;
        this.smsSender = sender;
        this.smsRecipient = recipient;
        this.smsMessage = message;
        put("smsMessage",smsMessage);
        put("smsSender",smsSender);    
        put("smsDate",smsDate);
        put("smsRecipient",smsRecipient);            
    }

    public SMSMessage(long date, String message, String sender, int type) {
    	this.smsDate = date;
        this.smsSender = sender;
        this.smsMessage = message;
        this.sentByMe = (type != TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
        this.isDelayed = (type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX);
        put("smsMessage",smsMessage);
        put("smsSender",smsSender);    
        put("smsDate",smsDate);        
    }

    // Id
    public int getId() {
        return this.smsId;
    }

    public void setId(int id) {
        this.smsId = id;
    }

    // Date
    public long getDate() {
    	return this.smsDate;
    }

    public void setDate(long date) {
    	this.smsDate = date;
    	put("smsDate",smsDate);        
	}

    // Sender
    public String getSender() {
        return this.smsSender;
    }
     
    public void setSender(String sender){
        this.smsSender = sender;
        put("smsSender",smsSender);        
    }
    // Sender
    public String getRecipient() {
        return this.smsRecipient;
    }
     
    public void setRecipient(String recipient){
        this.smsRecipient = recipient;
        put("smsRecipient",smsRecipient); 
    }
    // Message text
    public String getMessage(){
        return this.smsMessage;
    }

    public void setMessage(String message){
        this.smsMessage = message;
        put("smsMessage",smsMessage); 
    }
    
	public int getHashtagID() {
		return this.hashtagID;
	}

	public void setHashtagID(int indexOf) {
		this.hashtagID = indexOf;
		isHashtag=true;
		put("hashtagID",hashtagID); 
		
	}

	public boolean isHashtag() {
		return isHashtag;
	}
}
