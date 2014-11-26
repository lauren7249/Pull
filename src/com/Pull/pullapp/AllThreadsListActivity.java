package com.Pull.pullapp;
import java.util.Arrays;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.provider.Telephony.ThreadsColumns;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
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
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;

public class AllThreadsListActivity extends SherlockListActivity implements View.OnClickListener {
	
	private static ThreadItemsCursorAdapter adapter;
	private static ListView listview;
	private Context mContext;
	private RadioGroup radioGroup;
    protected static final int CONTEXTMENU_DELETEITEM = 0;
    protected static final int CONTEXTMENU_CONTACTITEM = 1;	
	private Cursor threads_cursor;
	private static int currentTab;
	private DatabaseHandler dbHandler;
	private String[] recipients;
	private String[] conversants;
	private String conversant;
	private String recipient;
	private Button hint, setDefault;
	private static UserInfoStore store;
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
	private SharedPreferences sharedPrefs;
	private Editor editor;
	private MenuItem mSettings;
	private BroadcastReceiver mBroadcastReceiver;
	private ImageView menu_button;
	private AllThreadsListActivity activity;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.threads_listactivity);
	    activity = this;
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

	    listview = getListView();
	    registerForContextMenu(listview);
        sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);	
        editor = sharedPrefs.edit();
	    currentTab = getIntent().getIntExtra(Constants.EXTRA_TAB_ID,R.id.my_conversation_tab);

	    radioGroup  = (RadioGroup) findViewById(R.id.switch_buttons);   
	    radioGroup.check(currentTab);	
	    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			    	
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				store.putPosition("tab"+currentTab,listview.getFirstVisiblePosition());
				currentTab = checkedId;		
				populateList();
				Log.i("previous position","position"+store.getPosition("tab"+currentTab));
		        if(store.getPosition("tab"+currentTab)>=0) {
		        	
		        	listview.setSelection(store.getPosition("tab"+currentTab));
		        }

			}
		});

		mixpanel = MixpanelAPI.getInstance(getBaseContext(), Constants.MIXEDPANEL_TOKEN);
		mixpanel.identify(ParseInstallation.getCurrentInstallation().getObjectId());
		
		mixpanel.track("AllThreadsListActivity created ", null);		
		myPackageName = getPackageName();
		currentapiVersion = android.os.Build.VERSION.SDK_INT;
		
		if (currentapiVersion >= android.os.Build.VERSION_CODES.KITKAT){
	        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
	        	//Log.i("default package",Telephony.Sms.getDefaultSmsPackage(this));
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    builder.setTitle("Use Pull to Text");
			    builder.setMessage("Do you want to use Pull as your default texting app? " +
			    		"Doing so will allow you automatically cancel texts within 5 seconds of sending! " +
			    		"Just click 'OK' to the following prompts.")
			           .setCancelable(true)
			           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int id)
			               {

			   	            Intent intent =
				                    new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
				            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, 
				                    myPackageName);
				            store.putPosition("tab"+currentTab,listview.getFirstVisiblePosition());
				            startActivity(intent);

			               	}
			           }) 
			           .setNegativeButton("No", new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int id) 
			               {
			                    dialog.cancel();
			               }
			           }).show();		        	

	        } 	
		}		
		
		menu_button = (ImageView) findViewById(R.id.menu_button);
		menu_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				activity.openOptionsMenu();
				
			}
			
		});		
		mBroadcastReceiver = 
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();		
				String[] numbers = intent.getStringArrayExtra(Constants.EXTRA_NUMBERS); 
				String[] names = intent.getStringArrayExtra(Constants.EXTRA_NAMES); 
				String thread_id = intent.getStringExtra(Constants.EXTRA_THREAD_ID);
				openConvo(context,thread_id,numbers,names,action);
				
			}
		}	;	
	}
	
	protected void shareTabClicked(int tab_num) {

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
            showcaseView.setShowcase(new ViewTarget(findViewById(R.id.graph_button)), true);
            showcaseView.setContentTitle("Interest graphs");
            showcaseView.setContentText(
            		"Graphs that show the other person's interest relative to yours");  
            showcaseView.setButtonText("Next");
            break;
        case 1:
            showcaseView.setShowcase(new ViewTarget(findViewById(R.id.add_person)), true);
            showcaseView.setContentTitle("Share with a friend");
            showcaseView.setContentText(
            		"Include your friend by sharing pieces of the conversation");  
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
			
        if (currentapiVersion >= android.os.Build.VERSION_CODES.KITKAT) {
        	if (!Telephony.Sms.getDefaultSmsPackage(mContext).equals(getPackageName())) {
        		editor.putBoolean("prefReceiveTexts", false);
        	} else {
        		editor.putBoolean("prefReceiveTexts", true);
        	}
        			
        }
		Log.i("previous position","position"+store.getPosition("tab"+currentTab));
        if(store.getPosition("tab"+currentTab)>=0) {
        	
        	listview.setSelection(store.getPosition("tab"+currentTab));
        }                
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_ADDPPL_TAB_CLICKED);
		intentFilter.addAction(Constants.ACTION_GRAPH_TAB_CLICKED);
		intentFilter.addAction(Constants.ACTION_ORIGINAL_TAB_CLICKED);
		registerReceiver(mBroadcastReceiver, intentFilter);		        
	}
	
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    super.onCreateContextMenu(menu, v, menuInfo);  
    	mixpanel.track("threadslist long press list item", null);
        menu.add(0, v.getId(), 0, "Delete Thread");  
        menu.add(0, v.getId(), 1, "Add to Contacts");  
    }  
    
    @Override
    public boolean onContextItemSelected (android.view.MenuItem item)
    {
    	AdapterView.AdapterContextMenuInfo info=
            (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Cursor threads  = (Cursor) getListView().getItemAtPosition(info.position);
  		switch(currentTab) {
  		case R.id.shared_tab:
  			if(item.getTitle().equals("Delete Thread")) {
	  			mixpanel.track("Sharedconvo deleted", null);
	  			String convoID = threads.getString(threads.getColumnIndex("_id"));
	  			int rows = dbHandler.deleteShared(convoID);
	  			threads_cursor = dbHandler.getSharedConversationCursor(columns);
	  			adapter.swapCursor(threads_cursor);
	  			adapter.notifyDataSetChanged();
  			} else {
  				String number = threads.getString(threads.getColumnIndex(DatabaseHandler.KEY_SHARED_WITH));
  	            Intent intent = new Intent(Intent.ACTION_INSERT);
  	            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
  	            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
  	            intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
  	            store.putPosition("tab"+currentTab,listview.getFirstVisiblePosition());
  	            startActivity(intent);    		
  	            
  	            //Log.i("position","position " + listview.getFirstVisiblePosition());  	            
  			}
  		    return true;
  		case R.id.my_conversation_tab: 
			String threadID = threads.getString(threads
				      .getColumnIndex(ThreadsColumns._ID));	
			String recipientIds = threads.getString(threads
				      .getColumnIndex(ThreadsColumns.RECIPIENT_IDS));
			String[] recipients = recipientIds.split(" ");
			String[] numbers = store.getPhoneNumbers(recipients);  			
  			if(item.getTitle().equals("Delete Thread")) {
  				ContentUtils.deleteConversation(mContext,threadID);
  				threads_cursor = ContentUtils.getThreadsCursor(mContext);
	  			adapter.swapCursor(threads_cursor);
	  			adapter.notifyDataSetChanged();
  			} else {  			
	  			mixpanel.track("add to contacts from threadslistactivity", null);
	            Intent intent = new Intent(Intent.ACTION_INSERT);
	            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
	            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
	            intent.putExtra(ContactsContract.Intents.Insert.PHONE, numbers[0]);
	            store.putPosition("tab"+currentTab,listview.getFirstVisiblePosition());
	            startActivity(intent);     
	    		
	    		//Log.i("position","position " + listview.getFirstVisiblePosition());	            
  			}
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
		    return;
		case R.id.my_conversation_tab: 
			mixpanel.track("My texts tab clicked ", null);
		    threads_cursor = ContentUtils.getThreadsCursor(mContext);
		    adapter = new ThreadItemsCursorAdapter(mContext, threads_cursor, currentTab, this);
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
			mixpanel.track("new message button clicked", null);
			switch(currentTab) {
				case R.id.my_conversation_tab: 
					c = MessageActivityCheckboxCursor.class;
		            intent = new Intent(mContext, c);
		            store.putPosition("tab"+currentTab,listview.getFirstVisiblePosition());
		            startActivity(intent);	
		    		
		    		//Log.i("position","position " + listview.getFirstVisiblePosition());		            
		            return true;
				default: 
			}

			return true;	
		case R.id.settings:
			mixpanel.track("settings button clicked", null);
            intent = new Intent(mContext, UserSettings.class);
            store.putPosition("tab"+currentTab,listview.getFirstVisiblePosition());
            startActivity(intent);
    		
    		//Log.i("position","position " + listview.getFirstVisiblePosition());            
			return true;				
		case R.id.terms:
			mixpanel.track("terms button clicked", null);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://www.thepullapp.com/termsAndPolicies.html"));
            store.putPosition("tab"+currentTab,listview.getFirstVisiblePosition());
            startActivity(intent);
    		
    		//Log.i("position","position " + listview.getFirstVisiblePosition());            
			return true;			
		default:
			return false;
		}
	}	

	public void launchSettings(View v) {
		//Log.i("launchSettings",myPackageName);
        Intent intent =
                new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, 
                myPackageName);
        store.putPosition("tab"+currentTab,listview.getFirstVisiblePosition());
        startActivity(intent);
		
		//Log.i("position","position " + listview.getFirstVisiblePosition());        
		
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

	        String threadID = threads.getString(threads
				      .getColumnIndex(ThreadsColumns._ID));	
	  		boolean read = (!threads.getString(threads
		  		      .getColumnIndex(ThreadsColumns.READ)).equals("0"));    	
			String[] recipientIds = threads.getString(threads
				      .getColumnIndex(ThreadsColumns.RECIPIENT_IDS)).split(" ");		
			threads.close();
			for(String recipientId : recipientIds) {
				String number = store.getPhoneNumber(recipientId);
		    	if(number==null) {
					number = PhoneNumberUtils.stripSeparators(
							ContentUtils.getAddressFromID(mContext, recipientId));
					store.setPhoneNumber(recipientId,number);
		    	} else {
		    		if(store.getRecipientID(number)==null) store.setPhoneNumber(recipientId, number);
		    	}
		    //	store.saveThreadID(number, threadID);
				String name = store.getName(number);
		    	if(name==null) {
		    		name = ContentUtils.getContactDisplayNameByNumber(mContext, number);
		    		store.setName(number, name);
		    	}		
			}
			String[] numbers = store.getPhoneNumbers(recipientIds);
			String[] names = store.getNames(numbers);
			Log.i("numbers allthredslist",numbers.length+"");
	    	openConvo(mContext,threadID,numbers,names,"");
	        return;
		default:
		//	Log.i("long id ", "long id " + id);
			String convoID = threads.getString(threads.getColumnIndex("_id"));
			String sender = threads.getString(threads.getColumnIndex(DatabaseHandler.KEY_SHARER));

			String clueless_persons_number = threads.getString(threads
					.getColumnIndex(DatabaseHandler.KEY_CONVERSATION_FROM));
			String clueless_persons_name = threads.getString(threads
					.getColumnIndex(DatabaseHandler.KEY_CONVERSATION_FROM_NAME));
			String confidante = 
					threads.getString(threads.getColumnIndex(DatabaseHandler.KEY_SHARED_WITH));
			int convo_type = Integer.parseInt(
					threads.getString(threads.getColumnIndex(DatabaseHandler.KEY_CONVO_TYPE)));
			openSharedConvo(mContext, convoID, sender, clueless_persons_number, 
					clueless_persons_name, confidante, convo_type);
	        return;
		}    	

    }   
    
	private void openConvo(Context context, String threadID,
			String[] numbers, String[] names, String action) {
    	Intent intent = new Intent(context, MessageActivityCheckboxCursor.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.EXTRA_NAMES,names);
        intent.putExtra(Constants.EXTRA_NUMBERS,numbers);
        intent.putExtra(Constants.EXTRA_THREAD_ID,threadID);
        intent.putExtra(Constants.EXTRA_STATUS,action);
        Log.i("position","position " + listview.getFirstVisiblePosition());
        store.putPosition("tab"+currentTab,listview.getFirstVisiblePosition());
        context.startActivity(intent);   
		
	}

	public static void openConvo(Context context, String number, String name, String action) {
    	Intent intent = new Intent(context, MessageActivityCheckboxCursor.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.EXTRA_NAMES, new String[]{name});
        intent.putExtra(Constants.EXTRA_NUMBERS,new String[] {PhoneNumberUtils.stripSeparators(number)});
        intent.putExtra(Constants.EXTRA_STATUS,action);
        Log.i("position","position " + listview.getFirstVisiblePosition());
        store.putPosition("tab"+currentTab,listview.getFirstVisiblePosition());
        context.startActivity(intent);   
	}

	public static void openSharedConvo(Context context, String convoID, String sender, String clueless_persons_number,
			String clueless_persons_name, String confidante, int convo_type) {
		Intent intent = new Intent(context, MessageActivityCheckboxCursor.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, convoID);
		intent.putExtra(Constants.EXTRA_SHARED_SENDER, sender);
		intent.putExtra(Constants.EXTRA_CLUELESS_PERSONS_NUMBER, clueless_persons_number);
		intent.putExtra(Constants.EXTRA_CLUELESS_PERSONS_NAME, clueless_persons_name);			
		intent.putExtra(Constants.EXTRA_SHARED_CONFIDANTE, confidante);		
		intent.putExtra(Constants.EXTRA_SHARED_CONVO_TYPE, convo_type);			
		//Log.i(Constants.EXTRA_CLUELESS_PERSONS_NUMBER, threads.getString(threads.getColumnIndex(DatabaseHandler.KEY_CONVERSATION_FROM)));
		store.putPosition("tab"+currentTab,listview.getFirstVisiblePosition());
		context.startActivity(intent);	
		
	}
    
	 // make the menu work
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//actionbar menu
		getSupportMenuInflater().inflate(R.menu.threads_list_menu, menu);
		
		if (currentapiVersion >= android.os.Build.VERSION_CODES.KITKAT){
			mSettings = menu.findItem(R.id.settings);
			mSettings.setVisible(false);
		}
		return true;
	}		
	

	@Override
	protected void onStart() {
		super.onStart();	
	}	

	@Override
	protected void onPause(){
		super.onPause();
		if(mBroadcastReceiver!=null) unregisterReceiver(mBroadcastReceiver);	
	}


	

} 
