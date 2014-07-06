package com.Pull.pullapp.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("ShareEvent")
public class ShareEvent extends ParseObject {
	
	
    public ShareEvent() {
    }
    
    public ShareEvent(String to, long time, SMSMessage smsMessage) {
    	put("sharedWith",to);
    	put("dateShared",time);
    	put("smsMessage",smsMessage);
    }
    
}
