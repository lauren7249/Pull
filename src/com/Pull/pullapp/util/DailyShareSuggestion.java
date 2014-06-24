package com.Pull.pullapp.util;

import java.util.Date;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.Pull.pullapp.MainApplication;
import com.Pull.pullapp.MessageActivityCheckboxCursor;
import com.Pull.pullapp.R;
import com.Pull.pullapp.model.ShareSuggestion;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class DailyShareSuggestion extends Thread {
    private Context mContext;
    private  MainApplication mApp;
    private String mUsername;
    public DailyShareSuggestion(Context mContext) {
    	this.mContext = mContext;
    }

    @Override
    public void run() {
        Date midnight = new Date();
        midnight.setHours(0);
        midnight.setMinutes(0);
        midnight.setSeconds(0);

        Date elevenfiftynine = new Date();
        elevenfiftynine.setHours(23);
        elevenfiftynine.setMinutes(59);
        elevenfiftynine.setSeconds(59);

    	final long date = System.currentTimeMillis();
    	final int days_backwards = 7;
    	final int hours_ago = 4;

    	ParseQuery<ShareSuggestion> query = ParseQuery.getQuery("ShareSuggestion");
    	query.whereEqualTo("user", ParseUser.getCurrentUser());
        //query.whereGreaterThan("createdAt", midnight);
        //query.whereLessThan("createdAt", elevenfiftynine);    	
    	query.findInBackground(new FindCallback<ShareSuggestion>() {
    	    public void done(List<ShareSuggestion> list, ParseException e) {
    	        if (e == null && list.size()==0) {
    	        	String best_friend_number = getBestFriendNumber(date, days_backwards);
    	        	String share_convo_number = getNumberToShareFrom(best_friend_number, hours_ago);
    	        	if(best_friend_number !=null && share_convo_number !=null ) {
    	        		notifyShareSuggestion(best_friend_number,share_convo_number);    	        		
    	        	}

    	        } else {
    	        	return;
    	        }
    	    }
    	});    	

      
    }

	private void notifyShareSuggestion(final String best_friend_number,
			final String share_convo_number) {
		final String best_friend_name = ContentUtils.getContactDisplayNameByNumber(mContext, best_friend_number);
		final String shared_from_name = ContentUtils.getContactDisplayNameByNumber(mContext, share_convo_number);
		final String shared_from_thread = ContentUtils.getThreadIDFromNumber(mContext, share_convo_number);
		
		final ShareSuggestion sh = new ShareSuggestion(ParseUser.getCurrentUser(),best_friend_number,best_friend_name,
				share_convo_number, shared_from_name);
		sh.saveInBackground(new SaveCallback(){

			@Override
			public void done(ParseException e) {
				if(e == null) {
					String shID = sh.getObjectId();
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
					ni.putExtra(Constants.EXTRA_SHARE_SUGGESTION_ID,shID);
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
				
			}
			
		});
		


		
	}

	private String getNumberToShareFrom(String best_friend_number, int hours_backwards) {
		String idiot = "";
		
		String[] variables = new String[]{TextBasedSmsColumns.ADDRESS};
		String querystring = TextBasedSmsColumns.TYPE + "=" 
					+ TextBasedSmsColumns.MESSAGE_TYPE_INBOX  
					+ " and " + TextBasedSmsColumns.DATE 
					+ ">=" + (System.currentTimeMillis() - hours_backwards*60*60*1000) 
					+ " and " + TextBasedSmsColumns.ADDRESS + "!='" + best_friend_number + "'";
	        
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
