package com.Pull.pullapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;


public class MainApplication extends Application{

	private SharedPreferences prefs;
	public static final String IS_SIGNED_IN = "isSignedIn";
	public static final String USER_EMAIL = "user_email";
	public static final String USER_NAME = "user_name";
	
	private ThreadsListActivity mMainActivity;

	@Override
	public void onCreate() {
		super.onCreate();

		prefs = getSharedPreferences(MainApplication.class.getSimpleName(), Context.MODE_PRIVATE);
		
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
