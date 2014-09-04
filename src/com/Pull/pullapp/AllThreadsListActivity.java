package com.Pull.pullapp;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.provider.Telephony.ThreadsColumns;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
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
import com.Pull.pullapp.util.SMSReceiver;
import com.Pull.pullapp.util.UserInfoStore;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;

public class AllThreadsListActivity extends SherlockListActivity implements View.OnClickListener {
	
	private ThreadItemsCursorAdapter adapter;
	private ListView listview;
	private Context mContext;
	private RadioGroup radioGroup;
    protected static final int CONTEXTMENU_DELETEITEM = 0;
    protected static final int CONTEXTMENU_CONTACTITEM = 1;	
	private Cursor threads_cursor;
	private int currentTab;
	private DatabaseHandler dbHandler;
    private RecipientsEditor mConfidantesEditor;
	private RecipientsAdapter mRecipientsAdapter;
    private LinearLayout mBox;
	private RecipientsEditor mConversantsEditor;	
	private String[] recipients;
	private String[] conversants;
	private String conversant;
	private String recipient;
	private Button hint, setDefault;
	private UserInfoStore store;
	private final String columns = DatabaseHandler.KEY_SHARED_WITH + 
			", " + DatabaseHandler.KEY_CONVERSATION_FROM_NAME +
			", " + DatabaseHandler.KEY_SHARER +
			", " + DatabaseHandler.KEY_ID + " as _id" +
			", " + DatabaseHandler.KEY_CONVO_TYPE +
			", " + DatabaseHandler.KEY_CONVERSATION_FROM;
	private int counter;
	private ShowcaseView showcaseView;
	private MixpanelAPI mixpanel;
	private String myPackageName;
	private int currentapiVersion;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.threads_listactivity);
	    
		showcaseView = new ShowcaseView.Builder(this)
        .setTarget(new ViewTarget(findViewById(R.id.my_conversation_tab)))
			    .setContentTitle("Your text messages")
			    .setContentText(
			    		"All of your regular text messages are here. You can use Pull to text or share conversations")        
        .setOnClickListener(this)
        .singleShot(108)
        .build();	    
		showcaseView.setButtonText("Next");
		
	    mContext = getApplicationContext();
	    store = new UserInfoStore(mContext);
	    
	    ParseAnalytics.trackAppOpened(getIntent());
	    dbHandler = new DatabaseHandler(mContext);
	    hint = (Button) findViewById(R.id.hint);   
	    hint.setVisibility(View.VISIBLE);
	    setDefault = (Button) findViewById(R.id.set_message_default);   
	    setDefault.setVisibility(View.GONE);
	    
	    listview = getListView();
	    registerForContextMenu(listview);
	    
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
		
		mixpanel = MixpanelAPI.getInstance(getBaseContext(), Constants.MIXEDPANEL_TOKEN);
		mixpanel.identify(ParseInstallation.getCurrentInstallation().getObjectId());
		
		mixpanel.track("AllThreadsListActivity created ", null);		
		myPackageName = getPackageName();
		currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.KITKAT){
	        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
	            Intent intent =
	                    new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
	            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, 
	                    myPackageName);
	            startActivity(intent);
	        } else {
				PackageManager pm = getPackageManager();
				ComponentName compName = 
				      new ComponentName(mContext, 
				            SMSReceiver.class);
				pm.setComponentEnabledSetting(
				      compName,
				      PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
				      PackageManager.DONT_KILL_APP);			
			} 		
		}		
		
	}
	
	@Override
	protected void onDestroy() {
		mixpanel.flush();
	    super.onDestroy();
	}		
	
	@Override
	public void onClick(View v) {
		mixpanel.track("Allthreadslistactivity Showcaseview next button", null);
        switch (counter) {
        case 0:
        	mixpanel.track("Showcasing shared texts tab", null);
            showcaseView.setShowcase(new ViewTarget(findViewById(R.id.shared_tab)), true);
            showcaseView.setContentTitle("Shared texts");
            showcaseView.setContentText(
            		"Conversations you've shared or that have been shared with you are in this tab");  
            break;
        case 1:
        	mixpanel.track("Showcasing conversations threads tab", null);
            showcaseView.setShowcase(new ViewTarget(findViewById(R.id.row)), true);
            showcaseView.setContentTitle("Conversation threads");
            showcaseView.setContentText(
            		"Click a row to open an existing conversation");  
            showcaseView.setButtonText("Finish");
            break;

        case 2:
        	showcaseView.hide();
            break;

	    }
	    counter++;
	}	
	
	@Override
	protected void onResume() {
		super.onResume();

		populateList();
		listview.refreshDrawableState();
			
		
	}
	
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    super.onCreateContextMenu(menu, v, menuInfo);  
    	mixpanel.track("threadslist long press list item", null);
        if(currentTab==R.id.shared_tab) menu.add(0, v.getId(), 0, "Delete Thread");  
        else menu.add(0, v.getId(), 0, "Add to Contacts");  
    }  
    
    @Override
    public boolean onContextItemSelected (android.view.MenuItem item)
    {
    	AdapterView.AdapterContextMenuInfo info=
            (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Cursor threads  = (Cursor) getListView().getItemAtPosition(info.position);
  		switch(currentTab) {
  		case R.id.shared_tab:
  			mixpanel.track("Sharedconvo deleted", null);
  			String convoID = threads.getString(threads.getColumnIndex("_id"));
  			int rows = dbHandler.deleteShared(convoID);
  			threads_cursor = dbHandler.getSharedConversationCursor(columns);
  			adapter.swapCursor(threads_cursor);
  			adapter.notifyDataSetChanged();
  		    return true;
  		case R.id.my_conversation_tab: 
  			mixpanel.track("add to contacts from threadslistactivity", null);
			String recipientId = threads.getString(threads
				      .getColumnIndex(ThreadsColumns.RECIPIENT_IDS));		
			String number = store.getPhoneNumber(recipientId);
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
            startActivity(intent);       			
  		    return true;
  		default: 
  			return true;
  		}	          
    } 
    


	private void populateList() {
		switch(currentTab) {
		case R.id.shared_tab:
			mixpanel.track("Shared tab clicked ", null);	
			threads_cursor = dbHandler.getSharedConversationCursor(columns);
		    adapter = new ThreadItemsCursorAdapter(mContext, threads_cursor, currentTab, this);
		    setListAdapter(adapter);  	
		    mBox.setVisibility(View.GONE);
		    hint.setVisibility(View.VISIBLE);
		    setDefault.setVisibility(View.GONE);
		    return;
		case R.id.my_conversation_tab: 
			mixpanel.track("My texts tab clicked ", null);
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
			mixpanel.track("new message button clicked", null);
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
			mixpanel.track("settings button clicked", null);
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

	public void launchSettings(View v) {
		Log.i("launchSettings",myPackageName);
        Intent intent =
                new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, 
                myPackageName);
        startActivity(intent);
		
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
    	mixpanel.track("thread clicked", null);
    	Cursor threads  = (Cursor) getListView().getItemAtPosition(position);
    	Intent intent;
		switch(currentTab) {
		case R.id.my_conversation_tab: 
	        intent = new Intent(mContext, MessageActivityCheckboxCursor.class);
	        String threadID = threads.getString(threads
				      .getColumnIndex(ThreadsColumns._ID));	
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
		//	Log.i("long id ", "long id " + id);
			String convoID = threads.getString(threads.getColumnIndex("_id"));
			//Log.i("convoID", convoID);
			intent = new Intent(mContext, MessageActivityCheckboxCursor.class);
			intent.putExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, convoID);
			intent.putExtra(Constants.EXTRA_SHARED_NAME, 
					threads.getString(threads.getColumnIndex(DatabaseHandler.KEY_CONVERSATION_FROM_NAME)));
			intent.putExtra(Constants.EXTRA_SHARED_SENDER, 
					threads.getString(threads.getColumnIndex(DatabaseHandler.KEY_SHARER)));
			intent.putExtra(Constants.EXTRA_SHARED_ADDRESS, 
					threads.getString(threads.getColumnIndex(DatabaseHandler.KEY_CONVERSATION_FROM)));
			intent.putExtra(Constants.EXTRA_SHARED_CONFIDANTE, 
					threads.getString(threads.getColumnIndex(DatabaseHandler.KEY_SHARED_WITH)));		
			intent.putExtra(Constants.EXTRA_SHARED_CONVO_TYPE, Integer.parseInt(
					threads.getString(threads.getColumnIndex(DatabaseHandler.KEY_CONVO_TYPE))));			
			//Log.i(Constants.EXTRA_SHARED_ADDRESS, threads.getString(threads.getColumnIndex(DatabaseHandler.KEY_CONVERSATION_FROM)));
	        startActivity(intent);	
	        return;
		}    	

    }   
    
	public void startShare(View v) {			
		mixpanel.track("startshare clicked from threadslist", null);
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
	

	@Override
	protected void onStart() {
		super.onStart();	
	}	

	@Override
	protected void onPause(){
		super.onPause();
	}


	

} 
