package com.Pull.pullapp.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.Pull.pullapp.R;
import com.Pull.pullapp.SharedListActivity;
import com.Pull.pullapp.model.SharedConversation;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;


public class GeneralBroadcastReceiver extends BroadcastReceiver {

    @SuppressWarnings("unused")
	@Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action.equals(Constants.ACTION_CHECK_OUT_SMS) && Constants.LOG_SMS) {
            new SmsLogger(context).start();
            return;
        }
        
        if (action.equals(Constants.ACTION_SEND_DELAYED_TEXT)) {
            String recipient = intent.getStringExtra(Constants.EXTRA_RECIPIENT);
            String message = intent.getStringExtra(Constants.EXTRA_MESSAGE_BODY);
            Log.i("new message",message);
            long launchedOn = intent.getLongExtra(Constants.EXTRA_TIME_LAUNCHED,0);
            
            //dont send if the user canceled (removed from outbox) or received a message since launching
            if(!messagedAfterLaunch(context,recipient,launchedOn) &&  
            		SendMessages.removeFromOutbox(context, message, recipient, launchedOn, false)>0) {
            	SendMessages.sendsms(context, recipient, message, launchedOn, true);

            }
            	
            return;
        }        
        if (action.equals(Constants.ACTION_SHARE_TAG)) {
            String recipient = intent.getStringExtra(Constants.EXTRA_RECIPIENT);
            String message = intent.getStringExtra(Constants.EXTRA_MESSAGE_BODY);
            SendMessages.sendsms(context, recipient, message, 0, false);
            return;
        }          
        
        if (action.equals(Constants.ACTION_RECEIVE_SHARE_TAG)) {
        	Log.i("received broadcase","ACTION_RECEIVE_SHARE_TAG");
            try {
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                JSONArray messageArray = json.getJSONArray("messageArray"); 
                String convoID = json.getString("convoID");
                int type = json.getInt("type");
                switch(type) {
                case(Constants.NOTIFICATION_NEW_SHARE):
                	ArrayList<String> messages = new ArrayList<String>();     
	                if (messageArray != null) { 
	                   for (int i=0;i<messageArray.length();i++){ 
	                	   messages.add(messageArray.get(i).toString());
	                   } 
	                } 
                	getConvoFromParse(convoID);
                	saveNewShare(context, convoID, messages);
                	notifyNewShare(context, convoID, messages);
                default:
                }
              } catch (JSONException e) {
            	  Log.i("exception",e.getMessage());
              }
            return;
        }   
        
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // avoid starting the alarm scheduler if the app hasn't even been run yet
            SharedPreferences prefs = context.getSharedPreferences(
                    Constants.PREFS, Context.MODE_MULTI_PROCESS);
            if (!prefs.getBoolean(Constants.KEY_FIRST_RUN, true) && Constants.DEBUG == false) {
                new AlarmScheduler(context, false).start();
            }
            return;
        }
        
    }
    
    private void getConvoFromParse(String convoID) {
    	ParseQuery<SharedConversation> convo = ParseQuery.getQuery(SharedConversation.class);
    	convo.whereEqualTo("objectId", convoID);
    	convo.findInBackground(new FindCallback<SharedConversation>() {
    	  public void done(List<SharedConversation> conversations, ParseException exception) {
    		  if(exception == null && conversations.size()==1) {
    			  Log.i("got it","found conversation!");
    			  SharedConversation s = conversations.get(0);
    			  Log.i("messages in comvo",s.getMessages().size() + " messages in convo");
    		  }
    	  }
    	});
		
	}

	private void saveNewShare(Context context, String convoID,
			ArrayList<String> messages) {
		// TODO Auto-generated method stub
		
	}

	private void notifyNewShare(Context context, String convoID, ArrayList<String> messages) {
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon;
		icon = R.drawable.ic_launcher;
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(icon).setContentTitle("name")
				.setContentText("msg")
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setOnlyAlertOnce(true);
		// TODO: Optional light notification.
		Intent ni = new Intent(context, SharedListActivity.class);
		ni.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi = PendingIntent.getActivity(context, 0,
				ni, 0);
		mBuilder.setContentIntent(pi);
		mBuilder.setAutoCancel(true);
		mNotificationManager.notify(777, mBuilder.build());
		
	}

	private boolean messagedAfterLaunch(Context context, String address, long launchTime) {
        Uri SMS_URI = Uri.parse("content://sms/inbox");
        String[] COLUMNS = new String[] {TextBasedSmsColumns.DATE,TextBasedSmsColumns.ADDRESS};
        String WHERE = TextBasedSmsColumns.ADDRESS + "='" + ContentUtils.addCountryCode(address) + "' and " +
        		TextBasedSmsColumns.TYPE + "=" + TextBasedSmsColumns.MESSAGE_TYPE_INBOX; 	
        Cursor cursor = context.getContentResolver().query(SMS_URI, COLUMNS,
                WHERE + " AND " + TextBasedSmsColumns.DATE + "> " + launchTime, null, 
                TextBasedSmsColumns.DATE + " DESC");
        if(cursor.moveToFirst()) {
        	if(Constants.DEBUG){
	        	long date = cursor.getLong(cursor.getColumnIndex(TextBasedSmsColumns.DATE));
	        	String sender = cursor.getString(cursor.getColumnIndex(TextBasedSmsColumns.ADDRESS));
	        	/*Log.i("messagedAfterLaunch", "LAST text was received on " + 
	        			date + " from "+ SendMessages.addCountryCode(sender));
	        	Log.i("messagedAfterLaunch", "delayed send was launched: " + 
	        			launchTime + " to "+ SendMessages.addCountryCode(address));*/
        	}
        	return true;
        }
        return false;
    }
    
}
