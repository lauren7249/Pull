package com.Pull.pullapp;

import java.util.List;

import com.Pull.pullapp.model.SharedConversation;
import com.Pull.pullapp.util.DatabaseHandler;
import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class SharedConversationActivity extends SherlockActivity {
	
	
	private Context mContext;
	private LinearLayout mLayout;
	private ListView sharedConversationListView;
	private ListView sharedConversationCommentListView;
	private DatabaseHandler dbHandler;
	private SharedConversation sharedConversationList;
	private SharedConversationMessageAdapter sharedConversationListAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shared_conversation_thread_activity);
		mContext = getApplicationContext();
		mLayout = (LinearLayout) findViewById(R.id.main_layout);
		dbHandler = new DatabaseHandler(mContext);
	    sharedConversationListView = (ListView) findViewById(R.id.shared_conversation_list_view);
	    sharedConversationCommentListView = (ListView) findViewById(R.id.shared_conversation_comment_list_view);
	    sharedConversationList = dbHandler.getSharedConversation(0);
//	    sharedConversationListAdapter = new SharedConversationMessageAdapter(mContext, sharedConversationList);
//	    sharedConversationListview.setAdapter(sharedConversationListAdapter);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
//		sharedConversationList = dbHandler.getSharedConversation(0);
//	    adapter.setItemList(sharedConversationList);
//	    adapter.notifyDataSetChanged();
	}
	

}
