package com.Pull.pullapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.Constants;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.PushService;

public class MainApplication extends Application {

	private SharedPreferences prefs;
	private ViewPagerSignIn mSignIn;
	private SharedPreferences.Editor editor ;
	
	@Override
	public void onCreate() {
		super.onCreate();

		prefs = getSharedPreferences(MainApplication.class.getSimpleName(), Context.MODE_PRIVATE);
		editor = prefs.edit();
		
		ParseObject.registerSubclass(SharedConversation.class);
		ParseObject.registerSubclass(Comment.class);
		ParseObject.registerSubclass(SMSMessage.class);

		Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_key));
		ParseFacebookUtils.initialize(getString(R.string.facebook_app_id));	
		
		PushService.setDefaultPushCallback(this, ViewPagerSignIn.class);

	}
	
	public void setSignedIn(boolean signedIn, String Name, String Password) {
		editor = prefs.edit();
		editor.putBoolean(Constants.IS_SIGNED_IN, signedIn);

		if(signedIn){
			editor.putString(Constants.USER_PASSWORD, Password);
			editor.putString(Constants.USER_NAME, Name);
		}else{
			editor.putString(Constants.USER_PASSWORD, "");
			editor.putString(Constants.USER_NAME, "");
		}
		Log.i("user signed in with mprefs", Name);
		editor.commit();
	}
	
	public boolean isSignedIn(){
		return prefs.getBoolean(Constants.IS_SIGNED_IN, false);
	}	
	public void setSignedIn(boolean signedIn) {
		editor = prefs.edit();
		editor.putBoolean(Constants.IS_SIGNED_IN, signedIn);	
		editor.commit();
	}	

	public void setMainActivity(ViewPagerSignIn main){
		mSignIn = main;
	}

	public ViewPagerSignIn getMainActivity(){
		return mSignIn;
	}

	public void setFacebookID(String facebookId) {
		editor = prefs.edit();
		editor.putString(Constants.FACEBOOK_USER_ID, facebookId);	
		editor.commit();
		
	}

	public String getFacebookID() {
		return prefs.getString(Constants.FACEBOOK_USER_ID, null);
	}

	public String getUserName() {
		return prefs.getString(Constants.USER_NAME, null);
	}	
	
	public String getPassword() {
		return prefs.getString(Constants.USER_PASSWORD, null);
	}		

}
