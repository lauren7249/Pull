package com.Pull.pullapp.util;

import com.Pull.pullapp.MessageActivityCheckboxCursor;
import com.Pull.pullapp.R;
import com.Pull.pullapp.SharedConversationActivity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

public class DailyShareSuggestion extends Thread {
    private Context mContext;
    public DailyShareSuggestion(Context mContext) {
    	this.mContext = mContext;

    }

    @Override
    public void run() {
    	
    	long date = System.currentTimeMillis();
    	int days_backwards = 7;
    	String best_friend_number = getBestFriendNumber(date, days_backwards);
    	String share_convo_number = getNumberToShareFrom(best_friend_number);
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
		icon = R.drawable.ic_launcher;
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				mContext).setSmallIcon(icon).setContentTitle("Share This!")
				.setContentText("Consider sharing your latest text from " + shared_from_name + " with " 
				+ best_friend_name)
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
		mNotificationManager.notify(777, mBuilder.build());

		
	}

	private String getNumberToShareFrom(String best_friend_number) {
		return "+19173644251";
	}

	private String getBestFriendNumber(long date, int days_backwards) {
		return "+16092402317";
	}
    
 
    
}
