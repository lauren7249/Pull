package com.Pull.pullapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.PushService;
import com.parse.SaveCallback;

public class MainApplication extends Application {

	private SharedPreferences prefs;
	public static final String IS_SIGNED_IN = "isSignedIn";
	public static final String USER_EMAIL = "user_email";
	public static final String USER_NAME = "user_name";
	
	private ThreadsListActivity mMainActivity;

	@Override
	public void onCreate() {
		super.onCreate();

		prefs = getSharedPreferences(MainApplication.class.getSimpleName(), Context.MODE_PRIVATE);
		Parse.initialize(this, "V78CyTgjJqFRP1nOiUclf9siu8Bcja3D65i1UG34", "ccQmmMwIY3wTRaBayFecdfZc4N0EIpYR30R5KdeH");
		PushService.setDefaultPushCallback(this, ThreadsListActivity.class);
		ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
	        @Override
	        public void done(ParseException e) {
	        		if (e == null) {
	                    Toast toast = Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT);
	                    toast.show();
	                } else {
	                    e.printStackTrace();

	                    Toast toast = Toast.makeText(getApplicationContext(), "failure", Toast.LENGTH_SHORT);
	                    toast.show();
	                }
	            }
	        });
	}
	

	public void setMainActivity(ThreadsListActivity main){
		mMainActivity = main;
	}
	
	public ThreadsListActivity getMainActivity(){
		return mMainActivity;
	}	
	public boolean isSignedIn(){
		return prefs.getBoolean(IS_SIGNED_IN, false);
	}
	
	public void setSignedIn(boolean signedIn, String Email, String Name) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(IS_SIGNED_IN, signedIn);
		
		if(signedIn){
			editor.putString(USER_EMAIL, Email);
			editor.putString(USER_NAME, Name);
		}else{
			editor.putString(USER_EMAIL, "");
			editor.putString(USER_NAME, "");
		}
		
		editor.commit();
	}
	

}
