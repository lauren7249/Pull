package com.Pull.pullapp;

import java.util.ArrayList;
import java.util.List;

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.DatabaseHandler;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class SharedConversationActivity extends SherlockActivity {
	
	
	private Context mContext;
	private LinearLayout mLayout;
	private ListView sharedConversationListView;
	private ListView sharedConversationCommentListView;
	private DatabaseHandler dbHandler;
	private SharedConversation sharedConversation;
	private SharedConversationMessageListAdapter sharedConversationMessageListAdapter;
	private String sharedConversationId;
	private EditText sharedConversationCommentEditText;
	private Button sharedConversationCommentSendButton;
	private SharedConversationCommentListAdapter sharedConversationCommentListAdapter;
	private View separatorView;
	private Handler mHandler = new Handler(); 
	
	private BroadcastReceiver tickReceiver;
	

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
		
		mContext = getApplicationContext();
		mLayout = (LinearLayout) findViewById(R.id.main_layout);
		dbHandler = new DatabaseHandler(mContext);
	    sharedConversationListView = (ListView) findViewById(R.id.shared_conversation_list_view);
	    separatorView = findViewById(R.id.separator);
	    sharedConversationCommentListView = (ListView) findViewById(R.id.shared_conversation_comment_list_view);
	    sharedConversationCommentEditText = (EditText) findViewById(R.id.shared_conversation_comment_edit_text);
	    sharedConversationCommentSendButton = (Button) findViewById(R.id.shared_conversation_comment_send_button);
	    
	    
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
				String text = sharedConversationCommentEditText.getText().toString().trim();
				if(text.length()>0){
					Comment c = new Comment(text, "Me", System.currentTimeMillis());
					DatabaseHandler db = new DatabaseHandler(mContext);
					db.addComment(sharedConversationId, c);
					ArrayList<Comment> commentList = db.getComments(sharedConversationId);
					sharedConversation.setComments(commentList);
					sharedConversationCommentListAdapter.setItemList(commentList);
					sharedConversationCommentEditText.setText("");
					hideKeyboard();
					sharedConversationCommentEditText.clearFocus();
					if(commentList.size()>0) sharedConversationCommentListView.setSelection(commentList.size()-1);
					long delay = (long)(Math.random() * 4000 + 1000);
					mHandler.postDelayed(mFakeReplayTask, delay);
					
				}
			}
		});
	    
	    sharedConversation = dbHandler.getSharedConversation(sharedConversationId);
	    sharedConversationMessageListAdapter = new SharedConversationMessageListAdapter(mContext, sharedConversation.getMessages());
	    sharedConversationListView.setAdapter(sharedConversationMessageListAdapter);
	    
	    sharedConversationCommentListAdapter = new SharedConversationCommentListAdapter(mContext, sharedConversation.getComments(), sharedConversation.getConfidante());
	    sharedConversationCommentListView.setAdapter(sharedConversationCommentListAdapter);
	    
	    
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
			ArrayList<Comment> commentList = db.getComments(sharedConversationId);
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
		sharedConversationMessageListAdapter.setItemList(sharedConversation.getMessages());
		sharedConversationCommentListAdapter.setItemList(sharedConversation.getComments());
		this.setTitle("Convo with " + 
		ContentUtils.getContactDisplayNameByNumber(mContext,sharedConversation.getConfidante()) 
		+ " about " + 
		ContentUtils.getContactDisplayNameByNumber(mContext,sharedConversation.getOriginalRecipient()) );
		registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

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
	
	
	

}
