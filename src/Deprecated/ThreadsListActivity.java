package Deprecated;
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

import com.Pull.pullapp.MainApplication;
import com.Pull.pullapp.MessageActivityCheckboxCursor;
import com.Pull.pullapp.R;
import com.Pull.pullapp.ThreadItemsCursorAdapter;
import com.Pull.pullapp.UserSettings;
import com.Pull.pullapp.ViewPagerSignIn;
import com.Pull.pullapp.R.id;
import com.Pull.pullapp.R.layout;
import com.Pull.pullapp.R.menu;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
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

public class ThreadsListActivity extends SherlockListActivity {
	
	private ThreadItemsCursorAdapter adapter;
	//private ThreadItemsListAdapter adapter;
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
	    //Log.i("super.onCreate(savedInstanceState);", ""+System.currentTimeMillis());
	    //long time1 = System.currentTimeMillis();
	    setContentView(R.layout.threads_listactivity);
	    mContext = getApplicationContext();
	    ParseAnalytics.trackAppOpened(getIntent());
	    
	    mApp = (MainApplication) this.getApplication();
	    mPhoneNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();

	    listview = getListView();
	    
	    threads_cursor = ContentUtils.getThreadsCursor(mContext);
	    //long time2 = System.currentTimeMillis();
	    
	    adapter = new ThreadItemsCursorAdapter(mContext, threads_cursor,R.id.my_conversation_tab);
	  //  adapter = new ThreadItemsListAdapter(mContext, R.layout.message_list_item,new ArrayList<ThreadItem>());	    
	    //long time3 = System.currentTimeMillis();
	    
	    setListAdapter(adapter);  
	    //long time4 = System.currentTimeMillis();
	    //Log.i("tag","time to set adapter " + (time4-time3));
	    

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
		
	    listview.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
	    	 
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view,
					ContextMenuInfo menuInfo) {			
		        menu.setHeaderTitle("Options");
		        menu.add(0, CONTEXTMENU_CONTACTITEM, 0, "Add to Contacts");
				
			}
	    });	   

	   // long time5 = System.currentTimeMillis();
	    //Log.i("tag","time for oncreate " + (time5-time1));			
		
	}
	// make the menu work
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//actionbar menu
		getSupportMenuInflater().inflate(R.menu.threads_list_menu, menu);
		return true;
	}		

    public boolean onContextItemSelected(MenuItem aItem) {
    	Toast.makeText(mContext, " + ", Toast.LENGTH_LONG).show();
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
        int position = menuInfo.position;
        String recipientID = adapter.recipientID_hash.get(position);
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
    }	
	@Override
	protected void onResume() {
		super.onResume();
		//long time1 = System.currentTimeMillis();
	
		
	    //long time5 = System.currentTimeMillis();
	    //Log.i("tag","time for onresume " + (time5-time1));		
	}

	@Override
	protected void onStart() {
		super.onStart();	
		threads_cursor = ContentUtils.getThreadsCursor(mContext);
		adapter.notifyDataSetChanged();
		currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			radioGroup.check(R.id.my_conversation_tab);
			listview.invalidateViews();
			listview.refreshDrawableState();
		} else {
		}			
	}	
	  private void startLoginActivity(int signin_result) {
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



	@Override
	protected void onPause(){
		super.onPause();
		//threads_cursor.close();
	}
	
	// when a user selects a menu item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.new_message:
            intent = new Intent(mContext, MessageActivityCheckboxCursor.class);
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
        intent.putExtra(Constants.EXTRA_THREAD_ID,threadID);
        intent.putExtra(Constants.EXTRA_NAME,name);
        intent.putExtra(Constants.EXTRA_READ,read);
        intent.putExtra(Constants.EXTRA_NUMBER,PhoneNumberUtils.stripSeparators(number));
        startActivity(intent);        

    }   
    
    
  
	

} 
