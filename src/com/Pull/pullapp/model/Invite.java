package com.Pull.pullapp.model;

import com.Pull.pullapp.util.data.ContentUtils;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Invite")
public class Invite extends ParseObject{

	public Invite(){
		 
	}
	public Invite(ParseUser user,String sent_to){
		put("sender",user);
		put("invitee",ContentUtils.addCountryCode(sent_to));
	}
	
}
