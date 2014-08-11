package com.Pull.pullapp.model;

import java.util.Date;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("ShareSuggestion")
public class ShareSuggestion extends ParseObject {
	
	public ShareSuggestion(){
		 
	}
	public ShareSuggestion(ParseUser user,String best_friend_number,String best_friend_name,  
			String shared_from_number, String shared_from_name){
		put("user",user);
		put("best_friend_number",best_friend_number);
		put("shared_from_number",shared_from_number);
		put("best_friend_name",best_friend_name);
		put("share_from_name",shared_from_name);	
	}
	
	public void setClicked(long currentTimeMillis) {
		put("clickedAt", new Date(currentTimeMillis));
		
	}

}
