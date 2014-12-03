package com.Pull.pullapp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
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
import android.telephony.TelephonyManager;
import android.util.Log;

import com.Pull.pullapp.AllThreadsListActivity;
import com.Pull.pullapp.FriendsActivity;
import com.Pull.pullapp.MainApplication;
import com.Pull.pullapp.MessageActivityCheckboxCursor;
import com.Pull.pullapp.R;
import com.Pull.pullapp.model.MMSMessage;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.threads.AlarmScheduler;
import com.Pull.pullapp.threads.DelayedMMSService;
import com.Pull.pullapp.threads.DownloadFriendPhoto;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class GeneralBroadcastReceiver extends BroadcastReceiver {
	private Context mContext;
	private TelephonyManager tmgr;
	private DatabaseHandler db;
	private UserInfoStore store;
    @SuppressWarnings("unused")
	@Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mContext = context;
        store = new UserInfoStore(context);
        db = new DatabaseHandler(context);
        tmgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (action.equals(Constants.ACTION_INVITE_FRIEND)) {
            try {
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                String userid = json.getString("userid");
                String number = json.getString("number");
                String name = store.getName(number);
                //TODO: TEST
                if(!store.isFriend(number) && !store.wasInvited(number)) notifyInvited(userid, name, number);
                else {
                	if(!store.isFriend(number)) {
 	            	   store.saveFriend(number, userid);
 	            	   if(store.getFriendBitmap(number) == null)
 	            		   new DownloadFriendPhoto(userid, mContext, number, store).start();                		
                	}
	            	SendUtils.confirmFriend(number, userid);             	
                }
            } catch (JSONException e) {
          	  Log.i("exception",e.getMessage());
            }
            return;
        }     
        
        if (action.equals(Constants.ACTION_CONFIRM_FRIEND)) {
            try {
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                final String sender_userid = json.getString("sender_userid");
                String receiver_userid = json.getString("receiver_userid");
                final String number = json.getString("number");
                if(receiver_userid.equals(ParseUser.getCurrentUser().getObjectId()) 
                		&& store.wasInvited(number)) {
            		ParseUser me = ParseUser.getCurrentUser();
            		ParseACL acl = new ParseACL();
            		acl.setReadAccess(sender_userid, true);
            		me.setACL(acl);
            		me.saveInBackground(new SaveCallback(){
						@Override
						public void done(ParseException e) {
		                	if(e==null && store.getFriendBitmap(number) == null){
		                		store.saveFriend(number, sender_userid);
		                		new DownloadFriendPhoto(sender_userid, mContext, number, store).start();
		                	}
						}
            			
            		});                	
            		String name = store.getName(number);
                	notifyFriendConfirmed(name);
                }
                
                
            } catch (JSONException e) {
          	  Log.i("exception",e.getMessage());
            }
            return;
        }              
        if (action.equals(Constants.ACTION_SEND_DELAYED_TEXT)) {
            String recipient = intent.getStringExtra(Constants.EXTRA_RECIPIENT);
            String[] recipients = intent.getStringArrayExtra(Constants.EXTRA_RECIPIENTS);
            String message = intent.getStringExtra(Constants.EXTRA_MESSAGE_BODY);
            String thread_id = intent.getStringExtra(Constants.EXTRA_THREAD_ID);
            String approver = intent.getStringExtra(Constants.EXTRA_APPROVER);
            boolean isDelayed = intent.getBooleanExtra(Constants.EXTRA_IS_DELAYED, false);
            ArrayList<String> attachment_paths = intent.getStringArrayListExtra(Constants.EXTRA_ATTACHMENT_PATHS);
            // Log.i("new message",message);
            long launchedOn = intent.getLongExtra(Constants.EXTRA_TIME_LAUNCHED,0);
            long scheduledFor = intent.getLongExtra(Constants.EXTRA_TIME_SCHEDULED_FOR,0);
            SMSMessage m;
            if(recipient!=null) {
				m = new SMSMessage(launchedOn, 
						message, recipient, 
						store.getName(recipient), TextBasedSmsColumns.MESSAGE_TYPE_SENT, store, 
						ParseUser.getCurrentUser().getUsername());		
				m.schedule(scheduledFor);
				m.isDelayed = isDelayed;
				m.launchedOn = launchedOn;
				//dont send if the user canceled (removed from outbox) or received a message since launching
	            if(!messagedAfterLaunch(context,recipient,launchedOn) &&  
	            		SendUtils.removeFromOutbox(context, message, recipient, launchedOn, 
	            				scheduledFor, false, approver)>0
	            		&& !disapproved(recipient,message,scheduledFor,approver, mContext)) 
	            {
	            	SendUtils.sendsms(context, recipient, message, launchedOn, scheduledFor, true);
					m.setType(TextBasedSmsColumns.MESSAGE_TYPE_SENT);      	
	            }
	            else {
	            	intent.setAction(Constants.ACTION_SMS_UNOUTBOXED);
	                mContext.sendBroadcast(intent);	     
	                m.setType(TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX);                      
	            }
            } else {
				m = new MMSMessage(launchedOn, 
						message, recipients, 
						store.getNames(recipients), TextBasedSmsColumns.MESSAGE_TYPE_SENT, store, 
						ParseUser.getCurrentUser().getUsername());		
				m.schedule(scheduledFor);
				m.isDelayed = isDelayed;
				m.launchedOn = launchedOn;
				//dont send if the user canceled (removed from outbox) or received a message since launching
	            if(!messagedAfterLaunch(context,recipients,launchedOn) &&  
	            		SendUtils.removeFromOutbox(context, message, recipients, launchedOn, 
	            				scheduledFor, false, approver)>0
	            		&& !disapproved(recipients,message,scheduledFor,approver, mContext)) 
	            {
	            	Intent serviceIntent = new Intent(mContext,DelayedMMSService.class);
	            	serviceIntent.putExtra(Constants.EXTRA_RECIPIENTS, recipients);
	            	serviceIntent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
	            	serviceIntent.putExtra(Constants.EXTRA_THREAD_ID, thread_id);
	            	serviceIntent.putExtra(Constants.EXTRA_TIME_LAUNCHED, launchedOn);    
					serviceIntent.putExtra(Constants.EXTRA_ATTACHMENT_PATHS, attachment_paths);         
	            	mContext.startService(serviceIntent);	            
	            	//Log.i("launching delayed mmsservice","launching delayed mmsservice");
	            	//new SendMMS(context, recipients, message, launchedOn, scheduledFor, true).run();
					m.setType(TextBasedSmsColumns.MESSAGE_TYPE_SENT);      	
	            }
	            else {
	            	intent.setAction(Constants.ACTION_SMS_UNOUTBOXED);
	                mContext.sendBroadcast(intent);	     
	                m.setType(TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX);                      
	            }         	
            }
			try {
				m.saveToParse();
				Log.i("saving to parse",m.getType() + "type");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}               	
            return;
        }        
        if (action.equals(Constants.ACTION_SHARE_TAG)) {
            String recipient = intent.getStringExtra(Constants.EXTRA_RECIPIENT);
            String message = intent.getStringExtra(Constants.EXTRA_MESSAGE_BODY);
            SendUtils.sendsms(context, recipient, message, 0, 0, false);
            return;
        }          
         
        
        //CURRENT METHOD FOR RECEIVING EVERYTHING. COMES FROM CLOUD PUSH NOTIFICATION
        if (action.equals(Constants.ACTION_RECEIVE_SHARED_MESSAGES)) {
        	
        	Log.i("received broadcast","ACTION_RECEIVE_SHARED_MESSAGES");
            try {
            	
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                Log.i("json",json.toString());
                final String sender = ContentUtils.addCountryCode(json.getString("from"));
                final String owner = ContentUtils.addCountryCode(json.getString("owner"));
                final String person_shared = json.getString("person_shared");
                final String to = json.getString("to");
                final String address = ContentUtils.addCountryCode(json.getString("address"));
                final int messageType = json.getInt("messageType");

                final int convoType;
                final String confidante;
                if(owner.equals(ParseUser.getCurrentUser().getUsername())) {
                		convoType = TextBasedSmsColumns.MESSAGE_TYPE_SENT;
                		confidante = sender;
                }
                else {
                		convoType = TextBasedSmsColumns.MESSAGE_TYPE_INBOX;
                		confidante = to;
                }
            	ParseQuery<SMSMessage> query = ParseQuery.getQuery("SMSMessage");
            	query.whereEqualTo("owner", owner);  
            	query.whereEqualTo("address", address); //number of other person
            	query.findInBackground(new FindCallback<SMSMessage>(){
					@Override
					public void done(List<SMSMessage> objects, ParseException e) {
						if(e==null && objects.size()>0) {
							notifySharedMessages(sender,owner, to, confidante,
									person_shared,address, objects, convoType, messageType);
						} else {

						}
						
					}
            		
            	});
                
                
              } catch (JSONException e) {
            	  Log.i("exception",e.getMessage());
              }
            return;
        }           

        
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, MainApplication.class);
            context.startService(serviceIntent);        	
            // avoid starting the alarm scheduler if the app hasn't even been run yet
            SharedPreferences prefs = context.getSharedPreferences(
                    Constants.PREFS, Context.MODE_MULTI_PROCESS);
            if (!prefs.getBoolean(Constants.KEY_FIRST_RUN, true) && Constants.DEBUG == false) {
                new AlarmScheduler(context, false).start();
            }
            return;
        }
        
    }
   

	private boolean disapproved(String[] recipients, String message,
			long scheduledFor, String approver, Context context) {
		if(approver==null || approver.length()==0) return false;
		//Log.i("approver",approver);
		//UserInfoStore store = new UserInfoStore(context);
		//if(!store.wasApproved(recipients,message,scheduledFor,approver)) return true;
		return false;
	}


	private boolean messagedAfterLaunch(Context context, String[] recipients,
			long launchTime) {
		//TODO: FIX THIS
       /* Uri SMS_URI = Uri.parse("content://sms/inbox");
        String[] COLUMNS = new String[] {TextBasedSmsColumns.DATE,TextBasedSmsColumns.ADDRESS};
        String WHERE = TextBasedSmsColumns.ADDRESS + "='" + ContentUtils.addCountryCode(tmgr,address) + "' and " +
        		TextBasedSmsColumns.TYPE + "=" + TextBasedSmsColumns.MESSAGE_TYPE_INBOX; 	
        Cursor cursor = context.getContentResolver().query(SMS_URI, COLUMNS,
                WHERE + " AND " + TextBasedSmsColumns.DATE + "> " + launchTime, null, 
                TextBasedSmsColumns.DATE + " DESC");
        if(cursor.moveToFirst()) {
        	if(Constants.DEBUG){
	        	long date = cursor.getLong(cursor.getColumnIndex(TextBasedSmsColumns.DATE));
	        	String sender = cursor.getString(cursor.getColumnIndex(TextBasedSmsColumns.ADDRESS));
	        	Log.i("messagedAfterLaunch", "LAST text was received on " + 
	        			date + " from "+ ContentUtils.addCountryCode(sender));
	        	Log.i("messagedAfterLaunch", "delayed send was launched: " + 
	        			launchTime + " to "+ ContentUtils.addCountryCode(address));
        	}
        	return true;
        }*/
        return false;
	}


	private boolean disapproved(String recipient, String message,
			long scheduledFor, String approver, Context context) {
		if(approver==null || approver.length()==0) return false;
		Log.i("approver",approver);
		UserInfoStore store = new UserInfoStore(context);
		if(!store.wasApproved(recipient,message,scheduledFor,approver)) return true;
		return false;
	}


	private void notifyFriendConfirmed(String name) {
		NotificationManager mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon;
		icon = R.drawable.explosion;
		String title = name + " is now your friend ";
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				mContext).setSmallIcon(icon).setContentTitle(title)
				.setContentText("Maybe share something?")
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setOnlyAlertOnce(true);
		// TODO: Optional light notification.
		Intent ni = new Intent(mContext, AllThreadsListActivity.class);
		ni.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);		
		PendingIntent pi = PendingIntent.getActivity(mContext, 0,
				ni, PendingIntent.FLAG_CANCEL_CURRENT);
		mBuilder.setContentIntent(pi);
		mBuilder.setAutoCancel(true);
		Notification notification = mBuilder.build();
		notification.defaults|= Notification.DEFAULT_SOUND;
		notification.defaults|= Notification.DEFAULT_LIGHTS;
		notification.defaults|= Notification.DEFAULT_VIBRATE;		
		mNotificationManager.notify(title.hashCode(), notification);
	}


	private void notifyInvited(String userid, String name, String number) {
		NotificationManager mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon;
		String title = name + " friend requested you ";
		icon = R.drawable.explosion;
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				mContext).setSmallIcon(icon).setContentTitle(title)
				.setContentText("Will you accept?")
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setOnlyAlertOnce(true);
		// TODO: Optional light notification.
		Intent ni = new Intent(mContext, FriendsActivity.class);
		ni.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);		
		ni.putExtra(Constants.EXTRA_USER_ID, userid);
		ni.putExtra(Constants.EXTRA_NUMBER, number);
		ni.putExtra(Constants.EXTRA_NAME, name);
		PendingIntent pi = PendingIntent.getActivity(mContext, 0,
				ni, PendingIntent.FLAG_CANCEL_CURRENT);
		mBuilder.setContentIntent(pi);
		mBuilder.setAutoCancel(true);
		Notification notification = mBuilder.build();
		notification.defaults|= Notification.DEFAULT_SOUND;
		notification.defaults|= Notification.DEFAULT_LIGHTS;
		notification.defaults|= Notification.DEFAULT_VIBRATE;		
		mNotificationManager.notify((title+number).hashCode(), notification);
		
	}


	private void notifySharedMessages(String sender, String owner, String to, String confidante,
			String person_shared, String address, List<SMSMessage> objects, int convoType, int messageType) {
		String conversant;
		if(convoType==TextBasedSmsColumns.MESSAGE_TYPE_INBOX) 
			conversant = owner;
		else 
			conversant = confidante;
		String convoID = owner+address+conversant;
		Log.i("convoid from broadcastreceiver", convoID);
		db = new DatabaseHandler(mContext);
		  
		for(SMSMessage m: new TreeSet<SMSMessage>(objects)) {
			if(m.getType()==Constants.MESSAGE_TYPE_SENT_COMMENT)
				m.setType(Constants.MESSAGE_TYPE_RECEIVED_COMMENT);
			db.addSharedMessage(convoID, m, convoType);
		}
		String from = store.getName(sender);
		db.addSharedConversation(convoID, confidante, person_shared,
					address, owner, convoType);	
		if(!to.equals(ParseUser.getCurrentUser().getUsername())) return;

		Intent ni = new Intent(mContext, MessageActivityCheckboxCursor.class);
		ni.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);		
		ni.putExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, convoID);
		ni.putExtra(Constants.EXTRA_CLUELESS_PERSONS_NUMBER, address);
		ni.putExtra(Constants.EXTRA_CLUELESS_PERSONS_NAME, person_shared);
		ni.putExtra(Constants.EXTRA_SHARED_SENDER, owner);
		ni.putExtra(Constants.EXTRA_MESSAGE_TYPE, messageType);
		ni.putExtra(Constants.EXTRA_SHARED_CONFIDANTE, confidante);
		ni.putExtra(Constants.EXTRA_SHARED_CONVO_TYPE, convoType);
		PendingIntent pi;
		pi = PendingIntent.getActivity(mContext, 0, ni, PendingIntent.FLAG_CANCEL_CURRENT);		
		//ignore this message type -- not yet implemented
		 if(messageType==4) { 
			 sendNotification(from + " needs approval!", "on a message to " + person_shared, pi);
		 }				 
		 else if(messageType==Constants.MESSAGE_TYPE_RECEIVED_COMMENT 
				 || messageType==Constants.MESSAGE_TYPE_SENT_COMMENT){
			 sendNotification("Comment from " + from, "about " + person_shared, pi);
		 } 
		 else {
			 sendNotification(from + "'s messages", "with " + person_shared, pi);
		 }

		 db.close();
	}

	private void sendNotification(CharSequence title, CharSequence content, PendingIntent pendingIntent) {

		NotificationManager mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon;
		icon = R.drawable.explosion;
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				mContext).setSmallIcon(icon).setContentTitle(title)
				.setContentText(content)
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setOnlyAlertOnce(true);
		// TODO: Optional light notification.
		mBuilder.setContentIntent(pendingIntent);
		mBuilder.setAutoCancel(true);
		Notification notification = mBuilder.build();
		notification.sound = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.jackie_sound_1);
		notification.defaults|= Notification.DEFAULT_LIGHTS;
		notification.defaults|= Notification.DEFAULT_VIBRATE;		
		mNotificationManager.notify(title.hashCode()+content.hashCode(), notification);		
	}

	private ArrayList<String> convertJSON(JSONArray messageArray) {
    	ArrayList<String> messages = new ArrayList<String>();     
        if (messageArray != null) { 
           for (int i=0;i<messageArray.length();i++){ 
        	   try {
				messages.add(messageArray.get(i).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           } 
        } 
        return messages;
	}
    
	private void getExistingConvoFromParse(JSONArray sms_ids, final String convoID) {
		Log.i("","existing conversation ids to search " + sms_ids.toString());
    	ParseQuery<SMSMessage> messages = ParseQuery.getQuery(SMSMessage.class);
    	messages.whereContainedIn("objectId", convertJSON(sms_ids));
    	messages.findInBackground(new FindCallback<SMSMessage>() {
    	  public void done(List<SMSMessage> message_list, ParseException exception) {
    		  if(exception == null && message_list.size()>0) {
    			  Log.i("got it","found messages from existing conversation " + message_list.size());
    			  db = new DatabaseHandler(mContext);
    			  db.addSharedMessages(convoID, message_list, TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
    			  db.close();
    		  }
    	  }
    	});
		
	}


	private boolean messagedAfterLaunch(Context context, String address, long launchTime) {
        Uri SMS_URI = Uri.parse("content://sms/inbox");
        String[] COLUMNS = new String[] {TextBasedSmsColumns.DATE,TextBasedSmsColumns.ADDRESS};
        String WHERE = TextBasedSmsColumns.ADDRESS + "='" + ContentUtils.addCountryCode(tmgr,address) + "' and " +
        		TextBasedSmsColumns.TYPE + "=" + TextBasedSmsColumns.MESSAGE_TYPE_INBOX; 	
        Cursor cursor = context.getContentResolver().query(SMS_URI, COLUMNS,
                WHERE + " AND " + TextBasedSmsColumns.DATE + "> " + launchTime, null, 
                TextBasedSmsColumns.DATE + " DESC");
        if(cursor.moveToFirst()) {
        	if(Constants.DEBUG){
	        	long date = cursor.getLong(cursor.getColumnIndex(TextBasedSmsColumns.DATE));
	        	String sender = cursor.getString(cursor.getColumnIndex(TextBasedSmsColumns.ADDRESS));
	        	Log.i("messagedAfterLaunch", "LAST text was received on " + 
	        			date + " from "+ ContentUtils.addCountryCode(sender));
	        	Log.i("messagedAfterLaunch", "delayed send was launched: " + 
	        			launchTime + " to "+ ContentUtils.addCountryCode(address));
        	}
        	return true;
        }
        return false;
    }
    
}
