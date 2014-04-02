package com.Pull.smsTest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.Pull.smsTest.util.Constants;
import com.Pull.smsTest.util.ContentUtils;
import com.Pull.smsTest.util.DelayedSend;
import com.Pull.smsTest.util.RecipientsAdapter;
import com.Pull.smsTest.util.RecipientsEditor;
import com.Pull.smsTest.util.sendSMS;
import com.Pull.smsTest.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Compose extends Activity implements OnSeekBarChangeListener {

	//Options menu
	private static final int MENU_START_BARRAGE    = 0;
	private static final int MENU_SAVE_DRAFT       = 1;
	private static final int MENU_SAVE_AS_TEMPLATE = 2;
	private static final int MENU_CANCEL           = 3;
	
	private SeekBar timeBar; 
	 
	private long mID;
	private String[] recipients;
	
	private RecipientsEditor mRecipientEditor;
	private EditText         mContents;
	private Button           mSend;
	private Button           mSave;
	private Context mContext;
	private TextView delay, sendOn;
	
	private int delayMinutes;
	private static final SimpleDateFormat df_time = new SimpleDateFormat("hh:mm aa");	
	private static final SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
	private Date sendTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose);
		
		mContext = getApplicationContext();
		
		mRecipientEditor = (RecipientsEditor)findViewById(R.id.recipients_editor);
		mContents        = (EditText)findViewById(R.id.message);
		mSend            = (Button)findViewById(R.id.sendlater);
        timeBar 		 = (SeekBar)findViewById(R.id.delay); // make seekbar object
        delay = (TextView)findViewById(R.id.delay_text);
        sendOn = (TextView)findViewById(R.id.time_to_send_text);
     
        timeBar.setOnSeekBarChangeListener(this); // set seekbar listener.
        timeBar.incrementProgressBy(1);
        
        setSendTime();
        
		mRecipientEditor.setAdapter(new RecipientsAdapter(this));
		
		String DELIVERED = "SMS_DELIVERED";
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
					case Activity.RESULT_OK: {
						//Toast.makeText(getApplicationContext(), "SMS delivered", Toast.LENGTH_SHORT).show();	
						if(recipients.length == 1) {
							String thread_id = ContentUtils.getThreadIDFromNumber(mContext,recipients[0]);
							String name = ContentUtils.getContactDisplayNameByNumber(mContext, recipients[0]);				
							Intent intent = new Intent(getApplicationContext(), MessageActivityCheckbox.class);
							intent.putExtra(Constants.EXTRA_THREAD_ID,thread_id);
							intent.putExtra(Constants.EXTRA_NAME,name);
							intent.putExtra(Constants.EXTRA_READ,1);		          
							intent.putExtra(Constants.EXTRA_NUMBER,recipients[0]);
							startActivity(intent);
							
						} else {
					          Intent intent = new Intent(getApplicationContext(), ThreadsListActivity.class);
					          startActivity(intent);					
						}						
						break;
					}
					default: {
						Toast.makeText(getApplicationContext(), "SMS not sent", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}
		}, new IntentFilter(DELIVERED));		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		
		if (true) {
			//TODO: don't display these until required fields are complete
			menu.add(0, MENU_START_BARRAGE, 0, R.string.menu_start_barrage);
			menu.add(0, MENU_SAVE_DRAFT, 0, R.string.menu_save_draft);
			menu.add(0, MENU_SAVE_AS_TEMPLATE, 0, R.string.menu_save_as_template);
		}
		menu.add(0, MENU_CANCEL, 0, R.string.menu_cancel);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_START_BARRAGE:
				return true;
			case MENU_SAVE_DRAFT:
				return true;
			case MENU_SAVE_AS_TEMPLATE:
				return true;
			case MENU_CANCEL:
				this.finish();
				return true;
		}
		return false;
	}
	
	public void sendMessage(View v)
	{
		String newMessage = mContents.getText().toString().trim(); 
		recipients = mRecipientEditor.constructContactsFromInput(false).getToNumbers();

		if(newMessage.length() > 0 && recipients.length>0)
		{
			for(String number: recipients) {
				sendSMS.sendsms(mContext, number, newMessage, true);
			}
   			
		}
	}
	
	public void sendMessageLater(View v)
	{
		String newMessage = mContents.getText().toString().trim(); 
		recipients = mRecipientEditor.constructContactsFromInput(false).getToNumbers();

		if(newMessage.length() > 0 && recipients.length>0)
		{
			for(String number: recipients) {
				new DelayedSend(mContext, number, newMessage, sendTime, System.currentTimeMillis()).start();
			}
			
		}
        Intent intent = new Intent(getApplicationContext(), ThreadsListActivity.class);
        startActivity(intent);				
	}	

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		setSendTime();
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}	
	
	private void setSendTime() {
		delayMinutes = timeBar.getProgress();
		int hours = delayMinutes/60;
		int minutes = delayMinutes % 60;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, delayMinutes);
		sendTime = calendar.getTime();	
		sendOn.setText("Send at " + df_time.format(sendTime));
		
		calendar.set(0, 0, 0, hours, minutes);
		delay.setText("Delay " + df2.format(calendar.getTime()));
		
	}
}