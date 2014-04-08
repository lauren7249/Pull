package com.Pull.pullapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.ThreadsColumns;
import android.telephony.PhoneNumberUtils;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.Pull.pullapp.model.ThreadItem;
import com.Pull.pullapp.util.AlarmScheduler;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.R;

import java.util.ArrayList;

public class ThreadsListActivity extends Activity {
	
	private ThreadItemsListAdapter adapter;
	private ListView listview;
	private ArrayList<ThreadItem> thread_list;
	private Context mContext;
    protected static final int CONTEXTMENU_DELETEITEM = 0;
    protected static final int CONTEXTMENU_CONTACTITEM = 1;	
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.listactivity);
	    mContext = getApplicationContext();
	    
	    if(Constants.LOG_SMS) new AlarmScheduler(mContext, false).start();
	    
	    
	    RadioGroup radioGroup  = (RadioGroup) findViewById(R.id.switch_buttons);
	   
	    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==R.id.shared_tab_button1){
					Intent i = new Intent(mContext, SharedListActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(i);
					
					overridePendingTransition(0,0);
				}
				if(checkedId==R.id.shared_tab_button2){
//					Intent i = new Intent(mContext, SharedWithMeListActivity.class);
//					i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//					startActivity(i);
					
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
		RadioButton initialButton = (RadioButton) findViewById(R.id.conversations_tab_button1);
	    initialButton.setChecked(true);
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
	
	} 
