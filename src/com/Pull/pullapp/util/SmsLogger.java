package com.Pull.pullapp.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.Pull.pullapp.R;
import com.Pull.pullapp.model.SMSMessage;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.model.*;
import com.amazonaws.services.s3.AmazonS3Client;
public class SmsLogger extends Thread {
    
    private static final Uri SMS_URI = Uri.parse("content://sms");
    private static final String[] COLUMNS = new String[] {"_id","date", "address", "body", "type"};
    private static final String WHERE = "type = 2 or type = 1";
    private static final String ORDER = "date DESC";
    
    protected AmazonSimpleDBClient sdbClient;
    private Context context;
    private SharedPreferences prefs;
    private TelephonyManager tm;
    private long timeLastChecked;
    private boolean connected, roaming;
    private String uuid;
    
    public SmsLogger(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_MULTI_PROCESS);
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        roaming = prefs.getBoolean(Constants.KEY_ROAMING_STATE, tm.isNetworkRoaming());
		
        AWSCredentials credentials = new BasicAWSCredentials( 
        		context.getString(R.string.aws_access_key_id), 
        		context.getString(R.string.aws_secret_key) );
        this.sdbClient = new AmazonSimpleDBClient( credentials);  
        
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        connected = (tm.getDataState() == TelephonyManager.DATA_CONNECTED); 
    }
    
    public SmsLogger(Context context, boolean roamingStateToUse) {
        this(context);
        roaming = roamingStateToUse;
    }
    
    @Override
    public void run() {
    	if (!connected) return;
    	Log.i("Outlogger", "starting" );
        timeLastChecked = prefs.getLong(Constants.KEY_TIME_LAST_CHECKED_OUT_SMS, Constants.DEFAULT_LONG);
        uuid = prefs.getString(Constants.KEY_UUID, Constants.NULL);
        
        Cursor cursor = context.getContentResolver().query(SMS_URI, COLUMNS,
                WHERE + " AND date > " + timeLastChecked, null, ORDER);
        String address, body, simNumber, id, type;
        long date;
        SMSMessage smsLog;
        Set<SMSMessage> smsLogs = null;
        if (cursor.moveToNext()) {
            smsLogs = new HashSet<SMSMessage>();
            //date of the last message logged
            
            simNumber = tm.getLine1Number();
            do {
            	id = cursor.getString(cursor.getColumnIndex("_id"));
                date = cursor.getLong(cursor.getColumnIndex("date"));
                address = cursor.getString(cursor.getColumnIndex("address"));
                body = cursor.getString(cursor.getColumnIndex("body"));
                type = cursor.getString(cursor.getColumnIndex("type"));
                if (type.equals("2")) {
                    smsLog = new SMSMessage(Integer.parseInt(id), date, simNumber, address, body);
                    smsLog.sentByMe = true;                	
                } else {
                    smsLog = new SMSMessage(Integer.parseInt(id), date, address, simNumber, body);
                    smsLog.sentByMe = false;                  	
                }
 
                if (smsLogs.contains(smsLog)) {
                    continue;
                }
                
                smsLogs.add(smsLog);
            } while (cursor.moveToNext());
        }
        
        if (smsLogs == null) {
        } else {
            List<SMSMessage> smsLogsList = new ArrayList<SMSMessage>();
            smsLogsList.addAll(smsLogs);
            for(SMSMessage s : smsLogsList) {
            	if(tm.getDataState() == TelephonyManager.DATA_CONNECTED) addMessage(s);
            	else break;
            }
        }
        
        cursor.close();
        Editor editor = prefs.edit();
        editor.putLong(Constants.KEY_TIME_LAST_CHECKED_OUT_SMS, timeLastChecked);
        editor.commit();
        
    }
	public void addMessage( SMSMessage msg ) {
		
		long date = msg.getDate();
		String from = msg.getSender();
		String to = msg.getRecipient();
		String body = msg.getMessage();
		String id = Integer.toString(msg.getId());

		ReplaceableAttribute dateAttribute = new ReplaceableAttribute( "Date", ""+date, Boolean.TRUE );
		ReplaceableAttribute fromAttribute = new ReplaceableAttribute( "From", from, Boolean.TRUE );
		ReplaceableAttribute toAttribute = new ReplaceableAttribute( "To", to, Boolean.TRUE );
		ReplaceableAttribute bodyAttribute = new ReplaceableAttribute( "Body", body, Boolean.TRUE );
		
		List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>();
		attrs.add( dateAttribute );
		attrs.add( fromAttribute );
		attrs.add( toAttribute );
		attrs.add( bodyAttribute );
		
		PutAttributesRequest par = new PutAttributesRequest( "SMSData",uuid+id, attrs);
		try {
			this.sdbClient.putAttributes( par );
			timeLastChecked = date;
		}
		catch ( Exception exception ) {
			System.out.println( "EXCEPTION = " + exception );
		}

	}	    
}
