package com.Pull.pullapp.threads;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.SendUtils;
import com.parse.ParseAnalytics;

public class DelayedSend extends Thread {

    private Context parent;
    private String message, recipient, threadID;
    private String[] recipients;
    private long sendOn, launchedOn;
	private String approver;
	private boolean isDelayed;
	private ArrayList<String> attachment_paths;
    
    public DelayedSend(Context parent, String recipient, String message, 
    		Date sendOn, long launchedOn, String approver) {
        this.parent = parent;
        this.recipient = recipient;
        this.message = message;
    	Map dimensions = new HashMap();
        if(sendOn!=null){
        	this.sendOn = sendOn.getTime();
        	this.isDelayed = true;
        	dimensions.put("delayed", true);
        }else{
        	this.sendOn = new Date().getTime() + (6)*1000;
        	this.isDelayed = false;
        	dimensions.put("delayed", false);
        }
        
        this.launchedOn = launchedOn;
        this.approver = approver;
        ParseAnalytics.trackEvent("SMS Sent", dimensions);    
    }
    
    public DelayedSend(Context parent, String[] recipients,
			String message, Date sendOn, long launchedOn, String approver, ArrayList<String> attachments) {
        this.parent = parent;
        this.recipients = recipients;
        this.message = message;
        this.attachment_paths = attachments;
    	Map dimensions = new HashMap();
        if(sendOn!=null){
        	this.sendOn = sendOn.getTime();
        	this.isDelayed = true;
        	dimensions.put("delayed", true);
        }else{
        	this.sendOn = new Date().getTime() + (6)*1000;
        	this.isDelayed = false;
        	dimensions.put("delayed", false);
        }
        
        this.launchedOn = launchedOn;
        this.approver = approver;
        ParseAnalytics.trackEvent("GroupmessageSent", dimensions);  
	}

	public void setThreadID(String threadID) {
    	this.threadID = threadID;
    }
    @Override
    public void run() {
    	
        AlarmManager am = (AlarmManager) parent.getSystemService(Context.ALARM_SERVICE);   
        Intent intent;
        if(recipient!=null) {
        	SendUtils.addMessageToOutbox(parent, recipient, message, launchedOn, sendOn, approver);
        	intent  = new Intent(Constants.ACTION_SEND_DELAYED_TEXT);
        	intent.putExtra(Constants.EXTRA_RECIPIENT, recipient);
        }
        else {
        	SendUtils.addMessageToOutbox(parent, recipients, message, launchedOn, sendOn, 
        			approver, threadID, attachment_paths);
        	intent  = new Intent(Constants.ACTION_SEND_DELAYED_TEXT);
        	intent.putExtra(Constants.EXTRA_RECIPIENTS, recipients);  
        	intent.putExtra(Constants.EXTRA_THREAD_ID, threadID);  
        	intent.putExtra(Constants.EXTRA_ATTACHMENT_PATHS, attachment_paths);  
        	Log.i("delayed send running for mms","attachments " + attachment_paths.size());
        }
        intent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
        intent.putExtra(Constants.EXTRA_TIME_LAUNCHED, launchedOn);
        intent.putExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, sendOn);
        intent.putExtra(Constants.EXTRA_IS_DELAYED, isDelayed);
        intent.putExtra(Constants.EXTRA_APPROVER, approver);
        PendingIntent sendMessage;
        sendMessage = PendingIntent.getBroadcast(parent, (int)launchedOn, 
        		intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC, sendOn, sendMessage);        
        
    }

	public void addPhotos(ArrayList<Uri> picture_uris) {
		// TODO Auto-generated method stub
		
	}
    
}
