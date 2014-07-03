package com.Pull.pullapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.app.NavUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.ShareSuggestion;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.Pull.pullapp.util.DelayedSend;
import com.Pull.pullapp.util.RecipientList.Recipient;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;
import com.Pull.pullapp.util.ShareTagAction;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.merge.MergeAdapter;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;

public class MessageActivityCheckboxCursor extends SherlockListActivity {
	
	protected static final int CONTEXTMENU_CONTACTITEM = 1;	
	protected static final int CONTEXTMENU_SHARE_SECTION = 2;	
	protected static final int CONTEXTMENU_SHARE_PERSISTENT = 3;
	private ArrayList<SMSMessage> messages;
	private HashMap<Long,Integer> delayedMessages = new HashMap<Long,Integer>();
	private MessageCursorAdapter messages_adapter;
	private EditText text;
	private String name, number, newMessage;
	private Context mContext;
	private final Calendar calendar = Calendar.getInstance();
	private Button pickDelay, send, share;
	private GetMessages loader;
	private boolean checkBoxesAreShowing, isPopulated;
	private CustomDateTimePicker customDateTimePicker;
	private Date sendDate;
	private ViewSwitcher viewSwitcher;
	private RecipientsEditor mRecipientEditor, mConfidantesEditor;
	private RecipientsAdapter mRecipientsAdapter;
	private String[] recipients;
	private ListView mListView, pListView;
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
	protected String convoID;
	private int shareType, scrollState;
	protected int itemInView;
	protected boolean startedScrolling;
	private Cursor messages_cursor;
	private QueuedMessageAdapter queue_adapter;
	private MergeAdapter merge_adapter;
	private String share_with;
	private String share_with_name;
	protected ShareSuggestion suggestion;
	private String[] confidantes;
	private CommentListAdapter comments_adapter;
	private ArrayList<Comment> comments;
	private MixpanelAPI mixpanel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_thread);
		
		mContext = getApplicationContext();
		tmgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		mPrefs = mContext.getSharedPreferences(Constants.PREFERENCE_TIME_DELAY_PROMPT, Context.MODE_PRIVATE);
		
		mListView = getListView();
		mListView.setFocusable(true);
		mListView.setFocusableInTouchMode(true);	
		mListView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		
		mLayout = (RelativeLayout) findViewById(R.id.main_layout);

		checkBoxesAreShowing = false;
		delayPressed = false;
		loader = new GetMessages();
		
		mRecipientsAdapter = new RecipientsAdapter(this);
		mConfidantesEditor = (RecipientsEditor)findViewById(R.id.confidantes_editor);
		mConfidantesEditor.setAdapter(mRecipientsAdapter);
			
		messages = new ArrayList<SMSMessage>();
		queue_adapter = new QueuedMessageAdapter(this,messages);
		comments = new ArrayList<Comment>();
		comments_adapter = new CommentListAdapter(mContext, comments, number, number);
		merge_adapter = new MergeAdapter();		

		mixpanel = MixpanelAPI.getInstance(mContext, Constants.MIXEDPANEL_TOKEN);
		mixpanel.identify(ParseInstallation.getCurrentInstallation().getObjectId());
		
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
				popup.setMessage("You haven't said anything yet");
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
	        	else if((isIdealLength || n_characters>0) && n_characters<Constants.MIN_TEXT_LENGTH) {
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
	    
		
		send = (Button) this.findViewById(R.id.send_button);
		share = (Button) this.findViewById(R.id.share_button);
		pickDelay = (Button) this.findViewById(R.id.time_delay_button);
		viewSwitcher = (ViewSwitcher) this.findViewById(R.id.viewSwitcher);
/**		
		hashtag = (MultiAutoCompleteTextView) this.findViewById(R.id.hashtags);
		//hashtag.setText("#");
		ArrayAdapter<String> aaEmo = new ArrayAdapter<String>(
				this,android.R.layout.simple_spinner_dropdown_item,Constants.ALL_HASHTAGS);
		hashtag.setAdapter(aaEmo);
		hashtag.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer() );	
		**/
		sendDate = null;
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mBroadcastReceiver = 
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				
				if(action.equals(Constants.ACTION_SHARE_STATE_CHANGED)) {
					
					if(messages_adapter.check_hash.size()==0) {
						viewSwitcher.setDisplayedChild(0);
					}
					else {
						viewSwitcher.setDisplayedChild(1);
					}		
					return;
				}
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
							//addNewMessage(message, false);
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
		mLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if ((mLayout.getRootView().getHeight() - mLayout.getHeight()) > mLayout.getRootView().getHeight() / 3) {
					//mListView.setSelection(merge_adapter.getCount()-1);
				} 
			}
		});				
		
		share_with = getIntent().getStringExtra(Constants.EXTRA_SHARE_TO_NUMBER);
		if(share_with != null) {
			share_with_name = ContentUtils.getContactDisplayNameByNumber(mContext, share_with);
			String shID = getIntent().getStringExtra(Constants.EXTRA_SHARE_SUGGESTION_ID);			
			//messages_adapter.showCheckboxes = true;
			//checkBoxesAreShowing = true;
			viewSwitcher.setDisplayedChild(1);	
			mConfidantesEditor.setText(share_with);
			confidantes = mConfidantesEditor.constructContactsFromInput(false).getNumbers();
			mConfidantesEditor.setText(Recipient.buildNameAndNumber(share_with_name, share_with));
			//for(int i = messages_adapter.getCount()-1; i>messages_adapter.getCount()-4; i--);			
			messages_adapter.notifyDataSetChanged();
			merge_adapter.notifyDataSetChanged();				
			if(shID != null) {			
				ParseQuery<ShareSuggestion> query = ParseQuery.getQuery("ShareSuggestion");
				query.whereEqualTo("objectId", shID);
		    	query.findInBackground(new FindCallback<ShareSuggestion>() {
		    	    public void done(List<ShareSuggestion> list, ParseException e) {
		    	        if (e == null && list.size()>0) {
		    	        	suggestion = list.get(0);
		    	        	suggestion.setClicked(System.currentTimeMillis());
		    	        	suggestion.saveInBackground();
		    	        } else {
		    	        	return;
		    	        }
		    	    }
		    	});    	
			}

		}		


	}
	

	@Override
	 public void onResume() {
		super.onResume();	
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_SMS_OUTBOXED);
		intentFilter.addAction(Constants.ACTION_SMS_UNOUTBOXED);
		intentFilter.addAction(Constants.ACTION_SMS_DELIVERED);		
		intentFilter.addAction(Constants.ACTION_SHARE_COMPLETE);	
		intentFilter.addAction(Constants.ACTION_SHARE_STATE_CHANGED);	
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
		messages_cursor = ContentUtils.getMessagesCursor(mContext,thread_id, number);
		messages_adapter = new MessageCursorAdapter(mContext, messages_cursor, number);
		merge_adapter.addAdapter(messages_adapter);
		merge_adapter.addAdapter(queue_adapter);
		merge_adapter.addAdapter(comments_adapter);
		setListAdapter(merge_adapter);	
		mListView.setSelection(merge_adapter.getCount()-1);				
	}

	private void updateTime(){
		//Log.i("time", "Updated Time");
		updateDelayButton();
		merge_adapter.notifyDataSetChanged();
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
		/*menu.add(0, CONTEXTMENU_CONTACTITEM, 0, "Add to Contacts");
		//menu.add(1, CONTEXTMENU_SHARE_PERSISTENT, 0, "Share Conversation");
		menu.add(2, CONTEXTMENU_SHARE_SECTION, 0, "Share Specific Messages");*/
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
		case R.id.menu_contacts:
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
            startActivity(intent);            	
            return true;	               
		case CONTEXTMENU_SHARE_PERSISTENT:
			viewSwitcher.setDisplayedChild(1);
            return true;			         
	/*	case R.id.menu_share:
			if(checkBoxesAreShowing) {
				messages_adapter.showCheckboxes = false;
				item.setTitle("SHARE");
				checkBoxesAreShowing = false;
				viewSwitcher.setDisplayedChild(0);
			}
			else {
				messages_adapter.showCheckboxes = true;
				item.setTitle("TEXT");
				checkBoxesAreShowing = true;
				viewSwitcher.setDisplayedChild(1);
			}
			return true;	*/	
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
        	if(sendDate!=null && calendar.getTime().before(sendDate)) {
        		pickDelay.setText(R.string.compose_select_time);
        		sendDate = null; 
        		//TODO: MAKE IT SO THAT YOU CANT DO THE PAST
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
		}
		else  {
			messages.add(0, m);
		}
		queue_adapter.notifyDataSetChanged();
		merge_adapter.notifyDataSetChanged();
		mListView.setSelection(merge_adapter.getCount()-1);
	}
	
	public void removeEditOption(int id) {
		//messages_adapter.getItem(id).isDelayed = false;
		merge_adapter.notifyDataSetChanged();	
	}
	
	public void removeMessage(int id) {
		messages.remove(id);
		merge_adapter.notifyDataSetChanged();	
	}	

    public void getShareContent() {
		final long date = System.currentTimeMillis();
		
		if(mConfidantesEditor.constructContactsFromInput(false).getNumbers().length==0 
				&& (confidantes==null || confidantes.length==0)) {
			Toast.makeText(mContext, "No valid recipients selected", Toast.LENGTH_LONG).show();
			return;
		}
		if(mConfidantesEditor.constructContactsFromInput(false).getNumbers().length>0) 
			confidantes = mConfidantesEditor.constructContactsFromInput(false).getToNumbers();
		//hashtags_string = hashtag.getText().toString().trim(); 
		
		if(confidantes==null || confidantes.length == 0) {
			Toast.makeText(mContext, "No recipients selected", Toast.LENGTH_LONG).show();
			return;
		}
		
		/*if(messages_adapter.check_hash.size()==0) {
			addPersistentSharers();
			return;
		}*/
		
		if(messages_adapter.check_hash.size()==0) {
			Toast.makeText(mContext, "No messages selected", Toast.LENGTH_LONG).show();
			return;
		}		
		
        share.setClickable(false);
        progress = new ProgressDialog(this);
        progress.setTitle("Sharing conversation");
        progress.setMessage("Sending...");
        progress.show();    
        
        mApp = (MainApplication) getApplication(); 
        
		ParseQuery<SharedConversation> query = ParseQuery.getQuery("SharedConversation");
		query.whereEqualTo("original_recipient", ContentUtils.addCountryCode(number));
		query.whereEqualTo("sharer", ContentUtils.addCountryCode(mApp.getUserName()));
		query.whereEqualTo("confidante", ContentUtils.addCountryCode(confidantes[0]));
		query.findInBackground(new FindCallback<SharedConversation>() {
		    public void done(List<SharedConversation> convoList, ParseException e) {
		        if (e==null && convoList.size()>0) {
		        	
		        	shareType = Constants.NOTIFICATION_UPDATE_SHARE;
		        	mSharedConversation = convoList.get(0);
		        	Log.d("isContinuation", "Of convo " + mSharedConversation.getObjectId());
		        	shareConvo(messages_adapter.check_hash);
		            
		        } else {
		        	shareType = Constants.NOTIFICATION_NEW_SHARE;
		        	mSharedConversation = new SharedConversation(date, confidantes[0], number);
		        	mSharedConversation.setSharer(mApp.getUserName());
		        	mSharedConversation.setOriginalRecipientName(name);		
		        	shareConvo(messages_adapter.check_hash);
		            Log.d("isContinuation", "Not a continuation");
		        }
		    }
		});	
		
    }     

	private void addPersistentSharers() {
    	SharedPreferences phoneNumber_SharedWith = mContext
    			.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "phoneNumber_SharedWith" + number ,Context.MODE_PRIVATE); 	
    	long currentTime = System.currentTimeMillis();
    	long sharedSince = phoneNumber_SharedWith.getLong(confidantes[0], currentTime);
    	if(sharedSince != currentTime && false) {
    		Toast.makeText(mContext, "Already sharing with this person", Toast.LENGTH_LONG).show();
    		return;
    	}
		Editor editor = phoneNumber_SharedWith.edit();
		editor.putLong(confidantes[0], currentTime);
		editor.commit();	
		showSharingMessage(confidantes[0], currentTime);
	}


	private void showSharingMessage(String sharedWith, long date) {
		SMSMessage c = new SMSMessage();
		c.setSender(sharedWith);
		c.setDate(date);
		c.setEvent(true);
		c.setMessage("Started sharing with " + ContentUtils
				.getContactDisplayNameByNumber(mContext, sharedWith));
		addNewMessage(c, false);
	}


	protected void shareConvo(TreeSet<SMSMessage> check_hash) {
		mSharedConversation.setMessages(check_hash);
        new ShareTagAction(mContext, mSharedConversation, shareType).start();					

        if(suggestion != null) {
        	suggestion.setSharedConvo(mSharedConversation);  
        	suggestion.saveInBackground();
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
		    	mixpanel.track("user checked box button", null);
		    	
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
		               public void onClick(DialogInterface dialog, int id)
		               {
		            	
		            		mixpanel.track("alertdialog for text delay, user clicked yes", null);
		            		
		            		
		            	   dialog.cancel();
		            	   customDateTimePicker.showDialog();	
		            	 
		               	}
		           }) 
		           .setNegativeButton("No", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) 
		               {
		            	   mixpanel.track("alertdialog for a text delay, user clicked no", null);
		            	   
		                    dialog.cancel();
		                    sendMessage(send);
		               }
		           }).show();		

	}
	private class GetMessages extends AsyncTask<Void,SMSMessage,Void> {
		DatabaseHandler dh;
	  	Cursor messages_cursor;
	  	SMSMessage m;
	  	@Override
		protected Void doInBackground(Void... params) {
				
	        dh = new DatabaseHandler(mContext);
	        messages_cursor = dh.getPendingMessagesCursor(number);    
	        Log.i("GetMessages  for number",number);
	        if(messages_cursor.moveToFirst()) {
	        	m = getNextOutboxMessage(messages_cursor);
	        	publishProgress(m);	
	        	while(messages_cursor.moveToNext()) {
	        		m = getNextOutboxMessage(messages_cursor);
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
	    }			

  }		
	private void hideKeyboard(){
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(text.getWindowToken(), 0);		
		
	}	
	@Override
	protected void onDestroy() {
		mixpanel.flush();
	    super.onDestroy();
	}	  		
}
