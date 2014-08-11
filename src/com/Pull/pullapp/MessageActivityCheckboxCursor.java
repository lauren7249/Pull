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
import android.app.FragmentTransaction;
import android.app.NotificationManager;
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
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.Pull.pullapp.adapter.CommentListAdapter;
import com.Pull.pullapp.adapter.MessageCursorAdapter;
import com.Pull.pullapp.adapter.QueuedMessageAdapter;
import com.Pull.pullapp.fragment.CustomDateTimePicker;
import com.Pull.pullapp.fragment.RecipientsPopupWindow;
import com.Pull.pullapp.fragment.RecipientsPopupWindow.ApproverDialogListener;
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
import com.Pull.pullapp.util.LinearLayoutThatDetectsSoftKeyboard;
import com.Pull.pullapp.util.RecipientList.Recipient;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;
import com.Pull.pullapp.util.UserInfoStore;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.merge.MergeAdapter;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.Pull.pullapp.util.*;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.Pull.pullapp.R;
import com.rockerhieu.emojicon.emoji.Emojicon;

public class MessageActivityCheckboxCursor extends SherlockFragmentActivity
	implements ApproverDialogListener, View.OnClickListener, EmojiconGridFragment.OnEmojiconClickedListener,
	EmojiconsFragment.OnEmojiconBackspaceClickedListener, 
	EmojiconsFragment.OnEmojiconTabClickedListener, LinearLayoutThatDetectsSoftKeyboard.Listener {
	
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
	private GetOutboxMessages loader;
	private boolean isPopulated;
	private CustomDateTimePicker customDateTimePicker;
	private Date sendDate;
	private ViewSwitcher viewSwitcher;
	private RecipientsEditor mRecipientEditor, mConfidantesEditor;
	private RecipientsAdapter mRecipientsAdapter;
	private String[] recipients;
	private ListView mListView;
	private LinearLayoutThatDetectsSoftKeyboard mLayout;
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
	private Button pickApprover;
	private String approver;
	private TextView title_view;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private LinearLayout mDrawerLinearLayout;
	private String indicatorText;
	private LinearLayout mButtonsBar;
	private TextWatcher watcher;
	private ShowcaseView showcaseView;
	private int counter;
	private InputMethodManager imm;
	private ViewPager emojisPager;
	private FragmentManager fm;
	private Fragment emojiFragment;
	private boolean inputtingEmoji;
	private boolean keyboardShowing;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_thread);
		
		showcaseView = new ShowcaseView.Builder(this)
        .setTarget(new ViewTarget(findViewById(R.id.list)))
        .setContentTitle("Tap to share")     
        .setOnClickListener(this)
        .singleShot(279)
        .build();	    
		showcaseView.setButtonText(">");
		
		mContext = getApplicationContext();
		mPrefs = mContext.getSharedPreferences(Constants.PREFERENCE_TIME_DELAY_PROMPT, Context.MODE_PRIVATE);
		
		store = new UserInfoStore(mContext);

		mListView = (ListView) findViewById(R.id.list);
		mListView.setFocusable(true);
		mListView.setFocusableInTouchMode(true);	
		mListView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		mButtonsBar = (LinearLayout) findViewById(R.id.buttons_box);
		mLayout = (LinearLayoutThatDetectsSoftKeyboard) findViewById(R.id.main_layout);
		mLayout.setListener(this);
		
		delayPressed = false;
		loader = new GetOutboxMessages();
		
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
		pickApprover  = (Button) this.findViewById(R.id.approvers_button);
		viewSwitcher = (ViewSwitcher) this.findViewById(R.id.viewSwitcher);
		text = (EditText) this.findViewById(R.id.text);
		mTextIndicatorButton = (ImageButton) findViewById(R.id.textIndicatorButton);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		emojisPager = (ViewPager) findViewById(R.id.emojis_pager);
		
		fm = getSupportFragmentManager();
	    emojiFragment = fm.findFragmentById(R.id.emojicons);
	        
		approver = "";
		if(getIntent() != null && !isPopulated) {
			
			number =  ContentUtils.addCountryCode(getIntent().getStringExtra(Constants.EXTRA_NUMBER)); 
			name =  getIntent().getStringExtra(Constants.EXTRA_NAME); 
			thread_id = getIntent().getStringExtra(Constants.EXTRA_THREAD_ID); 
			
			shared_confidante = getIntent().getStringExtra(Constants.EXTRA_SHARED_CONFIDANTE); 
			shared_sender = getIntent().getStringExtra(Constants.EXTRA_SHARED_SENDER); 
			shared_convoID = getIntent().getStringExtra(Constants.EXTRA_SHARED_CONVERSATION_ID); 
			person_shared = getIntent().getStringExtra(Constants.EXTRA_SHARED_NAME); 
			shared_address = getIntent().getStringExtra(Constants.EXTRA_SHARED_ADDRESS);
			shared_convo_type = getIntent().getIntExtra(Constants.EXTRA_SHARED_CONVO_TYPE,-1);
			Log.i("shared_convo_type",""+shared_convo_type );
			if(number!=null && name!=null) {
				populateMessages();
				
				getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
		                | ActionBar.DISPLAY_SHOW_HOME);
            	getSupportActionBar().setDisplayHomeAsUpEnabled(true);				
				getSupportActionBar().setCustomView(R.layout.message_thread_action_bar);		
				title_view = (TextView) findViewById(R.id.name);
				title_view.setText(name);
				setupComposeBox();
			} else if(person_shared!=null && shared_sender!=null){
				populateSharedMessages(shared_convoID);
				if(shared_convo_type == TextBasedSmsColumns.MESSAGE_TYPE_INBOX)
					setTitle(person_shared + "|" + store.getName(shared_sender));
				else
					setTitle(person_shared + "|Me");
			}
			else {
				
				getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
		                | ActionBar.DISPLAY_SHOW_HOME);
            	getSupportActionBar().setDisplayHomeAsUpEnabled(true);				
				getSupportActionBar().setCustomView(R.layout.recipients_editor);
				setupComposeBox();
				mRecipientEditor = (RecipientsEditor) getSupportActionBar().getCustomView().findViewById(R.id.recipients_editor);
				mRecipientEditor.setAdapter(mRecipientsAdapter);
				
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
				if(action.equals(Constants.ACTION_SMS_INBOXED)) {
					if(intent.getStringExtra(Constants.EXTRA_NUMBER).equals(number)) {
						messages_cursor = ContentUtils.getMessagesCursor(mContext,thread_id, number);
						messages_adapter.swapCursor(messages_cursor);							
						messages_adapter.notifyDataSetChanged();
						merge_adapter.notifyDataSetChanged();
						mListView.setSelection(mListView.getCount()-1);								
					}
					return;
				}				
				if(action.equals(Constants.ACTION_DATABASE_UPDATE) && shared_convoID!=null){
					Log.i("convoid from broadcastreceiver in messageactivity",
							intent.getStringExtra(Constants.EXTRA_SHARED_CONVERSATION_ID));
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
				Long scheduledFor = intent.getLongExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, 0);
				String intent_message = intent.getStringExtra(Constants.EXTRA_MESSAGE_BODY);
				String intent_approver = intent.getStringExtra(Constants.EXTRA_APPROVER);
				if(action.equals(Constants.ACTION_SMS_DELIVERED)) {
					switch (getResultCode()) {
						case Activity.RESULT_OK: {
								
							if(queue_adapter.delayedMessages.containsKey(scheduledOn) && scheduledOn>0) {
								removeMessage();
								SMSMessage m = new SMSMessage(scheduledFor, intent_message, intent_number, 
										name, TextBasedSmsColumns.MESSAGE_TYPE_SENT, store, 
										ParseUser.getCurrentUser().getUsername());		
								m.schedule(scheduledFor);
								m.setType(TextBasedSmsColumns.MESSAGE_TYPE_SENT);
								try {
									m.saveToParse();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							removeMessage();
							messages_cursor = ContentUtils.getMessagesCursor(mContext,thread_id, number);
							messages_adapter.swapCursor(messages_cursor);							
							messages_adapter.notifyDataSetChanged();
							merge_adapter.notifyDataSetChanged();
							break;
						}
						default: {
							text.setText(intent_message);
							Toast.makeText(getApplicationContext(), "SMS not sent", Toast.LENGTH_SHORT).show();
							break;
						}
					}
				} else if(action.equals(Constants.ACTION_SMS_OUTBOXED)) {
					
					queue_adapter.delayedMessages.put(scheduledOn, messages.size());
					Log.i("tag","queue_adapter.delayedMessages.put " + messages.size());
					SMSMessage m = new SMSMessage(scheduledFor, intent_message, intent_number, name,
							TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX, store, ParseUser.getCurrentUser().getUsername());
					m.schedule(scheduledFor);
					m.launchedOn = scheduledOn;
					m.setApprover(intent_approver);
					addNewMessage(m, false);
					try {
						m.saveToParse();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else if(action.equals(Constants.ACTION_SMS_UNOUTBOXED)) {
					if(scheduledFor-scheduledOn <= 6000) 
						sendDate = new Date(Math.max(scheduledFor,scheduledOn) + 6000);
					else 
						sendDate = new Date(scheduledFor);
					
					updateDelayButton();
					
					if(intent_approver!=null&& intent_approver.length()>0){
						approver = intent_approver;
						pickApprover.setText("if " + ContentUtils
								.getContactDisplayNameByNumber(context, intent_approver) + " approves");						
					}

					removeMessage();
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
		indicatorText = "Write something";
		mTextIndicatorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup = new SimplePopupWindow(v);
				popup.showLikeQuickAction();
				popup.setMessage(indicatorText);				
	
			}
		});
		
		isIdealLength = false;
		watcher = getTextWatcher();
	    text.addTextChangedListener(watcher); 	
		mLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if ((mLayout.getRootView().getHeight() - mLayout.getHeight()) > mLayout.getRootView().getHeight() / 3) {
					mButtonsBar.setVisibility(View.VISIBLE);
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
		intentFilter.addAction(Constants.ACTION_SMS_INBOXED);
		registerReceiver(mBroadcastReceiver, intentFilter);	
		
		if(merge_adapter!=null && queue_adapter!=null && messages_adapter!=null) {
			queue_adapter.notifyDataSetChanged();
			messages_adapter.notifyDataSetChanged();
			merge_adapter.notifyDataSetChanged();
		}
		
		if(number!=null) {
			NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(number.hashCode());			
		}
		
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
		mListView.setAdapter(merge_adapter);	
		mListView.setSelection(merge_adapter.getCount()-1);				
	}
	private void populateSharedMessages(final String shared_convoID) {
		Log.i("shared convo type",""+shared_convo_type);
		Log.i("shared_sender",shared_sender);
		Log.i("shared_confidante",shared_confidante);
		if(shared_convo_type==TextBasedSmsColumns.MESSAGE_TYPE_INBOX) 
			shared_conversant = shared_sender;
		else 
			shared_conversant = shared_confidante;		
		dh = new DatabaseHandler(mContext);
		messages_cursor = dh.getSharedMessagesCursor(shared_convoID);
		messages_adapter = new MessageCursorAdapter(mContext, messages_cursor, this, false, 
				shared_conversant, shared_address, person_shared);
		mListView.setAdapter(messages_adapter);	
		mListView.setSelection(messages_adapter.getCount()-1);		
		viewSwitcher.setDisplayedChild(0);
		mButtonsBar.setVisibility(View.GONE);
		pickDelay.setVisibility(View.GONE);
		text.setHint("Comment to " + store.getName(shared_conversant));
		text.removeTextChangedListener(watcher);
		mTextIndicatorButton.setBackground(getResources().getDrawable(R.drawable.comment_indicator));
		mTextIndicatorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup = new SimplePopupWindow(v);
				popup.showLikeQuickAction();
				popup.setMessage("Your comments are only visible to " + store.getName(shared_conversant));
			}
		});		
		//pickDelay.setVisibility(View.GONE);
		send.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				try {
					sendComment(text.getText().toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
			}
			
		});
	}

	protected void sendComment(String commentText) throws JSONException {
/*		Toast.makeText(mContext, commentText + " to " + shared_sender + " for " + shared_address, 
				Toast.LENGTH_LONG).show();
*/
		long currentDate = new Date().getTime();
		SMSMessage comment = new SMSMessage(currentDate, commentText, 
				shared_address, person_shared, Constants.MESSAGE_TYPE_SENT_COMMENT, store, shared_sender);
		//assume this is a conversation that was shared with us
		comment.addConfidante(shared_conversant);
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
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  savedInstanceState.putString("number", number);    
	  savedInstanceState.putString("name", name);    
	  savedInstanceState.putString("thread_id", thread_id);    
	  savedInstanceState.putString("shared_confidante", shared_confidante); 	 
	  savedInstanceState.putString("shared_sender", shared_sender);    
	  savedInstanceState.putString("shared_convoID", shared_convoID);    
	  savedInstanceState.putString("person_shared", person_shared);    
	  savedInstanceState.putString("shared_address", shared_address); 	 	  
	  savedInstanceState.putInt("shared_convo_type", shared_convo_type); 	 	
	  // etc.  
	  super.onSaveInstanceState(savedInstanceState);  
	}  
	//onRestoreInstanceState  
	    @Override  
	public void onRestoreInstanceState(Bundle savedInstanceState) {  
	  super.onRestoreInstanceState(savedInstanceState);  
	  number = savedInstanceState.getString("number");    
	  name = savedInstanceState.getString("name");    
	  thread_id = savedInstanceState.getString("thread_id");    
	  shared_confidante = savedInstanceState.getString("shared_confidante" ); 	 
	  shared_sender = savedInstanceState.getString("shared_sender");    
	  shared_convoID = savedInstanceState.getString("shared_convoID");    
	  person_shared = savedInstanceState.getString("person_shared");    
	  shared_address = savedInstanceState.getString("shared_address"); 	 	  
	  shared_convo_type = savedInstanceState.getInt("shared_convo_type"); 	
	}	
	private void updateDelayButton(){
		if(sendDate!=null){
			CharSequence date = DateUtils.getRelativeDateTimeString(mContext, sendDate.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
			pickDelay.setText(date);	
		}
	}
	
    /* Called whenever we call invalidateOptionsMenu() 
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerLinearLayout);
        menu.findItem(R.id.menu_contacts).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }	**/
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
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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
	
	@Override
	public void onBackPressed() {
	    if(!keyboardShowing && !inputtingEmoji) super.onBackPressed();
	    else {
			inputtingEmoji = false;
	    	hideKeyboard();
	    }
	}
	
	private SMSMessage getNextOutboxMessage(Cursor c) {
		String body="";
		body = c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.BODY)).toString();
		long scheduledFor = Long.parseLong(c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.DATE)).toString());
		long scheduledOn = Long.parseLong(c.getString(c.getColumnIndexOrThrow(TextBasedSmsColumns.DATE_SENT)).toString());
		String approver = c.getString(c.getColumnIndexOrThrow(DatabaseHandler.KEY_APPROVER));
		queue_adapter.delayedMessages.put(scheduledOn, messages.size());
		SMSMessage m = new SMSMessage(scheduledOn, body, number, name,
				TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX, store, ParseUser.getCurrentUser().getUsername());
		m.isDelayed = true;
		m.schedule(scheduledFor);
		m.launchedOn = scheduledOn;
		m.setApprover(approver);
		return m;	
	}	


	public void sendMessage(View v)
	{

		newMessage = text.getText().toString().trim(); 
		hideKeyboard();
		text.clearFocus();
		mButtonsBar.setVisibility(View.GONE);
		if(mListView.getCount()>0) mListView.setSelection(mListView.getCount()-1);
		
		if(newMessage.length() == 0) {
			popup = new SimplePopupWindow(v);
			popup.showLikeQuickAction();
			popup.setMessage("There's no message here!");					
			return;
		}		

		if(number == null) {
			recipients = mRecipientEditor.constructContactsFromInput(false).getToNumbers();
			if(recipients.length > 1) {
				popup = new SimplePopupWindow(v);
				popup.showLikeQuickAction();
				popup.setMessage("Only 1 recipient at a time");					
				return;
			}
			else if(recipients.length == 0) {
				popup = new SimplePopupWindow(v);
				popup.showLikeQuickAction();
				popup.setMessage("Type someone to send to!");
				return;
			}

			number = ContentUtils.addCountryCode(recipients[0]);	
		}			
      
        if(name == null) {
        	name = ContentUtils.getContactDisplayNameByNumber(mContext, number);
        	getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
        	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        	getSupportActionBar().setDisplayShowTitleEnabled(true);
        	if(Constants.DEBUG==false) this.setTitle(name);
        	populateMessages();
        }
		
		/**if(!delayPressed && mPrefs.getBoolean(Constants.PREFERENCE_TIME_DELAY_PROMPT, true)) {
			askAboutTimeDelay();
			return;
		}     **/          
        new DelayedSend(mContext, number, newMessage, sendDate, System.currentTimeMillis(), approver).start();
        
    	text.setText("");
		mTextIndicatorButton.setBackground(getResources().getDrawable(R.drawable.pendinh_indicator));
		mTextIndicatorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup = new SimplePopupWindow(v);
				popup.showLikeQuickAction();
				popup.setMessage("I don't think you wrote a message yet");
			}
		});	        	
    	if(sendDate!=null && calendar.getTime().before(sendDate)) {
    		pickDelay.setText(R.string.compose_select_time);
    		sendDate = null; 
    		//TODO: MAKE IT SO THAT YOU CANT DO THE PAST
    	}
    	newMessage = "";

		
	}
	
	public void pickTime(View v) {
		customDateTimePicker.showDialog();		
	}
	
	public void pickApprover(View v) {
		RecipientsPopupWindow wn = new 	RecipientsPopupWindow();
		wn.show(fm, "Text Message Approver");
		
		//wn.showLikePopDownMenu();
	}
		
	private void addNewMessage(SMSMessage m, boolean onTop)
	{
		if(messages.contains(m)) return;
		
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
	
	public void removeMessage() {
		queue_adapter.delayedMessages.clear();
		messages.clear();
		loader = new GetOutboxMessages();
		loader.execute(); 
		queue_adapter.notifyDataSetChanged();
		merge_adapter.notifyDataSetChanged();
	}	

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

		if(mConfidantesEditor.constructContactsFromInput(false).getNumbers().length==0 
				&& (confidantes==null || confidantes.length==0)) {
			popup = new SimplePopupWindow(v);
			popup.showLikeQuickAction();
			popup.setMessage("Try entering a contact from your address book");	
			return;
		}
		if(mConfidantesEditor.constructContactsFromInput(false).getNumbers().length>0) 
			confidantes = mConfidantesEditor.constructContactsFromInput(false).getToNumbers();
		//hashtags_string = hashtag.getText().toString().trim(); 
		
		if(confidantes==null || confidantes.length == 0) {
			popup = new SimplePopupWindow(v);
			popup.showLikeQuickAction();
			popup.setMessage("Try entering a contact from your address book");	
			return;
		}
		
		if(messages_adapter.check_hash.size()==0) {
			popup = new SimplePopupWindow(v);
			popup.showLikeQuickAction();
			popup.setMessage("Tap messages to select them for sharing");
			return;
		}		
		
        share.setClickable(false);		
		mConfidantesEditor.setText("");						
		viewSwitcher.setDisplayedChild(0);

		shared_sender = ParseUser.getCurrentUser().getUsername();
		person_shared = name;
		shared_address = number;
		shared_convo_type = TextBasedSmsColumns.MESSAGE_TYPE_SENT;	
	
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
			
			shared_confidante = ContentUtils.addCountryCode(confidante);
			shared_convoID = shared_sender+shared_address+shared_confidante;
			Log.i("convoid from sharemessages function", shared_convoID);
			new ShareMessages(mContext, shared_confidante, 
					name, number,  messagesHash).start();	  
			populateSharedMessages(shared_convoID);
		}	
		hideKeyboard();
		text.clearFocus();

		name = null;
		number = null;
		confidantes = null;			
		Intent intent = new Intent();
		intent.putExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, shared_convoID);
		intent.putExtra(Constants.EXTRA_SHARED_NAME, person_shared);
		intent.putExtra(Constants.EXTRA_SHARED_SENDER, shared_sender);
		intent.putExtra(Constants.EXTRA_SHARED_ADDRESS, shared_address);
		intent.putExtra(Constants.EXTRA_SHARED_CONFIDANTE, shared_confidante);		
		intent.putExtra(Constants.EXTRA_SHARED_CONVO_TYPE, shared_convo_type);			
		setIntent(intent);
		messages_adapter.check_hash.clear();
		messages_adapter.showCheckboxes = false;	
		messages_adapter.notifyDataSetChanged();
		mConfidantesEditor.setText("");
		
		share.setClickable(true);	
		
		//mButtonsBar.setVisibility(View.GONE);
		
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
		           .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id)
		               {
		            	
		            		mixpanel.track("alertdialog for text delay, user clicked yes", null);
		            		
		            		
		            	   dialog.cancel();
		            	   customDateTimePicker.showDialog();	
		            	 
		               	}
		           }) 
		           .setPositiveButton("No", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) 
		               {
		            	   mixpanel.track("alertdialog for a text delay, user clicked no", null);
		            	   
		                    dialog.cancel();
		                    sendMessage(send);
		               }
		           }).show();	

	}
	

	private class GetOutboxMessages extends AsyncTask<Void,SMSMessage,Void> {
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
		imm.hideSoftInputFromWindow(text.getWindowToken(), 0);	
		mButtonsBar.setVisibility(View.GONE);
		inputtingEmoji = false;
	}	
		
	@Override
	protected void onDestroy() {
		mixpanel.flush();
	    super.onDestroy();
	}




	@Override
	public void onFinishEditDialog(String inputText) {
		//Toast.makeText(mContext, inputText, Toast.LENGTH_LONG).show();
		if(inputText!=null) {
			pickApprover.setText("after " + ContentUtils
					.getContactDisplayNameByNumber(mContext, inputText) + " approves it");
			approver = inputText;
		}
	}
	private TextWatcher getTextWatcher() {
		return new TextWatcher(){
	        
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
	        		indicatorText = "Good length for this message";      		
	        		
	        	} 
	        	else if(isIdealLength && n_characters>Constants.MAX_TEXT_LENGTH) {
	        		isIdealLength = false;
	        		mTextIndicatorButton.setBackground(getResources().getDrawable(R.drawable.bad_indicator));
	        		indicatorText = "This message is a bit long";	        		
	        		
	        	} 	        	
	        	else if((isIdealLength || n_characters>0) && n_characters<Constants.MIN_TEXT_LENGTH) {
	        		isIdealLength = false;
	        		mTextIndicatorButton.setBackground(getResources().getDrawable(R.drawable.pendinh_indicator));
	        		indicatorText = "This message is a bit short";
	
	        	} 	 
				
			}
	    }	;
	}




	@Override
	public void onClick(View v) {
        switch (counter) {
        case 0:
        	mButtonsBar.setVisibility(View.VISIBLE);
            showcaseView.setShowcase(new ViewTarget(findViewById(R.id.buttons_box)), true);
            showcaseView.setContentTitle("Schedule texts");
            showcaseView.setContentText(
            		"Schedule your texts to send in the future, with the option to change them." +
            " If you receive a text from the person before yours goes out, yours will be canceled.");  
                 
            break;
        case 1:
        	mButtonsBar.setVisibility(View.GONE);
            showcaseView.setShowcase(new ViewTarget(findViewById(R.id.send_button)), true);
            showcaseView.setContentTitle("Cancel texts");
            showcaseView.setContentText(
            		"After you send a text, you automatically have 5 seconds to cancel or edit it");  
            showcaseView.hideButton();
            showcaseView.setHideOnTouchOutside(true);
            break;          
        case 2:
        	showcaseView.hide();
            break;

    }
    counter++;
		
	}

	@Override
	public void onEmojiconBackspaceClicked(View v) {
		EmojiconsFragment.backspace(text);
		
	}

	@Override
	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiconsFragment.input(text, emojicon);
		
	}
		

	@Override
	public void onSoftKeyboardShown(boolean isShowing) {
		keyboardShowing = isShowing;
		if(isShowing) {
	        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();			
			ft.show(emojiFragment).commit();
			text.setLines(3);
		} 	
		else if(!inputtingEmoji) {
	        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();			
			ft.hide(emojiFragment).commit();
			text.setLines(2);			
		}
		
	}

	@Override
	public void onEmojiconTabClicked() {
		inputtingEmoji = true;
		imm.hideSoftInputFromWindow(text.getWindowToken(), 0);	
	}
}
