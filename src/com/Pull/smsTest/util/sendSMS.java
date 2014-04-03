package com.Pull.smsTest.util;


import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.Telephony.TextBasedSmsColumns;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;

public class sendSMS  {    
  
	private static final String TAG = "Like_sendSMS";
	
	private static final Uri SENT_MSGS_CONTENT_PROVIDER = Uri.parse("content://sms/sent");
	private static final Uri OUTBOX_MSGS_CONTENT_PROVIDER = Uri.parse("content://sms/outbox");
	private static final Uri SMS_CONTENT_PROVIDER = Uri.parse("content://sms");
	
	boolean DEBUG = false;	// debug mode, display additional output, sends no sms.
	boolean SECRET = false;	// for secret mode => dont't save sent sms to sent folder.
	String contact = null;
	String val_num = null;	// validated Number
	String msg = null;		// message
	boolean valid = false;	// for user input validation
	int check = 0;			// getExtras check counter

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
	    		if (debugging)	{
	        		Log.d(TAG, "C1 found Database ID: " + contact_id + " with Entry: " + cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
	            }
	            // Cursor 2 search for valid MOBILE Telephone numbers (selection = Phone.TYPE 2)
	        	Uri uri2 = ContactsContract.Data.CONTENT_URI;	
	        	String[] projection2 = new String[]{ Phone.NUMBER, Phone.TYPE };
	        	String selection2 = Phone.CONTACT_ID + "=? AND " + Data.MIMETYPE + "=? AND " + Phone.TYPE + "=2";
	    		String[] selectionArgs2 = new String[]{ String.valueOf(contact_id), Phone.CONTENT_ITEM_TYPE };
	    		String sortOrder2 = Data.IS_PRIMARY + " desc"; 	
	        	Cursor cursor2 = cr.query(uri2, projection2, selection2, selectionArgs2, sortOrder2);
	            
	        	if(cursor2.moveToFirst()){
	                result = cursor2.getString(cursor2.getColumnIndex(Phone.NUMBER));
	        		if (debugging)	{
	                	Log.d(TAG, "C2 found number: " + result);
	                }
	            }
	            cursor2.close();
	        }
	        cursor1.close();
	    }
	    if (result != null)	{
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
	    }
	}
	
	// This function sends the sms with the SMSManager
	public static void sendsms(Context context, final String phoneNumber, final String message, 
			long launchedOn, final Boolean AddtoSent)	{
		try {
			Intent myIntent = new Intent(Constants.ACTION_SMS_DELIVERED);
			myIntent.putExtra(Constants.EXTRA_RECIPIENT, phoneNumber);
			myIntent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
			myIntent.putExtra(Constants.EXTRA_TIME_LAUNCHED, launchedOn);
	    	PendingIntent sentPI = PendingIntent
	    			.getBroadcast(context, (int)launchedOn, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	        
	    	SmsManager sms = SmsManager.getDefault();
	        ArrayList<String> msgparts = sms.divideMessage(message);
	    	ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
	    	int msgcount = msgparts.size();
	
	    	for (int i = 0; i < msgcount; i++) {
	            sentPendingIntents.add(sentPI);
	        }
	
	    	sms.sendMultipartTextMessage(phoneNumber, null, msgparts, sentPendingIntents, null);

	        if (AddtoSent)	{
				addMessageToSent(context, phoneNumber, message);
			}
		} catch (Exception e) {
	        e.printStackTrace();
	        Log.e(TAG, "undefined Error: SMS sending failed ... please REPORT to ISSUE Tracker");
	    }
	}
	// This function add's the sent sms to the SMS sent folder
	private static void addMessageToSent(Context context, String phoneNumber, String message) {
	    ContentValues sentSms = new ContentValues();
	    sentSms.put(TextBasedSmsColumns.ADDRESS, addCountryCode(phoneNumber));
	    sentSms.put(TextBasedSmsColumns.BODY, message);
	    
	    ContentResolver contentResolver = context.getContentResolver();
	    contentResolver.insert(SENT_MSGS_CONTENT_PROVIDER, sentSms);
	}
	public static void addMessageToOutbox(Context context, String recipient, String message,
			long timeScheduled, long scheduledFor) {
	    ContentValues outboxSms = new ContentValues();
	    outboxSms.put(TextBasedSmsColumns.DATE_SENT, timeScheduled);
	    outboxSms.put(TextBasedSmsColumns.DATE, scheduledFor);
	    outboxSms.put(TextBasedSmsColumns.BODY, message);
	    outboxSms.put(TextBasedSmsColumns.ADDRESS, addCountryCode(recipient));
	    ContentResolver contentResolver = context.getContentResolver();
	    contentResolver.insert(OUTBOX_MSGS_CONTENT_PROVIDER, outboxSms);
	    Intent intent = new Intent(Constants.ACTION_SMS_OUTBOXED);
	    intent.putExtra(Constants.EXTRA_RECIPIENT, recipient);
	    intent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
	    intent.putExtra(Constants.EXTRA_TIME_LAUNCHED, timeScheduled);
	    intent.putExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, scheduledFor);
        PendingIntent outbox;
        outbox = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);   
        am.set(AlarmManager.RTC, System.currentTimeMillis(), outbox);     	    
	}	
	

	public static int removeFromOutbox(Context context, String body, String recipient, long launchedOn, boolean clearFromScreen) {

		ContentResolver contentResolver = context.getContentResolver();
		String where = TextBasedSmsColumns.DATE_SENT + "=" + launchedOn + " and "
		+ TextBasedSmsColumns.TYPE + "=" + TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX;
		int rowsdeleted = contentResolver.delete(SMS_CONTENT_PROVIDER, where, null);
		if(rowsdeleted>0 && clearFromScreen) {
		    Intent intent = new Intent(Constants.ACTION_SMS_UNOUTBOXED);
		    intent.putExtra(Constants.EXTRA_RECIPIENT, recipient);
		    intent.putExtra(Constants.EXTRA_TIME_LAUNCHED, launchedOn);
		    intent.putExtra(Constants.EXTRA_MESSAGE_BODY, body);
	        PendingIntent outbox;
	        outbox = PendingIntent
	        		.getBroadcast(context, (int)launchedOn, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);   
	        am.set(AlarmManager.RTC, System.currentTimeMillis(), outbox);  			
		}
		return rowsdeleted;
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
}
