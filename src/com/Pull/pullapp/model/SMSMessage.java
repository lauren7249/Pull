
package com.Pull.pullapp.model;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONException;

import android.provider.Telephony.TextBasedSmsColumns;
import android.util.Log;

import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.UserInfoStore;
import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

@ParseClassName("SMSMessage")
public class SMSMessage extends ParseObject implements Comparable<SMSMessage> {
	
	public long launchedOn;
	private String composedBy;
    private boolean sentByMe;
    public boolean box, isDelayed;
	private boolean event;
	private UserInfoStore store;
	private ParseACL acl = new ParseACL();
	
    // Constructors
    public SMSMessage() {
    }
    
    /**
     * 
     * @param smsDate - date sent
     * @param smsMessage - body
     * @param address - the other persons phone number
     * 		  person - the other persons name
     * @param type - integer indicating whether it is an outgoing/incoming message/comment/etc
     * 		  sentbyme - contextual, used for rendering
     * 		  isdelayed - whether the message has actually been sent or is in queue
     * confidantes - people who have access to this record
     * username - person who created the record
     */
    
    public SMSMessage(long date, String message, String address, int type) { 
        put("smsMessage",message);
        put("address",ContentUtils.addCountryCode(address));    
        put("smsDate",date);      
        put("type",type);
        put("sentByMe",(
        		type == TextBasedSmsColumns.MESSAGE_TYPE_SENT) || 
        		type == Constants.MESSAGE_TYPE_SENT_COMMENT ||
        		type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX );
        put("isDelayed",(type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX));
    }
    
    public SMSMessage(long date, String message, String address, int type, UserInfoStore store) {
        put("smsMessage",message);
        put("address",ContentUtils.addCountryCode(address));    
        put("smsDate",date);      
        put("type",type);
        put("sentByMe",(
        		type == TextBasedSmsColumns.MESSAGE_TYPE_SENT) || 
        		type == Constants.MESSAGE_TYPE_SENT_COMMENT ||
        		type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX );
        put("isDelayed",(type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX));
        put("person", store.getName(address));
    	this.store = store;
    }    
    public void setType(int type) {
    	if(type == TextBasedSmsColumns.MESSAGE_TYPE_SENT 
    			|| type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX
    			|| type == Constants.MESSAGE_TYPE_SENT_COMMENT  )
    			sentByMe = true;
    	else sentByMe = false;
    	if(type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX ) isDelayed = true;
    	put("type",type);
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
		//String userid = store.getUserID(number);
		//also gotta do this on the backend if we don't know the userid because they are not friends
		//if(userid!=null && !userid.isEmpty()) acl.setReadAccess(userid, true);
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
	
	public void saveToParse() throws JSONException {
		put("user", ParseUser.getCurrentUser());
		put("hashCode", this.hashCode());
		put("username", ParseUser.getCurrentUser().getUsername());
		put("confidantes",new ArrayList<String>(getConfidantes()));
		this.setACL(acl);
		this.saveInBackground(new SaveCallback() {
			   public void done(ParseException e) {
				     if (e == null) {
				     } else {
				       Log.i("error", e.getMessage());
				     }
			   }
		});
	}


	public Set<String> getConfidantes() {
		return store.getSharedWith(this);
	}


	@Override
	public int compareTo(SMSMessage another) {
		return ((Long) getDate()).compareTo((Long)another.getDate());
	}


	public int getType() {
		return getInt("type");
	}

	public String getSender() {
		// TODO Auto-generated method stub
		return getString("username");
	}


}
