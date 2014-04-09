package com.Pull.pullapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DelayedSend;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;
import com.Pull.pullapp.util.ShareTagAction;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MessageActivityCheckbox extends SherlockListActivity {

	private ArrayList<SMSMessage> messages;
	private HashMap<Long,Integer> delayedMessages = new HashMap<Long,Integer>();
	private MessageAdapter adapter;
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
	
	private BroadcastReceiver tickReceiver;
	protected boolean isIdealLength;
	private ImageView mTextIndicatorImageView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_thread);
		
		mContext = getApplicationContext();
		mListView = getListView();
		mLayout = (RelativeLayout) findViewById(R.id.main_layout);
		
		isChecked = false;
		
		
		mRecipientsAdapter = new RecipientsAdapter(this);
		mConfidantesEditor = (RecipientsEditor)findViewById(R.id.confidantes_editor);
		mConfidantesEditor.setAdapter(mRecipientsAdapter);
		
		mTextIndicatorImageView = (ImageView) findViewById(R.id.textIndicatorImageView);
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
	        		mTextIndicatorImageView.setImageDrawable(getResources().getDrawable(R.drawable.good_indicator));
	        	} 
	        	else if(isIdealLength && n_characters>Constants.MAX_TEXT_LENGTH) {
	        		isIdealLength = false;
	        		mTextIndicatorImageView.setImageDrawable(getResources().getDrawable(R.drawable.bad_indicator));
	        	} 	        	
	        	else if(isIdealLength && n_characters<Constants.MIN_TEXT_LENGTH) {
	        		isIdealLength = false;
	        		mTextIndicatorImageView.setImageDrawable(getResources().getDrawable(R.drawable.pendinh_indicator));
	        	} 	 
				
			}
	    }); 	
		messages = new ArrayList<SMSMessage>();
		adapter = new MessageAdapter(this, messages);
		send = (Button) this.findViewById(R.id.send_button);
		share = (Button) this.findViewById(R.id.share_button);
		pickDelay = (Button) this.findViewById(R.id.time_delay_button);
		//sendInTimeLabel = (TextView) this.findViewById(R.id.send_in_time_label);
		viewSwitcher = (ViewSwitcher) this.findViewById(R.id.viewSwitcher);
		
		hashtag = (MultiAutoCompleteTextView) this.findViewById(R.id.hashtags);
		hashtag.setText("#");
		ArrayAdapter<String> aaEmo = new ArrayAdapter<String>(
				this,android.R.layout.simple_spinner_dropdown_item,Constants.ALL_HASHTAGS);
		hashtag.setAdapter(aaEmo);
		hashtag.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer() );	
		
		setListAdapter(adapter);
		
		sendDate = calendar.getTime();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mBroadcastReceiver = 
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
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
								removeMessage(id);
								delayedMessages.remove(scheduledOn);
								addNewMessage(new SMSMessage(intent_message, true));
							} else {
								addNewMessage(new SMSMessage(intent_message, true));
							}
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
					addNewMessage(m);

				} else if(action.equals(Constants.ACTION_SMS_UNOUTBOXED)) {
					
					int id = delayedMessages.get(scheduledOn);
					removeMessage(id);
					delayedMessages.remove(id);
					sendDate = calendar.getTime();
					text.setText(intent_message);
					
				}
			}
		};				
				
		
	    customDateTimePicker = new CustomDateTimePicker(this,
            new CustomDateTimePicker.ICustomDateTimeListener() {

                @Override
                public void onCancel() {

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
				}
            });
	    /**
	     * Pass Directly current time format it will return AM and PM if you set
	     * false
	     */
	    customDateTimePicker.set24HourFormat(false);		
	    customDateTimePicker.setDate(Calendar.getInstance());
        if(Constants.DEBUG==true) {
    		addNewMessage(new SMSMessage("hi",true));
    		addNewMessage(new SMSMessage("whats up",false));
    		addNewMessage(new SMSMessage("asl?",true));
    		addNewMessage(new SMSMessage("14/f/nm",false));        	
        }		
        
		tickReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateTime();
			}
		};

        
	}
	

	@Override
	 public void onResume() {
		super.onResume();	
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_SMS_OUTBOXED);
		intentFilter.addAction(Constants.ACTION_SMS_UNOUTBOXED);
		intentFilter.addAction(Constants.ACTION_SMS_DELIVERED);		
		registerReceiver(mBroadcastReceiver, intentFilter);	
		
		if(getIntent() != null && !isPopulated) {
		
			number =  getIntent().getStringExtra(Constants.EXTRA_NUMBER); 
			name =  getIntent().getStringExtra(Constants.EXTRA_NAME); 
			
			
			if(number!=null && name!=null) {
				mLayout.setBackgroundColor(getResources().getColor(R.color.messageDefaultBackground));
		        Cursor c = mContext.getContentResolver().query(Uri.parse("content://sms"),null,
			       		TextBasedSmsColumns.ADDRESS + " in ('"+ContentUtils.addCountryCode(number) + "', '" +
			       				number + "')" + " and " +
		        		TextBasedSmsColumns.TYPE + "!=" + TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX ,null,
		        		TextBasedSmsColumns.DATE);
		        if(c.moveToFirst()) {
		        	readMessage(c);
		        	while(c.moveToNext()) {
		        		readMessage(c);
		        	}
		        	c.close();		     
		        }
		        
		        c = mContext.getContentResolver().query(Uri.parse("content://sms"),null,
	       		TextBasedSmsColumns.ADDRESS + " in ('"+ContentUtils.addCountryCode(number) + "', '" +
	       				number + "')" + " and " +
	       		TextBasedSmsColumns.TYPE + "=" + TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX ,null,
	       		TextBasedSmsColumns.DATE);
		        if(c.moveToFirst()) {
		        	readOutboxMessage(c);
		        	while(c.moveToNext()) {
		        		readOutboxMessage(c);
		        	}
		        	c.close();		     
		        }	        
		        isPopulated=true;
				if(Constants.DEBUG==false) this.setTitle(name);
				if(messages.size()>0) mListView.setSelection(messages.size()-1);
			} else {
				
				getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
		                | ActionBar.DISPLAY_SHOW_HOME);
				
				getSupportActionBar().setCustomView(R.layout.recipients_editor);
				
				mRecipientEditor = (RecipientsEditor) getSupportActionBar().getCustomView().findViewById(R.id.recipients_editor);
				mRecipientEditor.setAdapter(mRecipientsAdapter);
//				mListView.setVisibility(View.GONE);
//				mLayout.setBackgroundColor(Color.WHITE);
			}
		}
		
		calendar.get(Calendar.HOUR_OF_DAY);
		calendar.get(Calendar.MINUTE);	
		registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
					
	}	
	
	private void updateTime(){
		Log.i("time", "Updated Time");
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
			mListView.invalidateViews();
			mListView.refreshDrawableState();
			setListAdapter(adapter);
			mListView.setSelection(messages.size()-1);
			//Toast.makeText(mContext, "hi", Toast.LENGTH_LONG).show();
			return true;		
		case R.id.menu_autoforward:
			
			return true;	
		default:
			return false;
		}
	}	

	private void readOutboxMessage(Cursor c) {
		String body="";
		int type = Integer.parseInt(c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.TYPE)).toString());
		if(type!=TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX) return;
		body = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.BODY)).toString();
		long scheduledFor = Long.parseLong(c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.DATE)).toString());
		long scheduledOn = Long.parseLong(c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.DATE_SENT)).toString());
		delayedMessages.put(scheduledOn, messages.size());
		SMSMessage m = new SMSMessage(body, true);
		m.isDelayed = true;
		m.futureSendTime = scheduledFor;
		m.launchedOn = scheduledOn;
		m.setRecipient(number);
		addNewMessage(m);	
	}	
	private void readMessage(Cursor c) {
		String body="", SmsMessageId="", address="", read="";
		long date;
		int type = Integer.parseInt(c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.TYPE)).toString());
		if(type==TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX) return;
		body = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.BODY)).toString();
    	SmsMessageId = c.getString(c.getColumnIndexOrThrow("_id")).toString();
    	address = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.ADDRESS)).toString();
    	read = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.READ)).toString();
    	date = c.getLong(c.getColumnIndexOrThrow(TextBasedSmsColumns.DATE));
    	addNewMessage(new SMSMessage(date, body, address, type));
    	if(read.equals("0") && !SmsMessageId.equals("")) {
        	ContentValues values = new ContentValues();
    		values.put("read",true);
    		getContentResolver().update(Uri.parse("content://sms/"),values, "_id="+SmsMessageId, null);	
    	}		
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
				number = recipients[0];	
			}			
            new DelayedSend(mContext, number, newMessage, 
            		sendDate, System.currentTimeMillis()).start();
            
            if(name == null) {

				String name = ContentUtils
						.getContactDisplayNameByNumber(mContext, number);
				Intent outintent = new Intent(mContext, MessageActivityCheckbox.class);
				outintent.putExtra(Constants.EXTRA_NUMBER, number);			
				outintent.putExtra(Constants.EXTRA_NAME, name);			
		        startActivity(outintent);		            	
            } else {
	        	text.setText("");
	        	newMessage = "";
	        	if(calendar.getTime().before(sendDate)) {
	        		pickDelay.setText(R.string.compose_select_time);
	        		sendDate = null; 
	        	}
            }
		}
		mListView.setSelection(messages.size()-1);
	}
	
	public void pickTime(View v) {
		customDateTimePicker.showDialog();		
	}
	
	private void addNewMessage(SMSMessage m)
	{
		messages.add(m);
		adapter.notifyDataSetChanged();
		mListView.setSelection(messages.size()-1);
	}
	
	public void removeEditOption(int id) {
		adapter.getItem(id).isDelayed = false;
		adapter.notifyDataSetChanged();	
		mListView.setSelection(messages.size()-1);
	}
	
	public void removeMessage(int id) {
		messages.remove(id);
		adapter.notifyDataSetChanged();	
		mListView.setSelection(messages.size()-1);
	}	

    public void getShareContent() {
		long date = System.currentTimeMillis();
		
		recipients = mConfidantesEditor.constructContactsFromInput(false).getToNumbers();
		hashtags_string = hashtag.getText().toString().trim(); 
		
    	mSharedConversation = new SharedConversation(date, recipients[0], number);
        for (SMSMessage p : messages) {
            if (p.box && !p.getMessage().equals("")) {
            	mSharedConversation.addMessage(p);
            }
        }
        for(String h : hashtags_string.split(",")) {
        	if(!h.trim().isEmpty()) {
	        	SMSMessage c = new SMSMessage();
	        	c.setDate(System.currentTimeMillis());
	        	c.setMessage(h);
	        	c.setHashtagID(Constants.ALL_HASHTAGS_LIST.indexOf(h));
	        	mSharedConversation.addMessage(c);
        	}
        }
    }     
    
	public void shareMessages(View v) throws InterruptedException
	{
		getShareContent();
		
		if(recipients.length>0 && mSharedConversation.getMessages().size()>0)
		{
            new ShareTagAction(mContext, mSharedConversation).start();					

			Intent outintent = new Intent(mContext, SharedListActivity.class);		
	        startActivity(outintent);			
	        
		}
					
	}	    
}
