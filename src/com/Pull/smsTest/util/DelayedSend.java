package com.Pull.smsTest.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.TrafficStats;
import android.util.Log;

public class DelayedSend extends Thread {

    private Context parent;
    private String message;
    private String recipient;
    private long sendOn, launchedOn;
    
    public DelayedSend(Context parent, String recipient, String message, Date sendOn, long launchedOn) {
        this.parent = parent;
        this.recipient = recipient;
        this.message = message;
        this.sendOn = sendOn.getTime();
        this.launchedOn = launchedOn;
    }
    
    @Override
    public void run() {
    	Log.i("Delayed send", "alarm" );
        AlarmManager am = (AlarmManager) parent.getSystemService(Context.ALARM_SERVICE);   
        Intent intent = new Intent(This.ACTION_SEND_DELAYED_TEXT);
        intent.putExtra(This.EXTRA_RECIPIENT, recipient);
        intent.putExtra(This.EXTRA_MESSAGE_BODY, message);
        intent.putExtra(This.EXTRA_TIME_LAUNCHED, launchedOn);
        PendingIntent outSmsLogger;
        outSmsLogger = PendingIntent.getBroadcast(parent, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC, sendOn, outSmsLogger);        
        
    }
    
}
