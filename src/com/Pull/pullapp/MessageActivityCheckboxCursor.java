package com.Pull.pullapp;

import it.sephiroth.android.library.widget.HListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.Threads;
import android.provider.Telephony.ThreadsColumns;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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

import com.Pull.pullapp.adapter.GraphAdapter;
import com.Pull.pullapp.adapter.MessageCursorAdapter;
import com.Pull.pullapp.adapter.QueuedMessageAdapter;
import com.Pull.pullapp.adapter.SharedWithCursorAdapter;
import com.Pull.pullapp.fragment.CustomDateTimePicker;
import com.Pull.pullapp.fragment.RecipientsPopupWindow;
import com.Pull.pullapp.fragment.RecipientsPopupWindow.ApproverDialogListener;
import com.Pull.pullapp.fragment.SimplePopupWindow;
import com.Pull.pullapp.model.MMSMessage;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.ShareSuggestion;
import com.Pull.pullapp.threads.DelayedSend;
import com.Pull.pullapp.threads.SendMMS;
import com.Pull.pullapp.threads.ShareMessages;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.Pull.pullapp.util.LinearLayoutThatDetectsSoftKeyboard;
import com.Pull.pullapp.util.RecipientList.Recipient;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;
import com.Pull.pullapp.util.SendUtils;
import com.Pull.pullapp.util.SwipeDetector;
import com.Pull.pullapp.util.TransactionSettings;
import com.Pull.pullapp.util.UserInfoStore;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.merge.MergeAdapter;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.mikhaellopez.circularimageview.CircularImageView;
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
import com.rockerhieu.emojicon.emoji.Emojicon;

