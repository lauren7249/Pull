package com.Pull.pullapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.viewpagerindicator.CirclePageIndicator;

public class ViewPagerSignIn extends BaseSampleActivity {
	private ParseUser mParseUser;
    private Button mSignInButton;
	private SharedPreferences prefs;
	private Context mContext;
	private String mPhoneNumber, mSerialNumber;
	private MainApplication mApp;
	private TelephonyManager tMgr;
	private int errorCode = 0;
	private ProfilePictureView profilePictureView;
	private GraphUser mGraphUser;
	protected String mFacebookID;
	private EditText mKeyHashBox;
	private boolean isNewUser;
	private Dialog progressDialog;
	private TextView mAssurance;
	private EditText mPassword;
	private EditText mConfirmPassword;
	private EditText mUserID;
	private Button mGenericSignInButton;
	private LinearLayout mLayout;
	private RelativeLayout mBottomHalf;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_viewpager);
        mLayout = (LinearLayout) findViewById(R.id.main_layout);
        mBottomHalf = (RelativeLayout) findViewById(R.id.signin_area);
        mAdapter = new TestFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        
        mContext = getApplicationContext();
        mSignInButton = (Button) findViewById(R.id.sign_in);
        mGenericSignInButton = (Button) findViewById(R.id.generic_sign_in);
        mUserID = (EditText) findViewById(R.id.mobileNumber);
        mPassword = (EditText) findViewById(R.id.password);
        mConfirmPassword = (EditText) findViewById(R.id.confirmPassword);
        
        mAssurance = (TextView) findViewById(R.id.assurance);
        mKeyHashBox = (EditText) findViewById(R.id.keyhash);
	    prefs = getSharedPreferences(MainApplication.class.getSimpleName(), Context.MODE_PRIVATE);
	    tMgr =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
	    mPhoneNumber = ContentUtils.addCountryCode(tMgr,tMgr.getLine1Number());

	    mSerialNumber = tMgr.getSimSerialNumber();
	    mApp = (MainApplication) this.getApplication();   
	    

	 // Find the user's profile picture custom view
	    profilePictureView = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
	    profilePictureView.setCropped(true);
	    
	    displayFacebookLoginOption();
		//if (mFacebookID!=null) profilePictureView.setProfileId(mFacebookID);	 
	    
	    String keyhashes = "";
	    // Add code to print out the key hash
	    try {
	        PackageInfo info = getPackageManager().getPackageInfo(
	                "com.Pull.pullapp", 
	                PackageManager.GET_SIGNATURES);
	        for (Signature signature : info.signatures) {
	            MessageDigest md = MessageDigest.getInstance("SHA");
	            md.update(signature.toByteArray());
	            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
	            keyhashes = keyhashes + " " + Base64.encodeToString(md.digest(), Base64.DEFAULT);
	            }
	    } catch (NameNotFoundException e) {

	    } catch (NoSuchAlgorithmException e) {

	    }	 			    
	    if(Constants.DEBUG) {
	    	mKeyHashBox.setText(keyhashes);
	    	mKeyHashBox.setVisibility(View.VISIBLE);
	    }
	    
		ParseUser mParseUser = ParseUser.getCurrentUser();
		if ((mParseUser != null) && ParseFacebookUtils.isLinked(mParseUser)) {
	    	Intent mIntent = new Intent(mContext, ThreadsListActivity.class);
	    	startActivity(mIntent);	    	
	    }
		/**
		int resultcode =  getIntent().getIntExtra(Constants.EXTRA_SIGNIN_RESULT, 0); 
		//facebook login failed
		if(resultcode == 1) {
			
		}*/
	    // Dirty Hack to detect keyboard
		mLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				if ((mLayout.getRootView().getHeight() - mLayout.getHeight()) > mLayout.getRootView().getHeight() / 3) {
					mBottomHalf.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 100f));
				} else {
					mBottomHalf.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 2f));
				}
			}
		});		
    }
	
	/**
	 * pulls up the facebook login screen
	 */
	public void facebookLogin(View v){
	    ViewPagerSignIn.this.progressDialog = ProgressDialog.show(
	    		ViewPagerSignIn.this, "", "Logging in...", true);	
		List<String> permissions = Arrays.asList("basic_info", "user_about_me",
				"user_relationships", "user_birthday", "user_location");
		ParseFacebookUtils.logIn(permissions,this, new LogInCallback() {
			  @Override
			  public void done(ParseUser user, ParseException err) {
			    if (user == null) {
			       Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
			       if(err != null) {
				       Log.i("errorcode from login","code "+ err.getCode());
				       if(err.getCode() == ParseException.OTHER_CAUSE) {
				    	   //askToBeAlpha();
				    	   //ViewPagerSignIn.this.progressDialog.cancel();
				   	    	Intent mIntent = new Intent(mContext, ThreadsListActivity.class);
				   	    	startActivity(mIntent);  						    	   
				       }
			       }
			    } else {
			    	Log.i("signin.facebooklogin","able to login through fb");
			    	mParseUser = user;
				    if (user.isNew()) {
				    	isNewUser = true;
				    	Log.d("MyApp", "User signed up and logged in through Facebook!");
					} else {
						isNewUser = false;
						Log.d("MyApp", "User logged in through Facebook!");
					}				    
				    Intent mIntent = new Intent(mContext, ThreadsListActivity.class);
			    	startActivity(mIntent);  
			    			    	
			    }
			  }
			});				
	}
	
	public void anonymousLogin(View v){
	    Intent mIntent = new Intent(mContext, ThreadsListActivity.class);
    	startActivity(mIntent);  			
	}	
	@Deprecated
	protected void displayAlternateLoginOption() {
		// TODO Auto-generated method stub
		mSignInButton.setVisibility(View.GONE);
		mGenericSignInButton.setText("Facebook Sign Up Method");
		mAssurance.setText("Oops! Looks like you need to create a login");
		mUserID.setVisibility(View.VISIBLE);
		mUserID.setText(mPhoneNumber);
		mPassword.setVisibility(View.VISIBLE);
		mConfirmPassword.setVisibility(View.VISIBLE);
	}
	
	public void toggleGenericLogin(View v) {
		if(mSignInButton.getVisibility()==View.VISIBLE) displayAlternateLoginOption();
		else displayFacebookLoginOption();
	}
	private void displayFacebookLoginOption() {
		mSignInButton.setVisibility(View.VISIBLE);
		mGenericSignInButton.setText("Sign Up Anonymously");
		mUserID.setVisibility(View.GONE);
		mPassword.setVisibility(View.GONE);
		mConfirmPassword.setVisibility(View.GONE);
		mAssurance.setText("We will never post anything to Facebook.");
		
	}
	

	protected void askToBeAlpha() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Welcome! Become an Alpha Tester");
	    builder.setMessage("Pull is not yet launched to the public. "+ 
	    "Like us on Facebook to be one of the first to try it. Interested?")
	           .setCancelable(true)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   dialog.cancel();
	            	   onBoardAsAlphaTester();
	            	 
	               	}
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	               }
	           }).show();	
		
	}

	protected void onBoardAsAlphaTester() {
		try {
		      //try to open page in facebook native app.
		      String uri = "fb://page/1485693048311315";    //Cutsom URL
		      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		      startActivity(intent);   
		}catch (ActivityNotFoundException ex){
		      //facebook native app isn't available, use browser.
		      String uri = "http://touch.facebook.com/pages/x/1485693048311315";  //Normal URL  
		      Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));    
		      startActivity(i); 
		}		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 32665 && data != null )
            ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);		
		super.onActivityResult(requestCode, resultCode, data);
	}	
	

		
}