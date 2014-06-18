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

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.FacebookUser;
import com.Pull.pullapp.model.Hashtag;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.ShareSuggestion;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.AlarmScheduler;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
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
	private ViewPagerSignIn mSignIn;
	private SharedPreferences.Editor editor ;
	private String mPhoneNumber, mFacebookID;
	private ParseUser currentUser;
	private GraphUser mGraphUser;
	
	
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
		
		Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_key));
		ParseFacebookUtils.initialize(getString(R.string.facebook_app_id));	
		
		PushService.setDefaultPushCallback(this, ViewPagerSignIn.class); 	
	    mPhoneNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();		    
	    
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
	void saveUserInfo(final String username, final String password) {
    	ParseQuery<ParseUser> query = ParseUser.getQuery();
    	query.whereEqualTo("username", ContentUtils.addCountryCode(mPhoneNumber));
    	query.findInBackground(new FindCallback<ParseUser>() {
    	  public void done(List<ParseUser> objects, ParseException e) {
    	    if (e==null && objects.size()>0) {
    	    	currentUser = objects.get(0);
    	    	Log.i("found a user","found a user");
    	    	finishSavingUser(username,password);
    	    } else {
    	    	signUp(username, password);
    	    }
    	  }
    	});		
		
	}	
	protected void finishSavingUser(final String username, final String password) {
		ParseUser.logInInBackground(username, password, new LogInCallback(){
			@Override
			public void done(ParseUser user, ParseException e) {
				if(e==null && user!=null) {
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
	}

	protected void signUp(final String username, final String password) {
		currentUser = new ParseUser();
		currentUser.setUsername(username);
		currentUser.setPassword(password);
		currentUser.signUpInBackground(new SignUpCallback() {
        	  public void done(ParseException e) {   
        		  if(e == null) checkUser(username, password);
        		  else {
        			  Log.i("could not sign up",e.getMessage());
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
    	    	Log.i("found a user","found a user");
    	    	finishSavingUser(username, password);
    	    } else {;
    	    	Log.i("error checking user",e.getMessage());
    	    	sendResult();
    	    }
    	  }
    	});	
	}	
	

	private void saveInstallation(){
	       ParseInstallation installation = ParseInstallation.getCurrentInstallation();
	       installation.put("user", currentUser);
	       installation.addAllUnique("channels", Arrays.asList(ContentUtils.setChannel(mPhoneNumber)));
	       installation.saveInBackground();		
	       Log.i("saved installation","installation saved");
	       
		}	

}
