package com.Pull.pullapp.util;


import java.util.ArrayList;
import java.util.HashMap;
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
import android.net.Uri;
import android.provider.Telephony.TextBasedSmsColumns;
import android.telephony.SmsManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.Pull.pullapp.model.Invite;
import com.Pull.pullapp.model.SMSMessage;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.parse.FunctionCallback;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParsePush;
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
			long launchedOn, long scheduledFor, final Boolean AddtoSent)	{
		try {
			Intent myIntent = new Intent(Constants.ACTION_SMS_DELIVERED);
			myIntent.putExtra(Constants.EXTRA_RECIPIENT, phoneNumber);
			myIntent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
			myIntent.putExtra(Constants.EXTRA_TIME_LAUNCHED, launchedOn);
			myIntent.putExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, scheduledFor);
			
	    	PendingIntent sentPI = PendingIntent
	    			.getBroadcast(context, (int)launchedOn, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	        
	    	SmsManager sms = SmsManager.getDefault();
	        ArrayList<String> msgparts = sms.divideMessage(message);
	    	ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
	    	int msgcount = msgparts.size();
	
	    	for (int i = 0; i < msgcount; i++) {
	            sentPendingIntents.add(sentPI);
	        }
	        if (AddtoSent)	{
				addMessageToSent(context, phoneNumber, message, scheduledFor);
			}	
	    	sms.sendMultipartTextMessage(phoneNumber, null, msgparts, sentPendingIntents, null);

		} catch (Exception e) {
	        e.printStackTrace();
	        Log.e(TAG, "undefined Error: SMS sending failed ... please REPORT to ISSUE Tracker");
	    }
	}
	public static void sendmms(Context context, String[] recipients,
			String message, long launchedOn, long scheduledFor, boolean AddtoSent, String thread_id) {
		try {	
			long long_thread_id;
			if(thread_id==null || thread_id.isEmpty() || AddtoSent) long_thread_id=0;
			else long_thread_id = Long.parseLong(thread_id);
			Log.i("sendmms","sendmms thread id " + thread_id);
			Settings sendSettings = new Settings();
	        TransactionSettings transactionSettings = new TransactionSettings(
	        		context, null);
			sendSettings.setMmsc(transactionSettings.getMmscUrl());
			sendSettings.setProxy(transactionSettings.getProxyAddress());
			sendSettings.setPort(Integer.toString(transactionSettings.getProxyPort()));
			sendSettings.setGroup(true);
			sendSettings.setDeliveryReports(false);
			sendSettings.setSplit(false);
			sendSettings.setSplitCounter(false);
			sendSettings.setStripUnicode(false);
			sendSettings.setSignature("");
			sendSettings.setSendLongAsMms(true);
			sendSettings.setSendLongAsMmsAfter(3);
			sendSettings.setRnrSe(null);
			Transaction sendTransaction = new Transaction(context, sendSettings);
			Message mMessage = new Message();
			mMessage.setSave(AddtoSent);
			mMessage.setAddresses(recipients);
			Log.i("numbers","recipeitns" + recipients.length);
			mMessage.setText(message);
			//Message mMessage = new Message("hola", "16507966210");
			mMessage.setType(Message.TYPE_SMSMMS);  // could also be Message.TYPE_VOICE	
			sendTransaction.sendNewMessage(mMessage, long_thread_id);	       
			
			Intent myIntent = new Intent(Constants.ACTION_MMS_DELIVERED);
			myIntent.putExtra(Constants.EXTRA_RECIPIENTS, recipients);
			myIntent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
			myIntent.putExtra(Constants.EXTRA_TIME_LAUNCHED, launchedOn);
			myIntent.putExtra(Constants.EXTRA_THREAD_ID, thread_id);
			myIntent.putExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, scheduledFor);
	    	PendingIntent sentPI = PendingIntent
	    			.getBroadcast(context, (int)launchedOn, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	    	context.sendBroadcast(myIntent);
		} catch (Exception e) {
	        e.printStackTrace();
	        //Log.e(TAG, "undefined Error: MMS sending failed ... please REPORT to ISSUE Tracker");
	    }		
	}	
	
	// This function add's the sent sms to the SMS sent folder
	private static void addMessageToSent(Context context, String phoneNumber, String message, long date) {
	    ContentValues sentSms = new ContentValues();
	    sentSms.put(TextBasedSmsColumns.ADDRESS, phoneNumber);
	    sentSms.put(TextBasedSmsColumns.BODY, message);
	    sentSms.put(TextBasedSmsColumns.DATE, date);
	    ContentResolver contentResolver = context.getContentResolver();
	    Uri inserted = contentResolver.insert(SENT_MSGS_CONTENT_PROVIDER, sentSms);
	    if(inserted!=null) Log.i("inserted ",inserted.toString());
	}
	public static void addMessageToOutbox(Context context, String recipient, String message,
			long timeScheduled, long scheduledFor, String approver) {
		DatabaseHandler db = new DatabaseHandler(context);
		db.addToOutbox(recipient, message, timeScheduled, scheduledFor, approver);
		db.close();
	    Intent intent = new Intent(Constants.ACTION_SMS_OUTBOXED);
	    intent.putExtra(Constants.EXTRA_RECIPIENT, recipient);
	    intent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
	    intent.putExtra(Constants.EXTRA_TIME_LAUNCHED, timeScheduled);
	    intent.putExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, scheduledFor);
	    intent.putExtra(Constants.EXTRA_APPROVER, approver);
	    context.sendBroadcast(intent);	
	}	
	

	public static int removeFromOutbox(Context context, String body, String recipient, 
			long launchedOn, long scheduledFor, boolean clearFromScreen, String approver) {
		DatabaseHandler db = new DatabaseHandler(context);
		int rowsdeleted = db.deleteFromOutbox(launchedOn);
		db.close();
		Log.i("rows deleted ", " " + rowsdeleted);
		if(rowsdeleted>0 && clearFromScreen) {
		    Intent intent = new Intent(Constants.ACTION_SMS_UNOUTBOXED);
		    if(recipient!=null) {
		    	intent.putExtra(Constants.EXTRA_RECIPIENT, recipient);
			}
		    intent.putExtra(Constants.EXTRA_TIME_LAUNCHED, launchedOn);
		    intent.putExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, scheduledFor);
		    intent.putExtra(Constants.EXTRA_MESSAGE_BODY, body);
		    intent.putExtra(Constants.EXTRA_APPROVER, approver);
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
	
	@SuppressWarnings("unchecked")
	public static void inviteFriend(final String number, final Context context, Activity a) {
		UserInfoStore store = new UserInfoStore(context);
		store.logInvite(number);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("phoneNumber", ContentUtils.addCountryCode(number));
		ParseCloud.callFunctionInBackground("findUser", params, new FunctionCallback() {
			@Override
			public void done(Object obj, ParseException e) {
		         if (e == null) {
		        	 inviteViaPush(number, context);			         
		         } else {
		        	 inviteViaSMS(number, context);
		         }
			} 
		});			
		
		Toast.makeText(context, "Invite sent", Toast.LENGTH_LONG).show();
		Invite inv = new Invite(ParseUser.getCurrentUser(),number);
		inv.saveInBackground();
	}

	protected static void inviteViaSMS(String number, Context context) {
		// TODO Make this use Twilio? but maybe not because the recipient would not trust it
		SendUtils.sendsms(context, number,
				"I'm inviting you to be my friend on Pull, so we can share texts without taking screenshots. Learn more at " 
				+ Constants.WEB_LINK, System.currentTimeMillis(), System.currentTimeMillis(), false);
		
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
			app_plug = "I'm using the Pull app to share my conversation with " + person_shared + ". " 
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

	public static void commentViaSMS(Context mContext, String person_shared,
			String shared_confidante, SMSMessage comment) {  
		String text = "[About my convo with " + person_shared + "]:" + comment.getMessage();
		SendUtils.sendsms(mContext, shared_confidante,text, 
				System.currentTimeMillis(), System.currentTimeMillis(), false);        
		
	}


	public static void addMessageToOutbox(Context context, String[] recipients,
			String message, long timeScheduled, long scheduledFor, String approver, String thread_id) {
		DatabaseHandler db = new DatabaseHandler(context);
		db.addToOutbox(recipients, message, timeScheduled, scheduledFor, approver);
		db.close();
	    Intent intent = new Intent(Constants.ACTION_MMS_OUTBOXED);
	    intent.putExtra(Constants.EXTRA_RECIPIENTS, recipients);
	    intent.putExtra(Constants.EXTRA_THREAD_ID, thread_id);
	    intent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
	    intent.putExtra(Constants.EXTRA_TIME_LAUNCHED, timeScheduled);
	    intent.putExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, scheduledFor);
	    intent.putExtra(Constants.EXTRA_APPROVER, approver);
	    Log.i("message added ","mms added to outbox");
	    context.sendBroadcast(intent);	
		
	}


	public static int removeFromOutbox(Context context, String body,
			String[] recipients, long launchedOn, long scheduledFor,
			boolean clearFromScreen, String approver) {
		DatabaseHandler db = new DatabaseHandler(context);
		int rowsdeleted = db.deleteFromOutbox(launchedOn);
		db.close();
		//Log.i("rows deleted ", " " + rowsdeleted);
		if(rowsdeleted>0 && clearFromScreen) {
		    Intent intent = new Intent(Constants.ACTION_SMS_UNOUTBOXED);
		    intent.putExtra(Constants.EXTRA_RECIPIENTS, recipients);
		    intent.putExtra(Constants.EXTRA_TIME_LAUNCHED, launchedOn);
		    intent.putExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, scheduledFor);
		    intent.putExtra(Constants.EXTRA_MESSAGE_BODY, body);
		    intent.putExtra(Constants.EXTRA_APPROVER, approver);
	        context.sendBroadcast(intent);		
		}
		return rowsdeleted;
	}
	
}
