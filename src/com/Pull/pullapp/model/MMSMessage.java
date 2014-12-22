package com.Pull.pullapp.model;

import java.util.ArrayList;
import java.util.Arrays;

import android.graphics.Bitmap;
import android.net.Uri;

import com.Pull.pullapp.util.data.ContentUtils;
import com.Pull.pullapp.util.data.UserInfoStore;
import com.parse.ParseClassName;

@ParseClassName("MMSMessage")
public class MMSMessage extends SMSMessage {
	private ArrayList<Uri> images = new ArrayList<Uri>();
	private String[] recipients;
	private String[] names;

	private ArrayList<String> image_paths = new ArrayList<String>();
	
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
		put("recipients",new ArrayList<String>(Arrays.asList(recipients)));
		put("names",new ArrayList<String>(Arrays.asList(names)));
	}

	public ArrayList<Uri> getImages() {
		// TODO Auto-generated method stub
		return images;
	}

	public void addImage(Uri uri) {
		images.add(uri);
		image_paths.add(uri.toString());
		put("n_images",images.size());
	}
	
	public void setImageNumber(int n) {
		put("n_images",n);
	}	
	public void setAttachments(ArrayList<Uri> intent_attachments) {
		images = intent_attachments;
		image_paths = getPaths(intent_attachments);
		put("n_images",images.size());
		
	}
	public static ArrayList<String> getPaths(ArrayList<Uri> uris) {
    	if(uris == null) return null;
		ArrayList<String> attachment_paths = new ArrayList<String>();
		for(Uri u : uris) {
			attachment_paths.add(u.toString());
		}
		// TODO Auto-generated method stub
		return attachment_paths ;
	}
	public String[] getRecipients() {
		// TODO Auto-generated method stub
		return recipients;
	}
	public ArrayList<String> getImagePaths() {
		// TODO Auto-generated method stub
		return image_paths;
	}    	

}
