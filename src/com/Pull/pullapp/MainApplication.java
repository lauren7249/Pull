package com.Pull.pullapp;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.Pull.pullapp.model.FacebookUser;
import com.Pull.pullapp.model.InitiatingData;
import com.Pull.pullapp.model.Invite;
import com.Pull.pullapp.model.MMSMessage;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.ShareEvent;
import com.Pull.pullapp.model.ShareSuggestion;
import com.Pull.pullapp.model.TwilioNumber;
import com.Pull.pullapp.threads.AlarmScheduler;
import com.Pull.pullapp.threads.UploadMyPhoto;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.data.ContentUtils;
import com.Pull.pullapp.util.data.UserInfoStore;
import com.facebook.model.GraphUser;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SignUpCallback;

public class MainApplication extends Application {

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor ;
	private String mPhoneNumber, mFacebookID;
	private ParseUser currentUser;
	private GraphUser mGraphUser;
	private MixpanelAPI mixpanel;
	private Context mContext;
	private UserInfoStore store;
	private int currentapiVersion;
	private String myPackageName;
//private HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs = getSharedPreferences(MainApplication.class.getSimpleName(), Context.MODE_PRIVATE);
		editor = prefs.edit();
		
		ParseObject.registerSubclass(SMSMessage.class);
		ParseObject.registerSubclass(MMSMessage.class);
		ParseObject.registerSubclass(FacebookUser.class);
		ParseObject.registerSubclass(ShareSuggestion.class);
		ParseObject.registerSubclass(Invite.class);
		ParseObject.registerSubclass(ShareEvent.class);
		ParseObject.registerSubclass(TwilioNumber.class);
		ParseObject.registerSubclass(InitiatingData.class);
		
		Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_key));
		ParseFacebookUtils.initialize(getString(R.string.facebook_app_id));	
		
		ParseACL acl = new ParseACL();
		acl.setPublicReadAccess(false);
		ParseACL.setDefaultACL(acl, true);
		
		PushService.setDefaultPushCallback(this, ViewPagerSignIn.class); 	
	    mPhoneNumber = getUserName();		    
	    mContext = getBaseContext();
	    
		mixpanel = MixpanelAPI.getInstance(getBaseContext(), Constants.MIXEDPANEL_TOKEN);
		mixpanel.identify(ParseInstallation.getCurrentInstallation().getObjectId());
		mixpanel.track("ViewPagerSignIn created", null);
		
	    new AlarmScheduler(mContext, false).start();
	    
	    store = new UserInfoStore(mContext);
	    currentapiVersion = android.os.Build.VERSION.SDK_INT;	    
	    
	    if (ParseUser.getCurrentUser()!=null) uploadPhoto(ParseUser.getCurrentUser());
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("to", "");		    
		ParseCloud.callFunctionInBackground("getTwilioNumber", params, new FunctionCallback<String>() {
		@Override
			public void done(String obj, ParseException e) {
	         	if (e == null && obj!=null) {
	         		store.saveTwilioNumber(obj);
		        }
			}
	  });	    
	}
	
	public void uploadPhoto(final ParseUser user) {
	    if(store.getFacebookID(mPhoneNumber) == null) {
	    	String userID = store.getUserID(mPhoneNumber);
    		Map<String, Object> params = new HashMap<String, Object>();
    		params.put("userID", userID);	  
    		params.put("phoneNumber",ContentUtils.addCountryCode(mPhoneNumber));
	    	ParseCloud.callFunctionInBackground("getFacebookID", params, new FunctionCallback<String>(){

				@Override
				public void done(String result, ParseException e) {
					if(result !=null && e==null) {
						store.saveFacebookID(mPhoneNumber, result);
						new UploadMyPhoto(mContext,result,user ).start();
					}
					
				}
	    		
	    		
	    	});
	    }
	    else if(store.getFacebookID(mPhoneNumber)!=null)
	    	new UploadMyPhoto(mContext, store.getFacebookID(mPhoneNumber),user).start();		
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
	
	public void setUsername(String Name) {
		editor = prefs.edit();
		editor.putString(Constants.USER_NAME, Name);
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


	public void setFacebookID(String facebookId) {
		editor = prefs.edit();
		editor.putString(Constants.FACEBOOK_USER_ID, facebookId);	
		editor.commit();
		
	}
	public void setPasswordSalt(String salt) {
		editor = prefs.edit();
		editor.putString(Constants.USER_PASSWORD_SALT, salt);	
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
	public String getPasswordSalt() {
		// TODO Auto-generated method stub
		return prefs.getString(Constants.USER_PASSWORD_SALT, null);
	}		

    static String generateStrongPasswordHash(String password, String saltString) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        int iterations = 1000;
        char[] chars = password.toCharArray();
        byte[] salt = saltString.getBytes();
        
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return iterations + ":" + toHex(salt) + ":" + toHex(hash);
    }
     
     
    private static String toHex(byte[] array) throws NoSuchAlgorithmException
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }		
	public void saveUserInfo(final String username, final String password) {
		mixpanel.track("saveUserInfo started running", null);
		ParseUser.logInInBackground(username, password, new LogInCallback(){
			@Override
			public void done(ParseUser user, ParseException e) {
				if(e==null && user!=null) {
					mixpanel.track("saveUserInfo found user", null);
					setSignedIn(true, username, password);	
					uploadPhoto(ParseUser.getCurrentUser());
					saveInstallation();
				}
				else {
	    	    	mixpanel.track("saveUserInfo did not find a user", null);
	    	    	signUp(username, password);					
				}
				
			}
			
		});		
	}
	private void sendResult() {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(Constants.ACTION_COMPLETE_SIGNUP);
		getBaseContext().sendBroadcast(broadcastIntent);	
		mixpanel.flush();
	}

	protected void signUp(final String username, final String password) {
		mixpanel.track("signUp started running", null);
		currentUser = new ParseUser();
		currentUser.setUsername(username);
		currentUser.setPassword(password);
		currentUser.signUpInBackground(new SignUpCallback() {
        	  public void done(ParseException e) {   
        		  if(e == null) {
        			  mixpanel.track("mapp.signUp succeeded", null);
        			  uploadPhoto(ParseUser.getCurrentUser());
        			  saveInstallation();
        		  }
        		  else {
        			  mixpanel.track("mapp.signUp failed " + e.getMessage(), null);
        			  //Log.i("could not sign up",e.getMessage());
        			  sendResult();
        		  }
        	  }
        	});	
	}	

	private void saveInstallation(){
		ParseInstallation installation = ParseInstallation.getCurrentInstallation();
		if(ParseUser.getCurrentUser().getObjectId()!=null) installation.put("user", ParseUser.getCurrentUser());
		installation.addAllUnique("channels", Arrays.asList(ContentUtils.setChannel(getUserName())));
		installation.saveInBackground();		
		mixpanel.track("saved installation",null);
		sendResult();
	}


}
