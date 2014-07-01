package Deprecated;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony.TextBasedSmsColumns;
import android.telephony.PhoneNumberUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.Pull.pullapp.MessageActivityCheckboxCursor;
import com.Pull.pullapp.R;
import com.Pull.pullapp.SharedConversationActivity;
import com.Pull.pullapp.R.color;
import com.Pull.pullapp.R.id;
import com.Pull.pullapp.R.layout;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.AlarmScheduler;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;

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
    private Button newShare;
    private RecipientsEditor mConfidantesEditor;
	private RecipientsAdapter mRecipientsAdapter;
    private LinearLayout mBox;
	private RecipientsEditor mConversantsEditor;
	private String[] recipients;
	private String[] conversants;
	private String conversant;
	private String recipient;
	private boolean visible;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.list_shared);
	    mContext = getApplicationContext();
	    
	    if(Constants.LOG_SMS) new AlarmScheduler(mContext, false).start();
	    radioGroup  = (RadioGroup) findViewById(R.id.switch_buttons);
	    
	    newShare = (Button) findViewById(R.id.new_button);
	    dbHandler = new DatabaseHandler(mContext);
	    listview = (ListView) findViewById(R.id.listView);
	    
	    mBox = (LinearLayout)findViewById(R.id.confidantes_box);
		mRecipientsAdapter = new RecipientsAdapter(this);
		mConfidantesEditor = (RecipientsEditor)findViewById(R.id.confidantes_editor);
		mConfidantesEditor.setAdapter(mRecipientsAdapter);
		mConversantsEditor = (RecipientsEditor)findViewById(R.id.recipient_editor);
		mConversantsEditor.setAdapter(mRecipientsAdapter);

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
				    		R.layout.list_item_thread, thread_list, type);
				    mBox.setVisibility(View.GONE);
				    newShare.setVisibility(View.GONE);
				    newShare.setText("SUBSCRIBE TO A CONVERSATION");
				    listview.setAdapter(adapter);
				}else if(checkedId==R.id.shared_tab){
					radioGroup.check(R.id.shared_tab);	
					type = TextBasedSmsColumns.MESSAGE_TYPE_SENT;
				    thread_list = dbHandler.getAllSharedConversation(type);
				    adapter = new SharedConversationsListAdapter(mContext,
				    		R.layout.list_item_thread, thread_list, type);	
				    if(visible) mBox.setVisibility(View.VISIBLE);
				    newShare.setVisibility(View.VISIBLE);
				    newShare.setText("SHARE A CONVERSATION");
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
		
		type = getIntent().getIntExtra(Constants.EXTRA_SHARE_TYPE,TextBasedSmsColumns.MESSAGE_TYPE_SENT);
		
	    thread_list = dbHandler.getAllSharedConversation(type);
	    
	    adapter = new SharedConversationsListAdapter(mContext,
	    		R.layout.list_item_thread, thread_list, type);	  
	    listview.setAdapter(adapter);
	    
	    if(type==TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
	    	radioGroup.check(R.id.shared_tab);
	    	newShare.setVisibility(View.VISIBLE);
	    	newShare.setText("SHARE A CONVERSATION");
	    }
		else {
			radioGroup.check(R.id.shared_with_me_tab);	
			mBox.setVisibility(View.GONE);
			newShare.setVisibility(View.GONE);
			newShare.setText("SUBSCRIBE TO A CONVERSATION");
			
		}		     
	

	    //new GetThreads().execute();    
	    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	
		      public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

		    	  final SharedConversation item = (SharedConversation) parent.getItemAtPosition(position);
		          Intent intent = new Intent(mContext, SharedConversationActivity.class);
		          intent.putExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, item.getObjectId());
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
	   // adapter.setItemList(thread_list);
	    //adapter.notifyDataSetChanged();
	}
	
	public void newShare(View v) {
		if(!visible) {
			visible = true;
			mBox.setVisibility(View.VISIBLE);	
			newShare.setBackgroundResource(R.color.pullMedium);
		}
		else {
			visible = false;
			mBox.setVisibility(View.GONE);
			newShare.setBackgroundResource(R.color.pullDark);
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
				
		newShare.setBackgroundResource(R.color.pullLight);
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

	@Override
	protected void onPause() {
		super.onPause();
	}	
} 