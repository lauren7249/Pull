package com.Pull.pullapp.model;

import java.util.ArrayList;

import android.graphics.Bitmap;

import com.Pull.pullapp.util.UserInfoStore;
import com.parse.ParseClassName;

@ParseClassName("MMSMessage")
public class MMSMessage extends SMSMessage {
	private ArrayList<Bitmap> images = new ArrayList<Bitmap>();
	public void addImage(Bitmap bitmap) {
		images.add(bitmap);
	}
	
    // Constructors
    public MMSMessage() {
    }	
    public MMSMessage(long date, String message, String address, String person,
    		int type, UserInfoStore store, String owner) {
    	super(date,message,address,person,type,store,owner);
    }

	public ArrayList<Bitmap> getImages() {
		// TODO Auto-generated method stub
		return images;
	}    	

}
