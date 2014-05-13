package com.Pull.pullapp;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony.TextBasedSmsColumns;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.AlarmScheduler;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.DatabaseHandler;

public class SharedListActivity extends Activity {
	
	private SharedConversationsListAdapter adapter;
	private ListView listview;
	private List<SharedConversation> thread_list;
	private Context mContext;
	private DatabaseHandler dbHandler;
	private RadioGroup radioGroup;
    protected static final int CONTEXTMENU_DELETEITEM = 0;
    protected static final int CONTEXTMENU_CONTACTITEM = 1;	
    private int type;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.shared_conversation_list_activity);
	    mContext = getApplicationContext();
	    
	    if(Constants.LOG_SMS) new AlarmScheduler(mContext, false).start();
	    radioGroup  = (RadioGroup) findViewById(R.id.switch_buttons);

	    dbHandler = new DatabaseHandler(mContext);
	    listview = (ListView) findViewById(R.id.listView);
	    
	    type = getIntent().getIntExtra(Constants.EXTRA_SHARE_TYPE,TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
		
	    if(type==TextBasedSmsColumns.MESSAGE_TYPE_SENT) radioGroup.check(R.id.shared_tab);
		else radioGroup.check(R.id.shared_with_me_tab);	   	    
	    
	    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==R.id.my_conversation_tab){
					Intent i = new Intent(mContext, ThreadsListActivity.class);
					startActivity(i);
				}else if(checkedId==R.id.shared_with_me_tab){
					radioGroup.check(R.id.shared_with_me_tab);	
					type = TextBasedSmsColumns.MESSAGE_TYPE_INBOX;
				    thread_list = dbHandler.getAllSharedConversation(type);
				    adapter = new SharedConversationsListAdapter(mContext,
				    		R.layout.shared_conversation_list_item, thread_list);	  
				    listview.setAdapter(adapter);
				}else if(checkedId==R.id.shared_tab){
					radioGroup.check(R.id.shared_tab);	
					type = TextBasedSmsColumns.MESSAGE_TYPE_SENT;
				    thread_list = dbHandler.getAllSharedConversation(type);
				    adapter = new SharedConversationsListAdapter(mContext,
				    		R.layout.shared_conversation_list_item, thread_list);	  
				    listview.setAdapter(adapter);
				}
				
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
		
	    thread_list = dbHandler.getAllSharedConversation(type);
	    
	    adapter = new SharedConversationsListAdapter(mContext,
	    		R.layout.shared_conversation_list_item, thread_list);	  
	    listview.setAdapter(adapter);
	    
	    type = getIntent().getIntExtra(Constants.EXTRA_SHARE_TYPE,TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
		
	    if(type==TextBasedSmsColumns.MESSAGE_TYPE_SENT) radioGroup.check(R.id.shared_tab);
		else radioGroup.check(R.id.shared_with_me_tab);	   		     
	

	    //new GetThreads().execute();    
	    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	
		      public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

		    	  final SharedConversation item = (SharedConversation) parent.getItemAtPosition(position);
		          Intent intent = new Intent(mContext, SharedConversationActivity.class);
		          intent.putExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, item.getId());
		          startActivity(intent);	
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
	    adapter.setItemList(thread_list);
	    adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}	
} 