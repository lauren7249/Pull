package com.Pull.pullapp;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.ThreadsColumns;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.Pull.pullapp.model.ThreadItem;
import com.Pull.pullapp.util.AlarmScheduler;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class ThreadsListActivity extends ListActivity {
	
	private ThreadItemsCursorAdapter adapter;
	private ListView listview;
	private Context mContext;
	private RadioGroup radioGroup;
    protected static final int CONTEXTMENU_DELETEITEM = 0;
    protected static final int CONTEXTMENU_CONTACTITEM = 1;	
	private SharedPreferences prefs;
	private String mPhoneNumber;
	private TelephonyManager tMgr;
	private int errorCode;
	private MainApplication mApp;	
	private GraphUser mGraphUser;
	private String mFacebookID;	
	private ParseUser currentUser;
	private Cursor threads_cursor;
	private String mPassword;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.threads_listactivity);
	    mContext = getApplicationContext();
	    ParseAnalytics.trackAppOpened(getIntent());	
	    
	    mApp = (MainApplication) this.getApplication();
	    mPhoneNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
		mPassword = mPhoneNumber;
		
	    listview = getListView();
	    long time1 = System.currentTimeMillis();
	    threads_cursor = ContentUtils.getThreadsCursor(mContext);
	    long time2 = System.currentTimeMillis();
	    
	    adapter = new ThreadItemsCursorAdapter(
	            mContext, threads_cursor);
	    long time3 = System.currentTimeMillis();
	    
	    setListAdapter(adapter);  
	    long time4 = System.currentTimeMillis();
	    Log.i("tag","time to set adapter " + (time4-time3));
	    if(Constants.LOG_SMS) new AlarmScheduler(mContext, false).start();

	    radioGroup  = (RadioGroup) findViewById(R.id.switch_buttons);   
	    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			    	
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Intent i = new Intent(mContext, SharedListActivity.class);			
				if(checkedId==R.id.shared_tab){
					radioGroup.check(R.id.shared_tab);	
					i.putExtra(Constants.EXTRA_SHARE_TYPE, TextBasedSmsColumns.MESSAGE_TYPE_SENT);
					startActivity(i);
				}else if(checkedId==R.id.shared_with_me_tab){
					radioGroup.check(R.id.shared_with_me_tab);	
					i.putExtra(Constants.EXTRA_SHARE_TYPE, TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
					startActivity(i);
				}
			}
		});
	    
        Button button = (Button) findViewById(R.id.new_message);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
                Intent intent =
                        new Intent(mContext, MessageActivityCheckbox.class);
                startActivity(intent);
            }
        });	    
        
		
	    listview.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
	    	 
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view,
					ContextMenuInfo menuInfo) {			
		        menu.setHeaderTitle("Options");
		        menu.add(0, CONTEXTMENU_CONTACTITEM, 0, "Add to Contacts");
				
			}
	    });	   
		// Fetch Facebook user info if the session is active
		Session session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			makeMeRequest(session);
		}
	    long time5 = System.currentTimeMillis();
	    Log.i("tag","time for oncreate " + (time5-time1));			
		
	}

    public boolean onContextItemSelected(MenuItem aItem) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
        int position = menuInfo.position;
        final ThreadItem item = (ThreadItem) listview.getAdapter().getItem(position);
        String number = item.number;
        switch (aItem.getItemId()) {
            case CONTEXTMENU_DELETEITEM:
                Toast.makeText(mContext, "not yet implemented", Toast.LENGTH_LONG).show();
                return true;
            case CONTEXTMENU_CONTACTITEM:
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
                
                mContext.startActivity(intent);            	
                return true;
        }
        return false;
    }	
	@Override
	protected void onResume() {
		super.onResume();
		long time1 = System.currentTimeMillis();
		threads_cursor = ContentUtils.getThreadsCursor(mContext);
		adapter.notifyDataSetChanged();
		currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			radioGroup.check(R.id.my_conversation_tab);
			listview.invalidateViews();
			listview.refreshDrawableState();
		} else {
		}		
	    long time5 = System.currentTimeMillis();
	    Log.i("tag","time for onresume " + (time5-time1));		
	}

	  private void startLoginActivity(int signin_result) {
		Intent intent = new Intent(this, ViewPagerSignIn.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(Constants.EXTRA_SIGNIN_RESULT, signin_result);
		startActivity(intent);
		
	}

	private void makeMeRequest(Session session) {
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
				

					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (user != null) {
							mGraphUser = user;
							mFacebookID = user.getId();
							
							linkAccount();
							saveUserInfo(mPhoneNumber,mPassword);
							finishSavingUser();
						} else if (response.getError() != null) {
							if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
									|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
								Log.d("tag",
										"The facebook session was invalidated.");
								otherLoginMethod();
							} else {
								Log.d("tag",
										"Some other error: "
												+ response.getError()
														.getErrorMessage());
							}
						}
					}
				});
		request.executeAsync();

	}
	protected void otherLoginMethod() {
		try {
			mPassword = generateStrongPasswordHash(mPhoneNumber);
			Log.i("password",mPassword);
			saveUserInfo(mPhoneNumber,mPassword);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}

	protected void onLogoutButtonClicked() {
		ParseUser.logOut();
		startLoginActivity(0);
		
	}

	private void saveUserInfo(final String username, final String password) {
    	ParseQuery<ParseUser> query = ParseUser.getQuery();
    	query.whereEqualTo("username", ContentUtils.addCountryCode(mPhoneNumber));
    	query.findInBackground(new FindCallback<ParseUser>() {
    	  public void done(List<ParseUser> objects, ParseException e) {
    	    if (objects.size()>0) {
    	    	//currentUser = objects.get(0);
    	    	finishSavingUser();
    	    } else {
    	    	signUp(username, password);
    	    }
    	  }
    	});		
		
	}	
	
	protected void finishSavingUser() {
		saveInstallation();
		mApp.setSignedIn(true, mPhoneNumber, mPassword);
	}

	protected void signUp(String username, String password) {
		currentUser = new ParseUser();
		currentUser.setUsername(username);
		currentUser.setPassword(password);
		currentUser.signUpInBackground(new SignUpCallback() {
        	  public void done(ParseException e) {
        	    if (e == null) {
        	    	Log.i("saved","data saved to server");    
        			if(mGraphUser != null) {
        				currentUser.put("facebookId", mGraphUser.getId());
        				currentUser.put("facebookID", mGraphUser.getId());
        				currentUser.put("name", mGraphUser.getName());
        				if(mGraphUser.getLocation() != null) {
        					if (mGraphUser.getLocation().getProperty("name") != null) {
        						currentUser.put("location", (String) mGraphUser
        								.getLocation().getProperty("name"));
        					}
        				}
        				if (mGraphUser.getProperty("gender") != null) {
        					currentUser.put("gender",
        							(String) mGraphUser.getProperty("gender"));
        				}
        				if (mGraphUser.getBirthday() != null) {
        					currentUser.put("birthday",
        							mGraphUser.getBirthday());
        				}
        				if (mGraphUser.getProperty("relationship_status") != null) {
        					currentUser
        							.put("relationship_status",
        									(String) mGraphUser
        											.getProperty("relationship_status"));
        				}	
        			}
        			currentUser.saveInBackground();
        	    } else {
        	    	Log.i("not saved","not saved " + e.getCode());
        	    }
        	    checkUser();
        	  }
        	});	
		finishSavingUser();
	}

	protected void checkUser() {
    	ParseQuery<ParseUser> query = ParseUser.getQuery();
    	query.whereEqualTo("username", ContentUtils.addCountryCode(mPhoneNumber));
    	query.findInBackground(new FindCallback<ParseUser>() {
    	  public void done(List<ParseUser> objects, ParseException e) {
    	    if (e == null && objects.size()>0) {
    	    	currentUser = objects.get(0);
    	    	finishSavingUser();
    	    } else {
    	    	otherLoginMethod();
    	    }
    	  }
    	});
		
  
		
	}

	private void augmentProfile(GraphUser user){
		currentUser = ParseUser.getCurrentUser();
		currentUser.put("facebookID", user.getId());
		currentUser.put("name", user.getName());
		if(user.getLocation() != null) {
			if (user.getLocation().getProperty("name") != null) {
				currentUser.put("location", (String) user
						.getLocation().getProperty("name"));
			}
		}
		if (user.getProperty("gender") != null) {
			currentUser.put("gender",
					(String) user.getProperty("gender"));
		}
		if (user.getBirthday() != null) {
			currentUser.put("birthday",
					user.getBirthday());
		}
		if (user.getProperty("relationship_status") != null) {
			currentUser
					.put("relationship_status",
							(String) user
									.getProperty("relationship_status"));
		}
		currentUser.saveInBackground();
		Log.i("tag","augmented profile");
	}
	@Override
	protected void onPause(){
		super.onPause();
		threads_cursor.close();
	}
	
	
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // Note: don't read the thread id data from the ConversationListItem view passed in.
        // It's unreliable to read the cached data stored in the view because the ListItem
        // can be recycled, and the same view could be assigned to a different position
        // if you click the list item fast enough. Instead, get the cursor at the position
        // clicked and load the data from the cursor.
        // (ConversationListAdapter extends CursorAdapter, so getItemAtPosition() should
        // return the cursor object, which is moved to the position passed in)
    	Cursor threads  = (Cursor) getListView().getItemAtPosition(position);
    	String threadID = threads.getString(threads
	  		      .getColumnIndex(ThreadsColumns._ID));   
  		boolean read = (!threads.getString(threads
	  		      .getColumnIndex(ThreadsColumns.READ)).equals("0"));    	
		String[] recipientIDs = threads.getString(threads
			      .getColumnIndex(ThreadsColumns.RECIPIENT_IDS)).split(" ");
		if(recipientIDs.length == 0) return;
		threads.close();
		String recipientId = recipientIDs[0];
		String number = ContentUtils.getAddressFromID(mContext, recipientId);
		String name = ContentUtils
				.getContactDisplayNameByNumber(mContext, number);  		
        Intent intent = new Intent(mContext, MessageActivityCheckbox.class);
        intent.putExtra(Constants.EXTRA_THREAD_ID,threadID);
        intent.putExtra(Constants.EXTRA_NAME,name);
        intent.putExtra(Constants.EXTRA_READ,read);
        intent.putExtra(Constants.EXTRA_NUMBER,PhoneNumberUtils.stripSeparators(number));
        startActivity(intent);        

    }    	
	/**
	 * Links a parse user to a facebook account if the user is not already linked
	 * @param user
	 */
	private void linkAccount() {
		if (!ParseFacebookUtils.isLinked(currentUser)) {
			  ParseFacebookUtils.link(currentUser, this, new SaveCallback() {
			    @Override
			    public void done(ParseException ex) {
			    	if(ex != null) {
			    		Log.i("tried to link account but error code " , " " +ex.getCode());
			    	}
			    	else {
				       if (ParseFacebookUtils.isLinked(currentUser)) {
				    	   Log.d("MyApp", "Woohoo, user logged in with Facebook!");
				       } else {
				    	   Log.i("signin.linkaccount","did not link with facebook");
				       }
			    	}
			    
			    }
			  });
		} else {
			Log.i("signin.linkaccount","user already linked in facebook");
		}
		
	}	
	private void saveInstallation(){
       ParseInstallation installation = ParseInstallation.getCurrentInstallation();
       installation.put("user", ParseUser.getCurrentUser());
       installation.addAllUnique("channels", Arrays.asList(ContentUtils.setChannel(mPhoneNumber)));
       installation.saveInBackground();				
	}
	
    private static String generateStrongPasswordHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        int iterations = 1000;
        char[] chars = password.toCharArray();
        byte[] salt = getSalt().getBytes();
         
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

} 
