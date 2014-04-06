package com.Pull.pullapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DelayedSend;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;
import com.Pull.pullapp.util.sendSMS;
import com.Pull.pullapp.R;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * MessageActivity is a main Activity to show a ListView containing SMSMessage items
 * 
 */
public class MessageActivityCheckbox extends SherlockListActivity {
	/** Called when the activity is first created. */

	private ArrayList<SMSMessage> messages;
	private HashMap<Long,Integer> delayedMessages = new HashMap<Long,Integer>();
	private MessageAdapter adapter;
	private EditText text;
	private String name, number, newMessage;
	private Context mContext;
	private final Calendar calendar = Calendar.getInstance();
	private Button pickDelay, send;
	private boolean isChecked, isPopulated;
	private CustomDateTimePicker customDateTimePicker;
	private Date sendDate;
	private RecipientsEditor mRecipientEditor;
	private String[] recipients;
	private ListView mListView;
	private RelativeLayout mLayout;
	private BroadcastReceiver mBroadcastReceiver;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_thread);
		
		mContext = getApplicationContext();
		mListView = getListView();
		mLayout = (RelativeLayout) findViewById(R.id.main_layout);
		
		isChecked = false;
		
		mRecipientEditor = (RecipientsEditor)findViewById(R.id.recipients_editor);
		mRecipientEditor.setAdapter(new RecipientsAdapter(this));
		
		text = (EditText) this.findViewById(R.id.text);
		messages = new ArrayList<SMSMessage>();
		adapter = new MessageAdapter(this, messages);
		send = (Button) this.findViewById(R.id.send_button);
		pickDelay = (Button) this.findViewById(R.id.time_delay_button);
		
		setListAdapter(adapter);
		
		sendDate = calendar.getTime();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_SMS_OUTBOXED);
		intentFilter.addAction(Constants.ACTION_SMS_UNOUTBOXED);
		intentFilter.addAction(Constants.ACTION_SMS_DELIVERED);
		
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
					text.setText(intent_message);
					
				}
			}
		};				
		registerReceiver(mBroadcastReceiver, intentFilter);			
		
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
					updateDelayButton(calendarSelected);
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
	}
	@Override
	 public void onResume() {
		super.onResume();	
		
		if(getIntent() != null && !isPopulated) {
		
			number =  getIntent().getStringExtra(Constants.EXTRA_NUMBER); 
			name =  getIntent().getStringExtra(Constants.EXTRA_NAME); 
			
			if(number!=null && name!=null) {
				mRecipientEditor.setVisibility(View.GONE);
				mLayout.setBackgroundColor(R.color.messageDefaultBackground);
		        Cursor c = mContext.getContentResolver().query(Uri.parse("content://sms"),null,
		        		TextBasedSmsColumns.ADDRESS + "='"+sendSMS.addCountryCode(number) + "' and " +
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
	       		TextBasedSmsColumns.ADDRESS + " in ('"+sendSMS.addCountryCode(number) + "', '" +
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
				mRecipientEditor.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.GONE);
				mLayout.setBackgroundColor(Color.WHITE);
			}
		}
		
		calendar.get(Calendar.HOUR_OF_DAY);
		calendar.get(Calendar.MINUTE);	

	}	
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mBroadcastReceiver);
	}	
	private void updateDelayButton(Calendar calendarSelected){
		String AM_PM = "PM";
		if(calendarSelected.get(Calendar.AM_PM)==0) AM_PM="AM";
		if(sendDate.getDay()==calendar.getTime().getDay()) {
			pickDelay.setText(
					new StringBuilder()
					.append(calendarSelected.get(Calendar.HOUR) + ":")
					.append(CustomDateTimePicker.pad(calendarSelected.get(Calendar.MINUTE)))
                    .append(" " + AM_PM));							
		} else {
			pickDelay.setText(
					new StringBuilder()
					.append(calendarSelected.get(Calendar.MONTH)+1)
					.append("/").append(calendarSelected.get(Calendar.DAY_OF_MONTH))
					.append("/").append(calendarSelected.get(Calendar.YEAR))
					.append(", ").append(calendarSelected.get(Calendar.HOUR) + ":")
					.append(CustomDateTimePicker.pad(calendarSelected.get(Calendar.MINUTE)))
                    .append(" " + AM_PM));
		}		
	}
	// make the menu work
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//actionbar menu
		getSupportMenuInflater().inflate(R.menu.home_action, menu);
		return true;
	}	
	

	// when a user selects a menu item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_share:
			if(isChecked) {
				adapter.showCheckboxes = false;
				isChecked = false;
			}
			else {
				adapter.showCheckboxes = true;
				isChecked = true;				
			}
			mListView.invalidateViews();
			mListView.refreshDrawableState();
			setListAdapter(adapter);
			//Toast.makeText(mContext, "hi", Toast.LENGTH_LONG).show();
			return true;
		case R.id.menu_send:
			showResult();
			return true;			
		case R.id.menu_autoforward:
			
			return true;	
		case R.id.menu_timeselect:
			
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
		int type = Integer.parseInt(c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.TYPE)).toString());
		if(type==TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX) return;
		body = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.BODY)).toString();
    	SmsMessageId = c.getString(c.getColumnIndexOrThrow("_id")).toString();
    	address = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.ADDRESS)).toString();
    	read = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.READ)).toString();
    	addNewMessage(new SMSMessage(body, address, type));
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
	        		pickDelay.setText("Set delay");
	        		sendDate = calendar.getTime(); 
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

    public void showResult() {
    	String text = "";
    	String name = "";
    	String address = "";
        ArrayList<String> checked_messages = new ArrayList<String>();
        for (SMSMessage p : messages) {
            if (p.box && !p.getMessage().equals("")) {
            	address = p.getSender();
            	name = ContentUtils.getContactDisplayNameByNumber(mContext, address);
            	if(p.sentByMe) {
            		text =  "Me: " + p.getMessage();
            	} else {
            		text = name + ": " + p.getMessage();
            	} 
            
                checked_messages.add(text);
            }
        }
        Intent intent = new Intent(mContext, ShareMoment.class);
        intent.putExtra(Constants.EXTRA_NAME,name);
        intent.putExtra(Constants.EXTRA_NUMBER,number);
        intent.putExtra(Constants.EXTRA_SET_OF_MESSAGES,checked_messages);
        startActivity(intent);	          
        //Toast.makeText(mContext, checked_messages.toString(), Toast.LENGTH_LONG).show();
    }      	
}