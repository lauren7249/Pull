package com.Pull.pullapp.util;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.Pull.pullapp.MainApplication;
import com.Pull.pullapp.adapter.ThreadItemsCursorAdapter;
import com.Pull.pullapp.model.SMSMessage;

public class UserInfoStore {
	
	private Context mContext;
	private SharedPreferences mPrefs_recipientID_phoneNumber;
	private SharedPreferences mPrefs_phoneNumber_Name;
	private SharedPreferences phoneNumber_objectID;
	private SharedPreferences mPrefs_phoneNumber_FacebookID;
	private SharedPreferences mPrefs_sms_sharedWith;
	private SharedPreferences phoneNumber_photoPath;
	public UserInfoStore(Context context) {
		this.mContext = context;
    	mPrefs_recipientID_phoneNumber = mContext.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "recipientId_phoneNumber",Context.MODE_PRIVATE);
    	mPrefs_phoneNumber_Name = mContext.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "phoneNumber_Name",Context.MODE_PRIVATE);		
		phoneNumber_objectID = mContext.getSharedPreferences(MainApplication.class.getSimpleName() 
				+ "phoneNumber_objectID",Context.MODE_PRIVATE);    	
		phoneNumber_photoPath = mContext.getSharedPreferences(MainApplication.class.getSimpleName() 
				+ "phoneNumber_photoPath",Context.MODE_PRIVATE);  		
    	mPrefs_phoneNumber_FacebookID = context
    			.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "phoneNumber_FacebookID",Context.MODE_PRIVATE); 	
    	mPrefs_sms_sharedWith = context
    			.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "mPrefs_sms_sharedWith",Context.MODE_PRIVATE); 	   
	}
	public String getPhoneNumber(String recipientId) {
		return mPrefs_recipientID_phoneNumber.getString(recipientId, null);
	}
	public void setPhoneNumber(String recipientId, String number) {
		Editor editor = mPrefs_recipientID_phoneNumber.edit();
		editor.putString(recipientId, ContentUtils.addCountryCode(number));
		editor.commit();
		
	}
	public String getName(String number) {
		// TODO Auto-generated method stub
		return mPrefs_phoneNumber_Name.getString(ContentUtils.addCountryCode(number), 
				ContentUtils.addCountryCode(number));
	}
	public void setName(String number, String name) {
		Editor editor = mPrefs_phoneNumber_Name.edit();
		editor.putString(ContentUtils.addCountryCode(number), name);
		editor.putString(number, name);
		editor.commit();
		
	}
	public String getFacebookID(String number) {
		// TODO Auto-generated method stub
		return mPrefs_phoneNumber_FacebookID.getString(ContentUtils.addCountryCode(number), "");
	}
	public Set<String> getSharedWith(SMSMessage smsMessage) {
		// TODO Auto-generated method stub
		return mPrefs_sms_sharedWith
				.getStringSet(Integer.toString(smsMessage.hashCode()), new HashSet<String>());
	}
	public void addSharedWith(String number, SMSMessage smsMessage) {
		Set<String> sharedWith = getSharedWith(smsMessage);
		Editor edit = mPrefs_sms_sharedWith.edit();
		sharedWith.add(ContentUtils.addCountryCode(number));
		edit.putStringSet(Integer.toString(smsMessage.hashCode()), sharedWith);
		edit.commit();	
		
	}
	public void logInvite(String number ){
		Editor editor = phoneNumber_objectID.edit();
		editor.putString(ContentUtils.addCountryCode(number), "");	
		editor.commit();		
	}
	
	public String getUserID(String number) {
		return phoneNumber_objectID.getString(ContentUtils.addCountryCode(number), null);
	}
	public boolean wasInvited(String number) {
		return (getUserID(ContentUtils.addCountryCode(number))!=null);
	}
	public boolean isFriend(String number) {
		return (wasInvited(ContentUtils.addCountryCode(number)) 
				&& !getUserID(ContentUtils.addCountryCode(number)).isEmpty());
	}	
	
	public void saveFriend(String number, String userID) {
		Editor editor = phoneNumber_objectID.edit();
		editor.putString(ContentUtils.addCountryCode(number), userID);	
		editor.commit();				
	}
	public void savePhotoPath(String number, String photoPath) {
		Editor editor = phoneNumber_photoPath.edit();
		editor.putString(ContentUtils.addCountryCode(number), photoPath);	
		Log.i("phone number", ContentUtils.addCountryCode(number));
		Log.i("path of photo", photoPath);
		editor.commit();	
	}
	public Bitmap getFriendBitmap(String number) {
		
		String path = phoneNumber_photoPath.getString(ContentUtils.addCountryCode(number), null);
		if(path == null) {
		//	Log.i("path is null", "getfriendbitmap path is null");
			return null;
		}
		return ContentUtils.getBitmapFromPath(path);
	}
	public void saveFacebookID(String number, String id) {
		Editor editor = mPrefs_phoneNumber_FacebookID.edit();
		editor.putString(ContentUtils.addCountryCode(number), id);	
		editor.commit();	
		
	}
	public String getPhotoPath(String number) {
		// TODO Auto-generated method stub
		return phoneNumber_photoPath.getString(ContentUtils.addCountryCode(number), null);
	}
	
}
