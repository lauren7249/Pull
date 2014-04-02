package com.Pull.smsTest;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.Pull.smsTest.model.SMSMessage;
import com.Pull.smsTest.util.Constants;
import com.Pull.smsTest.util.ContentUtils;
import com.Pull.smsTest.util.DelayedSend;
import com.Pull.smsTest.R;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * MessageActivity is a main Activity to show a ListView containing SMSMessage items
 * 
 */
public class MessageActivityCheckbox extends SherlockListActivity {
	/** Called when the activity is first created. */

	private ArrayList<SMSMessage> messages;
	private MessageAdapter adapter;
	private EditText text;
	private static Random rand = new Random();	
	private static String sender;
	private String name, threadID, number, newMessage;
	private Context mContext;
	private static final int TIME_DIALOG_ID = 0;
	private int mHour;
	private int mMinute;	
	private final Calendar calendar = Calendar.getInstance();
	private Button pickDelay, send;
	private boolean isChecked, isPopulated;
	private CustomDateTimePicker customDateTimePicker;
	private Date sendDate;
	private final String sent = "android.telephony.SmsManager.STATUS_ON_ICC_SENT";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_thread);
		
		mContext = getApplicationContext();
		
		isChecked = false;
		
		text = (EditText) this.findViewById(R.id.text);
		messages = new ArrayList<SMSMessage>();
		adapter = new MessageAdapter(this, messages);
		send = (Button) this.findViewById(R.id.send_button);
		pickDelay = (Button) this.findViewById(R.id.time_delay_button);
		
		setListAdapter(adapter);
		
		sendDate = calendar.getTime();
		
		String DELIVERED = "SMS_DELIVERED";
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
					case Activity.RESULT_OK: {
						addNewMessage(new SMSMessage(newMessage, true));						
						break;
					}
					default: {
						text.setText(newMessage);
						Toast.makeText(getApplicationContext(), "SMS not sent", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}
		}, new IntentFilter(DELIVERED));			
		
	    customDateTimePicker = new CustomDateTimePicker(this,
	            new CustomDateTimePicker.ICustomDateTimeListener() {

	                @Override
	                public void onCancel() {

	                }

					@Override
					public void onSet(Dialog dialog, Calendar calendarSelected,
							Date dateSelected, int year, String monthFullName,
							String monthShortName, int monthNumber, int date,
							String weekDayFullName, String weekDayShortName,
							int hour24, int hour12, int min, int sec,
							String AM_PM) {
						sendDate = dateSelected;
						if(sendDate.getDay()==calendar.getTime().getDay()) {
							pickDelay.setText(
									new StringBuilder()
									.append("Send at ")
									.append(hour12 + ":" + CustomDateTimePicker.pad(min)
		                                    + " " + AM_PM));							
						} else {
							pickDelay.setText(
									new StringBuilder()
									.append(monthNumber+1)
									.append("/").append(calendarSelected.get(Calendar.DAY_OF_MONTH))
									.append("/").append(year)
									.append(", ").append(hour12 + ":" + CustomDateTimePicker.pad(min)
		                                    + " " + AM_PM));
						}
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
			getListView().invalidateViews();
			getListView().refreshDrawableState();
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
	@Override
	 public void onResume() {
		super.onResume();	
		
		if(getIntent() != null && !isPopulated) {
			threadID =  getIntent().getStringExtra(Constants.EXTRA_THREAD_ID); 
			number =  getIntent().getStringExtra(Constants.EXTRA_NUMBER); 
			name =  getIntent().getStringExtra(Constants.EXTRA_NAME); 
			
	        Cursor c = mContext.getContentResolver().query(Uri.parse("content://sms"),
	        		null,"thread_id="+threadID,null,"date");
	        Log.i("tag","got past cursor");
	        if(c.moveToFirst()) {
	        	Log.i("tag","im not null");
	        	readMessage(c);
	        	while(c.moveToNext()) {
	        		readMessage(c);
	        	}
	        	c.close();		     
	        }
	        isPopulated=true;
		}
		
		mHour = calendar.get(Calendar.HOUR_OF_DAY);
		mMinute = calendar.get(Calendar.MINUTE);	

		if(Constants.DEBUG==false) this.setTitle(name);
		else this.setTitle("Julia");
		
        
       
	}
	
	private void readMessage(Cursor c) {
    	String body = c.getString(c.getColumnIndexOrThrow("body")).toString();
    	String SmsMessageId = c.getString(c.getColumnIndexOrThrow("_id")).toString();
    	String address = c.getString(c.getColumnIndexOrThrow("address")).toString();
    	String read = c.getString(c.getColumnIndexOrThrow("read")).toString();
    	boolean sent = c.getString(c.getColumnIndexOrThrow("type")).toString()
    			.equals(Integer.toString(TextBasedSmsColumns.MESSAGE_TYPE_SENT));
    	addNewMessage(new SMSMessage(body, address, sent));
    	if(read.equals("0")) {
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
			
            new DelayedSend(mContext, number, newMessage, sendDate, System.currentTimeMillis()).start();
        	text.setText("");

		}
	}
	
	public void pickTime(View v) {
		customDateTimePicker.showDialog();		
	}
	
	private void addNewMessage(SMSMessage m)
	{
		messages.add(m);
		adapter.notifyDataSetChanged();
		getListView().setSelection(messages.size()-1);
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
        intent.putExtra(Constants.EXTRA_SET_OF_MESSAGES,checked_messages);
        startActivity(intent);	          
        //Toast.makeText(mContext, checked_messages.toString(), Toast.LENGTH_LONG).show();
    }      	
}