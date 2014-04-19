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
import com.parse.Parse;
import com.parse.ParseException;
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
		Parse.initialize(this, "V78CyTgjJqFRP1nOiUclf9siu8Bcja3D65i1UG34", "ccQmmMwIY3wTRaBayFecdfZc4N0EIpYR30R5KdeH");
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
		

	    mContext = getApplicationContext();
	    tMgr =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
	    mPhoneNumber = tMgr.getLine1Number();	
	    	
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			setSignedIn(true);
		} else {
			setSignedIn(false);
			if(signIn() == ParseException.OBJECT_NOT_FOUND) {
				errorCode = 0;
				if(signUp() != 0) Log.i("error code:","error code:"+ errorCode);
			}
		}		
	}
	

	public void setMainActivity(ThreadsListActivity main){
		mMainActivity = main;
	}

	public ThreadsListActivity getMainActivity(){
		return mMainActivity;
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
	public int signUp() {
		ParseUser user = new ParseUser();
		user.setUsername(mPhoneNumber);
		user.setPassword(mPhoneNumber);
		
		try {
			user.signUp();
	      // Hooray! Let them use the app now.
	    	setSignedIn(true);
	    	PushService.subscribe(mContext, ContentUtils.setChannel(mPhoneNumber), ThreadsListActivity.class);		
		} catch (ParseException e) {
			errorCode = e.getCode();
		}
		return errorCode;
	}		
	
	public int signIn() {
		try {
			ParseUser.logIn(mPhoneNumber, mPhoneNumber);
	    	setSignedIn(true);
		} catch (ParseException e) {
			errorCode = e.getCode();
		}		
		return errorCode;
	}	

}
