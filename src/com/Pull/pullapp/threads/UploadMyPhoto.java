package com.Pull.pullapp.threads;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.data.ContentUtils;
import com.Pull.pullapp.util.data.UserInfoStore;
import com.parse.ParseFile;
import com.parse.ParseUser;

public class UploadMyPhoto extends Thread {

    private Context mContext;
    private String facebookID;
    private UserInfoStore store;
	private ParseUser user;
    private Bitmap bitmap;
    public UploadMyPhoto(Context mContext, String facebookID, ParseUser user) {
    	this.mContext = mContext;
    	this.facebookID = facebookID;
    	this.user = user;
    	store = new UserInfoStore(mContext);
    	if(user == null) Log.i("users null", "user is null");
    }

    public UploadMyPhoto(Context mContext, Bitmap bitmap, ParseUser user) {
    	this.mContext = mContext;
    	this.bitmap = bitmap;
    	this.user = user;
    	store = new UserInfoStore(mContext);
    	if(user == null) Log.i("users null", "user is null");
    	
    }    
    @Override 
    public void run() {
    	
    	if(bitmap==null && facebookID !=null && facebookID.length()>0) {
    		Log.i("facebookid",facebookID);
	    	String facebookPhotoPath = ContentUtils.getFacebookPath(facebookID);
	    	bitmap = ContentUtils.getBitmapFromRedirectingURL(facebookPhotoPath);
    	}

    	if(bitmap!=null && bitmap.getByteCount()>0) {
	    	String localPath = ContentUtils.getInternalStoragePath(mContext, user.getUsername());
	    	File f = new File(localPath);
	    	if(f.exists()) f.delete();
	    	f = new File(localPath);
	    	ContentUtils.saveToInternalStorage(mContext, bitmap, user.getUsername());
	    	if(!f.exists() || localPath == null) return;
	    	
	    	Log.i("bitmap exists",localPath + " exists: " + f.exists());
	    	store.savePhotoPath(user.getUsername(), localPath);
	    	
	    	if(facebookID != null && facebookID.length()>0) {
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(Constants.ACTION_FACEBOOK_PHOTO_OBTAINED);
				mContext.sendBroadcast(broadcastIntent);		
	    	}
	    	
	    	byte[] bytes = ContentUtils.getByteArray(bitmap);
	    	ParseFile parseFile = new ParseFile(bytes);
	    	user.put("profilePhoto", parseFile);
	    	user.saveInBackground();
    	}
    }
    



    
}
