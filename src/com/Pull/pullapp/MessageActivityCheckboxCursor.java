package com.Pull.pullapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONException;

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
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.Pull.pullapp.adapter.CommentListAdapter;
import com.Pull.pullapp.adapter.MessageCursorAdapter;
import com.Pull.pullapp.adapter.QueuedMessageAdapter;
import com.Pull.pullapp.fragment.CustomDateTimePicker;
import com.Pull.pullapp.fragment.SimplePopupWindow;
import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.ShareSuggestion;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.threads.DelayedSend;
import com.Pull.pullapp.threads.ShareMessages;
import com.Pull.pullapp.threads.ShareTagAction;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.Pull.pullapp.util.RecipientList.Recipient;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;
import com.Pull.pullapp.util.UserInfoStore;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bea.xml.stream.samples.Parse;
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
	private MessageCursorAdapter messages_adapter;
	private EditText text;
	private String name, number, newMessage;
	private Context mContext;
	private final Calendar calendar = Calendar.getInstance();
	private Button pickDelay, send, share;
	private GetMessages loader;
	private boolean isPopulated;
	private CustomDateTimePicker customDateTimePicker;
	private Date sendDate;
	private ViewSwitcher viewSwitcher;
	private RecipientsEditor mRecipientEditor, mConfidantesEditor;
	private RecipientsAdapter mRecipientsAdapter;
	private String[] recipients;
	private ListView mListView;
	private RelativeLayout mLayout;
	private BroadcastReceiver mBroadcastReceiver;
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
	protected String convoID;
	private int shareType;
	protected int itemInView;
	protected boolean startedScrolling;
	private QueuedMessageAdapter queue_adapter;
	private MergeAdapter merge_adapter;
	private String share_with;
	private String share_with_name;
	protected ShareSuggestion suggestion;
	private String[] confidantes;
	private CommentListAdapter comments_adapter;
	private ArrayList<Comment> comments;
	private MixpanelAPI mixpanel;
	private String shared_sender;
	private String shared_convoID;
	private DatabaseHandler dh;
	private UserInfoStore store;
	private String shared_confidante;
	private String shared_address;
	private String confidante_name;
	private String shared_conversant;
	private int shared_convo_type;
	private Cursor messages_cursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_thread);
		
		mContext = getApplicationContext();
		mPrefs = mContext.getSharedPreferences(Constants.PREFERENCE_TIME_DELAY_PROMPT, Context.MODE_PRIVATE);
		
		store = new UserInfoStore(mContext);
		
		mListView = getListView();
		mListView.setFocusable(true);
		mListView.setFocusableInTouchMode(true);	
		mListView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		
		mLayout = (RelativeLayout) findViewById(R.id.main_layout);
		mLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if ((mLayout.getRootView().getHeight() - mLayout.getHeight()) > mLayout.getRootView().getHeight() / 3) {
					//mListView.setSelection(merge_adapter.getCount()-1);
				} 
			}
		});		
		
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
		
		send = (Button) this.findViewById(R.id.send_button);
		share = (Button) this.findViewById(R.id.share_button);
		pickDelay = (Button) this.findViewById(R.id.time_delay_button);
		viewSwitcher = (ViewSwitcher) this.findViewById(R.id.viewSwitcher);
		text = (EditText) this.findViewById(R.id.text);
		mTextIndicatorButton = (ImageButton) findViewById(R.id.textIndicatorButton);
		
		if(getIntent() != null && !isPopulated) {
			
			number =  getIntent().getStringExtra(Constants.EXTRA_NUMBER); 
			name =  getIntent().getStringExtra(Constants.EXTRA_NAME); 
			thread_id = getIntent().getStringExtra(Constants.EXTRA_THREAD_ID); 
			
			//right now only for received ones
			shared_confidante = getIntent().getStringExtra(Constants.EXTRA_SHARED_CONFIDANTE); 
			shared_sender = getIntent().getStringExtra(Constants.EXTRA_SHARED_SENDER); 
			shared_convoID = getIntent().getStringExtra(Constants.EXTRA_SHARED_CONVERSATION_ID); 
			person_shared = getIntent().getStringExtra(Constants.EXTRA_SHARED_NAME); 
			shared_address = getIntent().getStringExtra(Constants.EXTRA_SHARED_ADDRESS);
			shared_convo_type = getIntent().getIntExtra(Constants.EXTRA_SHARED_CONVO_TYPE,-1);
			
			if(number!=null && name!=null) {
				populateMessages();
				
				if(Constants.DEBUG==false) this.setTitle(name);
				setupComposeBox();
			} else if(person_shared!=null && shared_sender!=null){
				populateSharedMessages(shared_convoID);
				setTitle(person_shared + "|" + store.getName(shared_sender));
			}
			else {
				
				getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
		                | ActionBar.DISPLAY_SHOW_HOME);
            	getSupportActionBar().setDisplayHomeAsUpEnabled(true);				
				getSupportActionBar().setCustomView(R.layout.recipients_editor);
				
				mRecipientEditor = (RecipientsEditor) getSupportActionBar().getCustomView().findViewById(R.id.recipients_editor);
				mRecipientEditor.setAdapter(mRecipientsAdapter);
				setupComposeBox();
			}
			
		}		

	    hideKeyboard();
	    text.clearFocus();
		
		sendDate = null;
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mBroadcastReceiver = 
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				
				if(action.equals(Constants.ACTION_DATABASE_UPDATE) && shared_convoID!=null){
					if(!intent.getStringExtra(Constants.EXTRA_SHARED_CONVERSATION_ID).equals(shared_convoID)) 
						return;
					dh = new DatabaseHandler(mContext);
					messages_cursor = dh.getSharedMessagesCursor(shared_convoID);
					messages_adapter.swapCursor(messages_cursor);
					messages_adapter.notifyDataSetChanged();
					mListView.setSelection(mListView.getCount()-1);
					return;
				}
				if(action.equals(Constants.ACTION_SHARE_STATE_CHANGED)) {
					hideKeyboard();
					if(messages_adapter.check_hash.size()==0) {
						confidantes = null;
						mConfidantesEditor.setText("");						
						viewSwitcher.setDisplayedChild(0);
						mListView.setSelection(merge_adapter.getCount()-1);	
						hideKeyboard();
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
								
							if(queue_adapter.delayedMessages.containsKey(scheduledOn) && scheduledOn>0) {
								int id = queue_adapter.delayedMessages.get(scheduledOn);
								removeMessage(messages.size() - id - 1);
								queue_adapter.delayedMessages.remove(scheduledOn);
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
					queue_adapter.delayedMessages.put(scheduledOn, messages.size());
					SMSMessage m = new SMSMessage(scheduledFor, intent_message, intent_number, TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX);
					m.schedule(scheduledFor);
					m.launchedOn = scheduledOn;
					addNewMessage(m, false);

				} else if(action.equals(Constants.ACTION_SMS_UNOUTBOXED)) {
					
					int id = queue_adapter.delayedMessages.get(scheduledOn);
					removeMessage(messages.size() - id - 1);
					queue_adapter.delayedMessages.remove(id);
					queue_adapter.notifyDataSetChanged();
					merge_adapter.notifyDataSetChanged();
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

		tickReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateTime();
			}
		};
		hideKeyboard();
		text.clearFocus();	
		
		
		share_with = getIntent().getStringExtra(Constants.EXTRA_SHARE_TO_NUMBER);
		if(share_with != null) {
			viewSwitcher.setDisplayedChild(1);	
			if(!share_with.isEmpty()) {
				share_with_name = ContentUtils.getContactDisplayNameByNumber(mContext, share_with);
				String shID = getIntent().getStringExtra(Constants.EXTRA_SHARE_SUGGESTION_ID);			
				//messages_adapter.showCheckboxes = true;
				//checkBoxesAreShowing = true;			
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


	}
	



	private void setupComposeBox() {
		
		mTextIndicatorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup = new SimplePopupWindow(v);
				popup.showLikePopDownMenu();
				popup.setMessage("You haven't said anything yet");
			}
		});
		
		isIdealLength = false;
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
		intentFilter.addAction(Constants.ACTION_DATABASE_UPDATE);
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
		messages_adapter = new MessageCursorAdapter(mContext, messages_cursor, number, this, true);
		merge_adapter.addAdapter(messages_adapter);
		merge_adapter.addAdapter(queue_adapter);
		merge_adapter.addAdapter(comments_adapter);
		setListAdapter(merge_adapter);	
		mListView.setSelection(merge_adapter.getCount()-1);				
	}
	private void populateSharedMessages(final String shared_convoID) {
		dh = new DatabaseHandler(mContext);
		messages_cursor = dh.getSharedMessagesCursor(shared_convoID);
		messages_adapter = new MessageCursorAdapter(mContext, messages_cursor, this, false);
		setListAdapter(messages_adapter);	
		mListView.setSelection(messages_adapter.getCount()-1);		
		viewSwitcher.setDisplayedChild(0);
		if(shared_convo_type==TextBasedSmsColumns.MESSAGE_TYPE_INBOX) 
			shared_conversant = shared_sender;
		else 
			shared_conversant = shared_confidante;
		text.setHint("Write a comment to " + store.getName(shared_conversant));
		mTextIndicatorButton.setBackground(getResources().getDrawable(R.drawable.comment_indicator));
		mTextIndicatorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup = new SimplePopupWindow(v);
				popup.showLikePopDownMenu();
				popup.setMessage("Your comments are only visible to " + store.getName(shared_conversant));
			}
		});		
		pickDelay.setVisibility(View.GONE);
		send.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				try {
					sendComment(shared_conversant, shared_address, text.getText().toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
			}
			
		});
	}

	protected void sendComment(String shared_sender, String shared_address,
			String commentText) throws JSONException {
/*		Toast.makeText(mContext, commentText + " to " + shared_sender + " for " + shared_address, 
				Toast.LENGTH_LONG).show();
*/
		SMSMessage comment = new SMSMessage(System.currentTimeMillis(), commentText, 
				shared_address, Constants.MESSAGE_TYPE_SENT_COMMENT, store);
		//assume this is a conversation that was shared with us
		comment.addConfidante(shared_sender);
		dh = new DatabaseHandler(mContext);
		
		dh.addSharedMessage(shared_convoID, comment, TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
		dh = new DatabaseHandler(mContext);
		messages_cursor = dh.getSharedMessagesCursor(shared_convoID);
		//dh.close();
		messages_adapter.swapCursor(messages_cursor);
		messages_adapter.notifyDataSetChanged();
		text.setText("");
		hideKeyboard();
		mListView.setSelection(mListView.getCount()-1);
		comment.saveToParse();
		
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
		default:
			return false;
		}
	}	

	private SMSMessage getNextOutboxMessage(Cursor c) {
		String body="";
		body = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.BODY)).toString();
		long scheduledFor = Long.parseLong(c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.DATE)).toString());
		long scheduledOn = Long.parseLong(c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.DATE_SENT)).toString());
		queue_adapter.delayedMessages.put(scheduledOn, messages.size());
		SMSMessage m = new SMSMessage(scheduledOn, body, number, TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX);
		m.isDelayed = true;
		m.schedule(scheduledFor);
		m.launchedOn = scheduledOn;
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
	
	public void removeMessage(int id) {
		messages.remove(id);
		Log.i("tag","removemessage removing message " + id);
		queue_adapter.notifyDataSetChanged();
		merge_adapter.notifyDataSetChanged();	
	}	

    public void getShareContent(View v) {
		final long date = System.currentTimeMillis();
		
		if(mConfidantesEditor.constructContactsFromInput(false).getNumbers().length==0 
				&& (confidantes==null || confidantes.length==0)) {
			//Toast.makeText(mContext, "No valid recipients selected", Toast.LENGTH_LONG).show();
			popup = new SimplePopupWindow(v);
			popup.showLikePopDownMenu();
			popup.setMessage("Try entering a contact from your address book");			
			return;
		}
		if(mConfidantesEditor.constructContactsFromInput(false).getNumbers().length>0) 
			confidantes = mConfidantesEditor.constructContactsFromInput(false).getToNumbers();

		if(confidantes==null || confidantes.length == 0) {
			popup = new SimplePopupWindow(v);
			popup.showLikePopDownMenu();
			popup.setMessage("Try entering a contact from your address book");	
			return;
		}
		
		/*if(messages_adapter.check_hash.size()==0) {
			addPersistentSharers();
			return;
		}*/
		
		if(messages_adapter.check_hash.size()==0) {
			popup = new SimplePopupWindow(v);
			popup.showLikePopDownMenu();
			popup.setMessage("Tap messages to select them for sharing");	
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
		        	//Log.d("isContinuation", "Of convo " + mSharedConversation.getObjectId());
		        	shareConvo(messages_adapter.check_hash);
		            
		        } else {
		        	shareType = Constants.NOTIFICATION_NEW_SHARE;
		        	mSharedConversation = new SharedConversation(date, confidantes[0], number);
		        	mSharedConversation.setSharer(mApp.getUserName());
		        	mSharedConversation.setOriginalRecipientName(name);		
		        	shareConvo(messages_adapter.check_hash);
		          //  Log.d("isContinuation", "Not a continuation");
		        }
		    }
		});	
		
    }     

	/**private void addPersistentSharers() {
    	SharedPreferences phoneNumber_SharedWith = mContext
    			.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "phoneNumber_SharedWith" + number ,Context.MODE_PRIVATE); 	
    	long currentTime = System.currentTimeMillis();
    	long sharedSince = phoneNumber_SharedWith.getLong(confidantes[0], currentTime);
    	if(sharedSince != currentTime) {
    		Toast.makeText(mContext, "Already sharing with this person", Toast.LENGTH_LONG).show();
    		return;
    	}
		Editor editor = phoneNumber_SharedWith.edit();
		editor.putLong(confidantes[0], currentTime);
		editor.commit();	
		showSharingMessage(confidantes[0], currentTime);
	}


	private void showSharingMessage(String sharedWith, long date) {
		SMSMessage c = new SMSMessage(date, "Started sharing with " + ContentUtils
				.getContactDisplayNameByNumber(mContext, sharedWith), sharedWith, TextBasedSmsColumns.MESSAGE_TYPE_SENT);
		c.setEvent(true);
		addNewMessage(c, false);
	}
**/

	protected void shareConvo(TreeSet<SMSMessage> check_hash) {
		mSharedConversation.setMessages(check_hash);
        new ShareTagAction(mContext, mSharedConversation, shareType).start();					

        if(suggestion != null) {
        	suggestion.setSharedConvo(mSharedConversation);  
        	suggestion.saveInBackground();
        }
		
	}

	public void shareMessages(View v) throws JSONException 
	{
		final long date = System.currentTimeMillis();
		
		if(mConfidantesEditor.constructContactsFromInput(false).getNumbers().length==0 
				&& (confidantes==null || confidantes.length==0)) {
			popup = new SimplePopupWindow(v);
			popup.showLikePopDownMenu();
			popup.setMessage("Try entering a contact from your address book");	
			return;
		}
		if(mConfidantesEditor.constructContactsFromInput(false).getNumbers().length>0) 
			confidantes = mConfidantesEditor.constructContactsFromInput(false).getToNumbers();
		//hashtags_string = hashtag.getText().toString().trim(); 
		
		if(confidantes==null || confidantes.length == 0) {
			popup = new SimplePopupWindow(v);
			popup.showLikePopDownMenu();
			popup.setMessage("Try entering a contact from your address book");	
			return;
		}
		
		if(messages_adapter.check_hash.size()==0) {
			popup = new SimplePopupWindow(v);
			popup.showLikePopDownMenu();
			popup.setMessage("Tap messages to select them for sharing");
			return;
		}		
		
        share.setClickable(false);		
		mConfidantesEditor.setText("");						
		viewSwitcher.setDisplayedChild(0);
		hideKeyboard();
		mListView.setSelection(mListView.getCount()-1);		        
        TreeSet<SMSMessage> messagesHash = (TreeSet<SMSMessage>) messages_adapter.check_hash.clone();
		for(SMSMessage m : messagesHash) {
			for(String confidante : confidantes) {
				
				m.addConfidante(confidante);
			}
			m.saveToParse();
		}	
		for(String confidante : confidantes) {
	    	confidante_name = ContentUtils.getContactDisplayNameByNumber(mContext, confidante);
	    	store.setName(confidante, confidante_name);
			
			new ShareMessages(mContext, confidante, 
					name, number,  messagesHash).start();	  
		}	
        
		messages_adapter.check_hash.clear();
		messages_adapter.showCheckboxes = false;	
		messages_adapter.notifyDataSetChanged();
		mConfidantesEditor.setText("");
		confidantes = null;	
		share.setClickable(true);		
		
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
	  	Cursor pending_messages_cursor;
	  	SMSMessage m;
	  	@Override
		protected Void doInBackground(Void... params) {
				
	        dh = new DatabaseHandler(mContext);
	        pending_messages_cursor = dh.getPendingMessagesCursor(number);    
	     //   Log.i("pending_messages_cursor","number is " + number);
	       // Log.i("pending_messages_cursor","size is " + pending_messages_cursor.getCount());
	        if(pending_messages_cursor.moveToFirst()) {
	        	m = getNextOutboxMessage(pending_messages_cursor);
	        	publishProgress(m);	
	        	while(pending_messages_cursor.moveToNext()) {
	        		m = getNextOutboxMessage(pending_messages_cursor);
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
			pending_messages_cursor.close();
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
