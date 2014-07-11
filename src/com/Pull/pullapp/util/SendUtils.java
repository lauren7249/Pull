package com.Pull.pullapp.util;


import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.provider.Telephony.TextBasedSmsColumns;
import android.telephony.SmsManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.Pull.pullapp.model.SMSMessage;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class SendUtils  {    
  
	private static final String TAG = "SendSMS";
	
	private static final Uri SENT_MSGS_CONTENT_PROVIDER = Uri.parse("content://sms/sent");
	
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
	
	@Deprecated
	// This function sends the sms with the SMSManager
	public static void sendmms(Context context, final String phoneNumber, final String message, 
			long launchedOn, final Boolean AddtoSent)	{
		try {
			Intent myIntent = new Intent(Constants.ACTION_SMS_DELIVERED);
			myIntent.putExtra(Constants.EXTRA_RECIPIENT, phoneNumber);
			myIntent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
			myIntent.putExtra(Constants.EXTRA_TIME_LAUNCHED, launchedOn);
	    	PendingIntent sentPI = PendingIntent
	    			.getBroadcast(context, (int)launchedOn, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	        
	    	Settings sendSettings = new Settings();

	    	Cursor cursor = context.getContentResolver().query(Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "current"),
	    	        null, null, null, null);
	    	cursor.moveToLast();
	    	String mmsc = cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MMSC));
	    	String proxy = cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MMSPROXY));
	    	String port = cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MMSPORT));
	    	sendSettings.setMmsc(mmsc);
	    	sendSettings.setProxy(proxy);
	    	sendSettings.setSendLongAsMms(true);
	    	sendSettings.setDeliveryReports(false);
	    	sendSettings.setSplit(false);
	    	sendSettings.setGroup(true);
	    	sendSettings.setStripUnicode(false);
	    	sendSettings.setSignature("");
	    	sendSettings.setPort(port);	 
	    	sendSettings.setRnrSe(null);
	    	
	    	Transaction sendTransaction = new Transaction(context, sendSettings);
	    	Message mMessage = new Message(message, phoneNumber);
	    	mMessage.setType(Message.TYPE_SMSMMS);  // could also be Message.TYPE_VOICE
	    	sendTransaction.sendNewMessage(mMessage,Transaction.NO_THREAD_ID);
	    	
	    	
	        if (AddtoSent)	{
				//addMessageToSent(context, phoneNumber, message);
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
		db.addToOutbox(recipient, message, timeScheduled, scheduledFor);
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
	        context.sendBroadcast(intent);		
		}
		return rowsdeleted;
	}
	public static void sendMessagetoNumber(String number,String message){
		ParsePush push = new ParsePush();
		push.setChannel(ContentUtils.setChannel(number));
		push.setMessage(message);
		push.sendInBackground();		
	}	
	
	public static void inviteFriend(final String number, final Context context, Activity a) {
		UserInfoStore store = new UserInfoStore(context);
		store.logInvite(number);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Channels");
		query.whereEqualTo("channel", ContentUtils.setChannel(number));
		query.findInBackground(new FindCallback<ParseObject>() {
		    public void done(List<ParseObject> list, ParseException e) {
		        if (e==null && list.size()>0) {
		        	inviteViaPush(number, context);
		        } else {
		        	inviteViaSMS(number, context);
		        }
		    }
		});			
		Toast.makeText(context, "Invite sent", Toast.LENGTH_LONG).show();
		
	}

	protected static void inviteViaSMS(String number, Context context) {
		// TODO Make this use Twilio? but maybe not because the recipient would not trust it
		SendUtils.sendsms(context, number,
				"Join me on Pull, the new Android app for sharing text conversations with friends. " 
				+ Constants.APP_PLUG_END, System.currentTimeMillis(), false);
		
	}

	protected static void inviteViaPush(String number, Context context) {
		JSONObject data = new JSONObject();
		String channel = ContentUtils.setChannel(number);
		try {
			data.put("action", Constants.ACTION_INVITE_FRIEND);
			data.put("number", ParseUser.getCurrentUser().get("username"));
			data.put("userid",ParseUser.getCurrentUser().getObjectId());
			ParsePush push = new ParsePush();
			push.setChannel(channel);
			push.setData(data);
			push.sendInBackground();
			//Log.i("push sent","to channel " + channel);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}	
	public static void shareViaSMS(Context parent, String person_shared, String confidante, 
			TreeSet<SMSMessage> messages) {
        AlarmManager am = (AlarmManager) parent.getSystemService(Context.ALARM_SERVICE);   
		DatabaseHandler db = new DatabaseHandler(parent);
		String app_plug, text;
		if(true) {
			app_plug = "Hey, check out my conversation with " + person_shared + ". " 
					+ Constants.APP_PLUG_END;
		}
		else {
			app_plug = "My conversation with " + person_shared + " continued.... " 
					+ Constants.APP_PLUG_END;
		}    	
		
		long currentDate = System.currentTimeMillis();
		SendUtils.setSendAlarm(am, app_plug, 1, currentDate, confidante, parent);
        int i = messages.size();
        Log.i("messages size", " " + messages.size());
        for(SMSMessage m: messages) {
        	long longdate = m.getDate();
    		String date = " [" + DateUtils.getRelativeDateTimeString(parent, 
    				longdate, DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0) + "]: ";
        	if(m.isSentByMe()) text =  "Me " + date + m.getMessage();
        	else text = person_shared + date + m.getMessage();
        	int elapse = (int) (currentDate + i*1000);
        	SendUtils.setSendAlarm(am, text, (int) elapse, (long) elapse, confidante, parent);
        	i--;
    	}
		
	}
	public static void setSendAlarm(AlarmManager am, String message, int id, long sendOn, String recipient,
			Context parent){
        Intent intent = new Intent(Constants.ACTION_SHARE_TAG);
        intent.putExtra(Constants.EXTRA_RECIPIENT, recipient);
        intent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
        PendingIntent sendMessage;
        sendMessage = PendingIntent.getBroadcast(parent, id, 
        		intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC, sendOn, sendMessage);         	
    }

	public static void confirmFriend(String number, String userID) {
		ParseUser me = ParseUser.getCurrentUser();
		ParseACL acl = new ParseACL();
		acl.setReadAccess(userID, true);
		me.setACL(acl);
		me.saveInBackground();
		sendFriendConfirmation(number, userID, me.getObjectId());
		
	}

	private static void sendFriendConfirmation(String number, String friend_userID,
			String my_userID) {
		JSONObject data = new JSONObject();
		String channel = ContentUtils.setChannel(number);
		try {
			data.put("action", Constants.ACTION_CONFIRM_FRIEND);
			data.put("number", ParseUser.getCurrentUser().get("username"));
			data.put("sender_userid",my_userID);
			data.put("receiver_userid",friend_userID);
			ParsePush push = new ParsePush();
			push.setChannel(channel);
			push.setData(data);
			push.sendInBackground();
			//Log.i("push sent","to channel " + channel);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}	


}
