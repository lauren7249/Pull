package com.Pull.pullapp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony.TextBasedSmsColumns;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.SharedConversation;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ShareTagAction extends Thread {

    private Context parent;
    private String recipient;
	private String person_shared;
	private String text, person_shared_name;
    private SharedConversation mSharedConversation;
	private String hashtags;
    private ArrayList<String> parseMessageIDs;
    private int totalMessageCount, savedMessageCount;
	private String convo_id;
	private boolean isPullUser;
    private TelephonyManager tmgr;
    private int shareType;
    private String app_plug;
    
    public ShareTagAction(Context mContext,
			SharedConversation mSharedConversation, int shareType) {
    	this.shareType = shareType;
    	this.parent = mContext;
    	this.mSharedConversation = mSharedConversation;
    	this.person_shared = ContentUtils.getContactDisplayNameByNumber(parent, 
    			mSharedConversation.getOriginalRecipient()); 
    	this.recipient = mSharedConversation.getConfidante();
    	this.hashtags = mSharedConversation.getHashtags();
    	this.tmgr = (TelephonyManager)parent.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public void run() {
    	
    	mSharedConversation.setType(TextBasedSmsColumns.MESSAGE_TYPE_SENT);
    	//in the background, check if recipient is a parse user. if not, we will send via SMS
    	checkParseUser(mSharedConversation.getConfidante());
        
    	saveToParse();
      
    }
    
    private void saveToParse() {
    	//save convo to parse with associations
    	totalMessageCount = mSharedConversation.getMessages().size();
    	savedMessageCount = 0;
    	parseMessageIDs = new ArrayList<String>();
    	for(final SMSMessage message : mSharedConversation.getMessages()) {
        	//Log.i("is hashtag", " " + message.isHashtag());
    		// This will save both message and conversation to Parse
        	message.saveInBackground(new SaveCallback(){
	        	public void done(ParseException e) {
	        		if (e == null) {
	        			addToSharedList(message);
				    } else {
						Intent broadcastIntent = new Intent();
						broadcastIntent.setAction(Constants.ACTION_SHARE_COMPLETE);
						broadcastIntent.putExtra(Constants.EXTRA_SHARE_RESULT_CODE, e.getCode());
						parent.sendBroadcast(broadcastIntent);	
				    }
				 }
			 });
        }
		
	}

	protected void addToPhoneStorage() {
        //add to phone storage
		//Log.d("number of messages in convo",""+mSharedConversation.getMessages().size());
		DatabaseHandler db = new DatabaseHandler(parent);
		if(!db.contains(mSharedConversation)) {
			db.addSharedConversation(mSharedConversation); 
			
			app_plug = "Hey, check out my conversation with " + person_shared + ". " 
					+ hashtags;
		}
		else {
			db.updateSharedConversation(mSharedConversation); 

			app_plug = "My conversation with " + person_shared + " continued.... " 
					+ hashtags;
		}
		db.close();
		//Log.i("shared messages in db", id  + " ");
		
	}

	protected void addToSharedList(SMSMessage message) {
    	String messageID = message.getObjectId();
    	parseMessageIDs.add(messageID);
    	savedMessageCount++;
    	if(savedMessageCount == totalMessageCount) {
    		finishSharing();
    	}
    	
	}

	private void finishSharing() {
		convo_id = mSharedConversation.getObjectId();
		//Log.i("finished sharing","convo id " + convo_id);
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(Constants.ACTION_SHARE_COMPLETE);
		broadcastIntent.putExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, convo_id);
		parent.sendBroadcast(broadcastIntent);		
		if(isPullUser) shareViaParse();
		addToPhoneStorage();
		
	}

	private void shareViaParse() {
		JSONObject data = new JSONObject();
		try {
			data.put("action", Constants.ACTION_RECEIVE_SHARE_TAG);
			data.put("convoID", convo_id);
			data.put("person_shared", person_shared);
			data.put("type", shareType);
			data.put("message_ids", new JSONArray(mSharedConversation.getMessageIDs()));
			Log.i("","sending message ids " + mSharedConversation.getMessageIDs().toString());
			ParsePush push = new ParsePush();
			push.setChannel(ContentUtils.setChannel(tmgr,recipient));
			push.setData(data);
			push.sendInBackground();				
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void checkParseUser(String confidante) {
    	ParseQuery<ParseUser> query = ParseUser.getQuery();
    	query.whereEqualTo("username", ContentUtils.addCountryCode(tmgr,confidante));
    	query.findInBackground(new FindCallback<ParseUser>() {
    	  public void done(List<ParseUser> objects, ParseException e) {
    	    if (e == null && objects.size()>0) {
    	    	isPullUser = true;
    	    } else {
    	    	isPullUser = false;
    	        shareViaSMS();
    	    }
    	  }
    	});
	}
	private void checkParseInstallation(String confidante) {
    	ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
    	query.whereEqualTo("channels", ContentUtils.setChannel(confidante));
    	query.findInBackground(new FindCallback<ParseInstallation>() {
    	  public void done(List<ParseInstallation> objects, ParseException e) {
    	    if (e == null && objects.size()>0) {
    	    	isPullUser = true;
    	    } else {
    	    	isPullUser = false;
    	        shareViaSMS();
    	    }
    	  }
    	});
	}
	protected void shareViaSMS() {

        AlarmManager am = (AlarmManager) parent.getSystemService(Context.ALARM_SERVICE);   
    	
        setSendAlarm(am, app_plug, 1, System.currentTimeMillis());
        ArrayList<SMSMessage> messages = new ArrayList<SMSMessage>(mSharedConversation.getMessages());

        int n = messages.size();
        for(int i = 0; i<n; i++) {
        	SMSMessage m = messages.get(n - i - 1);
        	try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	if(!m.isHashtag()){
	        	if(m.isSentByMe()) text =  "Me: " + m.getMessage();
	        	else text = person_shared + ": " + m.getMessage();
	        	setSendAlarm(am, text, (int) (System.currentTimeMillis()+(i*2000)), System.currentTimeMillis()+(i*2000));
        	}
    	}       
        n++;
        setSendAlarm(am, Constants.APP_PLUG_END, 
        		(int) (System.currentTimeMillis()+(n*2000)), 
        		System.currentTimeMillis()+(n*2000));
		
	}

	public void setSendAlarm(AlarmManager am, String message, int id, long sendOn){
        Intent intent = new Intent(Constants.ACTION_SHARE_TAG);
        intent.putExtra(Constants.EXTRA_RECIPIENT, recipient);
        intent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
        PendingIntent sendMessage;
        sendMessage = PendingIntent.getBroadcast(parent, id, 
        		intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC, sendOn, sendMessage);         	
    }
    
}
