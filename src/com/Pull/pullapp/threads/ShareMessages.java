package com.Pull.pullapp.threads;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.content.Context;
import android.provider.Telephony.TextBasedSmsColumns;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.Pull.pullapp.util.SendUtils;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ShareMessages extends Thread {

    private Context parent;
    private TelephonyManager tmgr;
    private String app_plug;
	private TreeSet<SMSMessage> messages;
	private String confidante;
	private String person_shared;
	private String address;
	private DatabaseHandler db;
    

    public ShareMessages(Context mContext, String confidante,
			String other_person_name, String other_person,
			TreeSet<SMSMessage> check_hash) {
    	this.confidante = confidante;
    	this.parent = mContext;
     	this.tmgr = (TelephonyManager)parent.getSystemService(Context.TELEPHONY_SERVICE);
     	this.person_shared = other_person_name;
     	this.address = other_person;
     	this.messages = check_hash;
    	Map dimensions = new HashMap(); 	
    	ParseAnalytics.trackEvent("Shared Conversation", dimensions);
	}

	@SuppressWarnings("unchecked")
	@Override
    public void run() {
		  String sender = ParseUser.getCurrentUser().getUsername();
	  
		  String convoID = sender+address+confidante;
		  db = new DatabaseHandler(parent);
		  
		  for(SMSMessage m: messages) {
			  db.addSharedMessage(convoID, m, TextBasedSmsColumns.MESSAGE_TYPE_SENT);
		  }

		  db.addSharedConversation(convoID, confidante, person_shared,
					address, sender, TextBasedSmsColumns.MESSAGE_TYPE_SENT);	
		  db.close();
		  
		  HashMap<String, Object> params = new HashMap<String, Object>();
		  params.put("phoneNumber", ContentUtils.addCountryCode(confidante));
		  ParseCloud.callFunctionInBackground("findUser", params, new FunctionCallback() {
			@Override
			public void done(Object arg0, ParseException e) {
		         if (e != null) {
		        	 	Log.i("nothing found", "going to send a text " + ContentUtils.addCountryCode(confidante));
			            SendUtils.shareViaSMS(parent, person_shared, confidante, messages);     
			         }
			}
		  });		  

			
      
    }



}
