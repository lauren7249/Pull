package com.Pull.pullapp;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.AlarmScheduler;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.DatabaseHandler;

public class SharedWithMeListActivity extends Activity {
	
	private SharedConversationsListAdapter adapter;
	private ListView listview;
	private List<SharedConversation> thread_list;
	private Context mContext;
	private DatabaseHandler dbHandler;
    protected static final int CONTEXTMENU_DELETEITEM = 0;
    protected static final int CONTEXTMENU_CONTACTITEM = 1;	
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.shared_conversation_thread_activity);
	    mContext = getApplicationContext();
	    
	    if(Constants.LOG_SMS) new AlarmScheduler(mContext, false).start();
	    
	    
	    RadioGroup radioGroup  = (RadioGroup) findViewById(R.id.switch_buttons1);
	   
	    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==R.id.conversations_tab_button1){
					Intent i = new Intent(mContext, ThreadsListActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(i);
					overridePendingTransition(0,0);
				}
			}
		});
	    dbHandler = new DatabaseHandler(mContext);
	    listview = (ListView) findViewById(R.id.listView);
	    thread_list = dbHandler.getAllSharedConversation();
	    adapter = new SharedConversationsListAdapter(getApplicationContext(),
	    		R.layout.shared_conversation_list_item, thread_list);	  
	    listview.setAdapter(adapter);
	    //new GetThreads().execute();    
	    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	
		      public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
//		    	  final ThreadItem item = (ThreadItem) parent.getItemAtPosition(position);
//    
//		          Intent intent = new Intent(mContext, MessageActivityCheckbox.class);
//		          intent.putExtra(Constants.EXTRA_THREAD_ID,item.ID);
//		          intent.putExtra(Constants.EXTRA_NAME,item.displayName);
//		          intent.putExtra(Constants.EXTRA_READ,item.read);
//		          intent.putExtra(Constants.EXTRA_NUMBER,item.number);
//		          startActivity(intent);	    	  
		     }
		
		   });			
	    listview.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
	    	 
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view,
					ContextMenuInfo menuInfo) {			
		        menu.setHeaderTitle("Options");
		        //menu.add(0, CONTEXTMENU_DELETEITEM, 1, "Delete thread");
		       // menu.add(0, CONTEXTMENU_CONTACTITEM, 0, "Add to Contacts");
				
			}
	    });	    
	}
	
    public boolean onContextItemSelected(MenuItem aItem) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
        int position = menuInfo.position;
        final SharedConversation item = (SharedConversation) listview.getAdapter().getItem(position);

        return false;
    }	
	@Override
	protected void onResume() {
		super.onResume();
		RadioButton initialButton = (RadioButton) findViewById(R.id.shared_tab_button1);
	    initialButton.setChecked(true);
	    thread_list = dbHandler.getAllSharedConversation();
	    adapter.setItemList(thread_list);
	    adapter.notifyDataSetChanged();
	}

//	  private class GetThreads extends AsyncTask<Void,SharedConversation,Void> {
//		  	@Override
//			protected Void doInBackground(Void... params) {
//				Cursor threads = ContentUtils.getThreadsCursor(mContext);
//				while (threads.moveToNext()) {
//			    	String threadID = threads.getString(threads
//			  		      .getColumnIndex("_id"));
//			    	if(threadID == null) continue;
//			    	if(threadID.length()==0) continue;
//			    	boolean read = (!threads.getString(threads
//				  		      .getColumnIndex("read")).equals("0"));	    	
//					String[] recipientIDs = threads.getString(threads
//				      .getColumnIndex("recipient_ids")).split(" ");
//
//					if(recipientIDs.length == 1 && recipientIDs[0].length()>0) {
//						String recipientId = recipientIDs[0];
//						String number = ContentUtils.getAddressFromID(mContext, recipientId);
//						String name = ContentUtils
//								.getContactDisplayNameByNumber(mContext, number);
//						ThreadItem t = new ThreadItem(threadID, name, number, read);
//						publishProgress(t);						
//
//					}
//					
//			    }	
//				threads.close();
//				return null;
//			}
//			@Override
//		    protected void onProgressUpdate(ThreadItem... t) {
//				adapter.addItem(t[0]);
//				adapter.notifyDataSetChanged();
//		    }				
//			
//			@Override
//		    protected void onPostExecute(Void result) {
//				super.onPostExecute(result);
//
//		    }			
//
//	  }
	
	} 
