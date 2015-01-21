package com.Pull.pullapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;

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
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Pull.pullapp.model.FacebookUser;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.data.ContentUtils;
import com.Pull.pullapp.util.data.UserInfoStore;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ViewPagerSignIn extends BaseActivity {
	private Timer myTimer;
	private ParseUser mParseUser;
    private Button mSignInButton;
	private SharedPreferences prefs;
	private Context mContext;
	private String mPhoneNumber, mPasswordSalt;
	private MainApplication mApp;
	private TelephonyManager tMgr;
	private int errorCode = 0;
	private String mVerificationCode;
	ProfilePictureView profilePictureView;
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
	private boolean receiverIsTrumped;
	private PhoneNumberFormattingTextWatcher mWatcher;
	private TextView mTerms;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);
        mLayout = (LinearLayout) findViewById(R.id.main_layout);
       // mBottomHalf = (RelativeLayout) findViewById(R.id.signin_area);
       // mAdapter = new SignInFragmentAdapter(getSupportFragmentManager());

      //  mPager = (ViewPager)findViewById(R.id.pager);
      //  mPager.setAdapter(mAdapter);

      //  mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        //mIndicator.setViewPager(mPager);
        
        mContext = getApplicationContext();
        
        store = new UserInfoStore(mContext);
        
        mSignInButton = (Button) findViewById(R.id.sign_in);
        mGenericSignInButton = (Button) findViewById(R.id.generic_sign_in);
        mUserID = (EditText) findViewById(R.id.mobileNumber);
        mPassword = (EditText) findViewById(R.id.password);
        mConfirmPassword = (EditText) findViewById(R.id.confirmPassword);
        mAssurance = (TextView) findViewById(R.id.assurance);
        mKeyHashBox = (EditText) findViewById(R.id.keyhash);
        mTerms = (TextView) findViewById(R.id.privacy);
        mTerms.setMovementMethod(LinkMovementMethod.getInstance());  
        mTerms.setText(R.string.terms_of_service);
        
	    prefs = getSharedPreferences(MainApplication.class.getSimpleName(), Context.MODE_PRIVATE);
	    tMgr =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
	    mApp = (MainApplication) this.getApplication();   
	    
	    if(mApp.getUserName()==null) {
	    	mPhoneNumber = ContentUtils.addCountryCode(tMgr,tMgr.getLine1Number());
	    	//mPhoneNumber = "";
	    }
	    else mPhoneNumber = mApp.getUserName();
	    
	    if(mApp.getPasswordSalt()==null) {
		    mPasswordSalt = tMgr.getSimSerialNumber();
			//mPasswordSalt = null;
	    }
	    else mPasswordSalt = mApp.getPasswordSalt();	 
	    
	    Intent intent = new Intent("android.provider.Telephony.SMS_RECEIVED");
	    List<ResolveInfo> infos = getPackageManager().queryBroadcastReceivers(intent, 0);
	    for (ResolveInfo info : infos) {
	      //  System.out.println("Receiver name:" + info.activityInfo.name + "; priority=" + info.priority);
	    	if(info.priority>=2147483647) receiverIsTrumped = true;
	    }
			    
		mBroadcastReceiver = 
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				
				if(action.equals(Constants.ACTION_COMPLETE_SIGNUP)) {
					if(ParseUser.getCurrentUser()!=null
							&& ParseUser.getCurrentUser().isAuthenticated()) {
						if(ParseUser.getCurrentUser().get("profilePhoto") ==null && false) openPhotoPicker();
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
				if(action.equals(Constants.ACTION_NUMBER_VERIFIED)) {
					hideKeyboard();
					mApp.setUsername(mPhoneNumber);
					mApp.setPasswordSalt(mVerificationCode);					
					mAssurance.setText("Number verified!");
					mAssurance.clearFocus();
					mAssurance.setTextColor(Color.BLACK);
					//mAssurance.setVisibility(View.GONE);
				    if(appRunning("com.facebook.katana")) {
				    	mixpanel.track("Facebook is running", jsonUser);
				    	displayFacebookLoginOption();
				    }
				    else {
				    	mixpanel.track("Facebook not running", jsonUser);
				    //	displayAlternateLoginOption();
				    	basicLogin(new View(mContext));
				    }						
					return;
				}	
				if(action.equals(Constants.ACTION_NUMBER_NOT_VERIFIED)) {
					mAssurance.setText("Could not verify number.");
					mAssurance.setTextColor(Color.RED);
					//displayPhoneNumberConfirmation();					
					return;
				}

			}
		};	
		
		intentFilter = new IntentFilter();	
		intentFilter.addAction(Constants.ACTION_COMPLETE_SIGNUP);		
		intentFilter.addAction(Constants.ACTION_NUMBER_VERIFIED);	
		intentFilter.addAction(Constants.ACTION_NUMBER_NOT_VERIFIED);
		registerReceiver(mBroadcastReceiver, intentFilter);		
		
	    jsonUser = new JSONObject();
	    try {
	    	jsonUser.put("mPhoneNumber", mPhoneNumber);
	    	jsonUser.put("mPasswordSalt", mPasswordSalt);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
		mixpanel = MixpanelAPI.getInstance(mContext, Constants.MIXEDPANEL_TOKEN);
		mixpanel.identify(ParseInstallation.getCurrentInstallation().getObjectId());
	//	mixpanel.track("ViewPagerSignIn created", jsonUser);
		mixpanel.track("Phone number " + mPhoneNumber , jsonUser);
	   // mUserID.setText(mPhoneNumber);
		
	 // Find the user's profile picture custom view
	//    profilePictureView = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
	  //  profilePictureView.setCropped(true);
	    
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
		if(mPhoneNumber!=null && mPasswordSalt!=null
				 && !mPhoneNumber.isEmpty() && !mPasswordSalt.isEmpty())	{
			mApp.setUsername(mPhoneNumber);
			mApp.setPasswordSalt(mPasswordSalt);
			if (mParseUser!=null &&  mParseUser.getUsername()!=null && !mParseUser.getUsername().isEmpty() &&
					mParseUser.isDataAvailable() && mParseUser.isAuthenticated()) {
				mixpanel.track("Authenticated & signed in", jsonUser);
				try {
					mPasswordString = MainApplication.generateStrongPasswordHash(mPhoneNumber,mPasswordSalt);
					ParseUser.logIn(mPhoneNumber, mPasswordString);
					if(mParseUser.get("profilePhoto")==null && false) openPhotoPicker();
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
			mUserID.setVisibility(View.GONE);
			mPassword.setVisibility(View.GONE);
			mConfirmPassword.setVisibility(View.GONE);			
		    if(appRunning("com.facebook.katana")) {
		    	mixpanel.track("Facebook is running", jsonUser);
		    	displayFacebookLoginOption();
		    }
		    else {
		    	mixpanel.track("Facebook not running", jsonUser);
		    	displayAlternateLoginOption();
		    }					
		} else {
			mixpanel.track("Need to confirm phone number", jsonUser);
			displayPhoneNumberConfirmation();
		}
		
	    // Dirty Hack to detect keyboard
		mLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				if ((mLayout.getRootView().getHeight() - mLayout.getHeight()) > mLayout.getRootView().getHeight() / 3) {
				//	mBottomHalf.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 100f));
				} else {
				//	mBottomHalf.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 2f));
				}
			}
		});		
		   
	      
	
    }
	private void displayPhoneNumberConfirmation() {
		final Random randomGenerator = new Random();
		mWatcher = new PhoneNumberFormattingTextWatcher();
		mUserID.setVisibility(View.VISIBLE);
		mUserID.addTextChangedListener(mWatcher);
		mPassword.setVisibility(View.GONE);
		mConfirmPassword.setVisibility(View.GONE);
		mSignInButton.setVisibility(View.GONE);
		mGenericSignInButton.setVisibility(View.VISIBLE);
		mGenericSignInButton.setBackgroundResource(R.drawable.neutral_signin_button);
		mGenericSignInButton.setTextColor(Color.WHITE);
		mGenericSignInButton.setText("Confirm");	
		mGenericSignInButton.setTextSize(20);
		mGenericSignInButton.setOnClickListener(new OnClickListener(){

			@SuppressWarnings("unchecked")
			@Override
			public void onClick(final View v) {
				mixpanel.track("user attempts to confirm phone number", jsonUser);
				mAssurance.setVisibility(View.VISIBLE);
				mPhoneNumber = ContentUtils.addCountryCode(
						PhoneNumberUtils.stripSeparators(mUserID.getText().toString()));
				mVerificationCode = Integer.toString(randomGenerator.nextInt(100000));
				store.saveVerificationCode(mVerificationCode);
				if(PhoneNumberUtils.isWellFormedSmsAddress(mPhoneNumber) &&
						PhoneNumberUtils.isGlobalPhoneNumber(mPhoneNumber)) {
					mAssurance.setText("Attempting verification...");
					mAssurance.setTextColor(Color.BLACK);		
					//mGenericSignInButton.setClickable(false);
					HashMap<String, Object> params = new HashMap<String, Object>();
					params.put("phoneNumber", mPhoneNumber);	
					params.put("verificationCode", mVerificationCode);	
					ParseCloud.callFunctionInBackground("phoneNumberVerification", params, new FunctionCallback() {
					

					@Override
						public void done(Object obj, ParseException e) {
				         	if (e == null) {
				         		if(obj!=null) {
									mAssurance.setText("Sending verification code...");
									mAssurance.setTextColor(Color.BLACK);
									mGenericSignInButton.setClickable(true);	
									if(receiverIsTrumped) {
										mAssurance.setText("Enter SMS verification code");
										mUserID.setText("");
										mUserID.removeTextChangedListener(mWatcher);
										mUserID.setHint("Enter code");
										mGenericSignInButton.setText("Confirm code");
										mGenericSignInButton.setOnClickListener(new OnClickListener(){
											
											@Override
											public void onClick(View v) {
												
												String code = mUserID.getText().toString();
									        	if(code.equals(store.getVerificationCode())) {
									        		mixpanel.track("user enters correct verification code", jsonUser);
									        	    Intent intent = new Intent(Constants.ACTION_NUMBER_VERIFIED);
									        	    mContext.sendBroadcast(intent);		
									        	}	
									        	else {
									        		mixpanel.track("user enters incorrect verification code", jsonUser);
													mAssurance.setText("Incorrect code. Please re-enter");
													mAssurance.setTextColor(Color.RED);								        		
									        	}
											}
											
										});
									}

				         		} 
				         		else {
									mAssurance.setText("Unable to verify");
									mAssurance.setTextColor(Color.BLACK);
									mGenericSignInButton.setClickable(true);				         			
				         		}
					        }
				         	else {
								mAssurance.setText(e.getMessage());
								mAssurance.setTextColor(Color.RED);
								mGenericSignInButton.setClickable(true);				         		
				         	}
						}
				  });						
				} else {
					mAssurance.setText("Invalid phone number");
					mAssurance.setTextColor(Color.RED);
					mGenericSignInButton.setClickable(true);
				}
				
			}
			
		});
		mAssurance.setTextSize(16);
		mAssurance.setText("Enter your mobile number");
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
			       basicLogin(v);	
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
	
	public void basicLogin(View v){
	    if(progressDialog==null || !progressDialog.isShowing()) progressDialog = ProgressDialog.show(
	    		ViewPagerSignIn.this, "", "Signing up...", true);	
		try {
			mPasswordString = MainApplication.generateStrongPasswordHash(mApp.getUserName(),mApp.getPasswordSalt());
			mApp.saveUserInfo(mApp.getUserName(), mPasswordString);
		} catch (NoSuchAlgorithmException e) {
			mixpanel.track(e.getLocalizedMessage(), jsonUser);
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			mixpanel.track(e.getLocalizedMessage(), jsonUser);
			e.printStackTrace();
		}
	}	

	protected void displayAlternateLoginOption() {
		// TODO Auto-generated method stub
		mSignInButton.setVisibility(View.GONE);
		mAssurance.setVisibility(View.GONE);
		mGenericSignInButton.setVisibility(View.VISIBLE);
		mGenericSignInButton.setBackgroundResource(R.drawable.neutral_signin_button);
		mGenericSignInButton.setTextColor(Color.WHITE);
		mGenericSignInButton.setText("Sign up");
		mGenericSignInButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mixpanel.track("Alternate login button clicked" , jsonUser);
				basicLogin(v);
				
			}
		});		
	}
	
	public void toggleGenericLogin(View v) {
		if(mSignInButton.getVisibility()==View.VISIBLE) displayAlternateLoginOption();
		else displayFacebookLoginOption();
	}
	
	private void displayFacebookLoginOption() {
		mSignInButton.setVisibility(View.VISIBLE);
		mUserID.setVisibility(View.GONE);
		mAssurance.setText("We will never post anything to Facebook.");
		mGenericSignInButton.setText("Sign up without Facebook");
		mGenericSignInButton.setBackgroundColor(Color.parseColor("#8fffffff"));
		mGenericSignInButton.setTextSize(14);
		mGenericSignInButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {  
				mixpanel.track("Alternate login button clicked" , jsonUser);
				basicLogin(v);
				
			}
		});			
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
						basicLogin(v);	
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
		if(ParseInstallation.getCurrentInstallation().getObjectId()==null ||
				ParseUser.getCurrentUser().getObjectId()==null) return;
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
	private void hideKeyboard(){
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mUserID.getWindowToken(), 0);	
		
	}	
}