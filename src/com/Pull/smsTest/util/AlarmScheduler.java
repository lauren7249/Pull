package com.Pull.smsTest.util;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.TrafficStats;
import android.util.Log;

public class AlarmScheduler extends Thread {
    
    private static final long A = 1L;
    private static final long B = 1L;
    private static final long C = 1L;
    
    private Context parent;
    private boolean firstRun;
    
    public AlarmScheduler(Context parent, boolean firstRun) {
        this.parent = parent;
        this.firstRun = firstRun;
    }
    
    @Override
    public void run() {
    	Log.i("Alarm", "alarm" );
        AlarmManager am = (AlarmManager) parent.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 1);
        if(Constants.LOG_SMS) {
	        PendingIntent outSmsLogger;
	        outSmsLogger = PendingIntent.getBroadcast(parent, 0, 
	        		new Intent(Constants.ACTION_CHECK_OUT_SMS), PendingIntent.FLAG_UPDATE_CURRENT);
	        am.cancel(outSmsLogger);
	        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 10000, outSmsLogger);  
        }
        
    }
    
}
