package com.Pull.pullapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

public class MainApplication extends Application {

	private SharedPreferences prefs;
	private ThreadsListActivity mMainActivity;
	private String mPhoneNumber;
	private TelephonyManager tMgr;
	private int errorCode;	
	private Context mContext;
	
	@Override
	public void onCreate() {
		super.onCreate();

		prefs = getSharedPreferences(MainApplication.class.getSimpleName(), Context.MODE_PRIVATE);
		ParseObject.registerSubclass(SharedConversation.class);
		ParseObject.registerSubclass(Comment.class);
		ParseObject.registerSubclass(SMSMessage.class);
		
		Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_key));
		ParseFacebookUtils.initialize(getString(R.string.facebook_app_id));	
		
		PushService.setDefaultPushCallback(this, ThreadsListActivity.class);
		ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
	        @Override
	        public void done(ParseException e) {
	        		if (e == null) {
	                    Toast toast = Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT);
	                    //toast.show();
	                } else {
	                    e.printStackTrace();

	                    Toast toast = Toast.makeText(getApplicationContext(), "failure", Toast.LENGTH_SHORT);
	                   // toast.show();
	                }
	            }
	        });
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			setSignedIn(true);
		} else {
			setSignedIn(false);
		}

	}
	
	public void setSignedIn(boolean signedIn, String Email, String Name) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(Constants.IS_SIGNED_IN, signedIn);

		if(signedIn){
			editor.putString(Constants.USER_EMAIL, Email);
			editor.putString(Constants.USER_NAME, Name);
		}else{
			editor.putString(Constants.USER_EMAIL, "");
			editor.putString(Constants.USER_NAME, "");
		}

		editor.commit();
	}
	
	public boolean isSignedIn(){
		return prefs.getBoolean(Constants.IS_SIGNED_IN, false);
	}	
	public void setSignedIn(boolean signedIn) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(Constants.IS_SIGNED_IN, signedIn);	
		editor.commit();
	}	

	public void setMainActivity(ThreadsListActivity main){
		mMainActivity = main;
	}

	public ThreadsListActivity getMainActivity(){
		return mMainActivity;
	}	


}
