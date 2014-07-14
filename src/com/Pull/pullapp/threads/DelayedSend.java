package com.Pull.pullapp.threads;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.SendUtils;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DelayedSend extends Thread {

    private Context parent;
    private String message, recipient, threadID;
    private long sendOn, launchedOn;
    
    public DelayedSend(Context parent, String recipient, String message, 
    		Date sendOn, long launchedOn) {
        this.parent = parent;
        this.recipient = recipient;
        this.message = message;
    	Map dimensions = new HashMap();
        if(sendOn!=null){
        	this.sendOn = sendOn.getTime();
        	dimensions.put("delayed", true);
        }else{
        	this.sendOn = System.currentTimeMillis()  + (5)*1000;
        	dimensions.put("delayed", false);
        }
        
        this.launchedOn = launchedOn;
        ParseAnalytics.trackEvent("SMS Sent", dimensions);    
    }
    
    public void setThreadID(String threadID) {
    	this.threadID = threadID;
    }
    @Override
    public void run() {
    	SendUtils.addMessageToOutbox(parent, recipient, message, launchedOn, sendOn);
        AlarmManager am = (AlarmManager) parent.getSystemService(Context.ALARM_SERVICE);   
        Intent intent = new Intent(Constants.ACTION_SEND_DELAYED_TEXT);
        intent.putExtra(Constants.EXTRA_RECIPIENT, recipient);
        intent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
        intent.putExtra(Constants.EXTRA_TIME_LAUNCHED, launchedOn);
        intent.putExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, sendOn);
        PendingIntent sendMessage;
        sendMessage = PendingIntent.getBroadcast(parent, (int)launchedOn, 
        		intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC, sendOn, sendMessage);        
        
    }
    
}
