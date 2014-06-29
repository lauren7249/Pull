package com.Pull.pullapp;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.ThreadsColumns;
import android.support.v4.app.NavUtils;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.model.GraphUser;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class AllThreadsListActivity extends SherlockListActivity {
	
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
	private int currentTab, shareType;
	private DatabaseHandler dbHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.threads_listactivity);
	    mContext = getApplicationContext();
	    ParseAnalytics.trackAppOpened(getIntent());
	    dbHandler = new DatabaseHandler(mContext);
	    mApp = (MainApplication) this.getApplication();
	    mPhoneNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();

	    listview = getListView();
	    
	    currentTab = getIntent().getIntExtra(Constants.EXTRA_TAB_ID,R.id.my_conversation_tab);
	    Log.i("about to make adapter",currentTab + " + ");
	    radioGroup  = (RadioGroup) findViewById(R.id.switch_buttons);   
	    radioGroup.check(currentTab);	
	    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			    	
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				currentTab = checkedId;		
				populateList();
			}
		});
		
	   /** listview.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
	    	 
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view,
					ContextMenuInfo menuInfo) {			
		        menu.setHeaderTitle("Options");
		        menu.add(0, CONTEXTMENU_CONTACTITEM, 0, "Add to Contacts");
				
			}
	    });	   **/		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		populateList();
	}
	


	private void populateList() {
		switch(currentTab) {
		case R.id.shared_tab:
			shareType = TextBasedSmsColumns.MESSAGE_TYPE_SENT;
			threads_cursor = dbHandler.getSharedConversationCursor(shareType);
		    adapter = new ThreadItemsCursorAdapter(mContext, threads_cursor, currentTab);
		    setListAdapter(adapter);  	
		    return;
		case R.id.shared_with_me_tab:
			shareType = TextBasedSmsColumns.MESSAGE_TYPE_INBOX;
			threads_cursor = dbHandler.getSharedConversationCursor(shareType);
		    adapter = new ThreadItemsCursorAdapter(mContext, threads_cursor, currentTab);
		    setListAdapter(adapter);  		
		    return;
		case R.id.my_conversation_tab: 
		    threads_cursor = ContentUtils.getThreadsCursor(mContext);
		    adapter = new ThreadItemsCursorAdapter(mContext, threads_cursor, currentTab);
		    setListAdapter(adapter);  	
		    return;
		default: 
			
		}	
	}

	// when a user selects a menu item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		Class c;
		switch (item.getItemId()) {
		case R.id.new_message:
			switch(currentTab) {
				case R.id.my_conversation_tab: c = MessageActivityCheckboxCursor.class;
				default: c = MessageActivityCheckboxCursor.class;
			}
            intent = new Intent(mContext, c);
            startActivity(intent);
			return true;	
		case R.id.settings:
            intent = new Intent(mContext, UserSettings.class);
            startActivity(intent);
			return true;				
		default:
			return false;
		}
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
		switch(currentTab) {
		case R.id.my_conversation_tab: 
	    	Cursor threads  = (Cursor) getListView().getItemAtPosition(position);
	    	String threadID = threads.getString(threads
		  		      .getColumnIndex(ThreadsColumns._ID));   
	  		boolean read = (!threads.getString(threads
		  		      .getColumnIndex(ThreadsColumns.READ)).equals("0"));    	
			String recipientId = threads.getString(threads
				      .getColumnIndex(ThreadsColumns.RECIPIENT_IDS));
			threads.close();
			String number = ContentUtils.getAddressFromID(mContext, recipientId);
			String name = ContentUtils
					.getContactDisplayNameByNumber(mContext, number);  		
	        Intent intent = new Intent(mContext, MessageActivityCheckboxCursor.class);
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
	        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
	        intent.putExtra(Constants.EXTRA_THREAD_ID,threadID);
	        intent.putExtra(Constants.EXTRA_NAME,name);
	        intent.putExtra(Constants.EXTRA_READ,read);
	        intent.putExtra(Constants.EXTRA_NUMBER,PhoneNumberUtils.stripSeparators(number));
	        startActivity(intent);      
		default:
		}    	

    }   
    
	 // make the menu work
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//actionbar menu
		getSupportMenuInflater().inflate(R.menu.threads_list_menu, menu);
		return true;
	}		
/**
    public boolean onContextItemSelected(MenuItem aItem) {
    	Toast.makeText(mContext, " + ", Toast.LENGTH_LONG).show();
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
        int position = menuInfo.position;
        String recipientID = adapter.number_hash.get(position);
        String number = ContentUtils.getAddressFromID(mContext, recipientID);
        //Toast.makeText(mContext, " + "+  number, Toast.LENGTH_LONG).show();
        switch (aItem.getItemId()) {
            case CONTEXTMENU_DELETEITEM:
                Toast.makeText(mContext, "not yet implemented", Toast.LENGTH_LONG).show();
                return true;
            case CONTEXTMENU_CONTACTITEM:
            	
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
                startActivity(intent);            	
                return true;
        }
        return false;
    }	**/  

	@Override
	protected void onStart() {
		super.onStart();	
	}	

	@Override
	protected void onPause(){
		super.onPause();
	}
		
	/**  private void startLoginActivity(int signin_result) {
	Intent intent = new Intent(this, ViewPagerSignIn.class);
	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	intent.putExtra(Constants.EXTRA_SIGNIN_RESULT, signin_result);
	startActivity(intent);
	
}


protected void onLogoutButtonClicked() {
	ParseUser.logOut();
	startLoginActivity(0);
	
}
**/    
} 
