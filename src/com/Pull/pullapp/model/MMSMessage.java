package com.Pull.pullapp.model;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.net.Uri;

import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.UserInfoStore;
import com.parse.ParseClassName;

@ParseClassName("MMSMessage")
public class MMSMessage extends SMSMessage {
	private ArrayList<Uri> images = new ArrayList<Uri>();
	private String[] recipients;
	private String[] names;
	/*public void addImage(Bitmap bitmap) {
		images.add(bitmap);
	}*/
	
    // Constructors
    public MMSMessage() {
    }	
    public MMSMessage(long date, String message, String address, String person,
    		int type, UserInfoStore store, String owner) {
    	super(date,message,address,person,type,store,owner);
    }

	public MMSMessage(long date, String message, String[] recipients,
			String[] names, int type, UserInfoStore store,
			String owner) {
		super(date,message,recipients[0],names[0],type,store,owner);
		this.recipients = recipients;
		this.names = names;
	}

	public ArrayList<Uri> getImages() {
		// TODO Auto-generated method stub
		return images;
	}

	public void addImage(Uri uri) {

		images.add(uri);
	}    	

}
