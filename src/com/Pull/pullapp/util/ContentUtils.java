package com.Pull.pullapp.util;

import com.Pull.pullapp.MainApplication;

import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.ThreadsColumns;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ContentUtils {

	public static String getContactDisplayNameByNumber(Context context, String number) {
		if(number == null) return null;
		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String name = number;
		ContentResolver contentResolver = context.getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] {
				BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME },
				null, null, null);
		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				contactLookup.moveToNext();
				name = contactLookup.getString(contactLookup
						.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}
		return name;
	}	
	public static String getAddressFromID(Context context, String recipientId) {
		  if(recipientId.length()==0) return null;
		  String address;
		  address = ""; 
		  Cursor c = context.getContentResolver().query(Uri
				  .parse("content://mms-sms/canonical-addresses"), null, BaseColumns._ID 
				  + " = " + recipientId, null, null);	  
		  if  (c.moveToNext()) {
			  address = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.ADDRESS)).toString();	  
		  }
		  c.close();
		  return address;
	  }
	  public static Cursor getThreadsCursor(Context context) {
		  
		  Cursor c = context.getContentResolver().query(
				  Uri.parse("content://mms-sms/conversations?simple=true"), 
				  new String[]{ThreadsColumns._ID, ThreadsColumns.READ, 
					  ThreadsColumns.RECIPIENT_IDS, ThreadsColumns.DATE,
					  ThreadsColumns.SNIPPET},
					  ThreadsColumns._ID+" is not null and replace("+ 
					  ThreadsColumns.RECIPIENT_IDS + ",' ','')=" + ThreadsColumns.RECIPIENT_IDS,
					  null,ThreadsColumns.DATE+" DESC");		  
		 // ContentQueryMap map = new ContentQueryMap(c, ThreadsColumns._ID, true, null);
		  return c;
	  }	
	  
	  public static void addMessage(String threadID, String number, String body) {
      	ContentValues values = new ContentValues();
  		values.put("read",true);			  
	  }
	  
	  public static String getNextThreadID(Context context, String recipient_ids) {
		  Uri threadIdUri = Uri.parse("content://mms-sms/threadID");
		  Uri.Builder builder = threadIdUri.buildUpon();
		  String[] recipients = recipient_ids.split(" ");
		  for(String recipient : recipients){
		      builder.appendQueryParameter("recipient", recipient);
		  }
		  Uri uri = builder.build();		  
		  double threadId = 0.0;
		  Cursor cursor = context.getContentResolver().query(uri, new String[]{BaseColumns._ID},
				  null, null, null);
		  if (cursor != null) {
		      try {
		          if (cursor.moveToFirst()) {
		              threadId = cursor.getLong(0);
		              }
		      } finally {
		              cursor.close();
		      }
		  }		
		  return Double.toString(threadId);
	  }
	  public static String getThreadIDFromNumber(Context context, String number) {
	      Cursor c = context.getContentResolver().query(Uri.parse("content://sms"),
	      		new String[]{"max(" + TextBasedSmsColumns.THREAD_ID + ") as " + 
	      				TextBasedSmsColumns.THREAD_ID,
	      				TextBasedSmsColumns.ADDRESS},
	      		TextBasedSmsColumns.ADDRESS+"='"+number+"'",null,null);
	     // Log.i("number",number);
	     // Log.i("size",c.getCount()+" ");
	      if (c!=null && c.getCount()>0) {
	    	  while(c.moveToNext()) {	
	    		  if (c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.THREAD_ID))==null) {
	    			 // Log.i("column","nothing");
	    			  return null;
	    		  }
	    		  String id = c.getString(c.getColumnIndexOrThrow(
	    				  TextBasedSmsColumns.THREAD_ID)).toString();
	    		  return id;
	    	}
	      }
	      	
	      return null;
	  }
	  
		// User input validation
		private static Boolean isNumberValid(String contact)	{
			if (contact == null)	{
				return false;
			}
			boolean valid1 = PhoneNumberUtils.isGlobalPhoneNumber(contact);
			boolean valid2 = PhoneNumberUtils.isWellFormedSmsAddress(contact);
			if ((valid1 == true) && (valid2 == true))	{
				return true;
			}
			return false;
		}
		public static String makeNumberValid(String contact)	{
			if (contact == null)	{
				return null;
			}
			String number = null;
			number = PhoneNumberUtils.formatNumber(contact);
			Boolean valid = isNumberValid(number);
			if (valid)	{
				return number;
			}
			return null;
		}
	    public static String subtractCountryCode(String number) {
	    	if(number == null) return number;
	    	if(number.trim().length()<=10) return number;
	    	return number.substring(number.length()-10);
	    }	
	    
	    public static String addCountryCode(String number) {
	    	if(number == null) return number;
	    	if(number.substring(0,1).equals("+")) return number.substring(1);
	    	if(number.trim().length()==10) return "1"+number;
	    	return number;
	    }		    
	    public static String addCountryCode(TelephonyManager tmgr, String number) {
	    	if(number == null) return number;
	    	if(number.substring(0,1).equals("+")) return number.substring(1);
	    	if(number.trim().length()==10) return "1"+number;
	    	return number;
	    }	
		// This function searches for an mobile phone entry for the contact
		public String getNumberfromContact(Context context, String contact, Boolean debugging)	{
			ContentResolver cr = context.getContentResolver();
			String result = null;
			boolean valid = false;	
			String val_num = null;
			int contact_id = 0;
		    // Cursor1 search for valid Database Entries who matches the contact name
			Uri uri = ContactsContract.Contacts.CONTENT_URI;
			String[] projection = new String[]{	ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER };
			String selection = ContactsContract.Contacts.DISPLAY_NAME + "=?";
			String[] selectionArgs = new String[]{String.valueOf(contact)};
			String sortOrder = null;
			Cursor cursor1 = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		
		    if(cursor1.moveToFirst()){
		    	if(cursor1.getInt(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) == 1){
		    		contact_id = cursor1.getInt(cursor1.getColumnIndex(ContactsContract.Contacts._ID));
		            // Cursor 2 search for valid MOBILE Telephone numbers (selection = Phone.TYPE 2)
		        	Uri uri2 = ContactsContract.Data.CONTENT_URI;	
		        	String[] projection2 = new String[]{ Phone.NUMBER, Phone.TYPE };
		        	String selection2 = Phone.CONTACT_ID + "=? AND " + Data.MIMETYPE + "=? AND " + Phone.TYPE + "=2";
		    		String[] selectionArgs2 = new String[]{ String.valueOf(contact_id), Phone.CONTENT_ITEM_TYPE };
		    		String sortOrder2 = Data.IS_PRIMARY + " desc"; 	
		        	Cursor cursor2 = cr.query(uri2, projection2, selection2, selectionArgs2, sortOrder2);
		            
		        	if(cursor2.moveToFirst()){
		                result = cursor2.getString(cursor2.getColumnIndex(Phone.NUMBER));
		            }
		            cursor2.close();
		        }
		        cursor1.close();
		    }
		    return result;
		}
		public static String setChannel(TelephonyManager tmgr, String recipient) {
			return "phoneNumber"+addCountryCode(tmgr,recipient);
		}
		public static String setChannel(String recipient) {
			return "phoneNumber"+addCountryCode(recipient);
		}
		public static Cursor getMessagesCursor(Context mContext,
				String thread_id, String number) {
			String querystring;
			if(thread_id == null) {
				querystring = 
				"REPLACE(REPLACE(REPLACE(REPLACE(" + TextBasedSmsColumns.ADDRESS + 
						",'(',''),')',''),' ',''),'-','') " 
						+ " in ('"+ 
		   				ContentUtils.subtractCountryCode(number) + "', '" +
		   				ContentUtils.addCountryCode(number) + "', '+" +
		   				ContentUtils.addCountryCode(number) + "', '" +
		   				number + "')" 
		   				+  " and " + TextBasedSmsColumns.TYPE + "!=" + 
		   				TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX 					
   				+  " and " + TextBasedSmsColumns.TYPE + "!=" + 
		   				TextBasedSmsColumns.MESSAGE_TYPE_DRAFT ;					

			}
			else querystring = TextBasedSmsColumns.THREAD_ID + "=" + thread_id 
					+ " and " + TextBasedSmsColumns.TYPE + "!=" + 
					TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX
				+  " and " + TextBasedSmsColumns.TYPE + "!=" + 
					TextBasedSmsColumns.MESSAGE_TYPE_DRAFT ;					
	        DatabaseHandler dh = new DatabaseHandler(mContext);
			Cursor messages_cursor = mContext.getContentResolver().query(Uri.parse("content://sms"),
					null,querystring ,null,TextBasedSmsColumns.DATE + " desc");	        
			return messages_cursor;
		}		
 
}
