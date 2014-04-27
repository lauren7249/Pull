package com.Pull.pullapp;

import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.Pull.pullapp.util.ContentUtils;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.parse.ParseFacebookUtils.Permissions;

@Deprecated
public class ParseSignIn extends Activity {
	private SharedPreferences prefs;
	private Context mContext;
	private EditText mUsername, mEmail, mPassword;
	private TextView usernameFeedback, passwordFeedback, emailFeedback;
	private Button mSignUpButton, mSignInButton;
	private String username, email, password, mPhoneNumber;
	private MainApplication mApp;
	private TelephonyManager tMgr;
	private int errorCode;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.parse_signin);
	    prefs = getSharedPreferences(MainApplication.class.getSimpleName(), Context.MODE_PRIVATE);
	    mContext = getApplicationContext();
	    tMgr =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
	    mPhoneNumber = tMgr.getLine1Number();	
	    ParseAnalytics.trackAppOpened(getIntent());
	    mApp = (MainApplication) this.getApplication();
	    
	    mUsername = (EditText)findViewById(R.id.username);
	    mPassword = (EditText)findViewById(R.id.password);
	    mEmail = (EditText)findViewById(R.id.email);
	    
	    mSignUpButton = (Button) findViewById(R.id.signUp);
	    mSignInButton = (Button) findViewById(R.id.signIn);
	    
	    usernameFeedback = (TextView) findViewById(R.id.usernameFeedback);
	    passwordFeedback = (TextView) findViewById(R.id.passwordFeedback);
	    emailFeedback = (TextView) findViewById(R.id.emailFeedback);
	    
	    //only make visibility if new user
	    mEmail.setVisibility(View.GONE);    
	    
	    
			
		if (mApp.isSignedIn()) 
			PushService.subscribe(mContext, ContentUtils.setChannel(mPhoneNumber), 
					ThreadsListActivity.class);	
	
	    if(mApp.isSignedIn() && ParseUser.getCurrentUser()!=null) {
	    	Intent mIntent = new Intent(mContext, ThreadsListActivity.class);
	    	startActivity(mIntent);
	    }

	    mUsername.setText(mPhoneNumber);
	}
	
	/**
	 * Signs in to parse with user inputted parameters
	 * @param v
	 */
	public void signIn(View v) {
		hideKeyboard();
		username = mUsername.getText().toString().trim();
		password = mPassword.getText().toString();
		ParseUser.logInInBackground(username, password, new LogInCallback() {
			  public void done(ParseUser user, ParseException e) {
			    if (user != null) {
			    	email = user.getEmail();
			    	mApp.setSignedIn(true, email, username);
			    	Intent mIntent = new Intent(mContext, ThreadsListActivity.class);
			    	startActivity(mIntent);			    	
			    } else {
			      // Signup failed. Look at the ParseException to see what happened.
			    	errorCode = e.getCode();
			    	switch(errorCode) {
			    	case(ParseException.OBJECT_NOT_FOUND):
			    		usernameFeedback.setText("Username not found");
			    		usernameFeedback.setTextColor(Color.RED);
			    	default:
			    	}
			    	//Toast.makeText(mContext, "error code:" + errorCode, Toast.LENGTH_LONG).show();
			    }
			  }
			});		
	}
	
	/**
	 * Signs up for parse with user given parameters
	 * @param v
	 */
	public void signUp(View v) {
		hideKeyboard();
		username = mUsername.getText().toString().trim();
		password = mPassword.getText().toString();
		ParseUser user = new ParseUser();
		user.setUsername(username);
		user.setPassword(password);
		if(mEmail.getVisibility() != View.VISIBLE) {
			mEmail.setVisibility(View.VISIBLE);
    		emailFeedback.setText("Enter email");
    		emailFeedback.setTextColor(Color.BLACK);			
			return;
		}
		email = mEmail.getText().toString().trim();
		user.setEmail(email);

	    //Toast.makeText(mContext, "phone number:" + mPhoneNumber, Toast.LENGTH_LONG).show();
	    
		// other fields can be set just like with ParseObject
		user.put("phone", mPhoneNumber); 
		user.signUpInBackground(new SignUpCallback() {
		  public void done(ParseException e) {
		    if (e == null) {
		      // Hooray! Let them use the app now.
		    	mApp.setSignedIn(true, email, username);
		    	PushService.subscribe(mContext, "phoneNumber"+ContentUtils.addCountryCode(mPhoneNumber), ThreadsListActivity.class);
		    	//Toast.makeText(mContext, "signed up!", Toast.LENGTH_LONG).show();
		    	Intent mIntent = new Intent(mContext, ThreadsListActivity.class);
		    	startActivity(mIntent);		    	
		    } else {
		    	errorCode = e.getCode();
		    	switch(errorCode) {
		    	case(ParseException.EMAIL_TAKEN):
		    		emailFeedback.setText("Email already registered");
	    			emailFeedback.setTextColor(Color.RED);	
		    	case(ParseException.USERNAME_TAKEN):
		    		usernameFeedback.setText("Username already registered");
		    		usernameFeedback.setTextColor(Color.RED);		    			
		    	default:
		    		Toast.makeText(mContext, "error:" + errorCode, Toast.LENGTH_LONG).show();
		    	}
		    	
		    }
		  }
		});
	}	
	
	
	/**
	 * pulls up the facebook login screen
	 */
	private void facebookLogin(){
		ParseFacebookUtils.logIn(Arrays.asList(Permissions.User.BIRTHDAY, 
				Permissions.User.HOMETOWN, Permissions.User.LOCATION),
				this, new LogInCallback() {
			  @Override
			  
			  public void done(ParseUser user, ParseException err) {
			    if (user == null) {
			      Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
			    } else {
			    	linkAccount(user);
			    	makeMeRequest(Session.getActiveSession());
				    if (user.isNew()) {
					      Log.d("MyApp", "User signed up and logged in through Facebook!");
					    } else {
					      Log.d("MyApp", "User logged in through Facebook!");
					    }			    	
			    }
			  }
			});				
	}
	
	/**
	 * Links a parse user to a facebook account if the user is not already linked
	 * @param user
	 */
	private void linkAccount(final ParseUser user) {
		if (!ParseFacebookUtils.isLinked(user)) {
			  ParseFacebookUtils.link(user, this, new SaveCallback() {
			    @Override
			    public void done(ParseException ex) {
			      if (ParseFacebookUtils.isLinked(user)) {
			        Log.d("MyApp", "Woohoo, user logged in with Facebook!");
			      }
			    }
			  });
		}
		
	}	
	private void makeMeRequest(final Session session) {
	    Request request = Request.newMeRequest(session, 
	            new Request.GraphUserCallback() {

	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (user != null) {
	                    String facebookId = user.getId();
	                    Log.i("facebook id", facebookId);
	                    Log.i("birthday", user.getBirthday());
	                    Log.i("location", user.getLocation().toString());
	                }
	            }
	            if (response.getError() != null) {
	                // Handle error
	            }
	        }
	    });
	    request.executeAsync();
	} 	
	private void hideKeyboard(){
		InputMethodManager inputManager = (InputMethodManager) this
	            .getSystemService(Context.INPUT_METHOD_SERVICE);
	    View v=getCurrentFocus();
	    if(v==null)return;
	    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}	
	
	/**
	 * sign up for parse
	 * @return
	 */
	@Deprecated
	public int signUp() {
		ParseUser user = new ParseUser();
		user.setUsername(username);
		user.setPassword(password);
		
		try {
			user.signUp();
	      // Hooray! Let them use the app now.
	    	mApp.setSignedIn(true);
		} catch (ParseException e) {
			errorCode = e.getCode();
		}
		return errorCode;
	}		
	 
	/**
	 * sign into parse
	 * @return
	 * error code if signin fails
	 */
	@Deprecated
	public int signIn() {
		try {
			ParseUser.logIn(username, password);
	    	mApp.setSignedIn(true);
		} catch (ParseException e) {
			errorCode = e.getCode();
		}		
		return errorCode;
	}		
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}	  
		
}
