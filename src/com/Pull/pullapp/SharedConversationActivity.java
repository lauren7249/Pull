package com.Pull.pullapp;

import java.util.ArrayList;
import java.util.List;

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.DatabaseHandler;
import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
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
	private int sharedConversationId;
	private EditText sharedConversationCommentEditText;
	private Button sharedConversationCommentSendButton;
	private SharedConversationCommentListAdapter sharedConversationCommentListAdapter;
	private View separatorView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		sharedConversationId =  i.getIntExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, -1); 
		
		if(sharedConversationId==-1){
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
					Comment c = new Comment(text, "placeholder", System.currentTimeMillis());
					DatabaseHandler db = new DatabaseHandler(mContext);
					db.addComment(sharedConversationId, c);
					ArrayList<Comment> commentList = db.getComments(sharedConversationId);
					sharedConversation.setComments(commentList);
					sharedConversationCommentListAdapter.setItemList(commentList);
					sharedConversationCommentEditText.setText("");
					if(commentList.size()>0) sharedConversationCommentListView.setSelection(commentList.size()-1);
				}
			}
		});
	    
	    sharedConversation = dbHandler.getSharedConversation(sharedConversationId);
	    sharedConversationMessageListAdapter = new SharedConversationMessageListAdapter(mContext, sharedConversation.getMessages());
	    sharedConversationListView.setAdapter(sharedConversationMessageListAdapter);
	    
	    sharedConversationCommentListAdapter = new SharedConversationCommentListAdapter(mContext, sharedConversation.getComments());
	    sharedConversationCommentListView.setAdapter(sharedConversationCommentListAdapter);

	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		sharedConversation = dbHandler.getSharedConversation(sharedConversationId);
		sharedConversationMessageListAdapter.setItemList(sharedConversation.getMessages());
		sharedConversationCommentListAdapter.setItemList(sharedConversation.getComments());
		
	}
	

}
