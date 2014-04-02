package com.Pull.smsTest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ShareMoment extends Activity  {

	private long mID;
	private String[] recipients;
	
	private RecipientsEditor mRecipientEditor;
	private EditText         mContents;
	private Button           mSend;
	private Context mContext;
	private TextView sendOn, mSharingTexts;
	private Calendar calendar;
	private String person_shared;
	private ArrayList<String> messages;
	private MultiAutoCompleteTextView hashtag;
	private String[] hashtags = {"#Anger","#Annoyance","#Contempt","#Disgust","#Irritation",
			"#Anxiety","#Embarrassment","#Fear","#Helplessness","#Powerlessness",
			"#Worry","#Doubt","#Envy","#Frustration","#Guilt","#Shame","#Boredom",
			"#Despair","#Disappointment","#Hurt","#Sadness","#Stress","#Shock","#Tension",
			"#Amusement","#Delight","#Elation","#Excitement","#Happiness","#Joy","#Pleasure",
			"#Affection","#Empathy","#Friendliness","#Love","#Courage","#Hope","#Pride",
			"#Satisfaction","#Trust","#Calm","#Content","#Relaxed","#Relieved","#Serene",
			"#Interest","#Politeness","#Surprised","#WTF","#B****Please"};	
	private static final String app_link = "Download the app at Google play: https://play.google.com/store/apps/details?id=com.pull.smsTest";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_moment);
		
		mContext = getApplicationContext();
		calendar = Calendar.getInstance();
		mRecipientEditor = (RecipientsEditor)findViewById(R.id.recipients_editor);

		mSend            = (Button)findViewById(R.id.sendlater);
		mSharingTexts = (TextView)findViewById(R.id.sharing_texts);
		mSharingTexts.setMovementMethod(new ScrollingMovementMethod());
		hashtag = (MultiAutoCompleteTextView) this.findViewById(R.id.hashtags);
		hashtag.setText("#");
		mRecipientEditor.setAdapter(new RecipientsAdapter(this));
		ArrayAdapter<String> aaEmo = new ArrayAdapter<String>(
				this,android.R.layout.simple_spinner_dropdown_item,hashtags);
		hashtag.setAdapter(aaEmo);
		hashtag.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer() );		
		String DELIVERED = "SMS_DELIVERED";
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
					case Activity.RESULT_OK: {
						//Toast.makeText(getApplicationContext(), "SMS delivered", Toast.LENGTH_SHORT).show();	
						if(recipients.length == 1) {

							
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
		person_shared =  getIntent().getStringExtra(Constants.EXTRA_NAME); 
		messages = getIntent().getStringArrayListExtra(Constants.EXTRA_SET_OF_MESSAGES);
		String lineSep = System.getProperty("line.separator");
		String preview = "Preview of your shared conversation:";
		for(String message: messages) {
			preview = preview + lineSep + message;
		}
		mSharingTexts.setText(preview);
		//Toast.makeText(mContext, messages.toString(), Toast.LENGTH_LONG).show();
	}	

	public void sendMessage(View v)
	{
		String newMessage = mContents.getText().toString().trim(); 
		recipients = mRecipientEditor.constructContactsFromInput(false).getToNumbers();

		if(newMessage.length() > 0 && recipients.length>0 && messages.size()>0)
		{
			for(String message: messages) {
				sendSMS.sendsms(mContext, recipients[0], message, true);
			}
   			
		}
	}
	
	public void sendMessageLater(View v) throws InterruptedException
	{
		String newMessage = hashtag.getText().toString().trim(); 
		recipients = mRecipientEditor.constructContactsFromInput(false).getToNumbers();
		String app_plug = "Hey, check out my conversation with " + person_shared + ". " + newMessage + ":";
		if(newMessage.length() > 0 && recipients.length>0 && messages.size()>0)
		{
			sendSMS.sendsms(mContext, recipients[0], app_plug, true);
			for(String message: messages) {
				//Thread.sleep(1000);
				sendSMS.sendsms(mContext, recipients[0], message, true);
			}
			//Thread.sleep(1000);
			sendSMS.sendsms(mContext, recipients[0], app_link, true);
   			
		}
        Intent intent = new Intent(getApplicationContext(), ThreadsListActivity.class);
        startActivity(intent);				
	}	


}