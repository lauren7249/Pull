package com.Pull.pullapp;

import it.sephiroth.android.library.widget.HListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.Pull.pullapp.adapter.MessageCursorAdapter;
import com.Pull.pullapp.adapter.QueuedMessageAdapter;
import com.Pull.pullapp.adapter.SharedWithCursorAdapter;
import com.Pull.pullapp.fragment.CustomDateTimePicker;
import com.Pull.pullapp.fragment.RecipientsPopupWindow;
import com.Pull.pullapp.fragment.RecipientsPopupWindow.ApproverDialogListener;
import com.Pull.pullapp.fragment.SimplePopupWindow;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.ShareSuggestion;
import com.Pull.pullapp.threads.DelayedSend;
import com.Pull.pullapp.threads.ShareMessages;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.Pull.pullapp.util.LinearLayoutThatDetectsSoftKeyboard;
import com.Pull.pullapp.util.RecipientList.Recipient;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;
import com.Pull.pullapp.util.SendUtils;
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
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconTextView;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.ViewPager;
import com.rockerhieu.emojicon.emoji.Emojicon;

public class MessageActivityCheckboxCursor extends SherlockFragmentActivity
	implements ApproverDialogListener, View.OnClickListener, EmojiconGridFragment.OnEmojiconClickedListener,
	EmojiconsFragment.OnEmojiconBackspaceClickedListener, 
	EmojiconsFragment.OnEmojiconTabClickedListener {
	
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
	private HListView sharedWithListView;
	private LinearLayoutThatDetectsSoftKeyboard mLayout;
	private BroadcastReceiver mBroadcastReceiver;
	private String person_shared;
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
	private MessageActivityCheckboxCursor activity;
	private boolean isMine;
	private String shared_conversant_name;
	private NotificationManager notificationManager;
	private int position;
	private LinearLayout emojiArea;
	private LinearLayout shared_with;
	private Cursor shared_with_cursor;
	private SharedWithCursorAdapter sharedWithAdapter;
	private ImageView image_view;
	private TextView initials_view;
	private ContentUtils cu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_thread);
		
		mContext = this;
		mPrefs = mContext.getSharedPreferences(Constants.PREFERENCE_TIME_DELAY_PROMPT, Context.MODE_PRIVATE);
		activity = this;
		
		store = new UserInfoStore(mContext);
		cu = new ContentUtils();
		//Log.i("log","oncreate ");
		
		mListView = (ListView) findViewById(R.id.list);
		mListView.setFocusable(true);
		mListView.setFocusableInTouchMode(true);	
		mListView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
		     public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
		    	 EmojiconTextView t = (EmojiconTextView) v.findViewById(R.id.message_text);
		    	 String m = t.getText().toString();
	             if(m!=null) text.setText(m);
	             return true; 
		    } 
		}); 
		
		mButtonsBar = (LinearLayout) findViewById(R.id.buttons_box);
		mLayout = (LinearLayoutThatDetectsSoftKeyboard) findViewById(R.id.main_layout);

		delayPressed = false;
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
		mRecipientsAdapter = new RecipientsAdapter(this);
		mConfidantesEditor = (RecipientsEditor)findViewById(R.id.confidantes_editor);
		mConfidantesEditor.setAdapter(mRecipientsAdapter);

		mixpanel = MixpanelAPI.getInstance(mContext, Constants.MIXEDPANEL_TOKEN);
		mixpanel.identify(ParseInstallation.getCurrentInstallation().getObjectId());
		
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME);
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);				
		getSupportActionBar().setCustomView(R.layout.message_thread_action_bar);
		
		send = (Button) this.findViewById(R.id.send_button);
		share = (Button) this.findViewById(R.id.share_button);
		pickDelay = (Button) this.findViewById(R.id.time_delay_button);
		pickApprover  = (Button) this.findViewById(R.id.approvers_button);
		viewSwitcher = (ViewSwitcher) this.findViewById(R.id.viewSwitcher);
		text = (EditText) this.findViewById(R.id.text);
		mTextIndicatorButton = (ImageButton) findViewById(R.id.textIndicatorButton);
		title_view = (TextView) findViewById(R.id.name);
		
		image_view = (ImageView) findViewById(R.id.original_person_image);
		initials_view = (TextView) findViewById(R.id.original_person_initials);			
		shared_with = (LinearLayout) findViewById(R.id.shared_with);
		sharedWithListView = (HListView) findViewById(R.id.shared_with_list);
		sharedWithAdapter = new SharedWithCursorAdapter(mContext,shared_with_cursor,activity);
		sharedWithListView.setAdapter(sharedWithAdapter);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		emojisPager = (ViewPager) findViewById(R.id.emojis_pager);
	    emojiArea = (LinearLayout) findViewById(R.id.emojicons_area);
	    
		approver = "";
		if(getIntent() != null && !isPopulated) {
			
			number =  ContentUtils.addCountryCode(getIntent().getStringExtra(Constants.EXTRA_NUMBER)); 
			name =  getIntent().getStringExtra(Constants.EXTRA_NAME); 
			thread_id = getIntent().getStringExtra(Constants.EXTRA_THREAD_ID); 
			Long scheduledFor = getIntent().getLongExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, 0);
			
			if(scheduledFor>0) sendDate = new Date(scheduledFor);
			else sendDate = null;
			
			shared_confidante = getIntent().getStringExtra(Constants.EXTRA_SHARED_CONFIDANTE); 
			shared_sender = getIntent().getStringExtra(Constants.EXTRA_SHARED_SENDER); 
			shared_convoID = getIntent().getStringExtra(Constants.EXTRA_SHARED_CONVERSATION_ID); 
			person_shared = getIntent().getStringExtra(Constants.EXTRA_SHARED_NAME); 
			shared_address = getIntent().getStringExtra(Constants.EXTRA_SHARED_ADDRESS);
			shared_convo_type = getIntent().getIntExtra(Constants.EXTRA_SHARED_CONVO_TYPE,-1);
				
			if(number!=null && name!=null) {	
				//Log.i("regular convo",number);
				showcaseView = new ShowcaseView.Builder(this)
		        .setTarget(new ViewTarget(findViewById(R.id.list)))
		        .setContentTitle("Tap to share")     
		        .setOnClickListener(this)
		        .singleShot(222)
		        .hideOnTouchOutside()
		        .build();	    
				showcaseView.setButtonText("OK");				
				title_view.setText(name);
				setupComposeBox();
				populateMessages();
			    hideKeyboard();
			    text.clearFocus();				
				notificationManager.cancel(number.hashCode());				
			} else if(person_shared!=null && shared_sender!=null){
				//Log.i("shared convo",person_shared);
				populateSharedMessages(shared_convoID);
			    hideKeyboard();
			    text.clearFocus();				
			}
			else {
				//Log.i("blank convo","nada");
				getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
		                | ActionBar.DISPLAY_SHOW_HOME);
            	getSupportActionBar().setDisplayHomeAsUpEnabled(true);				
				getSupportActionBar().setCustomView(R.layout.recipients_editor);
				setupComposeBox();
				mRecipientEditor = (RecipientsEditor) getSupportActionBar().getCustomView().findViewById(R.id.recipients_editor);
				mRecipientEditor.setAdapter(mRecipientsAdapter);
				mRecipientEditor.setEnabled(true);
				messages = new ArrayList<SMSMessage>();
				queue_adapter = new QueuedMessageAdapter(this,messages);
				merge_adapter = new MergeAdapter();						
				
			}
			
		}		

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
						notificationManager.cancel(number.hashCode());							
					}
					return;
				}				
				if(action.equals(Constants.ACTION_DATABASE_UPDATE) && shared_convoID!=null){
				//	Log.i("convoid from broadcastreceiver in messageactivity",
							//intent.getStringExtra(Constants.EXTRA_SHARED_CONVERSATION_ID));
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
						hideKeyboard();
					}
					else {
						viewSwitcher.setDisplayedChild(1);
					}		
					return;
				}		
				if(action.equals(Constants.ACTION_SHARE_TAB_CLICKED )) {
					shared_confidante = intent.getStringExtra(Constants.EXTRA_SHARED_CONFIDANTE); 
					shared_sender = ParseUser.getCurrentUser().getUsername();
					person_shared = intent.getStringExtra(Constants.EXTRA_SHARED_NAME); 
					shared_address = intent.getStringExtra(Constants.EXTRA_SHARED_ADDRESS); 
					shared_convo_type = TextBasedSmsColumns.MESSAGE_TYPE_SENT;						
					shared_convoID = shared_sender+shared_address+shared_confidante;
					name = null;
					number = null;
					messages.clear();
					messages_adapter.check_hash.clear();
					messages_adapter.showCheckboxes = false;	
					messages_adapter.notifyDataSetChanged();					
					queue_adapter.notifyDataSetChanged();
					populateSharedMessages(shared_convoID);
					Intent i = new Intent();
					i.putExtra(Constants.EXTRA_SHARED_CONFIDANTE, shared_confidante);
					i.putExtra(Constants.EXTRA_SHARED_SENDER, shared_sender);
					i.putExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, shared_convoID);
					i.putExtra(Constants.EXTRA_SHARED_NAME, person_shared);
					i.putExtra(Constants.EXTRA_SHARED_ADDRESS, shared_address);
					i.putExtra(Constants.EXTRA_SHARED_CONVO_TYPE, shared_convo_type);
					setIntent(i);
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
								SMSMessage m = new SMSMessage(new Date().getTime(), 
										intent_message, intent_number, 
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
					//Log.i("tag","queue_adapter.delayedMessages.put " + messages.size());
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
					mixpanel.track("sms canceled", null);
					//Log.i("sms canceled", "sms canceled");
					if(scheduledFor-scheduledOn <= 5000) 
						sendDate = new Date(Math.max(scheduledFor,scheduledOn) + 5000);
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
                	mixpanel.track("timepicker canceled", null);
                	delayPressed = true;
                }

				@Override
				public void onSet(Dialog dialog, Calendar calendarSelected,
						Date dateSelected, int year, String monthFullName,
						String monthShortName, int monthNumber, int date,
						String weekDayFullName, String weekDayShortName,
						int hour24, int hour12, int min, int sec,
						String AM_PM) {
					mixpanel.track("timepicker set", null);
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
			    	        	suggestion.setClicked(new Date().getTime());
			    	        	suggestion.saveInBackground();
			    	        } else {
			    	        	return;
			    	        }
			    	    }
			    	});    	
				}
			}

		}		
		text.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus) return;
    			if(!keyboardShowing) {
        			emojiArea.setVisibility(View.VISIBLE);
        			mButtonsBar.setVisibility(View.VISIBLE);
        			text.setLines(3);    			
        			imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    			}
    			keyboardShowing = true;
			}
			
		});

	}
	



	private void setupComposeBox() {
		indicatorText = "Write something";
		pickDelay.setVisibility(View.VISIBLE);
		mTextIndicatorButton.setBackground(getResources().getDrawable(R.drawable.pendinh_indicator));		
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
		send.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				sendMessage(v);
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
		intentFilter.addAction(Constants.ACTION_SHARE_TAB_CLICKED);
		registerReceiver(mBroadcastReceiver, intentFilter);	

		if(number!=null && name!=null) {
			//position = store.getPosition(number);
			rePopulateMessages();
			notificationManager.cancel(number.hashCode());				
		} else if(person_shared!=null && shared_sender!=null){
			//rePopulateSharedMessages(shared_convoID);
		}
		calendar.get(Calendar.HOUR_OF_DAY);
		calendar.get(Calendar.MINUTE);	
		registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		emojiArea.setVisibility(View.GONE);
		mButtonsBar.setVisibility(View.GONE);
		text.setLines(2);	

	}	
	
	
	private void rePopulateMessages() {
		Log.i("log","repopulate messages");
		removeMessage();
		messages_cursor = ContentUtils.getMessagesCursor(mContext,thread_id, number);
		messages_adapter.swapCursor(messages_cursor);
		messages_adapter.notifyDataSetChanged();
		merge_adapter.notifyDataSetChanged();
		mListView.setSelection(mListView.getCount()-1);			
	}




	private void populateMessages(){
		Log.i("log","populate messages");
		messages = new ArrayList<SMSMessage>();
		queue_adapter = new QueuedMessageAdapter(this,messages);
		merge_adapter = new MergeAdapter();				
		messages_adapter = new MessageCursorAdapter(mContext, messages_cursor, number, this);
		loader = new GetOutboxMessages();
		loader.execute(); 
		isPopulated = true;
		text.setHint("Text " + name);
		title_view.setText(name);
		merge_adapter.addAdapter(messages_adapter);
		merge_adapter.addAdapter(queue_adapter);
		mListView.setAdapter(merge_adapter);	
		getSharedWithTab(number, name);
		initials_view.setBackgroundResource(R.drawable.circle_pressed);
		sharedWithAdapter.current_tab = "";
		sharedWithAdapter.notifyDataSetChanged();	
		mListView.setSelection(mListView.getCount()-1);	
	}
	private void getSharedWithTab(final String original_number, final String original_name) {
		dh = new DatabaseHandler(mContext);
		shared_with_cursor = dh.getSharedWithCursor(original_number);
		if(shared_with_cursor.getCount()>0)		{
			shared_with.setVisibility(View.VISIBLE);		
	    	if(!store.isFriend(original_number)) {
	    		image_view.setVisibility(View.GONE);
	    		initials_view.setVisibility(View.VISIBLE);
	    		initials_view.setText(ContentUtils.getInitials(original_name));
	    		initials_view.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						originalConversation(original_number,original_name);
						initials_view.setBackgroundResource(R.drawable.circle_pressed);
						
					}
	    			
	    		});
	    	} else {
	    		image_view.setVisibility(View.VISIBLE);
	    		image_view.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						originalConversation(original_number,original_name);
						
					}
	    			
	    		});	    		
	    		initials_view.setVisibility(View.GONE);
	    		cu.loadBitmap(mContext, store.getPhotoPath(original_number),image_view, 0);
	    	}			
	    	sharedWithAdapter.swapCursor(shared_with_cursor);
			sharedWithAdapter.notifyDataSetChanged();
		}
		
	}




	protected void originalConversation(String original_number,
			String original_name) {
		shared_confidante = null; 
		shared_sender = null;
		person_shared = null; 
		shared_address = null; 
		shared_convo_type = -1;						
		shared_convoID = null;
		name = original_name;
		number = original_number;
		setupComposeBox();
		populateMessages();
		rePopulateMessages();
		Intent i = new Intent();
		i.putExtra(Constants.EXTRA_NUMBER, number);
		i.putExtra(Constants.EXTRA_NAME, name);
		setIntent(i);	
		initials_view.setBackgroundResource(R.drawable.circle_pressed);
		sharedWithAdapter.current_tab = "";
		sharedWithAdapter.notifyDataSetChanged();
	}




	private void populateSharedMessages(final String shared_convoID) {
		//Log.i("shared convo type",""+shared_convo_type);
		//Log.i("shared_sender",shared_sender);
		//Log.i("shared_confidante",shared_confidante);
		//Log.i("person_shared",person_shared);
		messages = new ArrayList<SMSMessage>();
		queue_adapter = new QueuedMessageAdapter(this,messages);
		merge_adapter = new MergeAdapter();				
		if(shared_convo_type==TextBasedSmsColumns.MESSAGE_TYPE_INBOX) {
			shared_conversant = shared_sender;
			isMine = false;
		}
		else {
			shared_conversant = shared_confidante;	
			isMine = true;
		}
		shared_conversant_name = store.getName(shared_conversant);
		//Log.i("shared_conversant_name ",shared_conversant_name + (title_view==null));
		title_view.setText(shared_conversant_name);	
		dh = new DatabaseHandler(mContext);
		messages_cursor = dh.getSharedMessagesCursor(shared_convoID);
		messages_adapter = new MessageCursorAdapter(mContext, messages_cursor, this, 
				shared_conversant, shared_address, person_shared, isMine);
		mListView.setAdapter(messages_adapter);	
		mListView.setSelection(mListView.getCount()-1);		
		viewSwitcher.setDisplayedChild(0);
		mButtonsBar.setVisibility(View.GONE);
		pickDelay.setVisibility(View.GONE);
		text.setHint("Message " + shared_conversant_name);
		text.removeTextChangedListener(watcher);
		mTextIndicatorButton.setBackground(getResources().getDrawable(R.drawable.comment_indicator));
		mTextIndicatorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup = new SimplePopupWindow(v);
				popup.showLikeQuickAction();
				popup.setMessage("Your comments are only visible to " + shared_conversant_name);
			}
		});		
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
		getSharedWithTab(shared_address, person_shared);
		sharedWithAdapter.current_tab = shared_confidante;
		initials_view.setBackgroundResource(R.drawable.circle);
		sharedWithAdapter.notifyDataSetChanged();
	}

	@SuppressWarnings("unchecked")
	protected void sendComment(String commentText) throws JSONException {
/*		Toast.makeText(mContext, commentText + " to " + shared_sender + " for " + shared_address, 
				Toast.LENGTH_LONG).show();
*/		mixpanel.track("send comment button pressed", null);
		if(commentText.length() == 0) {
			popup = new SimplePopupWindow(text);
			popup.showLikeQuickAction();
			popup.setMessage("There's no message here!");					
			return;
		}			
    	if(!store.isFriend(shared_conversant) && !store.wasInvited(shared_conversant)) {
    		
			View addFriendView = View.inflate(mContext, R.layout.add_friend, null);
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		    builder.setTitle("Send invite with your comment");
		    builder.setMessage(store.getName(shared_conversant) + " isn't your friend on Pull yet. " + 
		    " The comment you're sending may get sent as a regular text message.")
	           .setCancelable(true)
	           .setView(addFriendView)
	           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) 
	               {
	                    dialog.cancel();
	                    mixpanel.track("user did not invite friend", null);
	               }
	           })				           
	           .setNegativeButton("Invite friend", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id)
	               {

	            	   SendUtils.inviteFriend(shared_conversant, mContext, activity);
	            	   mixpanel.track("user invited friend", null);
	               	}
	           }) 
	           .show();		
    	} 
    	  			
		long currentDate = new Date().getTime();
		final SMSMessage comment = new SMSMessage(currentDate, commentText, 
				shared_address, person_shared, Constants.MESSAGE_TYPE_SENT_COMMENT, store, shared_sender);
		//assume this is a conversation that was shared with us
		comment.addConfidante(shared_conversant);
		dh = new DatabaseHandler(mContext);
		
		dh.addSharedMessage(shared_convoID, comment, TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
		dh = new DatabaseHandler(mContext);
		messages_cursor = dh.getSharedMessagesCursor(shared_convoID);
		messages_adapter.swapCursor(messages_cursor);
		messages_adapter.notifyDataSetChanged();
		text.setText("");
		hideKeyboard();
		mListView.setSelection(mListView.getCount()-1);
		comment.saveToParse();
		  HashMap<String, Object> params = new HashMap<String, Object>();
		  params.put("phoneNumber", ContentUtils.addCountryCode(shared_confidante));
		  ParseCloud.callFunctionInBackground("findUser", params, new FunctionCallback() {
			@Override
			public void done(Object arg0, ParseException e) {
		         if (e != null) {
		        	 	//Log.i("nothing found", "going to send a text " + ContentUtils.addCountryCode(shared_confidante));
			            SendUtils.commentViaSMS(mContext, person_shared, shared_confidante, comment);     
			         }
			}
		  });			
	}




	private void updateTime(){
		//Log.i("time", "Updated Time");
		updateDelayButton();
		merge_adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onPause() {
		//position = mListView.getSelectedItemPosition();
		super.onPause();
		unregisterReceiver(mBroadcastReceiver);	
		unregisterReceiver(tickReceiver);
		
		//if(number!=null)store.putPosition(number,position);
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
	  if(sendDate!=null) savedInstanceState.putLong("scheduled_for", sendDate.getTime()); 
	//  savedInstanceState.putInt("position", mListView.getSelectedItemPosition()); 	 
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
	  if(savedInstanceState.getLong("scheduled_for")>0) 
		  sendDate = new Date(savedInstanceState.getLong("scheduled_for"));
	//  position = savedInstanceState.getInt("position"); 	
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
		mixpanel.track("message activity create options", null);
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
			if(loader!=null) loader.cancel(true);	
            NavUtils.navigateUpFromSameTask(this);
            return true;	
		case R.id.menu_contacts:
			mixpanel.track("add to contacts", null);
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            if(number!=null) intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
            else intent.putExtra(ContactsContract.Intents.Insert.PHONE, shared_conversant);
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
		mixpanel.track("back button pressed", null);
		
		if(keyboardShowing) {
			hideKeyboard();
			keyboardShowing = false;
			text.clearFocus();	
			return;
		}
		if(emojiArea.getVisibility() == View.VISIBLE) {
			emojiArea.setVisibility(View.GONE);
			mButtonsBar.setVisibility(View.GONE);
			inputtingEmoji = false;
			keyboardShowing = false;
			text.setLines(2);
			text.clearFocus();	
			return;
		}		
	    super.onBackPressed();
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
		text.setText("");	
		inputtingEmoji = false;
    	keyboardShowing = false;
		text.setLines(2);	
		text.clearFocus();
		hideKeyboard();		
		emojiArea.setVisibility(View.GONE);
		mButtonsBar.setVisibility(View.GONE);		
		mixpanel.track("send message click", null);
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
        	getSupportActionBar().setCustomView(R.layout.message_thread_action_bar);
        	populateMessages();
        	rePopulateMessages();
        	Intent intent = new Intent();
        	intent.putExtra(Constants.EXTRA_NUMBER, number);
        	intent.putExtra(Constants.EXTRA_NAME, name);
        	setIntent(intent);
        }
 
        new DelayedSend(mContext, number, newMessage, sendDate, new Date().getTime(), approver).start();
        
    	
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
		mListView.setSelection(mListView.getCount()-1);
	}
	
	public void removeMessage() {
		queue_adapter.delayedMessages.clear();
		messages.clear();
		loader = new GetOutboxMessages();
		loader.execute(); 
		queue_adapter.notifyDataSetChanged();
		merge_adapter.notifyDataSetChanged();
	}	

	public void shareMessages(View v) throws JSONException 
	{
		
		mixpanel.track("share messages clicked in message activity", null);
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

		inputtingEmoji = false;
    	keyboardShowing = false;
		text.setLines(2);	
		text.clearFocus();
		hideKeyboard();		
		emojiArea.setVisibility(View.GONE);
		mButtonsBar.setVisibility(View.GONE);	
		
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
			//Log.i("convoid from sharemessages function", shared_convoID);
			new ShareMessages(mContext, shared_confidante, 
					name, number,  messagesHash).start();	  
			populateSharedMessages(shared_convoID);
		}	
		getSharedWithTab(number,name);
		sharedWithAdapter.current_tab = shared_confidante;
		sharedWithAdapter.notifyDataSetChanged();

		name = null;
		number = null;
		confidantes = null;			
		isPopulated = false;
		
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
		keyboardShowing = false;
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
		showcaseView.hide();
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
	public void onEmojiconTabClicked() {
		mixpanel.track("emojicon tab clicked", null);
		inputtingEmoji = true;
		imm.hideSoftInputFromWindow(text.getWindowToken(), 0);	
		keyboardShowing = false;
	}
}
