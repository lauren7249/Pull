package com.Pull.pullapp.model;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("InitiatingData")
public class InitiatingData extends ParseObject {
	
	
    private float hours_elapsed;

	public InitiatingData() {
    }

	public InitiatingData(float hours_elapsed, int retexting, int after_question, int previous_length, 
			int previous_words, int previous_hashCode, int hashCode) {
		this.hours_elapsed = hours_elapsed;
		put("hours_elapsed", hours_elapsed);
		put("retexting", (retexting==1));
		put("after_question", (after_question==1));
		put("previous_length",previous_length);
		put("previous_words",previous_words);
		put("previous_hashCode",previous_hashCode);
		put("hashCode",hashCode);
		put("user",ParseUser.getCurrentUser());
	}

	public boolean isInitiating() {
 		if(getBoolean("retexting") && hours_elapsed > 0.167) return true;
 		if(getBoolean("retexting")==false) {
 			if (hours_elapsed > 24) return true;
 			if (hours_elapsed > 8 && !getBoolean("after_question")) return true;
 		}
 		return false;
	}
    
    
}