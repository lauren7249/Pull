package com.Pull.pullapp.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("InitiatingData")
public class InitiatingData extends ParseObject {
	
	
    public InitiatingData() {
    }

	public InitiatingData(long hours_elapsed, int retexting, int after_question) {
		put("hours_elapsed", hours_elapsed);
		put("retexting", (retexting==1));
		put("after_question", (after_question==1));
	}
    
    
}