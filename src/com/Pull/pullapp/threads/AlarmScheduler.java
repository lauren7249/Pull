package com.Pull.pullapp.threads;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.Pull.pullapp.util.Constants;

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
        AlarmManager am;
        Calendar calendar;
        if(Constants.LOG_SMS) {
        	am = (AlarmManager) parent.getSystemService(Context.ALARM_SERVICE);
        	calendar = Calendar.getInstance();
	        PendingIntent outSmsLogger;
	        outSmsLogger = PendingIntent.getBroadcast(parent, 0, 
	        		new Intent(Constants.ACTION_CHECK_OUT_SMS), PendingIntent.FLAG_UPDATE_CURRENT);
	        am.cancel(outSmsLogger);
	        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 10000, outSmsLogger);  
        }
        if(Constants.SHARE_SUGGESTION_BOOLEAN) {
        	Log.i("Alarm", "setting daily share alarm " + Constants.ACTION_DAILY_SHARE_SUGGESTION);
        	am = (AlarmManager) parent.getSystemService(Context.ALARM_SERVICE);
        	calendar = Calendar.getInstance();        	
	        PendingIntent dailySuggestion;
	        dailySuggestion = PendingIntent.getBroadcast(parent, 0, 
	        		new Intent(Constants.ACTION_DAILY_SHARE_SUGGESTION), PendingIntent.FLAG_UPDATE_CURRENT);
	        am.cancel(dailySuggestion);
	        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis()/* + 1000*60*10*/, 24*60*60*1000, dailySuggestion);          	
        }
        
    }
    
}
