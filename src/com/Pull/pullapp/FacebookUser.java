package com.Pull.pullapp;

import android.content.Intent;
import android.util.Log;

import com.Pull.pullapp.util.Constants;
import com.facebook.model.GraphUser;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

@ParseClassName("FacebookUser")
public class FacebookUser extends ParseObject {
	
	private String facebookID, name, location, gender, birthday, relationship_status, installationID;
	public FacebookUser(){
		
	}
	
	public FacebookUser(GraphUser user, String phoneNumber) {
		this.facebookID = user.getId();
		this.name = user.getName();
		this.installationID = ParseInstallation.getCurrentInstallation().getObjectId();
		put("phoneNumber", phoneNumber);
		put("facebookID",facebookID);
		put("installationID",installationID);
		put("name",name);		
		if(user.getLocation() != null) { 
			if (user.getLocation().getProperty("name") != null) {
				this.location = (String) user
						.getLocation().getProperty("name");
				put("location",location);
				Log.i("location",location);
			}
		}
		if (user.getProperty("gender") != null) {
			this.gender = 
					(String) user.getProperty("gender");
			put("gender",gender);
		}
		if (user.getBirthday() != null) {
			this.birthday = user.getBirthday();
			put("birthday",birthday);
		} 
		if (user.getProperty("relationship_status") != null) {
			this.relationship_status = 
							(String) user
									.getProperty("relationship_status");
			put("relationship_status",relationship_status);
		}
		
	}
	
}
