package com.Pull.pullapp;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony.ThreadsColumns;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.Pull.pullapp.util.ContentUtils;
public class ThreadItemsCursorAdapter extends CursorAdapter {
	
    @SuppressWarnings("deprecation")
	public ThreadItemsCursorAdapter(Context context, Cursor cursor) {
    	super(context, cursor);
    }

	@Override
	public void bindView(View v, Context context, Cursor threads) {
    	String threadID = threads.getString(threads
	  		      .getColumnIndex(ThreadsColumns._ID));
    	if(threadID.length()==0) return;
    	String snippet = threads.getString(threads
	  		      .getColumnIndex(ThreadsColumns.SNIPPET));			    	

    	boolean read = (!threads.getString(threads
	  		      .getColumnIndex(ThreadsColumns.READ)).equals("0"));	    	
		String[] recipientIDs = threads.getString(threads
	      .getColumnIndex(ThreadsColumns.RECIPIENT_IDS)).split(" ");
		if(recipientIDs.length == 0) return;
		String recipientId = recipientIDs[0];
		String number = ContentUtils.getAddressFromID(context, recipientId);
		String name = ContentUtils
				.getContactDisplayNameByNumber(context, number);

 	   TextView name_view = (TextView) v.findViewById(R.id.txt_title);
 	   TextView snippet_view = (TextView) v.findViewById(R.id.txt_message_info);

 	   if(name!=null) name_view.setText(name);
 	   if(snippet!=null) snippet_view.setText(snippet);
 	   if(!read) v.setBackgroundResource(R.drawable.unread_row);
 	   else v.setBackgroundResource(R.drawable.read_row);		
	}

	@Override
	public View newView(Context context, Cursor threads, ViewGroup parent) {
		
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.message_list_item, parent, false);

    	String threadID = threads.getString(threads
	  		      .getColumnIndex(ThreadsColumns._ID));
    	if(threadID.length()==0) return v;
  		String snippet = threads.getString(threads
	  		      .getColumnIndex(ThreadsColumns.SNIPPET));			    	

  		boolean read = (!threads.getString(threads
	  		      .getColumnIndex(ThreadsColumns.READ)).equals("0"));	    	
		String[] recipientIDs = threads.getString(threads
	      .getColumnIndex(ThreadsColumns.RECIPIENT_IDS)).split(" ");
		if(recipientIDs.length == 0) return v;
		String recipientId = recipientIDs[0];
		String number = ContentUtils.getAddressFromID(context, recipientId);
		String name = ContentUtils
				.getContactDisplayNameByNumber(context, number);

	   TextView name_view = (TextView) v.findViewById(R.id.txt_title);
	   TextView snippet_view = (TextView) v.findViewById(R.id.txt_message_info);

	   if(name!=null) name_view.setText(name);
	   if(snippet!=null) snippet_view.setText(snippet);
	   if(!read) v.setBackgroundResource(R.drawable.unread_row);
	   else v.setBackgroundResource(R.drawable.read_row);	        
	   return v;
	}    
}
