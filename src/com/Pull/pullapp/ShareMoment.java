package com.Pull.pullapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
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

import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DelayedSend;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;
import com.Pull.pullapp.util.sendSMS;
import com.Pull.pullapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.Pull.pullapp.util.DatabaseHandler;

@Deprecated
public class ShareMoment extends Activity  {

	private long mID;
	private String[] recipients;
	private BroadcastReceiver mBroadcastReceiver;
	private RecipientsEditor mRecipientEditor;
	private EditText         mContents;
	private Button           mSend;
	private Context mContext;
	private TextView sendOn, mSharingTexts;
	private Calendar calendar;
	private String person_shared;
	private ArrayList<String> messages;
	private MultiAutoCompleteTextView hashtag;
	private String hastags_string;
	private String[] hashtags = {"#Anger","#Annoyance","#Contempt","#Disgust","#Irritation",
			"#Anxiety","#Embarrassment","#Fear","#Helplessness","#Powerlessness",
			"#Worry","#Doubt","#Envy","#Frustration","#Guilt","#Shame","#Boredom",
			"#Despair","#Disappointment","#Hurt","#Sadness","#Stress","#Shock","#Tension",
			"#Amusement","#Delight","#Elation","#Excitement","#Happiness","#Joy","#Pleasure",
			"#Affection","#Empathy","#Friendliness","#Love","#Courage","#Hope","#Pride",
			"#Satisfaction","#Trust","#Calm","#Content","#Relaxed","#Relieved","#Serene",
			"#Interest","#Politeness","#Surprised","#WTF","#B****Please"};
	private String number_shared;	
	private static final String app_link = "Download the app at Google play: https://play.google.com/store/apps/details?id=com.pull.pullapp";
	
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
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_SMS_DELIVERED);
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
					case Activity.RESULT_OK: {
						if(recipients.length == 1) {
							//add shared conversation to db
							SimpleDateFormat dateFormat = new SimpleDateFormat(
									"yyyy-MM-dd'T'HH:mm'Z'"); // ISO 8601, Local time zone.
							dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
							String date = dateFormat.format(new Date()); // Current time in UTC.
							DatabaseHandler db = new DatabaseHandler(mContext);
							db.addSharedMessage(new SharedConversation(date, recipients[0], number_shared, hastags_string)); // 0 is for no limit.	
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
		};			
		registerReceiver(mBroadcastReceiver, intentFilter);		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mBroadcastReceiver);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		person_shared =  getIntent().getStringExtra(Constants.EXTRA_NAME); 
		number_shared =  getIntent().getStringExtra(Constants.EXTRA_NUMBER); 
		messages = getIntent().getStringArrayListExtra(Constants.EXTRA_SET_OF_MESSAGES);
		String lineSep = System.getProperty("line.separator");
		String preview = "Preview of your shared conversation:";
		for(String message: messages) {
			preview = preview + lineSep + message;
		}
		mSharingTexts.setText(preview);
		//Toast.makeText(mContext, messages.toString(), Toast.LENGTH_LONG).show();
	}	
	
	
	public void sendMessageLater(View v) throws InterruptedException
	{
		hastags_string = hashtag.getText().toString().trim(); 
		recipients = mRecipientEditor.constructContactsFromInput(false).getToNumbers();
		String app_plug = "Hey, check out my conversation with " + person_shared + ". " + hastags_string + ":";
		if(hastags_string.length() > 0 && recipients.length>0 && messages.size()>0)
		{
			sendSMS.sendsms(mContext, recipients[0], app_plug, 0, true);
			for(String message: messages) {
				//Thread.sleep(1000);
				sendSMS.sendsms(mContext, recipients[0], message, 0, true);
			}
			//Thread.sleep(1000);
			sendSMS.sendsms(mContext, recipients[0], app_link, 0, true);
   			
		}
					
	}	


}