package com.Pull.pullapp.threads;

import android.content.Context;
import android.database.Cursor;

import com.Pull.pullapp.adapter.MessageCursorAdapter;
import com.Pull.pullapp.util.data.ContentUtils;
import com.Pull.pullapp.util.data.UserInfoStore;

public class UploadConvo extends Thread {

	private UserInfoStore store;
	private Context mContext;
	private String thread_id;
	private String number;
	private boolean hasMMS;
	private MessageCursorAdapter messages_adapter;
    

    public UploadConvo(UserInfoStore store, Context context,
			String thread_id, String number,boolean hasMMS, MessageCursorAdapter messages_adapter) {
    	this.store = store;
    	this.mContext = context;
    	this.thread_id = thread_id;
    	this.number = number;
    	this.hasMMS = hasMMS;
    	this.messages_adapter = messages_adapter;
	}

	@Override
    public void run() {
		Cursor messages_cursor = ContentUtils.getMessagesCursor(mContext,thread_id, number, hasMMS);
		if(messages_cursor==null) return;
   	 			
   	 	int num = messages_cursor.getCount();
	   	//String owner = ParseUser.getCurrentUser().getUsername();			
	   	for (int i=0; i<num; i++) {
	   		//int k = messages_cursor.getPosition();
	   		//if(k==-1) k = num-1;
	   		messages_cursor.moveToPosition(i);
	   		messages_adapter.populateTextConvo(mContext, messages_cursor, null, true);
	   		//messages_cursor.moveToPosition(k);
	   		try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	   	store.setConvoUploaded(number);			
	      
	    }



}
