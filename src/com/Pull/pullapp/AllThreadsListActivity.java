package com.Pull.pullapp;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.ThreadsColumns;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.Pull.pullapp.adapter.ThreadItemsCursorAdapter;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;
import com.Pull.pullapp.util.UserInfoStore;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.model.GraphUser;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;

public class AllThreadsListActivity extends SherlockListActivity {
	
	private ThreadItemsCursorAdapter adapter;
	private ListView listview;
	private Context mContext;
	private RadioGroup radioGroup;
    protected static final int CONTEXTMENU_DELETEITEM = 0;
    protected static final int CONTEXTMENU_CONTACTITEM = 1;	
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
    private RecipientsEditor mConfidantesEditor;
	private RecipientsAdapter mRecipientsAdapter;
    private LinearLayout mBox;
	private RecipientsEditor mConversantsEditor;	
	private String[] recipients;
	private String[] conversants;
	private String conversant;
	private String recipient;
	private boolean visible;	
	private Button hint;
	private UserInfoStore store;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.threads_listactivity);
	    mContext = getApplicationContext();
	    store = new UserInfoStore(mContext);
	    
	    ParseAnalytics.trackAppOpened(getIntent());
	    dbHandler = new DatabaseHandler(mContext);
	    mApp = (MainApplication) this.getApplication();
	    mPhoneNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
	    
	    hint = (Button) findViewById(R.id.hint);   
	    hint.setVisibility(View.VISIBLE);
	    listview = getListView();
	    
	    currentTab = getIntent().getIntExtra(Constants.EXTRA_TAB_ID,R.id.my_conversation_tab);

	    radioGroup  = (RadioGroup) findViewById(R.id.switch_buttons);   
	    radioGroup.check(currentTab);	
	    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			    	
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				currentTab = checkedId;		
				populateList();
			}
		});

	    mBox = (LinearLayout)findViewById(R.id.confidantes_box);
		mRecipientsAdapter = new RecipientsAdapter(this);
		mConfidantesEditor = (RecipientsEditor)findViewById(R.id.confidantes_editor);
		mConfidantesEditor.setAdapter(mRecipientsAdapter);
		mConversantsEditor = (RecipientsEditor)findViewById(R.id.recipient_editor);
		mConversantsEditor.setAdapter(mRecipientsAdapter);    	
    	
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
		String columns = DatabaseHandler.KEY_SHARED_WITH + 
				", " + DatabaseHandler.KEY_CONVERSATION_FROM_NAME +
				", " + DatabaseHandler.KEY_SHARER +
				", " + DatabaseHandler.KEY_ID + " as _id" +
				", " + TextBasedSmsColumns.TYPE ;
		switch(currentTab) {
		case R.id.shared_tab:
			shareType = TextBasedSmsColumns.MESSAGE_TYPE_SENT;
			threads_cursor = dbHandler.getSharedConversationCursor(columns);
		    adapter = new ThreadItemsCursorAdapter(mContext, threads_cursor, currentTab, this);
		    setListAdapter(adapter);  	
		    mBox.setVisibility(View.GONE);
		    hint.setVisibility(View.VISIBLE);
		    return;
		case R.id.my_conversation_tab: 
		    threads_cursor = ContentUtils.getThreadsCursor(mContext);
		    adapter = new ThreadItemsCursorAdapter(mContext, threads_cursor, currentTab, this);
		    setListAdapter(adapter); 
		    mBox.setVisibility(View.GONE);
		    hint.setVisibility(View.GONE);
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
				case R.id.my_conversation_tab: 
					c = MessageActivityCheckboxCursor.class;
		            intent = new Intent(mContext, c);
		            startActivity(intent);	
		            return true;
				default: 
					mBox.setVisibility(View.VISIBLE);
			}

			return true;	
		case R.id.settings:
            intent = new Intent(mContext, UserSettings.class);
            startActivity(intent);
			return true;				
		default:
			return false;
		}
	}	
	
	public void showShareBox(View v) {
		mBox.setVisibility(View.VISIBLE);
		hint.setVisibility(View.GONE);
		
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
    	Intent intent;
		switch(currentTab) {
		case R.id.my_conversation_tab: 
	        intent = new Intent(mContext, MessageActivityCheckboxCursor.class);
	  		boolean read = (!threads.getString(threads
		  		      .getColumnIndex(ThreadsColumns.READ)).equals("0"));    	
			String recipientId = threads.getString(threads
				      .getColumnIndex(ThreadsColumns.RECIPIENT_IDS));		
			threads.close();
			String number = store.getPhoneNumber(recipientId);
	    	if(number==null) {
				number = ContentUtils.getAddressFromID(mContext, recipientId);
				store.setPhoneNumber(recipientId,number);
	    	}				
			String name = store.getName(number);
	    	if(name==null) {
	    		name = ContentUtils.getContactDisplayNameByNumber(mContext, number);
	    		store.setName(number, name);
	    	}				

	        intent.putExtra(Constants.EXTRA_NAME,name);
	        intent.putExtra(Constants.EXTRA_READ,read);
	        intent.putExtra(Constants.EXTRA_NUMBER,PhoneNumberUtils.stripSeparators(number));
	        startActivity(intent);   
	        return;
		default:
			String convoID = threads.getString(threads
				      .getColumnIndex("_id"));
			intent = new Intent(mContext, SharedConversationActivity.class);
	        intent.putExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, convoID);
	        startActivity(intent);	
	        return;
		}    	

    }   
    
	public void startShare(View v) {			

		if(mConversantsEditor.constructContactsFromInput(false).getNumbers().length==0) {
			Toast.makeText(mContext, "No converstions selected", Toast.LENGTH_LONG).show();
			return;
		}
		conversants = mConversantsEditor.constructContactsFromInput(false).getToNumbers();
		
		if(conversants.length == 0) {
			Toast.makeText(mContext, "No converstions selected", Toast.LENGTH_LONG).show();
			return;
		}		
		if(conversants.length > 1) {
			Toast.makeText(mContext, "Select only one conversation", Toast.LENGTH_LONG).show();
			return;
		}			
		if(mConfidantesEditor.constructContactsFromInput(false).getNumbers().length==0) {
			Toast.makeText(mContext, "No valid recipients selected", Toast.LENGTH_LONG).show();
			return;
		}
		recipients = mConfidantesEditor.constructContactsFromInput(false).getToNumbers();
		
		if(recipients.length == 0) {
			Toast.makeText(mContext, "No recipients selected", Toast.LENGTH_LONG).show();
			return;
		}		
		if(recipients.length > 1) {
			Toast.makeText(mContext, "Select only one person to share with", Toast.LENGTH_LONG).show();
			return;
		}			
				
		mBox.setVisibility(View.GONE);
		//newShare.setBackgroundResource(R.color.pullLight);
		conversant = conversants[0];
		recipient = recipients[0];
		
		final String shared_from_thread = ContentUtils.
				getThreadIDFromNumber(mContext, conversant);
		final String shared_from_name = ContentUtils.
				getContactDisplayNameByNumber(mContext, conversant);
		Intent ni = new Intent(mContext, MessageActivityCheckboxCursor.class);
		ni.putExtra(Constants.EXTRA_THREAD_ID,shared_from_thread);
		ni.putExtra(Constants.EXTRA_NAME,shared_from_name);
		ni.putExtra(Constants.EXTRA_READ,true);
		ni.putExtra(Constants.EXTRA_NUMBER,PhoneNumberUtils.stripSeparators(conversant));	
		ni.putExtra(Constants.EXTRA_SHARE_TO_NUMBER,PhoneNumberUtils.stripSeparators(recipient));	
		startActivity(ni);
	

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
