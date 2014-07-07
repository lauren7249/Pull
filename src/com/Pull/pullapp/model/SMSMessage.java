
package com.Pull.pullapp.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Telephony.TextBasedSmsColumns;
import android.util.Log;

import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.UserInfoStore;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("SMSMessage")
public class SMSMessage extends ParseObject implements Comparable<SMSMessage> {
	
	public long launchedOn;
	private String composedBy;
    private boolean sentByMe;
    public boolean box, isDelayed;
	private boolean event;
	private UserInfoStore store;
	
    // Constructors
    public SMSMessage() {
    }

    
    public SMSMessage(long date, String message, String address, int type) { 
        put("smsMessage",message);
        put("address",ContentUtils.addCountryCode(address));    
        put("smsDate",date);      
        put("sentByMe",(type != TextBasedSmsColumns.MESSAGE_TYPE_INBOX));
        put("isDelayed",(type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX));
    }
    
    public SMSMessage(long date, String message, String address, int type, UserInfoStore store) {
        put("smsMessage",message);
        put("address",ContentUtils.addCountryCode(address));    
        put("smsDate",date);      
        put("sentByMe",(type != TextBasedSmsColumns.MESSAGE_TYPE_INBOX));
        put("isDelayed",(type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX));
    	this.store = store;
    }    
    public void setType(int type) {
    	if(type == TextBasedSmsColumns.MESSAGE_TYPE_SENT || type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX )
    			sentByMe = true;
    	else sentByMe = false;
    	if(type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX ) isDelayed = true;
    	put("sentByMe",sentByMe);
    	put("isDelayed",isDelayed);

    }
    // Date
    public long getDate() {
    	return getLong("smsDate");
    }

    public void setDate(long date) {
    	put("smsDate",date);        
	}

    // Sender
    public String getSender() {
    	return getString("smsSender");
    }
     
     
    // Message text
    public String getMessage(){
    	return getString("smsMessage");
    }

    public void setMessage(String message){
        put("smsMessage",message); 
    }
    
	public boolean isSentByMe() {
		return getBoolean("sentByMe");
	}
	public void setSentByMe(boolean s) {
		put("sentByMe",s);
	}
	
    @Override
    public int hashCode() {
        return (Long.toBinaryString(getDate())+getMessage()+isSentByMe()+getAddress()).hashCode();
    }	
    
    
    public String getAddress() {
    	return getString("address");
	}


	@Override
    public boolean equals(Object obj) {
    	SMSMessage m=(SMSMessage)obj;
    	if(m == null) return false;
    	return (this.hashCode()==m.hashCode());
    }

	public void setEvent(boolean b) {
		this.event = b;
		
	}

	public boolean isEvent() {
		// TODO Auto-generated method stub
		return this.event;
	}

	
	public void addConfidante(String to) {
		String number = ContentUtils.addCountryCode(to);	
		store.addSharedWith(number, this);
		ShareEvent e = new ShareEvent(number,System.currentTimeMillis(), this);
		e.saveInBackground();	
	}
	
	public void schedule(long date) {
		put("isDelayed", true);
		put("futureSendTime", date);
	}
	
	public long getFutureSendTime() {
		return getLong("futureSendTime");
	}
	
	public void saveToParse() {
		put("user", ParseUser.getCurrentUser());
		put("hashCode", this.hashCode());
		put("username", ParseUser.getCurrentUser().get("username"));
		this.saveInBackground();
	}


	public Set<String> getConfidantes() {
		return store.getSharedWith(this);
	}


	@Override
	public int compareTo(SMSMessage another) {
		return ((Long) getDate()).compareTo((Long)another.getDate());
	}


}
