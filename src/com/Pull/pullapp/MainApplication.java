package com.Pull.pullapp;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.Pull.pullapp.model.Channels;
import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.FacebookUser;
import com.Pull.pullapp.model.Hashtag;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.ShareEvent;
import com.Pull.pullapp.model.ShareSuggestion;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.model.TwilioNumber;
import com.Pull.pullapp.threads.AlarmScheduler;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.facebook.model.GraphUser;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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

	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs = getSharedPreferences(MainApplication.class.getSimpleName(), Context.MODE_PRIVATE);
		editor = prefs.edit();
		
		ParseObject.registerSubclass(SharedConversation.class);
		ParseObject.registerSubclass(Comment.class);
		ParseObject.registerSubclass(SMSMessage.class);
		ParseObject.registerSubclass(Hashtag.class);
		ParseObject.registerSubclass(FacebookUser.class);
		ParseObject.registerSubclass(ShareSuggestion.class);
		ParseObject.registerSubclass(Channels.class);
		ParseObject.registerSubclass(ShareEvent.class);
		ParseObject.registerSubclass(TwilioNumber.class);
		
		Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_key));
		ParseFacebookUtils.initialize(getString(R.string.facebook_app_id));	
		
		ParseACL.setDefaultACL(new ParseACL(), true);
		
		PushService.setDefaultPushCallback(this, ViewPagerSignIn.class); 	
	    mPhoneNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();		    
	    
		mixpanel = MixpanelAPI.getInstance(getBaseContext(), Constants.MIXEDPANEL_TOKEN);
		mixpanel.identify(ParseInstallation.getCurrentInstallation().getObjectId());
		mixpanel.track("ViewPagerSignIn created", null);
		
	    new AlarmScheduler(getBaseContext(), false).start();
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
     
    private static String getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt.toString();
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
    	ParseQuery<ParseUser> query = ParseUser.getQuery();
    	query.whereEqualTo("username", ContentUtils.addCountryCode(mPhoneNumber));
    	query.findInBackground(new FindCallback<ParseUser>() {
    	  public void done(List<ParseUser> objects, ParseException e) {
    	    if (e==null && objects.size()>0) {
    	    	currentUser = objects.get(0);
    	    	//Log.i("found a user","found a user");
    	    	mixpanel.track("saveUserInfo found a user", null);
    	    	finishSavingUser(username,password);
    	    } else {
    	    	mixpanel.track("saveUserInfo did not find a user", null);
    	    	signUp(username, password);
    	    }
    	  }
    	});		
		
	}	
	protected void finishSavingUser(final String username, final String password) {
		mixpanel.track("finishSavingUser started running", null);
		ParseUser.logInInBackground(username, password, new LogInCallback(){
			@Override
			public void done(ParseUser user, ParseException e) {
				if(e==null && user!=null) {
					mixpanel.track("finishSavingUser found user", null);
					saveInstallation();
					setSignedIn(true, username, password);							
				}
				sendResult();
				
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
        			  checkUser(username, password);
        		  }
        		  else {
        			  mixpanel.track("mapp.signUp failed " + e.getMessage(), null);
        			  //Log.i("could not sign up",e.getMessage());
        			  sendResult();
        		  }
        	  }
        	});	
	}	
	
	protected void checkUser(final String username, final String password) {
    	ParseQuery<ParseUser> query = ParseUser.getQuery();
    	query.whereEqualTo("username", ContentUtils.addCountryCode(mPhoneNumber));
    	query.findInBackground(new FindCallback<ParseUser>() {
    	  public void done(List<ParseUser> objects, ParseException e) {
    	    if (e == null && objects.size()>0) {
    	    	currentUser = objects.get(0);
	    	    mixpanel.track("checkUser found a user",null);
    	    	//Log.i("found a user","found a user");
    	    	finishSavingUser(username, password);
    	    } else {;
    	    	mixpanel.track("checkUser did not find a user " + e.getMessage(),null);
    	    	//Log.i("error checking user",e.getMessage());
    	    	sendResult();
    	    }
    	  }
    	});	
	}	
	

	private void saveInstallation(){
		Channels c = new Channels(ContentUtils.setChannel(mPhoneNumber));
		if(currentUser.getObjectId()!=null) c.put("user", currentUser);
		c.saveInBackground();
		ParseInstallation installation = ParseInstallation.getCurrentInstallation();
		if(currentUser.getObjectId()!=null) installation.put("user", currentUser);
		installation.addAllUnique("channels", Arrays.asList(ContentUtils.setChannel(mPhoneNumber)));
		installation.saveInBackground();		
		mixpanel.track("saved installation",null);
	       
	}	

}
