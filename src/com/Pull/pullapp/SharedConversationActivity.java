package com.Pull.pullapp;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.Pull.pullapp.util.SendMessages;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class SharedConversationActivity extends SherlockActivity {
	
	
	private Context mContext;
	private LinearLayout mLayout;
	private ListView sharedConversationListView;
	private ListView sharedConversationCommentListView;
	private DatabaseHandler dbHandler;
	private SharedConversation sharedConversation;
	private SharedConversationMessageListAdapter sharedConversationMessageListAdapter;
	private String sharedConversationId, confidanteName;
	private EditText sharedConversationCommentEditText;
	private Button sharedConversationCommentSendButton;
	private SharedConversationCommentListAdapter sharedConversationCommentListAdapter;
	private View separatorView;
	private Handler mHandler = new Handler(); 
	private ArrayList<Comment> commentList = new ArrayList<Comment>();
	private BroadcastReceiver tickReceiver;
	private boolean isEmpty;
	private String commentText;
	private TextView hint;
	protected String recipient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		sharedConversationId =  i.getStringExtra(Constants.EXTRA_SHARED_CONVERSATION_ID); 
		
		if(sharedConversationId.equals("")){
			finish();
			return;
		}

		setContentView(R.layout.shared_conversation_thread_activity);
		isEmpty = true;
		mContext = getApplicationContext();
		mLayout = (LinearLayout) findViewById(R.id.main_layout);
		dbHandler = new DatabaseHandler(mContext);
	    sharedConversationListView = (ListView) findViewById(R.id.shared_conversation_list_view);
	    separatorView = findViewById(R.id.separator);
	    sharedConversationCommentListView = (ListView) findViewById(R.id.shared_conversation_comment_list_view);
	    sharedConversationCommentEditText = (EditText) findViewById(R.id.shared_conversation_comment_edit_text);
	    sharedConversationCommentSendButton = (Button) findViewById(R.id.shared_conversation_comment_send_button);
	    hint = (TextView) findViewById(R.id.hint);
	    
	    sharedConversation = dbHandler.getSharedConversation(sharedConversationId);
	    if (sharedConversation.getType()==TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
	    	recipient = sharedConversation.getConfidante();
	    } else {
	    	//recipient = sharedConversation.getSharer();
	    }
	    
	    // Dirty Hack to detect keyboard
		mLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				if ((mLayout.getRootView().getHeight() - mLayout.getHeight()) > mLayout.getRootView().getHeight() / 3) {
					sharedConversationListView.setVisibility(View.GONE);
					separatorView.setVisibility(View.GONE);
				} else {
					sharedConversationListView.setVisibility(View.VISIBLE);
					separatorView.setVisibility(View.VISIBLE);
				}
			}
		});
		
		sharedConversationCommentSendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				commentText = sharedConversationCommentEditText.getText().toString().trim();
				if(commentText.length()>0){
					if(isEmpty) {
						isEmpty = false;
						hint.setVisibility(View.GONE);
						sharedConversationCommentListView.setVisibility(View.VISIBLE);
					}
					Comment c = new Comment(commentText, "Me", System.currentTimeMillis());
					DatabaseHandler db = new DatabaseHandler(mContext);
					db.addComment(sharedConversationId, c);
					commentList = db.getComments(sharedConversationId);
					sharedConversation.setComments(commentList);
					sharedConversationCommentListAdapter.setItemList(commentList);
					sharedConversationCommentListView.invalidateViews();
					sharedConversationCommentListView.refreshDrawableState();					
					sharedConversationCommentEditText.setText("");
					hideKeyboard();
					sharedConversationCommentEditText.clearFocus();
					if(commentList.size()>0) sharedConversationCommentListView.setSelection(commentList.size()-1);
					
					
					checkParseUser(recipient);
//					long delay = (long)(Math.random() * 4000 + 1000);
//					mHandler.postDelayed(mFakeReplayTask, delay);
					
				}
			}
		});
	    
	    sharedConversationMessageListAdapter = new SharedConversationMessageListAdapter(mContext, sharedConversation.getMessages());
	    sharedConversationListView.setAdapter(sharedConversationMessageListAdapter);
	    
	    sharedConversationCommentListAdapter = new SharedConversationCommentListAdapter(mContext, sharedConversation.getComments(), sharedConversation.getConfidante());
	    sharedConversationCommentListView.setAdapter(sharedConversationCommentListAdapter);
	    
	    confidanteName = ContentUtils.getContactDisplayNameByNumber(mContext,sharedConversation.getConfidante());
	    sharedConversationCommentEditText.setHint("Write to " + confidanteName);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
		tickReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateTime();
			}
		};

	}
	
	
	private void hideKeyboard(){
		InputMethodManager inputManager = (InputMethodManager) this
	            .getSystemService(Context.INPUT_METHOD_SERVICE);
	    View v=getCurrentFocus();
	    if(v==null)return;
	    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	private void updateTime(){
		sharedConversationMessageListAdapter.notifyDataSetChanged();
		sharedConversationCommentListAdapter.notifyDataSetChanged();
	}
	
	
   private Runnable mFakeReplayTask = new Runnable() {
       public void run() {
    	   DatabaseHandler db = new DatabaseHandler(mContext);
			commentList = db.getComments(sharedConversationId);
			String reply;
			if(commentList.get(commentList.size()-1).getMessage().trim().endsWith("?")){
				reply = "I don't know, what do you think?";
			}else{
				reply = "Very true!!!";
			}
			Comment c = new Comment(reply, sharedConversation.getConfidante(), System.currentTimeMillis());
			db.addComment(sharedConversationId, c);
			commentList.add(c);
			sharedConversation.setComments(commentList);
			sharedConversationCommentListAdapter.setItemList(commentList);
			sharedConversationCommentEditText.setText("");
			if(commentList.size()>0) sharedConversationCommentListView.setSelection(commentList.size()-1);
       }
    };



	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		sharedConversation = dbHandler.getSharedConversation(sharedConversationId);
		commentList = dbHandler.getComments(sharedConversationId);
		if(commentList.size()>0) {
			isEmpty = false;
			hint.setVisibility(View.GONE);
			sharedConversationCommentListView.setVisibility(View.VISIBLE);			
		}
		sharedConversation.setComments(commentList);
		sharedConversationCommentListAdapter.setItemList(commentList);
		sharedConversationCommentListView.invalidateViews();
		sharedConversationCommentListView.refreshDrawableState();		
				
		sharedConversationMessageListAdapter.setItemList(sharedConversation.getMessages());
		this.setTitle("Convo with " + confidanteName + " about " + 
		ContentUtils.getContactDisplayNameByNumber(mContext,sharedConversation.getOriginalRecipient()) );
		registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		if(isEmpty) {
			hint.setText("Write a comment to " + confidanteName);
			hint.setVisibility(View.VISIBLE);
			sharedConversationCommentListView.setVisibility(View.GONE);
		}

	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(tickReceiver);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        default:
        	return false;
		}
	}
	
	
	private void checkParseUser(final String confidante) {
    	ParseQuery<ParseUser> query = ParseUser.getQuery();
    	query.whereEqualTo("username", ContentUtils.addCountryCode(confidante));
    	query.findInBackground(new FindCallback<ParseUser>() {
    	  public void done(List<ParseUser> objects, ParseException e) {
    	    if (e == null && objects.size()>0) {
    	    } else {
    	        commentViaSMS(confidante);
    	    }
    	  }
    	});
	}

	protected void commentViaSMS(String recipient) {
		SendMessages.sendsms(mContext, recipient, commentText, 0, false);
	}	

}
