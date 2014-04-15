package com.Pull.pullapp.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.SharedConversation;

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
    	
		DatabaseHandler db = new DatabaseHandler(parent);
		int id = db.addSharedConversation(mSharedConversation); 
		db.close();
		Log.i("shared messages in db", id  + " ");
		String app_plug = "Hey, check out my conversation with " + person_shared + ". " 
				+ hashtags;
        AlarmManager am = (AlarmManager) parent.getSystemService(Context.ALARM_SERVICE);   
    	
        setSendAlarm(am, app_plug, 1, System.currentTimeMillis());
        int i=0;
        for(SMSMessage m : mSharedConversation.getMessages()) {
        	i++;
        	try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	if(m.getHashtagID()!=-1){
	        	if(m.sentByMe) text =  "Me: " + m.getMessage();
	        	else text = person_shared + ": " + m.getMessage();
	        	setSendAlarm(am, text, (int) (System.currentTimeMillis()+(i*1000)), System.currentTimeMillis()+(i*1000));
        	}
    	}       
        i++;
        setSendAlarm(am, Constants.APP_PLUG_END, (int) (System.currentTimeMillis()+(i*1000)), System.currentTimeMillis()+(i*1000));
      
        
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
