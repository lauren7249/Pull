package Deprecated;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.app.NavUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.Pull.pullapp.CustomDateTimePicker;
import com.Pull.pullapp.MainApplication;
import com.Pull.pullapp.QueuedMessageAdapter;
import com.Pull.pullapp.R;
import com.Pull.pullapp.SharedConversationActivity;
import com.Pull.pullapp.SimplePopupWindow;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.Pull.pullapp.util.DelayedSend;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;
import com.Pull.pullapp.util.ShareTagAction;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

@Deprecated
public class MessageActivityCheckbox extends SherlockListActivity {

	private ArrayList<SMSMessage> messages;
	private HashMap<Long,Integer> delayedMessages = new HashMap<Long,Integer>();
	private QueuedMessageAdapter adapter;
	private EditText text;
	private String name, number, newMessage;
	private Context mContext;
	private final Calendar calendar = Calendar.getInstance();
	private Button pickDelay, send, share;
	private boolean isChecked, isPopulated;
	private CustomDateTimePicker customDateTimePicker;
	private Date sendDate;
	private ViewSwitcher viewSwitcher;
	private RecipientsEditor mRecipientEditor, mConfidantesEditor;
	private RecipientsAdapter mRecipientsAdapter;
	private String[] recipients;
	private ListView mListView;
	private RelativeLayout mLayout;
	private BroadcastReceiver mBroadcastReceiver;
	private MultiAutoCompleteTextView hashtag;
	private String hashtags_string;
	private String person_shared;
	private SharedConversation mSharedConversation;
	private int n_characters;
	private SimplePopupWindow popup;
	private BroadcastReceiver tickReceiver;
	protected boolean isIdealLength;
	private ImageButton mTextIndicatorButton;
	private ProgressDialog progress;
	private boolean delayPressed;
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor editor;
	private MainApplication mApp;
	private String thread_id;
	private TelephonyManager tmgr;
	private GetMessages loader;
	protected String convoID;
	private int shareType, scrollState;
	protected int itemInView;
	protected boolean startedScrolling;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_thread);
		
		mContext = getApplicationContext();
		tmgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		mPrefs = mContext.getSharedPreferences(Constants.PREFERENCE_TIME_DELAY_PROMPT, Context.MODE_PRIVATE);
		
		mListView = getListView();
		mLayout = (RelativeLayout) findViewById(R.id.main_layout);

		isChecked = false;
		delayPressed = false;
		loader = new GetMessages();
		
		mRecipientsAdapter = new RecipientsAdapter(this);
		mConfidantesEditor = (RecipientsEditor)findViewById(R.id.confidantes_editor);
		mConfidantesEditor.setAdapter(mRecipientsAdapter);
				
		if(getIntent() != null && !isPopulated) {
			
			number =  getIntent().getStringExtra(Constants.EXTRA_NUMBER); 
			name =  getIntent().getStringExtra(Constants.EXTRA_NAME); 
			thread_id = getIntent().getStringExtra(Constants.EXTRA_THREAD_ID); 
			
			if(number!=null && name!=null) {
				populateMessages();
				
				if(Constants.DEBUG==false) this.setTitle(name);
			} else {
				
				getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
		                | ActionBar.DISPLAY_SHOW_HOME);
            	getSupportActionBar().setDisplayHomeAsUpEnabled(true);				
				getSupportActionBar().setCustomView(R.layout.recipients_editor);
				
				mRecipientEditor = (RecipientsEditor) getSupportActionBar().getCustomView().findViewById(R.id.recipients_editor);
				mRecipientEditor.setAdapter(mRecipientsAdapter);
			}
		}		
		mTextIndicatorButton = (ImageButton) findViewById(R.id.textIndicatorButton);
		mTextIndicatorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup = new SimplePopupWindow(v);
				popup.showLikePopDownMenu();
				popup.setMessage("Write more! You haven't said enough");
			}
		});
		
		isIdealLength = false;
		text = (EditText) this.findViewById(R.id.text);
		
	    text.addTextChangedListener(new TextWatcher(){
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
	        	n_characters = s.length();
	        	if(n_characters>=Constants.MIN_TEXT_LENGTH && 
	        			n_characters<=Constants.MAX_TEXT_LENGTH && !isIdealLength) {
	        		isIdealLength = true;
	        		mTextIndicatorButton.setBackground(getResources().getDrawable(R.drawable.good_indicator));
	        		mTextIndicatorButton.setOnClickListener(new View.OnClickListener() {
	        			@Override
	        			public void onClick(View v) {
	        				popup = new SimplePopupWindow(v);
	        				popup.showLikePopDownMenu();
	        				popup.setMessage("This message looks great!");
	        			}
	        		});	        		
	        		
	        	} 
	        	else if(isIdealLength && n_characters>Constants.MAX_TEXT_LENGTH) {
	        		isIdealLength = false;
	        		mTextIndicatorButton.setBackground(getResources().getDrawable(R.drawable.bad_indicator));
	        		mTextIndicatorButton.setOnClickListener(new View.OnClickListener() {
	        			@Override
	        			public void onClick(View v) {
	        				popup = new SimplePopupWindow(v);
	        				popup.showLikePopDownMenu();
	        				popup.setMessage("This message is too long :/");
	        			}
	        		});		        		
	        		
	        	} 	        	
	        	else if(isIdealLength && n_characters<Constants.MIN_TEXT_LENGTH) {
	        		isIdealLength = false;
	        		mTextIndicatorButton.setBackground(getResources().getDrawable(R.drawable.pendinh_indicator));
	        		mTextIndicatorButton.setOnClickListener(new View.OnClickListener() {
	        			@Override
	        			public void onClick(View v) {
	        				popup = new SimplePopupWindow(v);
	        				popup.showLikePopDownMenu();
	        				popup.setMessage("Write more! You haven't said enough");
	        			}
	        		});
	        	} 	 
				
			}
	    }); 	
	    hideKeyboard();
	    text.clearFocus();
	    
		messages = new ArrayList<SMSMessage>();
		adapter = new QueuedMessageAdapter(this, messages);
		send = (Button) this.findViewById(R.id.send_button);
		share = (Button) this.findViewById(R.id.share_button);
		pickDelay = (Button) this.findViewById(R.id.time_delay_button);
		viewSwitcher = (ViewSwitcher) this.findViewById(R.id.viewSwitcher);
		
		hashtag = (MultiAutoCompleteTextView) this.findViewById(R.id.hashtags);
		//hashtag.setText("#");
		ArrayAdapter<String> aaEmo = new ArrayAdapter<String>(
				this,android.R.layout.simple_spinner_dropdown_item,Constants.ALL_HASHTAGS);
		hashtag.setAdapter(aaEmo);
		hashtag.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer() );	
		
		setListAdapter(adapter);
		
		sendDate = null;
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mBroadcastReceiver = 
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				
				if(action.equals(Constants.ACTION_SHARE_COMPLETE)) {
					int resultCode = intent.getIntExtra(Constants.EXTRA_SHARE_RESULT_CODE, 0);
					switch(resultCode) {
					case(0):
						String convo_id = intent.getStringExtra(Constants.EXTRA_SHARED_CONVERSATION_ID);
			            Intent finished = new Intent(mContext, SharedConversationActivity.class);
			            finished.putExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, convo_id);
			            startActivity(finished);	
			            break;
					case(ParseException.CONNECTION_FAILED):
						Toast.makeText(mContext, "Share failed: not connected", 
								Toast.LENGTH_LONG).show();	
						break;
					default:
						Toast.makeText(mContext, "Share failed with error code " + resultCode, 
								Toast.LENGTH_LONG).show();
					}
					progress.dismiss(); 
					share.setClickable(true);
					return;
				}				
				
				String intent_number = intent.getStringExtra(Constants.EXTRA_RECIPIENT);
				if(number==null) return;
				if(intent_number==null) return;
				if(!intent_number.equals(number)) return;

				Long scheduledOn = intent.getLongExtra(Constants.EXTRA_TIME_LAUNCHED, 0);
				String intent_message = intent.getStringExtra(Constants.EXTRA_MESSAGE_BODY);

				if(action.equals(Constants.ACTION_SMS_DELIVERED)) {
					switch (getResultCode()) {
						case Activity.RESULT_OK: {
								
							if(delayedMessages.containsKey(scheduledOn) && scheduledOn>0) {
								int id = delayedMessages.get(scheduledOn);
								removeMessage(messages.size() - id - 1);
								delayedMessages.remove(scheduledOn);
							}
							SMSMessage message = new SMSMessage(intent_message, true);
							message.setDate(System.currentTimeMillis());
							addNewMessage(message, false);
							break;
						}
						default: {
							text.setText(intent_message);
							Toast.makeText(getApplicationContext(), "SMS not sent", Toast.LENGTH_SHORT).show();
							break;
						}
					}
				} else if(action.equals(Constants.ACTION_SMS_OUTBOXED)) {
					Long scheduledFor = intent.getLongExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, 0);
					delayedMessages.put(scheduledOn, messages.size());
					SMSMessage m = new SMSMessage(intent_message, true);
					m.isDelayed = true;
					m.futureSendTime = scheduledFor;
					m.launchedOn = scheduledOn;
					m.setRecipient(intent_number);
					addNewMessage(m, false);

				} else if(action.equals(Constants.ACTION_SMS_UNOUTBOXED)) {
					
					int id = delayedMessages.get(scheduledOn);
					removeMessage(messages.size() - id - 1);
					delayedMessages.remove(id);
					text.setText(intent_message);
					
				} 
			}
		};				
				
		
	    customDateTimePicker = new CustomDateTimePicker(this,
            new CustomDateTimePicker.ICustomDateTimeListener() {

                @Override
                public void onCancel() {
                	delayPressed = true;
                }

				@SuppressWarnings("deprecation")
				@Override
				public void onSet(Dialog dialog, Calendar calendarSelected,
						Date dateSelected, int year, String monthFullName,
						String monthShortName, int monthNumber, int date,
						String weekDayFullName, String weekDayShortName,
						int hour24, int hour12, int min, int sec,
						String AM_PM) {
					sendDate = dateSelected;
					updateDelayButton();
					delayPressed = true;
				}
            });
	    /**
	     * Pass Directly current time format it will return AM and PM if you set
	     * false
	     */
	    customDateTimePicker.set24HourFormat(false);		
	    customDateTimePicker.setDate(Calendar.getInstance());
        if(Constants.DEBUG==true) {
    		addNewMessage(new SMSMessage("hi",true),false);
    		addNewMessage(new SMSMessage("whats up",false),false);
    		addNewMessage(new SMSMessage("asl?",true),false);
    		addNewMessage(new SMSMessage("14/f/nm",false),false);        	
        }		
        
		tickReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateTime();
			}
		};
		hideKeyboard();
		text.clearFocus();	

		mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mListView.setStackFromBottom(true);
		mListView.setOnScrollListener(new OnScrollListener() {

		    @Override
		    public void onScrollStateChanged(AbsListView view, int s) {
		        scrollState = s;	   
		        if(scrollState != SCROLL_STATE_IDLE) startedScrolling=true;
		        else if (mListView.getLastVisiblePosition() == adapter.getCount() -1 /**&&
		    			mListView.getChildAt(mListView.getChildCount() - 1).getBottom() <= mListView.getHeight()*/)
		    		{
		    		startedScrolling=false;
		    		Log.i("at bottom","at bottom");

		    		}		  
		    }

		    @Override
		    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		    	if (mListView.getLastVisiblePosition() == adapter.getCount() -1 /**&&
		    			mListView.getChildAt(mListView.getChildCount() - 1).getBottom() <= mListView.getHeight()*/)
		    		{
		    		startedScrolling=false;
		    		Log.i("at bottom","at bottom");

		    		}				    
		    }
		});
		
		mLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				if ((mLayout.getRootView().getHeight() - mLayout.getHeight()) > mLayout.getRootView().getHeight() / 3) {

				} 
			}
		});			
	}
	

	@Override
	 public void onResume() {
		super.onResume();	
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_SMS_OUTBOXED);
		intentFilter.addAction(Constants.ACTION_SMS_UNOUTBOXED);
		intentFilter.addAction(Constants.ACTION_SMS_DELIVERED);		
		intentFilter.addAction(Constants.ACTION_SHARE_COMPLETE);		
		registerReceiver(mBroadcastReceiver, intentFilter);	
		
		calendar.get(Calendar.HOUR_OF_DAY);
		calendar.get(Calendar.MINUTE);	
		registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		hideKeyboard();
		text.clearFocus();
	}	
	
	
	private void populateMessages(){
		loader.execute(); 
		isPopulated = true;
	}
	
	private class GetMessages extends AsyncTask<Void,SMSMessage,Void> {
		DatabaseHandler dh;
	  	Cursor messages_cursor;
	  	SMSMessage m;
	  	@Override
		protected Void doInBackground(Void... params) {
			String querystring;
			if(thread_id == null) {
				querystring = 
				"REPLACE(REPLACE(REPLACE(REPLACE(" + TextBasedSmsColumns.ADDRESS + 
						",'(',''),')',''),' ',''),'-','') " 
						+ " in ('"+ 
		   				ContentUtils.subtractCountryCode(number) + "', '" +
		   				ContentUtils.addCountryCode(number) + "', '+" +
		   				ContentUtils.addCountryCode(number) + "', '" +
		   				number + "')" 
		   				+  " and " + TextBasedSmsColumns.TYPE + "!=" + 
		   				TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX 					
   				+  " and " + TextBasedSmsColumns.TYPE + "!=" + 
		   				TextBasedSmsColumns.MESSAGE_TYPE_DRAFT ;					

			}
			else querystring = TextBasedSmsColumns.THREAD_ID + "=" + thread_id 
					+ " and " + TextBasedSmsColumns.TYPE + "!=" + 
					TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX
				+  " and " + TextBasedSmsColumns.TYPE + "!=" + 
					TextBasedSmsColumns.MESSAGE_TYPE_DRAFT ;					
	        dh = new DatabaseHandler(mContext);
	        messages_cursor = dh.getPendingMessagesCursor(number);
	        
	        if(messages_cursor.moveToFirst()) {
	        	m = getNextOutboxMessage(messages_cursor);
	        	publishProgress(m);	
	        	while(messages_cursor.moveToNext()) {
	        		m = getNextOutboxMessage(messages_cursor);
	        		publishProgress(m);	
	        		if (isCancelled()) break;
	        	}	     
	        }	  
	        
			messages_cursor = mContext.getContentResolver().query(Uri.parse("content://sms"),
					null,querystring ,null,TextBasedSmsColumns.DATE + " desc");
			
	        if(messages_cursor.moveToFirst()) {
	        	m = getNextMessage(messages_cursor);
	        	publishProgress(m);		
	        	while(messages_cursor.moveToNext()) {
	        		m = getNextMessage(messages_cursor);
	        		publishProgress(m);		
	        		if (isCancelled()) break;
	        	}	     
	        }
	        
			return null;
		}
		@Override
	    protected void onProgressUpdate(SMSMessage... t) {
			addNewMessage(t[0],true);
	    }				
		
		@Override
	    protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			messages_cursor.close();
			dh.close();
		/*	mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
			adapter.notifyDataSetChanged();
			mListView.refreshDrawableState();*/
	    }			

  }	
	
	private void updateTime(){
		//Log.i("time", "Updated Time");
		updateDelayButton();
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mBroadcastReceiver);
		unregisterReceiver(tickReceiver);
	}
	
	private void updateDelayButton(){
		if(sendDate!=null){
			CharSequence date = DateUtils.getRelativeDateTimeString(mContext, sendDate.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
			pickDelay.setText(date);	
		}
	}
	// make the menu work
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//actionbar menu
		getSupportMenuInflater().inflate(R.menu.thread_menu, menu);
		return true;
	}	
	

	// when a user selects a menu item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			loader.cancel(true);	
            NavUtils.navigateUpFromSameTask(this);
            return true;
		case R.id.menu_share:
			if(isChecked) {
				adapter.showCheckboxes = false;
				isChecked = false;
				viewSwitcher.setDisplayedChild(0);
			}
			else {
				adapter.showCheckboxes = true;
				isChecked = true;
				viewSwitcher.setDisplayedChild(1);
			}
			return true;		
		case R.id.menu_autoforward:
			
			return true;	
		default:
			return false;
		}
	}	

	private SMSMessage getNextOutboxMessage(Cursor c) {
		String body="";
		body = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.BODY)).toString();
		long scheduledFor = Long.parseLong(c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.DATE)).toString());
		long scheduledOn = Long.parseLong(c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.DATE_SENT)).toString());
		delayedMessages.put(scheduledOn, messages.size());
		SMSMessage m = new SMSMessage(body, true);
		m.isDelayed = true;
		m.futureSendTime = scheduledFor;
		m.launchedOn = scheduledOn;
		m.setRecipient(number);
		return m;	
	}	
	
	private SMSMessage getNextMessage(Cursor c) {
		String body="", SmsMessageId="", address="", read="";
		long date;
		int type = Integer.parseInt(c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.TYPE)).toString());
		if(type==TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX) return null;
		body = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.BODY)).toString();
    	SmsMessageId = c.getString(c.getColumnIndexOrThrow("_id")).toString();
    	address = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.ADDRESS)).toString();
    	read = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.READ)).toString();
    	date = c.getLong(c.getColumnIndexOrThrow(TextBasedSmsColumns.DATE));
    	if(!SmsMessageId.equals("")) {
        	ContentValues values = new ContentValues();
    		values.put("read",true);
    		getContentResolver().update(Uri.parse("content://sms/"),values, "_id="+SmsMessageId, null);	
    	}	
    	return new SMSMessage(date, body, address, type);
	}	

	public void sendMessage(View v)
	{

		newMessage = text.getText().toString().trim(); 

		if(newMessage.length() > 0)
		{
			
			if(number == null) {
				recipients = mRecipientEditor.constructContactsFromInput(false).getToNumbers();
				if(recipients.length > 1) {
					Toast.makeText(mContext, "Can only send to 1 recipient", Toast.LENGTH_LONG).show();
					return;
				}
				else if(recipients.length == 0) {
					Toast.makeText(mContext, "No recipients selected", Toast.LENGTH_LONG).show();
					return;
				}

				number = recipients[0];	
			}			
          
            if(name == null) {
            	name = ContentUtils.getContactDisplayNameByNumber(mContext, number);
            	getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
            	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            	getSupportActionBar().setDisplayShowTitleEnabled(true);
            	if(Constants.DEBUG==false) this.setTitle(name);
            	populateMessages();
            }
			
			if(!delayPressed && mPrefs.getBoolean(Constants.PREFERENCE_TIME_DELAY_PROMPT, true)) {
				askAboutTimeDelay();
				return;
			}               
            new DelayedSend(mContext, number, newMessage, sendDate, System.currentTimeMillis()).start();
            
        	text.setText("");
        	newMessage = "";
        	if(sendDate!=null&&calendar.getTime().before(sendDate)) {
        		pickDelay.setText(R.string.compose_select_time);
        		sendDate = null; 
        	}
            
            
		}
		hideKeyboard();
		text.clearFocus();
	}
	
	public void pickTime(View v) {
		customDateTimePicker.showDialog();		
	}
	
	private void addNewMessage(SMSMessage m, boolean onTop)
	{
		if(onTop) {
			messages.add(m);
			if(!startedScrolling) {
				adapter.notifyDataSetChanged();	
				mListView.refreshDrawableState();
			}
			
		}
		else  {
			messages.add(0, m);
			adapter.notifyDataSetChanged();			
		}
	}
	
	public void removeEditOption(int id) {
		adapter.getItem(id).isDelayed = false;
		adapter.notifyDataSetChanged();	
	}
	
	public void removeMessage(int id) {
		messages.remove(id);
		adapter.notifyDataSetChanged();	
	}	

    public void getShareContent() {
		final long date = System.currentTimeMillis();
		
		recipients = mConfidantesEditor.constructContactsFromInput(false).getToNumbers();
		hashtags_string = hashtag.getText().toString().trim(); 
		
		mApp = (MainApplication) getApplication(); 
		ParseQuery<SharedConversation> query = ParseQuery.getQuery("SharedConversation");
		query.whereEqualTo("original_recipient", ContentUtils.addCountryCode(number));
		query.whereEqualTo("sharer", ContentUtils.addCountryCode(mApp.getUserName()));
		query.whereEqualTo("confidante", ContentUtils.addCountryCode(recipients[0]));
		query.findInBackground(new FindCallback<SharedConversation>() {
		    public void done(List<SharedConversation> convoList, ParseException e) {
		        if (convoList.size()>0) {
		        	shareType = Constants.NOTIFICATION_UPDATE_SHARE;
		        	mSharedConversation = convoList.get(0);
		        	addMessages();
		            Log.d("isContinuation", "Of convo " + mSharedConversation.getObjectId());
		        } else {
		        	shareType = Constants.NOTIFICATION_NEW_SHARE;
		        	mSharedConversation = new SharedConversation(date, recipients[0], number);
		        	mSharedConversation.setSharer(mApp.getUserName());
		        	mSharedConversation.setOriginalRecipientName(name);		
		        	addMessages();
		            Log.d("isContinuation", "Not a continuation");
		        }
		    }
		});	
		
    }     

	protected void addMessages() {
        for (SMSMessage p : messages) {
            if (p.box && !p.getMessage().equals("")) {
            	mSharedConversation.addMessage(p);
            }
        }
        if (hashtags_string.length()>0) for(String h : hashtags_string.split(",")) {
        	h = h.trim();
        	if(!h.isEmpty()) {
        		SMSMessage ht = new SMSMessage();
	        	ht.setDate(System.currentTimeMillis());
	        	ht.setMessage(h);
	        	ht.setHashtag();
	        	mSharedConversation.addMessage(ht);
	        	
        	}
        }
        Log.d("number of messages in convo",""+mSharedConversation.getMessages().size());
		if(recipients.length>0 && mSharedConversation.getMessages().size()>0)
		{
            new ShareTagAction(mContext, mSharedConversation, shareType).start();					
            share.setClickable(false);
            progress = new ProgressDialog(this);
            progress.setTitle("Sharing conversation");
            progress.setMessage("Sending...");
            progress.show();
                       
	        
		}        
		
	}


	public void shareMessages(View v) throws InterruptedException
	{
		getShareContent();

	}	    
	
	public void askAboutTimeDelay(){
		View checkBoxView = View.inflate(this, R.layout.checkbox, null);
		CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    	editor = mPrefs.edit();
		        // Save to shared preferences
				editor.putBoolean(Constants.PREFERENCE_TIME_DELAY_PROMPT, !isChecked);	
				editor.commit();		    	
		    }
		});
		delayPressed = true;
		checkBox.setText("Never ask me about text delay");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setTitle("Set text delay?");
		    builder.setMessage("I noticed you didn't set a time delay for that text. " + 
		    "Maybe you will want to edit your text before it gets sent." + 
		    " Want to set a delay?")
		           .setView(checkBoxView)
		           .setCancelable(true)
		           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		            	   dialog.cancel();
		            	   customDateTimePicker.showDialog();	
		            	 
		               	}
		           })
		           .setNegativeButton("No", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                    dialog.cancel();
		                    sendMessage(send);
		               }
		           }).show();		

	}
	private void hideKeyboard(){
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
		
	}	
		
}