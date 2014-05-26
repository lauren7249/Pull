package com.Pull.pullapp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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
import android.widget.AdapterView;
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

public class ThreadsListActivity extends Activity {
	
	private ThreadItemsListAdapter adapter;
	private ListView listview;
	private ArrayList<ThreadItem> thread_list;
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.threads_listactivity);
	    mContext = getApplicationContext();
	    ParseAnalytics.trackAppOpened(getIntent());	
	    
	    mApp = (MainApplication) this.getApplication();
	    mPhoneNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
		
	    thread_list = new ArrayList<ThreadItem>();
	    listview = (ListView) findViewById(R.id.listview);
	    adapter = new ThreadItemsListAdapter(getApplicationContext(),
	    		R.layout.message_list_item, thread_list);	  
	    listview.setAdapter(adapter);
	    new GetThreads().execute();   
	    
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
		currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			radioGroup.check(R.id.my_conversation_tab);
			new GetThreads().execute();  
			listview.invalidateViews();
			listview.refreshDrawableState();
		} else {
			// If the user is not logged in, go to the
			// activity showing the login view.
			startLoginActivity();
		}		
			
	}

	  private void startLoginActivity() {
		Intent intent = new Intent(this, ViewPagerSignIn.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		
	}

	private class GetThreads extends AsyncTask<Void,ThreadItem,Void> {
		  	Cursor threads;
		  	@Override
			protected Void doInBackground(Void... params) {
				threads = ContentUtils.getThreadsCursor(mContext);
				while (threads.moveToNext()) {
			    	String threadID = threads.getString(threads
			  		      .getColumnIndex(ThreadsColumns._ID));
			    	String snippet = threads.getString(threads
				  		      .getColumnIndex(ThreadsColumns.SNIPPET));			    	
			    	if(threadID == null) continue;
			    	if(threadID.length()==0) continue;
			    	boolean read = (!threads.getString(threads
				  		      .getColumnIndex(ThreadsColumns.READ)).equals("0"));	    	
					String[] recipientIDs = threads.getString(threads
				      .getColumnIndex(ThreadsColumns.RECIPIENT_IDS)).split(" ");

					if(recipientIDs.length == 1 && recipientIDs[0].length()>0) {
						String recipientId = recipientIDs[0];
						String number = ContentUtils.getAddressFromID(mContext, recipientId);
						String name = ContentUtils
								.getContactDisplayNameByNumber(mContext, number);
						ThreadItem t = new ThreadItem(threadID, name, number, snippet, read);
						publishProgress(t);						

					}
					
			    }	
				return null;
			}
			@Override
		    protected void onProgressUpdate(ThreadItem... t) {
				adapter.addItem(t[0]);	
				adapter.notifyDataSetChanged();
		    }				
			
			@Override
		    protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				adapter.notifyDataSetChanged();
				threads.close();
		    }			

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
							augmentProfile(mGraphUser);
							saveUserInfo(mPhoneNumber,mFacebookID);
						} else if (response.getError() != null) {
							if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
									|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
								Log.d("tag",
										"The facebook session was invalidated.");
								onLogoutButtonClicked();
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
	protected void onLogoutButtonClicked() {
		ParseUser.logOut();
		startLoginActivity();
		
	}

	private void saveUserInfo(final String username, final String password) {
    	ParseQuery<ParseUser> query = ParseUser.getQuery();
    	query.whereEqualTo("username", ContentUtils.addCountryCode(mPhoneNumber));
    	query.findInBackground(new FindCallback<ParseUser>() {
    	  public void done(List<ParseUser> objects, ParseException e) {
    	    if (objects.size()>0) {
    	    	currentUser = objects.get(0);
    	    	finishSavingUser();
    	    } else {
    	    	signUp(username, password);
    	    }
    	  }
    	});		
		
	}	
	
	protected void finishSavingUser() {
		saveInstallation();
		mApp.setSignedIn(true, mPhoneNumber, mFacebookID);
	}

	protected void signUp(String username, String password) {
		currentUser.setUsername(username);
		currentUser.setPassword(password);
		currentUser.signUpInBackground(new SignUpCallback() {
        	  public void done(ParseException e) {
        	    if (e == null) {
        	    	Log.i("saved","data saved to server");    	       
        	    } else {
        	    	Log.i("not saved","not saved " + e.getCode());
        	    }
        	    checkUser();
        	  }
        	});	
		
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
    	    	onLogoutButtonClicked();
    	    }
    	  }
    	});
		
  
		
	}

	private void augmentProfile(GraphUser user){
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
       installation.put("user", currentUser);
       installation.addAllUnique("channels", Arrays.asList(ContentUtils.setChannel(mPhoneNumber)));
       installation.saveInBackground();				
	}

} 
