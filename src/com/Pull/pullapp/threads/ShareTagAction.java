package com.Pull.pullapp.threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony.TextBasedSmsColumns;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.Pull.pullapp.util.SendUtils;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
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
    	this.tmgr = (TelephonyManager)parent.getSystemService(Context.TELEPHONY_SERVICE);
    	Map dimensions = new HashMap(); 	
    	ParseAnalytics.trackEvent("Shared Conversation", dimensions);
    }

    @Override
    public void run() {
    	
    	mSharedConversation.setType(TextBasedSmsColumns.MESSAGE_TYPE_SENT);
    	//in the background, check if recipient is a parse user. if not, we will send via SMS
    	checkParseInstallation(mSharedConversation.getConfidante());
        
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
		DatabaseHandler db = new DatabaseHandler(parent);
		if(!db.contains(mSharedConversation) ) {
			db.addSharedConversation(mSharedConversation); 
		}
		else {
			db.updateSharedConversation(mSharedConversation); 
		}
		db.close();
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
    	        SendUtils.shareViaSMS(parent, person_shared, mSharedConversation.getConfidante(),
    	        		mSharedConversation.getMessages());		
    	    }
    	  }
    	});
	}
	public void checkParseInstallation(String confidante) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Channels");
		query.whereEqualTo("channel", ContentUtils.setChannel(confidante));
		query.findInBackground(new FindCallback<ParseObject>() {
		    public void done(List<ParseObject> list, ParseException e) {
		        if (e==null && list.size()>0) {
		        	isPullUser = true;
		        } else {
	    	    	isPullUser = false;
	    	        SendUtils.shareViaSMS(parent, person_shared, mSharedConversation.getConfidante(),
	    	        		mSharedConversation.getMessages());		        	
		        }
		    }
		});	
	}


    
}
