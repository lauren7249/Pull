package com.Pull.pullapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
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
		String name="", number, snippet,threadID,recipientId;
		boolean read;
		
    	threadID = threads.getString(0);
    	read = (!threads.getString(1).equals("0"));	
    	recipientId = threads.getString(2);
    	snippet = threads.getString(4);		    		    	
    	 

		number = ContentUtils.getAddressFromID(context, recipientId);
		Log.i("recipientId",recipientId);
		name = ContentUtils.getContactDisplayNameByNumber(context, number);

		TextView name_view = (TextView) v.findViewById(R.id.txt_title);
 	   	TextView snippet_view = (TextView) v.findViewById(R.id.txt_message_info);

 	   	if(name!=null) name_view.setText(name);
 	   	if(snippet!=null) snippet_view.setText(snippet);
 	   	if(!read) v.setBackgroundResource(R.drawable.unread_row);
	
	}

	@Override
	public View newView(Context context, Cursor threads, ViewGroup parent) {
		String name="", number, snippet,threadID,recipientId;
		boolean read;
		
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.message_list_item, parent, false);

    	threadID = threads.getString(0);
    	read = (!threads.getString(1).equals("0"));	 
    	recipientId = threads.getString(2);
    	snippet = threads.getString(4);		    		    	
    	    	
		
		number = ContentUtils.getAddressFromID(context, recipientId);
		Log.i("recipientId",recipientId);
    	name = ContentUtils.getContactDisplayNameByNumber(context, number);
		TextView name_view = (TextView) v.findViewById(R.id.txt_title);
		TextView snippet_view = (TextView) v.findViewById(R.id.txt_message_info);

		if(name!=null) name_view.setText(name);
		if(snippet!=null) snippet_view.setText(snippet);
		if(!read) v.setBackgroundResource(R.drawable.unread_row);  
		return v;
	}


}
