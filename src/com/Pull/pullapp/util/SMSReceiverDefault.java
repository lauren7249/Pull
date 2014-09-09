package com.Pull.pullapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony.Sms.Intents;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import com.Pull.pullapp.MessageActivityCheckboxCursor;
import com.Pull.pullapp.R;
import com.Pull.pullapp.model.SMSMessage;
import com.parse.ParseUser;

public class SMSReceiverDefault extends BroadcastReceiver {
	public SMSReceiverDefault() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction()
				.equals(Intents.SMS_DELIVER_ACTION)) {
	        SharedPreferences sharedPrefs = PreferenceManager
	                .getDefaultSharedPreferences(context);			
	        UserInfoStore store = new UserInfoStore(context);
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				// Get SMS objects.
				Object[] pdus = (Object[]) bundle.get("pdus");
				if (pdus.length == 0) {
					return;
				}
				// Large message might be broken into many.
				SmsMessage[] messages = new SmsMessage[pdus.length];
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < pdus.length; i++) {
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					sb.append(messages[i].getMessageBody());
				}
				String sender = ContentUtils.addCountryCode(messages[0].getOriginatingAddress());

				// Contact not in address book: log message and don't let it through.
				String message = sb.toString();	        
		        String twilioNumber = ContentUtils.addCountryCode(store.getTwilioNumber());
		        if(twilioNumber!=null && sender!=null && 
		        		ContentUtils.addCountryCode(sender).equals(twilioNumber)){
		        	abortBroadcast();
		        	if(message.equals(store.getVerificationCode())) {
		        	    intent = new Intent(Constants.ACTION_NUMBER_VERIFIED);
		        	    context.sendBroadcast(intent);		
		        	   // Log.i("received broadcast",Constants.ACTION_NUMBER_VERIFIED);
		        	}
		        	return;
		        }
		        boolean receive = sharedPrefs.getBoolean("prefReceiveTexts", false);
				if (receive) {
					abortBroadcast();					
						// Prevent other broadcast receivers from receiving broadcast.
					
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm'Z'"); // ISO 8601, Local time zone.
					dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

					String name = ContentUtils.getContactDisplayNameByNumber(context, sender);
					String threadID = ContentUtils.getThreadIDFromNumber(context, sender);
					if(threadID == null || threadID.length()==0) {
						threadID = ContentUtils.getNextThreadID(context,sender);
						Log.i("thread id",threadID);
					}
					pushMessage(context,message,sender,threadID);		
					SMSMessage m = new SMSMessage(new Date().getTime(), message, sender, 
							name, TextBasedSmsColumns.MESSAGE_TYPE_INBOX, store, 
							ParseUser.getCurrentUser().getUsername());
					try {
						m.saveToParse();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					NotificationManager mNotificationManager = (NotificationManager) context
							.getSystemService(Context.NOTIFICATION_SERVICE);
					int icon;
					icon = R.drawable.ic_launcher_gray;
					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
							context).setSmallIcon(icon).setContentTitle(name)
							.setContentText(message)
							.setPriority(NotificationCompat.PRIORITY_LOW)
							.setOnlyAlertOnce(true);
					Intent ni = new Intent(context, MessageActivityCheckboxCursor.class);
					ni.putExtra(Constants.EXTRA_THREAD_ID,threadID);
					ni.putExtra(Constants.EXTRA_NAME,name);
			        ni.putExtra(Constants.EXTRA_NUMBER,sender);
					ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					PendingIntent pi = PendingIntent.getActivity(context, 0,
							ni, PendingIntent.FLAG_CANCEL_CURRENT);
					mBuilder.setContentIntent(pi);
					mBuilder.setAutoCancel(true);
					Notification notification = mBuilder.build();
					notification.defaults|= Notification.DEFAULT_SOUND;
					notification.defaults|= Notification.DEFAULT_LIGHTS;
					notification.defaults|= Notification.DEFAULT_VIBRATE;		
					mNotificationManager.notify(sender.hashCode(),notification);
				}
			}
			return;
		}
	}
	public static void pushMessage(Context context, String message, String number, String thread_id) {

		ContentValues values = new ContentValues(7);
		values.put("address", number);
		values.put("read", false);
		values.put("subject", "");
		values.put("body", message);
		values.put("thread_id", thread_id);
		Uri uri = Uri.parse("content://sms/inbox");
		context.getContentResolver().insert(uri, values);		
	    Intent intent = new Intent(Constants.ACTION_SMS_INBOXED);
	    intent.putExtra(Constants.EXTRA_NUMBER, number);
	    context.sendBroadcast(intent);	

	}	

}
