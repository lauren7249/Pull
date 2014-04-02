package com.Pull.smsTest.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
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
		  Cursor c = context.getContentResolver().query(Uri
				  .parse("content://mms-sms/canonical-addresses"), null, "_id = " + recipientId, null, null);	  
		  if (c!=null) while (c.moveToNext()) {
				return c.getString(c
						.getColumnIndexOrThrow("address")).toString();	  
		  }
		  return "";
	  }
	  public static Cursor getThreadsCursor(Context context) {
		  return context.getContentResolver().query(Uri.parse("content://mms-sms/conversations?simple=true"), 
				  null, null,null,"date DESC");
	  }	
	  
	  public static void addMessage(String threadID, String number, String body) {
      	ContentValues values = new ContentValues();
  		values.put("read",true);
  		//getContentResolver().update(Uri.parse("content://sms/"),values, "_id="+SmsMessageId, null);			  
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
		  Cursor cursor = context.getContentResolver().query(uri, new String[]{"_id"}, null, null, null);
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
	   /* Cursor a = context.getContentResolver().query(Uri.parse("content://mms-sms/conversations?simple=true"),
		      		new String[]{"max(_id) as _id"},null,null,null);	
	    if(a.moveToNext()) {
	    	String max_id = a.getString(a.getColumnIndexOrThrow("_id")).toString().trim();
	    	String next_id = Integer.toString(Integer.parseInt(max_id) + 1);
	    	return next_id;
	    }
		return "1";		  */
	  }
	  public static String getThreadIDFromNumber(Context context, String number) {
	      Cursor c = context.getContentResolver().query(Uri.parse("content://sms"),
	      		new String[]{"max(thread_id) as thread_id","address"},"address='"+number+"'",null,null);
	      Log.i("number",number);
	      Log.i("size",c.getCount()+" ");
	      if (c!=null && c.getCount()>0) {
	    	  while(c.moveToNext()) {	
	    		  if (c.getString(c.getColumnIndexOrThrow("thread_id"))==null) {
	    			  Log.i("column","nothing");
	    			  return null;
	    		  }
	    		  String id = c.getString(c.getColumnIndexOrThrow("thread_id")).toString();
	    		  return id;
	    	}
	      }
	      	
	      return null;
	  }
}
