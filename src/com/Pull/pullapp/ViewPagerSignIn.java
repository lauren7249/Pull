package com.Pull.pullapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberUtils;
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
import android.widget.Toast;

import com.Pull.pullapp.adapter.SignInFragmentAdapter;
import com.Pull.pullapp.model.FacebookUser;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.UserInfoStore;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.viewpagerindicator.CirclePageIndicator;

public class ViewPagerSignIn extends BaseActivity {
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
	private String mPasswordString;
	private BroadcastReceiver mBroadcastReceiver;
	private MixpanelAPI mixpanel;
	protected boolean showdialog;
	private JSONObject jsonUser;
	private IntentFilter intentFilter;
	private static final Class nextActivity = ImagePickerActivity.class;
	private UserInfoStore store;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_viewpager);
        mLayout = (LinearLayout) findViewById(R.id.main_layout);
        mBottomHalf = (RelativeLayout) findViewById(R.id.signin_area);
        mAdapter = new SignInFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        
        mContext = getApplicationContext();
        
        store = new UserInfoStore(mContext);
        
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
	    
		mBroadcastReceiver = 
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				
				if(action.equals(Constants.ACTION_COMPLETE_SIGNUP)) {
					if(ParseUser.getCurrentUser()!=null
							&& ParseUser.getCurrentUser().isAuthenticated()) {
						if(ParseUser.getCurrentUser().get("profilePhoto") ==null) openPhotoPicker();
						else openThreads();
					} else {
						if(ParseUser.getCurrentUser()!=null) {
							mixpanel.track("Error signing in, user is null", jsonUser);
							
							Toast.makeText(mContext, "Error signing in, user is null", Toast.LENGTH_LONG).show();
						}
						else {
							mixpanel.track("Error signing in, not authenticated", jsonUser);
							Toast.makeText(mContext, "Error signing in, not authenticated", Toast.LENGTH_LONG).show();
						}
						progressDialog.dismiss();
					}					
					//
					return;
				}				
				

			}
		};				
		intentFilter = new IntentFilter();	
		intentFilter.addAction(Constants.ACTION_COMPLETE_SIGNUP);		
		registerReceiver(mBroadcastReceiver, intentFilter);		
		
	    jsonUser = new JSONObject();
	    try {
	    	jsonUser.put("mPhoneNumber", mPhoneNumber);
	    	jsonUser.put("mSerialNumber", mSerialNumber);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
		mixpanel = MixpanelAPI.getInstance(mContext, Constants.MIXEDPANEL_TOKEN);
		mixpanel.identify(ParseInstallation.getCurrentInstallation().getObjectId());
		mixpanel.track("ViewPagerSignIn created", jsonUser);
		mixpanel.track("Phone number " + mPhoneNumber , jsonUser);
	   // mUserID.setText(mPhoneNumber);
		
	 // Find the user's profile picture custom view
	    profilePictureView = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
	    profilePictureView.setCropped(true);
	    
		//if (mFacebookID!=null) profilePictureView.setProfileId(mFacebookID);	 
	    if(Constants.DEBUG) {
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
	    
	    	mKeyHashBox.setText(keyhashes);
	    	mKeyHashBox.setVisibility(View.VISIBLE);
	    }  
		mParseUser = ParseUser.getCurrentUser();
		if (mParseUser!=null && mParseUser.isDataAvailable() && mParseUser.isAuthenticated()) {
			mixpanel.track("Authenticated & signed in", jsonUser);
			Log.i("isDataAvailable",""+mParseUser.isDataAvailable());
			try {
				mPasswordString = MainApplication.generateStrongPasswordHash(mPhoneNumber,mSerialNumber);
				ParseUser.logIn(mParseUser.getUsername(), mPasswordString);
				if(mParseUser.get("profilePhoto")==null) openPhotoPicker();
				else openThreads();				
			} catch (NoSuchAlgorithmException e) {
				mixpanel.track(e.getLocalizedMessage(), jsonUser);
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				mixpanel.track(e.getLocalizedMessage(), jsonUser);
				e.printStackTrace();
			} catch (ParseException e) {
				mixpanel.track(e.getLocalizedMessage(), jsonUser);
				e.printStackTrace();
			}
			
	    }

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
		   
	    if(appRunning("com.facebook.katana")) {
	    	mixpanel.track("Facebook is running", jsonUser);
	    	displayFacebookLoginOption();
	    }
	    else {
	    	mixpanel.track("Facebook not running", jsonUser);
	    	displayAlternateLoginOption();
	    }		
	      
	
    }


	private void openPhotoPicker() {
	    Intent mIntent = new Intent(mContext, ImagePickerActivity.class);
	    
	    //together this means that when you press the back button in the new task you will not go back
	    //to the original task, but rather close out of the app
	    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
	    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(mIntent); 	
		
	}


	private void openThreads() {
	    Intent mIntent = new Intent(mContext, AllThreadsListActivity.class);
	    
	    //together this means that when you press the back button in the new task you will not go back
	    //to the original task, but rather close out of the app
	    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
	    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(mIntent); 	
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mBroadcastReceiver!=null) unregisterReceiver(mBroadcastReceiver);
		if(progressDialog!=null && progressDialog.isShowing()) progressDialog.dismiss();
	}	
	
	protected void onResume() {
		super.onResume();
		if(mBroadcastReceiver!=null) registerReceiver(mBroadcastReceiver, intentFilter);
	}		

	/**
	 * pulls up the facebook login screen
	 */
	public void facebookLogin(final View v){
		mixpanel.track("Facebook login button clicked" , jsonUser);
	    progressDialog = ProgressDialog.show(
	    		ViewPagerSignIn.this, "", "Signing up...", true);	
	    showdialog=false;
		List<String> permissions = Arrays.asList("basic_info", "user_about_me",
				"user_relationships", "user_birthday", "user_location");
		ParseFacebookUtils.logIn(permissions,this, new LogInCallback() {
			  @Override
			  public void done(ParseUser user, ParseException err) {
			    if (user == null) {  
			       Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
			       if(err != null) {
				       mixpanel.track("errorcode from ParseFacebookUtils.logIn " + err.getMessage(), jsonUser);
				       if(err.getCode() == ParseException.OTHER_CAUSE) {
				    	   				    	   
				       }
			       }
			       progressDialog.dismiss();
			       anonymousLogin(v);	
			    } else {
			    	
			    	mixpanel.track("ParseFacebookUtils.logIn successful", jsonUser);
			    	mParseUser = user;
				    if (user.isNew()) {
				    	isNewUser = true;
				    	mixpanel.track("New parse user", jsonUser);
				    	//Log.d("MyApp", "User signed up and logged in through Facebook!");
					} else {
						isNewUser = false;
						mixpanel.track("Old parse user", jsonUser);
						//Log.d("MyApp", "User logged in through Facebook!");
					}			
				    // Fetch Facebook user info if the session is active
					Session session = ParseFacebookUtils.getSession();
					if (session != null && session.isOpened()) {
						makeMeRequest(session, v); 
					} 						  			    	
			    }
			  }
			});			

	}
	
	public void anonymousLogin(View v){
		mixpanel.track("Alternate login button clicked" , jsonUser);
	    if(progressDialog==null || !progressDialog.isShowing()) progressDialog = ProgressDialog.show(
	    		ViewPagerSignIn.this, "", "Signing up...", true);	
		if(mUserID.getText().toString() != null && !PhoneNumberUtils.isWellFormedSmsAddress(mPhoneNumber)) {
			mPhoneNumber = mUserID.getText().toString();
			if(!PhoneNumberUtils.isWellFormedSmsAddress(mPhoneNumber)) {
				Toast.makeText(mContext, "Not a valid number", Toast.LENGTH_LONG).show();
				mixpanel.track("Invalid phone number " + mPhoneNumber, jsonUser);
				return;
			}
		}
		try {
			mPasswordString = MainApplication.generateStrongPasswordHash(mPhoneNumber,mSerialNumber);
			mApp.saveUserInfo(mPhoneNumber,mPasswordString);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}	

	protected void displayAlternateLoginOption() {
		// TODO Auto-generated method stub
		mSignInButton.setVisibility(View.GONE);
		mGenericSignInButton.setVisibility(View.VISIBLE);
		mGenericSignInButton.setBackgroundResource(R.drawable.neutral_signin_button);
		mGenericSignInButton.setTextColor(Color.WHITE);
		mGenericSignInButton.setText("Sign up");
		
		if(!PhoneNumberUtils.isWellFormedSmsAddress(mPhoneNumber)) {
			mAssurance.setText("Confirm Phone Number");
			mUserID.setVisibility(View.VISIBLE);
			mixpanel.track("Internal phone number is not well formed " + mPhoneNumber , jsonUser);
		}
		else {
			mAssurance.setText("");
			mixpanel.track("Internal phone number is well formed" , jsonUser);
		}
	}
	
	public void toggleGenericLogin(View v) {
		if(mSignInButton.getVisibility()==View.VISIBLE) displayAlternateLoginOption();
		else displayFacebookLoginOption();
	}
	
	private void displayFacebookLoginOption() {
		mSignInButton.setVisibility(View.VISIBLE);
		//mGenericSignInButton.setVisibility(View.GONE);
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
	           .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   dialog.cancel();
	            	   onBoardAsAlphaTester();
	            	 
	               	}
	           })
	           .setPositiveButton("No", new DialogInterface.OnClickListener() {
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
	private void makeMeRequest(Session session, final View v) {
		mixpanel.track("makeMeRequest", jsonUser);
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						showdialog = false;
						store.saveFacebookID(mPhoneNumber, user.getId());
						mixpanel.track("makeMeRequest completed", jsonUser);
						anonymousLogin(v);	
						if (user != null) {
							mixpanel.track("makeMeRequest found graph user", jsonUser);
							//Log.d("tag","Found graph user");
							augmentProfile(user);
							
						} else if (response.getError() != null) {
							mixpanel.track("makeMeRequest error " + response.getError().getCategory(), jsonUser);
							if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
									|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
								Log.d("tag","The facebook session was invalidated.");
							} else {
								Log.d("tag","Some other error: "+ response.getError().getErrorMessage());
							}
						}
					}
				});
		request.executeAsync();

	}	
	private void augmentProfile(GraphUser user){
		FacebookUser fb = new FacebookUser(user,mPhoneNumber);  
    	fb.saveInBackground(new SaveCallback(){
        	public void done(ParseException e) {
        		if(e==null) {
        			Log.i("saved fb user succcess","saved success");
        			mixpanel.track("saved fb user ", jsonUser);
        		}
        		else {
        			mixpanel.track("saved fb user NOT saved failure " + e.getMessage(), jsonUser);
        		}
			 }
		 });	
	}	
		
    private boolean appInstalled(String uri)
    {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try
        {
               pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
               app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
               app_installed = false;
        }
        return app_installed ;
    }
    
    private boolean appRunning(String uri) {
        ActivityManager activityManager = (ActivityManager) this.getSystemService( ACTIVITY_SERVICE );
        List<RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for(int i = 0; i < procInfos.size(); i++)
        {
            if(procInfos.get(i).processName.equals(uri)) 
            {
                return true;
            }
        }    
        return false;
    }
	@Override
	protected void onDestroy() {
		mixpanel.flush();
	    super.onDestroy();
	}	  		
}