package com.Pull.pullapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.Pull.pullapp.util.Constants;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

public class MainApplication extends Application {

	private SharedPreferences prefs;
	private ThreadsListActivity mMainActivity;
	private ParseSignIn mMainActivity2;
	
	@Override
	public void onCreate() {
		super.onCreate();

		prefs = getSharedPreferences(MainApplication.class.getSimpleName(), Context.MODE_PRIVATE);
		Parse.initialize(this, "V78CyTgjJqFRP1nOiUclf9siu8Bcja3D65i1UG34", "ccQmmMwIY3wTRaBayFecdfZc4N0EIpYR30R5KdeH");
		PushService.setDefaultPushCallback(this, ParseSignIn.class);
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
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			setSignedIn(true);
		} else {
			setSignedIn(false);
		}		
	}
	

	public void setMainActivity(ThreadsListActivity main){
		mMainActivity = main;
	}
	public void setMainActivity(ParseSignIn main){
		mMainActivity2 = main;
	}
	
	public ThreadsListActivity _getMainActivity(){
		return mMainActivity;
	}	
	
	public ParseSignIn getMainActivity(){
		return mMainActivity2;
	}	
	
	public boolean isSignedIn(){
		if (!prefs.getBoolean(Constants.IS_SIGNED_IN, false)) return false;
		ParseUser user = ParseUser.getCurrentUser();
		//user.refresh();
		return (user!=null);
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
	
	public void setSignedIn(boolean signedIn) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(Constants.IS_SIGNED_IN, signedIn);	
		editor.commit();
	}
	

}