public class MessageActivityCheckboxCursor extends SherlockFragmentActivity
	implements ApproverDialogListener, View.OnClickListener, 
	EmojiconGridFragment.OnEmojiconClickedListener,
	EmojiconsFragment.OnEmojiconBackspaceClickedListener, 
	EmojiconsFragment.OnEmojiconTabClickedListener {
	
	protected static final int CONTEXTMENU_CONTACTITEM = 1;	
	protected static final int CONTEXTMENU_SHARE_SECTION = 2;	
	protected static final int CONTEXTMENU_SHARE_PERSISTENT = 3;
	private ArrayList<SMSMessage> messages;
	private MessageCursorAdapter messages_adapter;
	private EditText text;
	private String name,number,newMessage;
	private Context mContext;
	private final Calendar calendar = Calendar.getInstance();
	private Button pickDelay, send, share;
	private GetOutboxMessages outbox_loader;
	private GetMMSMessages mms_loader;
	private boolean isPopulated;
	private CustomDateTimePicker customDateTimePicker;
	private Date sendDate;
	private ViewSwitcher viewSwitcher;
	private RecipientsEditor mRecipientEditor, mConfidantesEditor;
	private RecipientsAdapter mRecipientsAdapter;
	private String[] numbers;
	private ListView mListView;
	private HListView sharedWithListView;
	private LinearLayoutThatDetectsSoftKeyboard mLayout;
	private BroadcastReceiver mBroadcastReceiver;
	private String clueless_persons_name;
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
	private static UserInfoStore store;
	private String shared_confidante;
	private String clueless_persons_number;
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
	private CircularImageView image_view;
	private TextView initials_view;
	private ContentUtils cu;
	private SwipeDetector swipeDetector;
	private ImageView addPerson;
	private int tab_counter;
	private LinearLayout mGraphView;
	private ViewSwitcher graphViewSwitcher;
	private ImageView home_button;
	private ViewSwitcher topViewSwitcher;
	private ImageView graphButton;
	private TextView header_title;
	private String status;
    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
	private ImageView menu_button;
	private boolean hasMMS;
	private String[] names;
	private boolean isGroupMessage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_thread);
		
		mContext = this;
		mPrefs = mContext.getSharedPreferences(Constants.PREFERENCE_TIME_DELAY_PROMPT, Context.MODE_PRIVATE);
		activity = this;
		
		store = new UserInfoStore(mContext);
		cu = new ContentUtils();
		tab_counter=1;
		swipeDetector = new SwipeDetector();
		mListView = (ListView) findViewById(R.id.list);
		mListView.setFocusable(true);
		mListView.setFocusableInTouchMode(true);	
		mListView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		//mListView.setOnTouchListener(swipeDetector);	
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
		     public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
	             if (swipeDetector.swipeDetected()) {
	                 if (swipeDetector.getAction() == SwipeDetector.Action.LR) {
	                	 swipeRight();
	                 }
	                 if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
	                	 swipeLeft();
	                 }
	             }	      
		    	 EmojiconTextView t = (EmojiconTextView) v.findViewById(R.id.message_text);
		    	 String m = t.getText().toString();
	             if(m!=null) text.setText(m);
	             	             
	             return true; 
		    } 
		}); 
		mListView.setOnItemClickListener(new OnItemClickListener() {
		     public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {

	             if (swipeDetector.swipeDetected()) {
	                 if (swipeDetector.getAction() == SwipeDetector.Action.LR) {
	                	 swipeRight();
	                 }
	                 if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
	                	 swipeLeft();
	                 }
	             }	      
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

		send = (Button) this.findViewById(R.id.send_button);
		share = (Button) this.findViewById(R.id.share_button);
		pickDelay = (Button) this.findViewById(R.id.time_delay_button);
		pickApprover  = (Button) this.findViewById(R.id.approvers_button);
		viewSwitcher = (ViewSwitcher) this.findViewById(R.id.viewSwitcher);
		graphViewSwitcher = (ViewSwitcher) this.findViewById(R.id.big_viewSwitcher);
		topViewSwitcher = (ViewSwitcher) this.findViewById(R.id.top_viewSwitcher);
		text = (EditText) this.findViewById(R.id.text);
		mTextIndicatorButton = (ImageButton) findViewById(R.id.textIndicatorButton);
		title_view = (TextView) findViewById(R.id.name);
		
		image_view = (CircularImageView) findViewById(R.id.original_person_image);
		initials_view = (TextView) findViewById(R.id.original_person_initials);			
		shared_with = (LinearLayout) findViewById(R.id.shared_with);
		home_button = (ImageView) findViewById(R.id.home_button);
		graphButton = (ImageView) findViewById(R.id.graph_button);
		addPerson = (ImageView) findViewById(R.id.add_person);
		
		sharedWithListView = (HListView) findViewById(R.id.shared_with_list);
		sharedWithAdapter = new SharedWithCursorAdapter(mContext,shared_with_cursor,activity);
		sharedWithListView.setAdapter(sharedWithAdapter);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		emojisPager = (ViewPager) findViewById(R.id.emojis_pager);
	    emojiArea = (LinearLayout) findViewById(R.id.emojicons_area);
	       // Instantiate a ViewPager and a PagerAdapter.
   //     mPager = (ViewPager) findViewById(R.id.pager);
        
		home_button.setOnClickListener(new OnClickListener (){

			@Override
			public void onClick(View v) {
				NavUtils.navigateUpFromSameTask(activity);
				return;			
			}
			
		}) ;	
		 
		menu_button = (ImageView) findViewById(R.id.menu_button);
		menu_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				activity.openOptionsMenu();
				
			}
			
		});
		approver = "";
		if(getIntent() != null && !isPopulated) {

			status = getIntent().getStringExtra(Constants.EXTRA_STATUS); 
			if(status==null)  status = "";
			
			numbers = getIntent().getStringArrayExtra(Constants.EXTRA_NUMBERS);
			names = getIntent().getStringArrayExtra(Constants.EXTRA_NAMES);
			thread_id = getIntent().getStringExtra(Constants.EXTRA_THREAD_ID); 
			
			Long scheduledFor = getIntent().getLongExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, 0);
			if(scheduledFor>0) sendDate = new Date(scheduledFor);
			else sendDate = null;
			
			shared_confidante = getIntent().getStringExtra(Constants.EXTRA_SHARED_CONFIDANTE); 
			shared_sender = getIntent().getStringExtra(Constants.EXTRA_SHARED_SENDER); 
			shared_convoID = getIntent().getStringExtra(Constants.EXTRA_SHARED_CONVERSATION_ID); 
			clueless_persons_name = getIntent().getStringExtra(Constants.EXTRA_CLUELESS_PERSONS_NAME); 
			clueless_persons_number = getIntent().getStringExtra(Constants.EXTRA_CLUELESS_PERSONS_NUMBER);
			shared_convo_type = getIntent().getIntExtra(Constants.EXTRA_SHARED_CONVO_TYPE,-1);
			
			if(numbers == null && thread_id!=null) {
				Uri uri=Uri.parse("content://mms-sms/conversations?simple=true");
				String selection="_id=? and recipient_ids is not null and recipient_ids is not ''";
				Cursor cursor = mContext.getContentResolver().query(uri, null, 
						selection, new String[]{thread_id}, null);
				if(cursor.moveToNext()) {
                	String[] recipientIds = cursor.getString(cursor.getColumnIndex(Threads.RECIPIENT_IDS)).split(" ");
                	Log.i("recipient ids", cursor.getString(cursor.getColumnIndex(Threads.RECIPIENT_IDS)));
                	numbers = store.getPhoneNumbers(recipientIds);
                	names = store.getNames(numbers);
				}				
			}
			//TODO: FIX THIS
			if(numbers!=null && numbers.length>1) {
				name = Arrays.asList(names).toString().substring(1).replace("]", "");
				Log.i("threadid",thread_id);
				hasMMS = true;
				isGroupMessage = true;
				title_view.setText(name);
				setupComposeBox();
				populateMessages();
			    hideKeyboard();
			    text.clearFocus();				
				notificationManager.cancel(thread_id.hashCode());				
			}
			else if(thread_id!=null && numbers!=null && numbers.length==1) {
				number =  ContentUtils.addCountryCode(numbers[0]); 
				name =  names[0];				
				title_view.setText(name);
				setupComposeBox();
				populateMessages();
			    hideKeyboard();
			    text.clearFocus();				
				notificationManager.cancel(number.hashCode());
			} else if(clueless_persons_name!=null && shared_sender!=null){
				populateSharedMessages(shared_convoID);
			    hideKeyboard();
			    text.clearFocus();				
			}
			else {
				topViewSwitcher.setDisplayedChild(1);
				setupComposeBox();
				mRecipientEditor = (RecipientsEditor) findViewById(R.id.recipients_editor);
				mRecipientEditor.setAdapter(mRecipientsAdapter);
				messages = new ArrayList<SMSMessage>();
				queue_adapter = new QueuedMessageAdapter(this,messages);
				merge_adapter = new MergeAdapter();		
			}
			
		}		
		
		
		mBroadcastReceiver = 
		new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();			
				Log.i("action","Message activity broadcastreeiver " + action);
				if(action.equals(Constants.ACTION_SMS_INBOXED)) {
					if(intent.getStringArrayExtra(Constants.EXTRA_NUMBERS)[0].equals(numbers[0])) {
						messages_cursor = ContentUtils.getMessagesCursor(mContext,thread_id, numbers[0], false);
						//messages_cursor = ContentUtils.getMessagesCursor(mContext,thread_id, number);
						messages_adapter.swapCursor(messages_cursor);							
						messages_adapter.notifyDataSetChanged();
						merge_adapter.notifyDataSetChanged();
						
						//mListView.setSelection(mListView.getCount()-1);			
						notificationManager.cancel(number.hashCode());							
					}
					return;
				}	
				if(action.equals(Constants.ACTION_MMS_INBOXED)) {
					if(intent.getStringExtra(Constants.EXTRA_THREAD_ID)!=null &&
							intent.getStringExtra(Constants.EXTRA_THREAD_ID).equals(thread_id)) {
						mms_loader = new GetMMSMessages();
						mms_loader.execute(); 	
						//mListView.setSelection(mListView.getCount()-1);			
						notificationManager.cancel(thread_id.hashCode());							
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
					//mListView.setSelection(mListView.getCount()-1);
					return;
				}
				if(action.equals(Constants.ACTION_SHARE_STATE_CHANGED)) {
					if(messages_adapter.check_hash.size()==0) {
						mConfidantesEditor.setHint("Tap messages to select for sharing");
					}
					else {
						mConfidantesEditor.setHint("Type a friend's name");
					}		
					return;
				}	
				if(action.equals(Constants.ACTION_SHARE_TAB_CLICKED )) {
					tab_counter = intent.getIntExtra(Constants.EXTRA_TAB_POSITION,0) + 3; 				
					shareTabClicked();
					return;
				}			
				String intent_number = intent.getStringExtra(Constants.EXTRA_RECIPIENT);
				String intent_thread_id = intent.getStringExtra(Constants.EXTRA_THREAD_ID);
				if(thread_id!=null && intent_thread_id!=null && !intent_thread_id.equals(thread_id)) {	
					if(number==null) return;					
					if(intent_number==null) return;
					if(!intent_number.equals(number)) return;
				}
				Long scheduledOn = intent.getLongExtra(Constants.EXTRA_TIME_LAUNCHED, 0);
				Long scheduledFor = intent.getLongExtra(Constants.EXTRA_TIME_SCHEDULED_FOR, 0);
				String intent_message = intent.getStringExtra(Constants.EXTRA_MESSAGE_BODY);
				String intent_approver = intent.getStringExtra(Constants.EXTRA_APPROVER);
				if(action.equals(Constants.ACTION_SMS_DELIVERED)) {
					switch (getResultCode()) {
						case Activity.RESULT_OK: {
								
							if(queue_adapter.delayedMessages.containsKey(scheduledOn) && scheduledOn>0) {
								removeMessage();
							}
							messages_cursor = ContentUtils.getMessagesCursor(mContext,thread_id, number, false);
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
				} else if(action.equals(Constants.ACTION_MMS_DELIVERED)) {

					if(queue_adapter.delayedMessages.containsKey(scheduledOn) && scheduledOn>0) {
						removeMessage();
					}
					//Log.i("mms got delivered","mms got delivered");
					mms_loader = new GetMMSMessages();
					mms_loader.execute(); 						
							
				}
				else if(action.equals(Constants.ACTION_SMS_OUTBOXED)) {
					
					addDelayedMessage(scheduledOn);
					SMSMessage m = new SMSMessage(scheduledOn, intent_message, intent_number, name,
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
					if(!m.isDelayed) {
						new CountDownTimer(scheduledFor-scheduledOn,1000) {

						     public void onTick(long millisUntilFinished) {
						    	 if(millisUntilFinished<=60000) {
							         queue_adapter.notifyDataSetChanged();
							         merge_adapter.notifyDataSetChanged();
						    	 }
						     }

						     public void onFinish() {
						         queue_adapter.notifyDataSetChanged();
						         merge_adapter.notifyDataSetChanged();						        
						     }
						  }.start();						
					}

				} 
				else if(action.equals(Constants.ACTION_MMS_OUTBOXED)) {
					if(!intent_thread_id.equals(thread_id)) return;					
					addDelayedMessage(scheduledOn);
					MMSMessage m = new MMSMessage(scheduledOn, intent_message, numbers, names,
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
					if(!m.isDelayed) {
						new CountDownTimer(scheduledFor-scheduledOn,1000) {

						     public void onTick(long millisUntilFinished) {
						    	 if(millisUntilFinished<=60000) {
							         queue_adapter.notifyDataSetChanged();
							         merge_adapter.notifyDataSetChanged();
						    	 }
						     }

						     public void onFinish() {
						         queue_adapter.notifyDataSetChanged();
						         merge_adapter.notifyDataSetChanged();						        
						     }
						  }.start();						
					}

				}
				else if(action.equals(Constants.ACTION_SMS_UNOUTBOXED)) {
					mixpanel.track("sms canceled", null);
					//Log.i("sms canceled", "sms canceled");
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
    			if(!keyboardShowing && text.hasFocus()) {
        			emojiArea.setVisibility(View.VISIBLE);
        			mButtonsBar.setVisibility(View.VISIBLE);
        			text.setLines(3);    			
        			imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
        			//Log.i("has focus and keyboard is not showing","has focus and keyboard is not showing");
    			}
    			keyboardShowing = true;
			}
			
		});


	}
	


	protected void shareTabClicked() {			
		graphViewSwitcher.setDisplayedChild(0);
		shared_with_cursor.moveToPosition(tab_counter-3);
		shared_confidante = shared_with_cursor.getString(0);
		clueless_persons_number = shared_with_cursor.getString(1); 
		clueless_persons_name = shared_with_cursor.getString(2); 			
		addPerson.setBackgroundResource(R.drawable.add);
		addPerson.setSelected(false);
		graphButton.setBackgroundResource(R.drawable.graph);
		graphButton.setSelected(false);		
		shared_sender = ParseUser.getCurrentUser().getUsername();
		shared_convo_type = TextBasedSmsColumns.MESSAGE_TYPE_SENT;					
		shared_convoID = shared_sender+clueless_persons_number+shared_confidante;
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
		i.putExtra(Constants.EXTRA_CLUELESS_PERSONS_NAME, clueless_persons_name);
		i.putExtra(Constants.EXTRA_CLUELESS_PERSONS_NUMBER, clueless_persons_number);
		i.putExtra(Constants.EXTRA_SHARED_CONVO_TYPE, shared_convo_type);
		setIntent(i);
		hideInputs();	
	}



	//move to the right
	protected void swipeLeft() {
		if(tab_counter>=shared_with_cursor.getCount()+3) return;
		tab_counter++;
		updateTab();
		
	}

	private void updateTab() {
		if(shared_with_cursor.moveToPosition(tab_counter-3)) {			
			shareTabClicked();	
		} else if(tab_counter==0) {
			NavUtils.navigateUpFromSameTask(activity);
		} else if(tab_counter==1) {
			if(number==null) originalPersonClicked(clueless_persons_number,clueless_persons_name);
			else originalPersonClicked(number,name);				
		} else if(tab_counter==2) {
			if(number==null) shareTabSelected(clueless_persons_number,clueless_persons_name);
			else shareTabSelected(number,name);		

		} else {

		}
	}



	protected void swipeRight() {
		if(tab_counter==-1) {

			NavUtils.navigateUpFromSameTask(this);
			return;
		}
		tab_counter--;	
		updateTab();
	}


	protected void addDelayedMessage(Long scheduledOn) {
		queue_adapter.delayedMessages.put(scheduledOn, messages.size());
		
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
		intentFilter.addAction(Constants.ACTION_MMS_OUTBOXED);
		intentFilter.addAction(Constants.ACTION_SMS_UNOUTBOXED);
		intentFilter.addAction(Constants.ACTION_SMS_DELIVERED);	
		intentFilter.addAction(Constants.ACTION_MMS_DELIVERED);		
		intentFilter.addAction(Constants.ACTION_SHARE_COMPLETE);	
		intentFilter.addAction(Constants.ACTION_SHARE_STATE_CHANGED);	
		intentFilter.addAction(Constants.ACTION_DATABASE_UPDATE);
		intentFilter.addAction(Constants.ACTION_SMS_INBOXED);
		intentFilter.addAction(Constants.ACTION_MMS_INBOXED);
		intentFilter.addAction(Constants.ACTION_SHARE_TAB_CLICKED);
		registerReceiver(mBroadcastReceiver, intentFilter);	
		hideInputs();
		if(thread_id!=null) {
			//position = store.getPosition(number);
			rePopulateMessages();
			if(number!=null) {
				notificationManager.cancel(number.hashCode());			
			
				if(status.equals(Constants.ACTION_GRAPH_TAB_CLICKED)) {
					graphTabSelected(number, name);
				} else if(status.equals(Constants.ACTION_ADDPPL_TAB_CLICKED)) {
					shareTabSelected(number,name);	
				}		
			} 
			notificationManager.cancel(thread_id.hashCode());		
		} else if(clueless_persons_name!=null && shared_sender!=null){
			//rePopulateSharedMessages(shared_convoID);
		}
		calendar.get(Calendar.HOUR_OF_DAY);
		calendar.get(Calendar.MINUTE);	
		registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		

	}	
	

	 private static class UploadGraph extends AsyncTask<Cursor, Void, Void> {
	     protected Void doInBackground(Cursor... c) {
			Cursor messages_cursor = c[0];
	    	int num = messages_cursor.getCount();
	    	String owner = ParseUser.getCurrentUser().getUsername();			
	    	for (int i=0; i<num; i++) {
	    		messages_cursor.moveToPosition(i);
				long date = messages_cursor.getLong(6);
				String body = messages_cursor.getString(2).toString();
				int type = Integer.parseInt(messages_cursor.getString(1).toString());
				String address = messages_cursor.getString(4).toString();
				  
			    SMSMessage message = new SMSMessage(date, body, address, store.getName(address), type, store, owner);			  
				message.setGraphed();
				
			    try {
			    	Thread.sleep(100);
			    	message.saveToParse();
			    } catch (JSONException | InterruptedException e) {
				// TODO Auto-generated catch block
			    	e.printStackTrace();
				}
			}
			return null;
	     }
	 }	
	private void rePopulateMessages() {
		//Log.i("log","repopulate messages");
		removeMessage();
		messages_cursor = ContentUtils.getMessagesCursor(mContext,thread_id, number, hasMMS);

		messages_adapter.swapCursor(messages_cursor);

		//Log.i("merge_adapter","merge_adapter size "+merge_adapter.getCount());

		messages_adapter.notifyDataSetChanged();
		merge_adapter.notifyDataSetChanged();
		
		mListView.setStackFromBottom(true);
		mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mListView.setAdapter(merge_adapter);	
		//Log.i("mListView","mListView size "+mListView.getCount());
	}

	private void populateMessages(){
		//Log.i("log","populate messages");
		isMine = true;
		messages = new ArrayList<SMSMessage>();
		queue_adapter = new QueuedMessageAdapter(this,messages);
		merge_adapter = new MergeAdapter();				
		messages_adapter = new MessageCursorAdapter(mContext, messages_cursor, numbers, this);

		outbox_loader = new GetOutboxMessages();
		outbox_loader.execute(); 
		mms_loader = new GetMMSMessages();
		mms_loader.execute(); 		
		isPopulated = true;
		text.setHint("Text " + name);
		title_view.setText(name);
		merge_adapter.addAdapter(messages_adapter);
		merge_adapter.addAdapter(queue_adapter);
		//mListView.setAdapter(merge_adapter);	
		getSharedWithTab(number, name);
		initials_view.setBackgroundResource(R.drawable.circle_pressed);
		initials_view.setTypeface(null, Typeface.BOLD);	
		initials_view.setSelected(true);
		sharedWithAdapter.setCurrentTab("");
		sharedWithAdapter.notifyDataSetChanged();	
		//Log.i("merge_adapter.getCount()","merge_adapter.getCount()" + merge_adapter.getCount());
		//mListView.setSelection(merge_adapter.getCount()-1);	
	}
	private void getSharedWithTab(final String original_number, final String original_name) {
		dh = new DatabaseHandler(mContext);
		shared_with_cursor = dh.getSharedWithCursor(original_number);
		
    	if(!store.isFriend(original_number) || store.getPhotoPath(original_number)==null) {
    		image_view.setVisibility(View.GONE);
    		initials_view.setVisibility(View.VISIBLE);
    		initials_view.setText(ContentUtils.getInitials(original_name, original_number));
    		initials_view.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					//Log.i("ORIG NUM",original_number);
					selectOriginalPerson(original_number, original_name);
						
				}
    			
    		});
    	} else {
    		image_view.setVisibility(View.VISIBLE);
    		image_view.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					originalPersonClicked(original_number,original_name);
				}
    			
    		});	    		
    		initials_view.setVisibility(View.GONE);
    		cu.loadBitmap(mContext, store.getPhotoPath(original_number),image_view, 0);
    	}			
    	sharedWithAdapter.swapCursor(shared_with_cursor);
		sharedWithAdapter.notifyDataSetChanged();
		addPerson.setOnClickListener(new OnClickListener (){

			@Override
			public void onClick(View v) {

				if(addPerson.isSelected()) {
					popup = new SimplePopupWindow(addPerson);
					popup.showLikePopDownMenu();
					popup.setMessage("Add a friend without " + original_name + " knowing");			
				} else  {

					shareTabSelected(original_number,original_name);			}
				return;			
				
			}
			
		}) ;
		graphButton.setOnClickListener(new OnClickListener (){

			@Override
			public void onClick(View v) {

				if(graphButton.isSelected()) {
					popup = new SimplePopupWindow(graphButton);
					popup.showLikePopDownMenu();
					popup.setMessage("Get insight about your conversation with  " + original_name);			
				} else  {

					graphTabSelected(original_number,original_name);			}
				return;			
				
			}
			
		}) ;	
	}


	protected void graphTabSelected(String original_number, String original_name) {
		initials_view.setBackgroundResource(R.drawable.circle);
		initials_view.setTypeface(null, Typeface.NORMAL);
		initials_view.setSelected(false);
		title_view.setText(original_name);
		addPerson.setBackgroundResource(R.drawable.add);			
		addPerson.setSelected(false);
		//graphButton.setBackgroundResource(R.drawable.circle_pressed);
		graphButton.setBackgroundResource(R.drawable.graph_pressed);
		graphButton.setSelected(true);			
		graphViewSwitcher.setDisplayedChild(1);		
		if(messages_cursor.getCount()>1 && mGraphView==null) {
		    mGraphView = (LinearLayout) findViewById(R.id.graph_area);
			HashMap<String, TreeMap<Long, Float>> data = ContentUtils.getDataSeries(messages_cursor, mContext);
			HListView lv = (HListView) findViewById(R.id.graphs_list);
			TreeMap<String, ArrayList<View>> graph_sections = new TreeMap<String, ArrayList<View>> ();
			String[] graphs = new String[]{Constants.GRAPH_CONTACT_INIT_FREQ_THEM,
					Constants.GRAPH_CONTACT_INIT_FREQ_ME,
					Constants.GRAPH_CONTACT_INIT_FREQ_RATIO};
			String graph_title = "How often you each text first";
			
			ArrayList<View> views = ContentUtils.getGraphs(activity, original_name, data, graphs, "");
			graph_sections.put(graph_title, views);

			graphs = new String[]{Constants.GRAPH_RESPONSE_TIME_THEM,
				Constants.GRAPH_RESPONSE_TIME_ME,
				Constants.GRAPH_RESPONSE_TIME_RATIO};
		
			graph_title = "How long you each take to respond";
			views = ContentUtils.getGraphs(activity, original_name, data, graphs, "");	
			graph_sections.put(graph_title, views);
	        lv.setAdapter(new GraphAdapter(mContext, R.layout.graph_item, null, graph_sections));			
			new UploadGraph().execute(messages_cursor);

		}
		
		hideInputs();	
	}


	 
	protected void originalPersonClicked(String original_number,
			String original_name) {
		graphViewSwitcher.setDisplayedChild(0);
		originalConversation(original_number,original_name);
		messages_adapter.showCheckboxes=false;
		messages_adapter.notifyDataSetChanged();
		merge_adapter.notifyDataSetChanged();	
		viewSwitcher.setDisplayedChild(0);
		addPerson.setBackgroundResource(R.drawable.add);
		addPerson.setSelected(false);
		graphButton.setBackgroundResource(R.drawable.graph);
		graphButton.setSelected(false);		
		hideInputs();	
	}



	protected void shareTabSelected(String original_number, String original_name) {
		originalConversation(original_number,original_name);
		graphViewSwitcher.setDisplayedChild(0);
		viewSwitcher.setDisplayedChild(1);
		messages_adapter.showCheckboxes=true;
		initials_view.setBackgroundResource(R.drawable.circle);
		initials_view.setTypeface(null, Typeface.NORMAL);
		initials_view.setSelected(false);
		messages_adapter.notifyDataSetChanged();
		merge_adapter.notifyDataSetChanged();	
		title_view.setText(original_name);
		mConfidantesEditor.setHint("Tap messages to select");
		addPerson.setBackgroundResource(R.drawable.add_pressed);
		//addPerson.setBackgroundResource(R.drawable.circle_pressed);
		addPerson.setSelected(true);
		graphButton.setBackgroundResource(R.drawable.graph);
		graphButton.setSelected(false);				
		hideInputs();	
	}



	protected void originalConversation(String original_number,
			String original_name) {
		shared_confidante = null; 
		shared_sender = null;
		clueless_persons_name = null; 
		clueless_persons_number = null; 
		shared_convo_type = -1;						
		shared_convoID = null;
		name = original_name;
		number = original_number;
		numbers = new String[]{number};
		names = new String[]{name};
		setupComposeBox();
		populateMessages();
		rePopulateMessages();
		Intent i = new Intent();
		i.putExtra(Constants.EXTRA_NUMBERS, numbers);
		i.putExtra(Constants.EXTRA_NAMES, names);
		setIntent(i);	
		initials_view.setBackgroundResource(R.drawable.circle_pressed);
		initials_view.setTypeface(null, Typeface.BOLD);
		initials_view.setSelected(true);
		sharedWithAdapter.setCurrentTab("");
		sharedWithAdapter.notifyDataSetChanged();
	}




	private void populateSharedMessages(final String shared_convoID) {
		//Log.i("shared convo type",""+shared_convo_type);
		//Log.i("shared_sender",shared_sender);
		//Log.i("shared_confidante",shared_confidante);
		//Log.i("clueless_persons_name",clueless_persons_name);
		messages = new ArrayList<SMSMessage>();
		queue_adapter = new QueuedMessageAdapter(this,messages);
		merge_adapter = new MergeAdapter();		
		
		if(shared_convo_type==TextBasedSmsColumns.MESSAGE_TYPE_INBOX) {
			shared_conversant = shared_sender;
			isMine = false;
			shared_with.setVisibility(View.GONE);
			shared_conversant_name = store.getName(shared_conversant);
			header_title.setVisibility(View.VISIBLE);
			header_title.setText(shared_conversant_name + "'s convo");
		}
		else {
			shared_conversant = shared_confidante;	
			shared_conversant_name = store.getName(shared_conversant);
			isMine = true;
			clueless_persons_name = store.getName(clueless_persons_number);
		}
		

		//Log.i("shared_conversant_name ",shared_conversant_name + (title_view==null));
		title_view.setText(shared_conversant_name);	
		dh = new DatabaseHandler(mContext);
		messages_cursor = dh.getSharedMessagesCursor(shared_convoID);
		messages_adapter = new MessageCursorAdapter(mContext, messages_cursor, this, 
				shared_conversant, clueless_persons_number, clueless_persons_name, isMine);
		mListView.setAdapter(messages_adapter);	
		//mListView.setSelection(mListView.getCount()-1);		
		viewSwitcher.setDisplayedChild(0);
		mButtonsBar.setVisibility(View.GONE);
		pickDelay.setVisibility(View.GONE);
		text.setHint("Message " + shared_conversant_name + " about " + clueless_persons_name);
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
		getSharedWithTab(clueless_persons_number, clueless_persons_name);
		sharedWithAdapter.setCurrentTab(shared_confidante);
		initials_view.setBackgroundResource(R.drawable.circle);
		initials_view.setTypeface(null, Typeface.NORMAL);
		initials_view.setSelected(false);
		sharedWithAdapter.notifyDataSetChanged();
	}

	private void selectOriginalPerson(String original_number, String original_name) {
		if(initials_view.isSelected()) {
			popup = new SimplePopupWindow(initials_view);
			popup.showLikePopDownMenu();
			popup.setMessage(text.getHint().toString());						
		} else {
			initials_view.setBackgroundResource(R.drawable.circle_pressed);
			initials_view.setTypeface(null, Typeface.BOLD);
			initials_view.setSelected(true);
			if(isMine) originalPersonClicked(original_number,original_name);
		}
	}



	@SuppressWarnings("unchecked")
	protected void sendComment(String commentText) throws JSONException {
/*		Toast.makeText(mContext, commentText + " to " + shared_sender + " for " + clueless_persons_number, 
				Toast.LENGTH_LONG).show();
*/		mixpanel.track("send comment button pressed", null);
		if(commentText.length() == 0) {
			popup = new SimplePopupWindow(text);
			popup.showLikeQuickAction();
			popup.setMessage("There's no message here!");					
			return;
		}		
		
		hideInputs();
		text.setText("");
		
    	if(!store.isFriend(shared_conversant) && !store.wasInvited(shared_conversant) && 
    			false) {
    		
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
				clueless_persons_number, clueless_persons_name, Constants.MESSAGE_TYPE_SENT_COMMENT, store, shared_sender);
		//assume this is a conversation that was shared with us
		comment.addConfidante(shared_conversant);
		dh = new DatabaseHandler(mContext);
		
		dh.addSharedMessage(shared_convoID, comment, TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
		dh = new DatabaseHandler(mContext);
		messages_cursor = dh.getSharedMessagesCursor(shared_convoID);
		messages_adapter.swapCursor(messages_cursor);
		messages_adapter.notifyDataSetChanged();
		//mListView.setSelection(mListView.getCount()-1);
		comment.saveToParse();
		  HashMap<String, Object> params = new HashMap<String, Object>();
		  params.put("phoneNumber", ContentUtils.addCountryCode(shared_confidante));
		  ParseCloud.callFunctionInBackground("findUser", params, new FunctionCallback() {
			@Override
			public void done(Object arg0, ParseException e) {
		         if (e != null) {
		        	 	//Log.i("nothing found", "going to send a text " + ContentUtils.addCountryCode(shared_confidante));
			            SendUtils.commentViaSMS(mContext, clueless_persons_name, shared_confidante, comment);     
			         }
			}
		  });			
	}




	private void hideInputs() {
		text.setLines(2);	
		text.clearFocus();
		hideKeyboard();	
		emojiArea.setVisibility(View.GONE);
		mButtonsBar.setVisibility(View.GONE);	
		//Log.i("hide inputs","hide inputs");
		inputtingEmoji = false;
    	keyboardShowing = false;		
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
	  savedInstanceState.putString("clueless_persons_name", clueless_persons_name);    
	  savedInstanceState.putString("clueless_persons_number", clueless_persons_number); 	 	  
	  savedInstanceState.putInt("shared_convo_type", shared_convo_type); 	
	  if(sendDate!=null) savedInstanceState.putLong("scheduled_for", sendDate.getTime()); 
	//  savedInstanceState.putInt("position", mListView.getSelectedItemPosition()); 	 
	  // etc.  
	  super.onSaveInstanceState(savedInstanceState);  
	}  
	//onRestoreInstanceState  
	/*    @Override  
	public void onRestoreInstanceState(Bundle savedInstanceState) {  
	  super.onRestoreInstanceState(savedInstanceState);  
	  number = savedInstanceState.getString("number");    
	  name = savedInstanceState.getString("name");    
	  thread_id = savedInstanceState.getString("thread_id");    
	  shared_confidante = savedInstanceState.getString("shared_confidante" ); 	 
	  shared_sender = savedInstanceState.getString("shared_sender");    
	  shared_convoID = savedInstanceState.getString("shared_convoID");    
	  clueless_persons_name = savedInstanceState.getString("clueless_persons_name");    
	  clueless_persons_number = savedInstanceState.getString("clueless_persons_number"); 	 	  
	  shared_convo_type = savedInstanceState.getInt("shared_convo_type"); 	
	  if(savedInstanceState.getLong("scheduled_for")>0) 
		  sendDate = new Date(savedInstanceState.getLong("scheduled_for"));
	//  position = savedInstanceState.getInt("position"); 	
	}	*/
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
		mixpanel.track("message activity create options", null);
		getSupportMenuInflater().inflate(R.menu.thread_menu, menu);
		return true;
	}	
	

	// when a user selects a menu item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			if(outbox_loader!=null) outbox_loader.cancel(true);	
			if(mms_loader!=null) mms_loader.cancel(true);	
            NavUtils.navigateUpFromSameTask(this);
            return true;	
		case R.id.menu_contacts:
			mixpanel.track("add to contacts", null);
            intent = new Intent(Intent.ACTION_INSERT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            if(number!=null) intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
            else intent.putExtra(ContactsContract.Intents.Insert.PHONE, shared_conversant);
            startActivity(intent);            	
            return true;	
		case R.id.menu_call:
			 String uri = "tel:" + number ;
			 intent = new Intent(Intent.ACTION_CALL);
			 intent.setData(Uri.parse(uri));
			 startActivity(intent);        	
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
		
		if(keyboardShowing || emojiArea.getVisibility()==View.VISIBLE) {
			hideInputs();
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
		addDelayedMessage(scheduledOn);
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
		boolean isMMS = false;
		if(isGroupMessage) isMMS = true;
		newMessage = text.getText().toString().trim(); 
		text.setText("");	
		hideInputs();
		mixpanel.track("send message click", null);
		//if(mListView.getCount()>0) mListView.setSelection(mListView.getCount()-1);
		
		if(newMessage.length() == 0) {
			popup = new SimplePopupWindow(v);
			popup.showLikeQuickAction();
			popup.setMessage("There's no message here!");					
			return;
		}		

		if(numbers == null) {
			numbers = mRecipientEditor.constructContactsFromInput(false).getToNumbers();
			if(numbers.length == 0) {
				popup = new SimplePopupWindow(v);
				popup.showLikeQuickAction();
				popup.setMessage("Type someone to send to!");
				return;
			}
			if(numbers.length>1) isMMS = true;
			number = ContentUtils.addCountryCode(numbers[0]);	
		}			
      
        if(names == null) {
        	names = store.getNames(numbers);
        	name = names[0];
        	//name = ContentUtils.getContactDisplayNameByNumber(mContext, number);
        	topViewSwitcher.setDisplayedChild(0);
        	populateMessages();
        	rePopulateMessages();
        	Intent intent = new Intent();
        	intent.putExtra(Constants.EXTRA_NUMBERS, numbers);
        	intent.putExtra(Constants.EXTRA_NAMES, names);
        	setIntent(intent);
        }
        
		//sendMMS();
        if(!isMMS) 
        	new DelayedSend(mContext, number, newMessage, sendDate, new Date().getTime(), approver).start();
        else {
        	DelayedSend d = new DelayedSend(mContext, numbers, newMessage, sendDate, new Date().getTime(), approver);
        	d.setThreadID(thread_id);
        	d.start();
        	//new SendMMS(mContext, numbers, newMessage, new Date().getTime(), new Date().getTime(), true).run();

        }
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
		//mListView.setSelection(mListView.getCount()-1);
	}
	
	public void removeMessage() {
		queue_adapter.delayedMessages.clear();
		messages.clear();
		outbox_loader = new GetOutboxMessages();
		outbox_loader.execute(); 
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
			popup.setMessage("First enter a contact in the 'Share with' box below");	
			return;
		}
		if(mConfidantesEditor.constructContactsFromInput(false).getNumbers().length>0) 
			confidantes = mConfidantesEditor.constructContactsFromInput(false).getToNumbers();
		//hashtags_string = hashtag.getText().toString().trim(); 
		
		if(confidantes==null || confidantes.length == 0) {
			popup = new SimplePopupWindow(v);
			popup.showLikeQuickAction();
			popup.setMessage("First enter a contact in the 'Share with' box below");	
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
		graphViewSwitcher.setDisplayedChild(0);
		
		shared_sender = ParseUser.getCurrentUser().getUsername();
		clueless_persons_name = names[0];
		clueless_persons_number = numbers[0];
		shared_convo_type = TextBasedSmsColumns.MESSAGE_TYPE_SENT;	
		addPerson.setBackgroundResource(R.drawable.add);
		addPerson.setSelected(false);
		graphButton.setBackgroundResource(R.drawable.graph);
		graphButton.setSelected(false);				
		//mListView.setSelection(mListView.getCount()-1);		        
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
			shared_convoID = shared_sender+clueless_persons_number+shared_confidante;
			//Log.i("convoid from sharemessages function", shared_convoID);
			new ShareMessages(mContext, shared_confidante, 
					name, number,  messagesHash).start();	  
			populateSharedMessages(shared_convoID);
		}	
		getSharedWithTab(number,name);
		sharedWithAdapter.setCurrentTab(shared_confidante);
		
		name = null;
		number = null;
		confidantes = null;			
		isPopulated = false;
		
		Intent intent = new Intent();
		intent.putExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, shared_convoID);
		intent.putExtra(Constants.EXTRA_CLUELESS_PERSONS_NAME, clueless_persons_name);
		intent.putExtra(Constants.EXTRA_SHARED_SENDER, shared_sender);
		intent.putExtra(Constants.EXTRA_CLUELESS_PERSONS_NUMBER, clueless_persons_number);
		intent.putExtra(Constants.EXTRA_SHARED_CONFIDANTE, shared_confidante);		
		intent.putExtra(Constants.EXTRA_SHARED_CONVO_TYPE, shared_convo_type);			
		setIntent(intent);
		messages_adapter.check_hash.clear();
		messages_adapter.showCheckboxes = false;	
		messages_adapter.notifyDataSetChanged();
		mConfidantesEditor.setText("");
		
		share.setClickable(true);	
		sharedWithAdapter.notifyDataSetChanged();
		hideInputs();
		
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
	  		if(number==null) return null;
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
			if(pending_messages_cursor!=null) pending_messages_cursor.close();
			if(dh!=null) dh.close();
	    }			

	}			
	
	private class GetMMSMessages extends AsyncTask<Void,MMSMessage,Void> {
		
	  	Cursor mms_cursor;
	  	MMSMessage m;
	  	@Override
		protected Void doInBackground(Void... params) {

	        mms_cursor = ContentUtils.getMMSCursor(mContext, thread_id);

	        if(mms_cursor!=null && mms_cursor.moveToFirst()) {
	        	hasMMS = true;
	        	m = getNextMMSMessage(mms_cursor);
	        	publishProgress(m);	
	        	while(mms_cursor.moveToNext()) {
	        		m = getNextMMSMessage(mms_cursor);
	        		publishProgress(m);	
	        		if (isCancelled()) break;
	        	}	     
	        }	  
			return null;
		}
		@Override
	    protected void onProgressUpdate(MMSMessage... t) {
			messages_adapter.insert(t[0]);
			messages_adapter.notifyDataSetChanged();
	    }				
		
		@Override
	    protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mms_cursor.close();
			messages_adapter.notifyDataSetChanged();
			mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
			//mListView.setSelection(messages_adapter.getCount()-1);
	    }			

	}			
	private void hideKeyboard(){
		imm.hideSoftInputFromWindow(text.getWindowToken(), 0);	
		mButtonsBar.setVisibility(View.GONE);
		inputtingEmoji = false;
		keyboardShowing = false;
	}	




	public MMSMessage getNextMMSMessage(Cursor mCursor) {
		String mmsId = mCursor.getString(mCursor.getColumnIndexOrThrow(Telephony.BaseMmsColumns._ID));
		long date = 1000 * mCursor.getLong(mCursor.getColumnIndexOrThrow(Telephony.BaseMmsColumns.DATE));
		int m_type = mCursor.getInt(mCursor.getColumnIndex(Telephony.BaseMmsColumns.MESSAGE_BOX));
		String address = getAddressNumber(Integer.parseInt(mmsId));
		String read = mCursor.getString(mCursor.getColumnIndex(Telephony.BaseMmsColumns.READ));
    	if(!mmsId.equals("") && read.equals("0")) {
        	ContentValues values = new ContentValues();
    		values.put(Telephony.BaseMmsColumns.READ,true);
    		mContext.getContentResolver().update(Telephony.Mms.CONTENT_URI,
    				values, Telephony.BaseMmsColumns._ID+"="+mmsId, null);	
    	}			
		MMSMessage m = new MMSMessage(date, "", address, store.getName(address), m_type, 
				store, ParseUser.getCurrentUser().getUsername());
		String selectionPart = "mid=" + mmsId;
		Uri uri = Uri.parse("content://mms/part");
		Cursor cursor = mContext.getContentResolver().query(uri, null,
		    selectionPart, null, null);
		if (cursor.moveToFirst()) {
		    do {
		        String partId = cursor.getString(cursor.getColumnIndex("_id"));
		        String type = cursor.getString(cursor.getColumnIndex("ct"));
		        if ("text/plain".equals(type)) {
		            String data = cursor.getString(cursor.getColumnIndex("_data"));
		            String body = "";
		            if (data != null) {
		                // implementation of this method below
		                body = getMmsText(partId);
		            } else {
		                body = cursor.getString(cursor.getColumnIndex("text"));
		            }
		           // Log.i("body",body);
		           if(body!=null) m.setMessage(body);
		        }
		        else if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
		                "image/gif".equals(type) || "image/jpg".equals(type) ||
		                "image/png".equals(type)) {
		            Bitmap bitmap = getMmsImage(partId);
		            if(bitmap!=null) m.addImage(bitmap);
		        }		        
		    } while (cursor.moveToNext());
		}

		return m;
	}


	private String getAddressNumber(int id) {
	//Log.i("getaddressnumber id",""+id);
	    String selectionAdd = new String("msg_id=" + id);
	    String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
	    Uri uriAddress = Uri.parse(uriStr);
	    Cursor cAdd = mContext.getContentResolver().query(uriAddress, null,
	        selectionAdd, null, null);
	    String name = null;
	    if (cAdd!=null && cAdd.moveToFirst()) {
	        do {
	            String number = cAdd.getString(cAdd.getColumnIndex("address"));
	            if (number != null) {
	                try {
	                    Long.parseLong(number.replace("-", ""));
	                    name = number;
	                } catch (NumberFormatException nfe) {
	                    if (name == null) {
	                        name = number;
	                    }
	                }
	            }
	        } while (cAdd.moveToNext());
	    }
	    if (cAdd != null) {
	        cAdd.close();
	    }
	    return name;
	}	
	private Bitmap getMmsImage(String _id) {
	    Uri partURI = Uri.parse("content://mms/part/" + _id);
	    InputStream is = null;
	    Bitmap bitmap = null;
	    try {
	        is = mContext.getContentResolver().openInputStream(partURI);
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inSampleSize = 8;
	        bitmap = BitmapFactory.decodeStream(is,null, options);
	    } catch (IOException e) {}
	    finally {
	        if (is != null) {
	            try {
	                is.close();
	            } catch (IOException e) {}
	        }
	    }
	    return bitmap;
	}	
	private String getMmsText(String id) {
	    Uri partURI = Uri.parse("content://mms/part/" + id);
	    InputStream is = null;
	    StringBuilder sb = new StringBuilder();
	    try {
	        is = mContext.getContentResolver().openInputStream(partURI);
	        if (is != null) {
	            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
	            BufferedReader reader = new BufferedReader(isr);
	            String temp = reader.readLine();
	            while (temp != null) {
	                sb.append(temp);
	                temp = reader.readLine();
	            }
	        }
	    } catch (IOException e) {}
	    finally {
	        if (is != null) {
	            try {
	                is.close();
	            } catch (IOException e) {}
	        }
	    }
	    return sb.toString();
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
/**		mixpanel.track("MessageActivity Showcaseview next button", null);
        switch (counter) {
        case 1:
        	showcaseView.hide();
            break;
        case 0:
            showcaseView.setShowcase(new ViewTarget(findViewById(R.id.graph_button)), true);
            showcaseView.setContentTitle("Interest graphs");
            showcaseView.setContentText(
            		"Graphs that show the other person's interest relative to yours");  
            showcaseView.setButtonText("Finish");
            break;
	    }
	    counter++;**/
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
