package com.Pull.pullapp.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("InitiatingData")
public class Conversation extends ParseObject {
	
	
    public Conversation() {
    }

	public Conversation(float hours_elapsed, int retexting, int after_question) {
		put("hours_elapsed", hours_elapsed);
		put("retexting", (retexting==1));
		put("after_question", (after_question==1));
	}

	public boolean isInitiating() {
 		if(getBoolean("retexting") && getLong("hours_elapsed") > 0.167) return true;
 		if(getBoolean("retexting")==false) {
 			if (getLong("hours_elapsed") > 24) return true;
 			if (getLong("hours_elapsed") > 8 && !getBoolean("after_question")) return true;
 		}
 		return false;
	}
    
    
}