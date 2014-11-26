package com.Pull.pullapp.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
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
	private SharedPreferences phoneNumber_threadID;
	private SharedPreferences approvals;
	private SharedPreferences mPrefs;
	private SharedPreferences mPrefs_phoneNumber_recipientID;
	public UserInfoStore(Context context) {
		this.mContext = context;
    	mPrefs = mContext.getSharedPreferences(MainApplication.class.getSimpleName(),Context.MODE_PRIVATE);		
    	mPrefs_recipientID_phoneNumber = mContext.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "recipientId_phoneNumber",Context.MODE_PRIVATE);
    	mPrefs_phoneNumber_recipientID = mContext.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "mPrefs_phoneNumber_recipientID",Context.MODE_PRIVATE);    	
    	mPrefs_phoneNumber_Name = mContext.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "phoneNumber_Name",Context.MODE_PRIVATE);		
		phoneNumber_objectID = mContext.getSharedPreferences(MainApplication.class.getSimpleName() 
				+ "phoneNumber_objectID",Context.MODE_PRIVATE);    	
		phoneNumber_photoPath = mContext.getSharedPreferences(MainApplication.class.getSimpleName() 
				+ "phoneNumber_photoPath",Context.MODE_PRIVATE);  		
    	mPrefs_phoneNumber_FacebookID = context
    			.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "phoneNumber_FacebookID",Context.MODE_PRIVATE); 	
    	phoneNumber_threadID = context
    			.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "phoneNumber_threadID",Context.MODE_PRIVATE); 	    	
    	mPrefs_sms_sharedWith = context
    			.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "mPrefs_sms_sharedWith",Context.MODE_PRIVATE); 
    	approvals = context
    			.getSharedPreferences(MainApplication.class.getSimpleName() 
    			+ "approvals",Context.MODE_PRIVATE); 	     	
	}
	public String getPhoneNumber(String recipientId) {
		return mPrefs_recipientID_phoneNumber.getString(recipientId, null);
	}
	public void setPhoneNumber(String recipientId, String number) {
		Editor editor = mPrefs_recipientID_phoneNumber.edit();
		editor.putString(recipientId, ContentUtils.addCountryCode(number));
		editor.commit();
		editor = mPrefs_phoneNumber_recipientID.edit();
		editor.putString(ContentUtils.addCountryCode(number), recipientId);
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
	public boolean wasApproved(String phoneNumber, String message,
			long scheduledFor, String approver) {
		return approvals.getBoolean(getApprovalString(phoneNumber,message,scheduledFor,approver),false);
	}
	public boolean wasDisapproved(String phoneNumber, String message,
			long scheduledFor, String approver) {
		return approvals.contains(getApprovalString(phoneNumber,message,scheduledFor,approver)) &&
				approvals.getBoolean(getApprovalString(phoneNumber,message,scheduledFor,approver),false)==false;
	}	
	
	public boolean wasApproved(String approvalString) {
		return approvals.getBoolean(approvalString,false);
	}
	public boolean wasDisapproved(String approvalString) {
		return approvals.contains(approvalString) &&
				approvals.getBoolean(approvalString,false)==false;
	}		
	public static String getApprovalString(String phoneNumber, String message,
			long scheduledFor, String approver) {
		return Integer.toString((phoneNumber+message+Long.toString(scheduledFor)+approver).hashCode());
	}
	public void saveTwilioNumber(String obj) {
		Editor editor = mPrefs.edit();
		editor.putString("twilioNumber", obj);	
		editor.commit();	
		
	}
	public void saveVerificationCode(String mVerificationCode) {
		Editor editor = mPrefs.edit();
		editor.putString("mVerificationCode", mVerificationCode);	
		editor.commit();	
	}
	public String getTwilioNumber() {
		// TODO Auto-generated method stub
		return mPrefs.getString("twilioNumber", null);
	}
	public String getVerificationCode() {
		// TODO Auto-generated method stub
		return mPrefs.getString("mVerificationCode", null);
	}
	public void putPosition(String number, int position) {
		Editor editor = mPrefs.edit();
		editor.putInt(number+"position", position);	
		Log.i(number+position,"cool");
		editor.commit();	
	}
	public int getPosition(String number) {
		return mPrefs.getInt(number+"position", -1);
	}
	public String getThreadID(String number) {
		return phoneNumber_threadID.getString(ContentUtils.addCountryCode(number), null);
	}
	public void saveThreadID(String number, String threadID) {
		Editor editor = phoneNumber_threadID.edit();
		editor.putString(ContentUtils.addCountryCode(number), threadID);	
		editor.commit();
		
	}
	public String[] getNames(String[] recipients) {
		String[] names = new String[recipients.length];
		for(int i=0; i<recipients.length; i++) {
			names[i] = getName(recipients[i]);
		}
		return names;
	}
	public String[] getPhoneNumbers(String[] recipients) {
		String[] numbers = new String[recipients.length];
		for(int i=0; i<recipients.length; i++) {
			numbers[i] = getPhoneNumber(recipients[i]);
		}
		return numbers;
	}
	public ArrayList<String> getRecipientIDs(String[] numbers) {
		ArrayList<String> recipientIds = new ArrayList<String>();
		for(int i=0; i<numbers.length; i++) {
			String recipient = getRecipientID(numbers[i]);
			recipientIds.add(recipient);
		}
		return recipientIds;
	}
	public String getRecipientID(String number) {
		return mPrefs_phoneNumber_recipientID.getString(ContentUtils.addCountryCode(number), null);
	}

}
