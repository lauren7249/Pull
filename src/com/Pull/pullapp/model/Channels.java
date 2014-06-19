package com.Pull.pullapp.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Channels")
public class Channels extends ParseObject {

	public Channels(){
		
	}
	public Channels(String c) {
		put("channel",c);
	}


}
