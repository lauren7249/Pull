package com.Pull.pullapp.threads;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.SendUtils;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
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

	@Override
    public void run() {
		
		shareViaParse();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Channels");
		Log.i("searching channell", ContentUtils.setChannel(confidante));
		query.whereEqualTo("channel", ContentUtils.setChannel(confidante));
		query.findInBackground(new FindCallback<ParseObject>() {
		    public void done(List<ParseObject> list, ParseException e) {
		        if (e==null && list.size()>0) {

		        } else {
		    		Log.i("about to search numbers","in sharemessages");
		    		HashMap<String, Object> params = new HashMap<String, Object>();
		    		params.put("to", confidante);
		    		params.put("from", ParseUser.getCurrentUser().get("username"));
		    		ParseCloud.callFunctionInBackground("getTwilioNumber", params, new FunctionCallback<String>() {
		    		   public void done(String num, ParseException e) {
		    		       if (e == null) {
		    		    	   Log.i("found number",num);
		    		       } else {
		    		    	   Log.i("no numbers","no numbers");
		    		    	   SendUtils.shareViaSMS(parent, person_shared, confidante, messages);
		    		       }
		    		   }
		    		});          	
		        }
		    }
		});		
			
      
    }

	private void shareViaParse() {
		JSONObject data = new JSONObject();
		String channel = ContentUtils.setChannel(tmgr,confidante);
		try {
			data.put("action", Constants.ACTION_RECEIVE_SHARED_MESSAGES);
			data.put("person_shared", person_shared);
			data.put("address",address);
			data.put("from", ContentUtils.addCountryCode(tmgr.getLine1Number()));
			ParsePush push = new ParsePush();
			push.setChannel(channel);
			push.setData(data);
			push.sendInBackground();
			//Log.i("push sent","to channel " + channel);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}



}
