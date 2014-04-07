package com.Pull.pullapp.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.ThreadsColumns;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;

public class ContentUtils {

	public static String getContactDisplayNameByNumber(Context context, String number) {
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
		  if (c!=null) while (c.moveToNext()) {
			  address = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.ADDRESS)).toString();	  
		  }
		  return address;
	  }
	  public static Cursor getThreadsCursor(Context context) {
		  return context.getContentResolver().query(
				  Uri.parse("content://mms-sms/conversations?simple=true"), 
				  null, null,null,ThreadsColumns.DATE+" DESC");
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
	      Log.i("number",number);
	      Log.i("size",c.getCount()+" ");
	      if (c!=null && c.getCount()>0) {
	    	  while(c.moveToNext()) {	
	    		  if (c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.THREAD_ID))==null) {
	    			  Log.i("column","nothing");
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
	    	if(number.trim().length()<=10) return number;
	    	return number.substring(number.length()-10);
	    }	
	    public static String addCountryCode(String number) {
	    	if(number.trim().length()>=11) return number;
	    	if(number.trim().length()==10) return "+1"+number;
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
		   /* if (result != null)	{
		    	valid = isNumberValid(result);
		    }
			if (!valid)	{
				val_num = makeNumberValid(result);
				if (val_num != null)	{
					valid = true;
					result = val_num;
				}
			}
		    if (valid)	{
		    	return result;
		    } else	{
		    	return null;
		    }*/
		    return result;
		}	  
}
