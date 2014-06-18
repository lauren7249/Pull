package com.Pull.pullapp.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.Pull.pullapp.MessageActivityCheckboxCursor;
import com.Pull.pullapp.R;
import com.Pull.pullapp.model.SMSMessage;

public class DailyShareSuggestion extends Thread {
    private Context mContext;
    public DailyShareSuggestion(Context mContext) {
    	this.mContext = mContext;
    }

    @Override
    public void run() {
    	
    	long date = System.currentTimeMillis();
    	int days_backwards = 7;
    	int hours_ago = 4;
    	String best_friend_number = getBestFriendNumber(date, days_backwards);
    	String share_convo_number = getNumberToShareFrom(best_friend_number, hours_ago);
    	notifyShareSuggestion(best_friend_number,share_convo_number);
      
    }

	private void notifyShareSuggestion(String best_friend_number,
			String share_convo_number) {
		String best_friend_name = ContentUtils.getContactDisplayNameByNumber(mContext, best_friend_number);
		String shared_from_name = ContentUtils.getContactDisplayNameByNumber(mContext, share_convo_number);
		String shared_from_thread = ContentUtils.getThreadIDFromNumber(mContext, share_convo_number);
		
		NotificationManager mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon;
		icon = R.drawable.ic_launcher_gray;
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				mContext).setSmallIcon(icon).setContentTitle("Share this with " + best_friend_name + "!")
				.setContentText("Show " + best_friend_name + " your conversation with " + shared_from_name)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setOnlyAlertOnce(true);
		
		// TODO: Optional light notification.
		Intent ni = new Intent(mContext, MessageActivityCheckboxCursor.class);
		ni.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);		
		ni.putExtra(Constants.EXTRA_THREAD_ID,shared_from_thread);
		ni.putExtra(Constants.EXTRA_NAME,shared_from_name);
		ni.putExtra(Constants.EXTRA_READ,true);
		ni.putExtra(Constants.EXTRA_NUMBER,PhoneNumberUtils.stripSeparators(share_convo_number));	
		ni.putExtra(Constants.EXTRA_SHARE_TO_NUMBER,PhoneNumberUtils.stripSeparators(best_friend_number));	
		PendingIntent pi = PendingIntent.getActivity(mContext, 0,
				ni, PendingIntent.FLAG_CANCEL_CURRENT);
		mBuilder.setContentIntent(pi);
		mBuilder.setAutoCancel(true);
		Notification notification = mBuilder.build();
		notification.defaults|= Notification.DEFAULT_SOUND;
		notification.defaults|= Notification.DEFAULT_LIGHTS;
		notification.defaults|= Notification.DEFAULT_VIBRATE;		
		mNotificationManager.notify(777, notification);

		
	}

	private String getNumberToShareFrom(String best_friend_number, int hours_backwards) {
		String idiot = "";
		String[] variables = new String[]{TextBasedSmsColumns.ADDRESS};
		String querystring = TextBasedSmsColumns.TYPE + "=" + TextBasedSmsColumns.MESSAGE_TYPE_INBOX + 
					" and " + TextBasedSmsColumns.DATE 
					+ ">=" + (System.currentTimeMillis() - hours_backwards*60*60*1000) 
					+ " and " + TextBasedSmsColumns.ADDRESS + "!=" + best_friend_number;
	        
		Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://sms"),
					variables,querystring ,null,"date desc");	      
        if (cursor.moveToFirst()) {
            do {
            	String contact = cursor.getString(0);
            	if(idiot.length()==0) idiot = contact;
            	String name = ContentUtils.getContactDisplayNameByNumber(mContext, contact);
            	if(!name.equals(contact)) {
            		Log.i("idiot", name);
            		idiot = contact;
            		cursor.close();
            		return idiot;
            	}
            } while (cursor.moveToNext());
        }
        cursor.close();
		return idiot;
	}

	private String getBestFriendNumber(long date, int days_backwards) {
		String bestie = "";
		String[] variables = new String[]{"count(*) as count",TextBasedSmsColumns.ADDRESS};
		String querystring = TextBasedSmsColumns.TYPE + "=" + TextBasedSmsColumns.MESSAGE_TYPE_SENT + 
					" and " + TextBasedSmsColumns.DATE 
					+ ">=" + (System.currentTimeMillis() - days_backwards*24*60*60*1000) 
					+ ") group by (" + TextBasedSmsColumns.ADDRESS;
	        
		Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://sms"),
					variables,querystring ,null,"count desc");	      
        if (cursor.moveToFirst()) {
            do {
            	String contact = cursor.getString(1);
            	if(bestie.length()==0) bestie = contact;
            	String name = ContentUtils.getContactDisplayNameByNumber(mContext, contact);
            	if(!name.equals(contact)) {
            		Log.i("bestie", name);
            		bestie = contact;
            		cursor.close();
            		return bestie;
            	}
            } while (cursor.moveToNext());
        }
        cursor.close();
		return bestie;
	}
    
    
}
