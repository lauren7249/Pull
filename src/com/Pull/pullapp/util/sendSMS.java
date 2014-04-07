package com.Pull.pullapp.util;


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
  
	private static final String TAG = "SendSMS";
	
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
	    sentSms.put(TextBasedSmsColumns.ADDRESS, phoneNumber);
	    sentSms.put(TextBasedSmsColumns.BODY, message);
	    
	    ContentResolver contentResolver = context.getContentResolver();
	    Uri inserted = contentResolver.insert(SENT_MSGS_CONTENT_PROVIDER, sentSms);
	    if(inserted!=null) Log.i("inserted ",inserted.toString());
	}
	public static void addMessageToOutbox(Context context, String recipient, String message,
			long timeScheduled, long scheduledFor) {
		DatabaseHandler db = new DatabaseHandler(context);
		int inserted = db.addToOutbox(recipient, message, timeScheduled, scheduledFor);
		db.close();
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
		DatabaseHandler db = new DatabaseHandler(context);
		int rowsdeleted = db.deleteFromOutbox(launchedOn);
		db.close();
		Log.i("rows deleted ", " " + rowsdeleted);
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
	

}
