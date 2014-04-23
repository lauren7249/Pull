package com.Pull.pullapp;
import java.util.ArrayList;
import java.util.Arrays;

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
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.PushService;
import com.parse.ParseFacebookUtils.Permissions;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.listactivity);
	    mContext = getApplicationContext();
	    ParseAnalytics.trackAppOpened(getIntent());	
	    
	    mApp = (MainApplication) this.getApplication();
	    
	    if(Constants.LOG_SMS) new AlarmScheduler(mContext, false).start();

	    radioGroup  = (RadioGroup) findViewById(R.id.switch_buttons);
		   
	    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==R.id.shared_tab){
					Intent i = new Intent(mContext, SharedListActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					i.putExtra(Constants.EXTRA_SHARE_TYPE, TextBasedSmsColumns.MESSAGE_TYPE_SENT);
					startActivity(i);
					overridePendingTransition(0,0);
				}else if(checkedId==R.id.shared_with_me_tab){
					Intent i = new Intent(mContext, SharedListActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					i.putExtra(Constants.EXTRA_SHARE_TYPE, TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
					startActivity(i);
					overridePendingTransition(0,0);
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
       
	    thread_list = new ArrayList<ThreadItem>();
	    listview = (ListView) findViewById(R.id.listview);
	    adapter = new ThreadItemsListAdapter(getApplicationContext(),
	    		R.layout.message_list_item, thread_list);	  
	    listview.setAdapter(adapter);
	    new GetThreads().execute();    
	    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	
		      public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
		    	  final ThreadItem item = (ThreadItem) parent.getItemAtPosition(position);
    
		          Intent intent = new Intent(mContext, MessageActivityCheckbox.class);
		          intent.putExtra(Constants.EXTRA_THREAD_ID,item.ID);
		          intent.putExtra(Constants.EXTRA_NAME,item.displayName);
		          intent.putExtra(Constants.EXTRA_READ,item.read);
		          intent.putExtra(Constants.EXTRA_NUMBER,PhoneNumberUtils.stripSeparators(item.number));
		          //Log.i("phone number",PhoneNumberUtils.stripSeparators(item.number));
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
	    mContext = getApplicationContext();
	    tMgr =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
	    mPhoneNumber = tMgr.getLine1Number();	
	    	
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			mApp.setSignedIn(true);
		} else {
			mApp.setSignedIn(false);
			if(signIn() == ParseException.OBJECT_NOT_FOUND) {
				errorCode = 0;
				if(signUp() != 0) Log.i("error code:","error code:"+ errorCode);
			}
			
		}	
		if (mApp.isSignedIn()) 
			PushService.subscribe(mContext, ContentUtils.setChannel(mPhoneNumber), 
					ThreadsListActivity.class);				
	}
	protected void linkAccount(final ParseUser user) {
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
		radioGroup.check(R.id.my_conversation_tab);
		((ThreadItemsListAdapter) listview.getAdapter()).notifyDataSetChanged();
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
				threads.close();

		    }			

	  }
	  
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}	  
	
	
	public int signUp() {
		ParseUser user = new ParseUser();
		user.setUsername(mPhoneNumber);
		user.setPassword(mPhoneNumber);
		
		try {
			user.signUp();
	      // Hooray! Let them use the app now.
	    	mApp.setSignedIn(true);
		} catch (ParseException e) {
			errorCode = e.getCode();
		}
		return errorCode;
	}		
	
	public int signIn() {
		try {
			ParseUser.logIn(mPhoneNumber, mPhoneNumber);
	    	mApp.setSignedIn(true);
		} catch (ParseException e) {
			errorCode = e.getCode();
		}		
		return errorCode;
	}		
	} 
