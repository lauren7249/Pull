package com.Pull.pullapp.util;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.SharedConversation;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ShareTagAction extends Thread {

    private Context parent;
    private String recipient;
	private String person_shared;
	private String text;
    private SharedConversation mSharedConversation;
	private String hashtags;
    
    public ShareTagAction(Context mContext,
			SharedConversation mSharedConversation) {
    	this.parent = mContext;
    	this.mSharedConversation = mSharedConversation;
    	this.person_shared = ContentUtils.getContactDisplayNameByNumber(parent, 
    			mSharedConversation.getOriginalRecipient()); 
    	this.recipient = mSharedConversation.getConfidante();
    	this.hashtags = mSharedConversation.getHashtags();
    }

    @Override
    public void run() {
    	
    	checkParseUser(mSharedConversation.getConfidante());
        for(SMSMessage message : mSharedConversation.getMessages()) {
        	message.put("parent", mSharedConversation);
    		// This will save both message and conversation to Parse
        	message.saveInBackground();
        }
        SendMessages.sendMessagetoNumber(mSharedConversation.getConfidante(),"shared a convo");

    	
		DatabaseHandler db = new DatabaseHandler(parent);
		int id = db.addSharedConversation(mSharedConversation); 
		db.close();
		Log.i("shared messages in db", id  + " ");

      
    }
    
    private void checkParseUser(String confidante) {
    	ParseQuery<ParseUser> query = ParseUser.getQuery();
    	query.whereEqualTo("username", ContentUtils.addCountryCode(confidante));
    	query.findInBackground(new FindCallback<ParseUser>() {
    	  public void done(List<ParseUser> objects, ParseException e) {
    	    if (e == null) {
    	    } else {
    	        shareViaSMS();
    	    }
    	  }
    	});
	}

	protected void shareViaSMS() {
		String app_plug = "Hey, check out my conversation with " + person_shared + ". " 
				+ hashtags;
        AlarmManager am = (AlarmManager) parent.getSystemService(Context.ALARM_SERVICE);   
    	
        setSendAlarm(am, app_plug, 1, System.currentTimeMillis());
        int i=0;
        for(SMSMessage m : mSharedConversation.getMessages()) {
        	i++;
        	try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	if(!m.isHashtag()){
	        	if(m.sentByMe) text =  "Me: " + m.getMessage();
	        	else text = person_shared + ": " + m.getMessage();
	        	setSendAlarm(am, text, (int) (System.currentTimeMillis()+(i*2000)), System.currentTimeMillis()+(i*2000));
        	}
    	}       
        i++;
        setSendAlarm(am, Constants.APP_PLUG_END, (int) (System.currentTimeMillis()+(i*2000)), System.currentTimeMillis()+(i*2000));
		
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
