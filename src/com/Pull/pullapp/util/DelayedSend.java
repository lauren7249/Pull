package com.Pull.pullapp.util;

import java.util.Date;

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
        this.sendOn = sendOn.getTime();
        this.launchedOn = launchedOn;
    }
    
    public void setThreadID(String threadID) {
    	this.threadID = threadID;
    }
    @Override
    public void run() {
    	Log.i("Delayed send", "alarm" );
    	sendSMS.addMessageToOutbox(parent, recipient, message, launchedOn, sendOn);
        AlarmManager am = (AlarmManager) parent.getSystemService(Context.ALARM_SERVICE);   
        Intent intent = new Intent(Constants.ACTION_SEND_DELAYED_TEXT);
        intent.putExtra(Constants.EXTRA_RECIPIENT, recipient);
        intent.putExtra(Constants.EXTRA_MESSAGE_BODY, message);
        intent.putExtra(Constants.EXTRA_TIME_LAUNCHED, launchedOn);
        PendingIntent sendMessage;
        sendMessage = PendingIntent.getBroadcast(parent, (int)launchedOn, 
        		intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC, sendOn, sendMessage);        
        
    }
    
}
