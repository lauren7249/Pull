
package com.Pull.pullapp.model;

import android.provider.Telephony.TextBasedSmsColumns;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("SMSMessage")
public class SMSMessage extends ParseObject {
	private int smsId;
	private long smsDate;
	private String smsSender, smsMessage, smsRecipient;
    private boolean sentByMe;
    public boolean box, isDelayed;
    private boolean isHashtag;
	public long futureSendTime;
	public long launchedOn;
	private Hashtag ht;
	
    // Constructors
    public SMSMessage() {
    }

    public SMSMessage(String message, boolean sentByMe) {
        this.smsMessage = message;
        this.sentByMe = sentByMe;
        put("smsMessage",smsMessage);
        put("sentByMe",sentByMe);
        put("isHashtag",isHashtag);
    }
    
    public SMSMessage(String message, String sender, boolean sentByMe) {
        this.smsMessage = message;
        this.smsSender = sender;
        this.sentByMe = sentByMe;
        put("sentByMe",sentByMe);
        put("smsMessage",smsMessage);
        put("smsSender",smsSender);
        put("isHashtag",isHashtag);
    }    
    public SMSMessage(String message, String sender, int type) {
        this.smsMessage = message;
        this.smsSender = sender;
        this.sentByMe = (type != TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
        this.isDelayed = (type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX);
        put("smsMessage",smsMessage);
        put("smsSender",smsSender); 
        put("sentByMe",sentByMe);
        put("isDelayed",isDelayed);
        put("isHashtag",isHashtag);
    }  
    
    public SMSMessage(int id, long date, String sender, String recipient, String message) {
        this.smsId = id;
        this.smsDate = date;
        this.smsSender = sender;
        this.smsRecipient = recipient;
        this.smsMessage = message;
        put("smsId",smsId);
        put("smsMessage",smsMessage);
        put("smsSender",smsSender);    
        put("smsDate",smsDate);
        put("smsRecipient",smsRecipient);            
        put("isHashtag",isHashtag);
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
        put("sentByMe",sentByMe);
        put("isDelayed",isDelayed);
        put("isHashtag",isHashtag);
    }

    // Id
    public int getId() {
        //return this.smsId;
    	return getInt("smsId");
    }

    public void setId(int id) {
        this.smsId = id;
        put("smsId",smsId);
    }

    // Date
    public long getDate() {
    	//return this.smsDate;
    	return getLong("smsDate");
    }

    public void setDate(long date) {
    	this.smsDate = date;
    	put("smsDate",smsDate);        
	}

    // Sender
    public String getSender() {
        //return this.smsSender;
    	return getString("smsSender");
    }
     
    public void setSender(String sender){
        this.smsSender = sender;
        put("smsSender",smsSender);        
    }
    // Sender
    public String getRecipient() {
        //return this.smsRecipient;
    	return getString("smsRecipient");
    }
     
    public void setRecipient(String recipient){
        this.smsRecipient = recipient;
        put("smsRecipient",smsRecipient); 
    }
    // Message text
    public String getMessage(){
        //return this.smsMessage;
    	return getString("smsMessage");
    }

    public void setMessage(String message){
        this.smsMessage = message;
        put("smsMessage",smsMessage); 
    }
    
	public void setHashtag() {
		put("isHashtag",true); 
	}

	public boolean isHashtag() {
		return getBoolean("isHashtag");
	}
	public boolean isSentByMe() {
		return getBoolean("sentByMe");
	}
	public void setSentByMe(boolean s) {
		this.sentByMe = s;
		put("sentByMe",sentByMe);
	}
	
    @Override
    public int hashCode() {
        return (smsSender+smsRecipient+smsMessage+Long.toString(smsDate)).hashCode();
    }	
    
    @Override
    public boolean equals(Object obj) {
    	SMSMessage m=(SMSMessage)obj;
    	if(m == null) return false;
    	if((m.smsSender == null) != (this.smsSender == null)) return false;
    	if((m.smsRecipient == null) != (this.smsRecipient == null)) return false;
    	if((m.smsMessage == null) != (this.smsMessage == null)) return false;
    	if(m.smsSender!=null && this.smsSender!=null) {
    		if(!m.smsSender.equals(this.smsSender)) return false;
    	}
    	if(m.smsRecipient!=null && this.smsRecipient!=null) {
    		if(!m.smsRecipient.equals(this.smsRecipient)) return false;
    	}
    	if(m.smsMessage!=null && this.smsMessage!=null) {
    		if(!m.smsMessage.equals(this.smsMessage)) return false;
    	}
    	if(m.smsDate != this.smsDate) return false;
		return true;
    }    
}
