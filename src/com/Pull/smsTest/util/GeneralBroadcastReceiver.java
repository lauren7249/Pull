package com.Pull.smsTest.util;

import java.util.ArrayList;
import java.util.Set;

import com.Pull.smsTest.model.SMSMessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


public class GeneralBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action.equals(This.ACTION_CHECK_OUT_SMS) && This.DEBUG == false) {
            new SmsLogger(context).start();
            return;
        }
        
        if (action.equals(This.ACTION_SEND_DELAYED_TEXT)) {
            String recipient = intent.getStringExtra(This.EXTRA_RECIPIENT);
            String message = intent.getStringExtra(This.EXTRA_MESSAGE_BODY);
            long launchedOn = intent.getLongExtra(This.EXTRA_TIME_LAUNCHED,0);
            if(!messagedAfterLaunch(context,recipient,launchedOn))
            	sendSMS.sendsms(context, recipient, message, true);
            return;
        }        
        
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // avoid starting the alarm scheduler if the app hasn't even been run yet
            SharedPreferences prefs = context.getSharedPreferences(
                    This.PREFS, Context.MODE_MULTI_PROCESS);
            if (!prefs.getBoolean(This.KEY_FIRST_RUN, true) && This.DEBUG == false) {
                new AlarmScheduler(context, false).start();
            }
            return;
        }
        
    }
    
    private boolean messagedAfterLaunch(Context context, String address, long launchTime) {
        Uri SMS_URI = Uri.parse("content://sms/inbox");
        String[] COLUMNS = new String[] {"date","address"};
        String WHERE = "address='" + address + "'"; 	
        Cursor cursor = context.getContentResolver().query(SMS_URI, COLUMNS,
                WHERE + " AND date > " + launchTime, null, "DATE DESC");
        if(cursor.moveToFirst()) {
        	if(This.DEBUG){
	        	long date = cursor.getLong(cursor.getColumnIndex("date"));
	        	String sender = cursor.getString(cursor.getColumnIndex("address"));
	        	Log.i("messagedAfterLaunch", "LAST text was received on " + 
	        			date + " from "+ subtractCountryCode(sender));
	        	Log.i("messagedAfterLaunch", "delayed send was launched: " + 
	        			launchTime + " to "+subtractCountryCode(address));
        	}
        	return true;
        }
        return false;
    }
    
    private String subtractCountryCode(String number) {
    	if(number.trim().length()<=10) return number;
    	return number.substring(number.length()-10);
    }
}
