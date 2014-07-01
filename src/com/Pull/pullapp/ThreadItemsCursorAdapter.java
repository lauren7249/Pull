package com.Pull.pullapp;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.widget.CursorAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Pull.pullapp.model.FacebookUser;
import com.Pull.pullapp.util.ContentUtils;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
public class ThreadItemsCursorAdapter extends CursorAdapter {
	
	private SharedPreferences mPrefs_recipientID_phoneNumber, mPrefs_phoneNumber_Name, 
					mPrefs_phoneNumber_FacebookID;
	public HashMap<Integer,String> recipientID_hash;
	private int cursorType;
	
    @SuppressWarnings("deprecation")
	public ThreadItemsCursorAdapter(Context context, Cursor cursor, int cursorType) {
    	super(context, cursor);
    	mPrefs_recipientID_phoneNumber = context.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "recipientId_phoneNumber",Context.MODE_PRIVATE);
    	mPrefs_phoneNumber_Name = context.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "phoneNumber_Name",Context.MODE_PRIVATE);
    	mPrefs_phoneNumber_FacebookID = context.getSharedPreferences(ThreadItemsCursorAdapter.class.getSimpleName() 
    			+ "phoneNumber_FacebookID",Context.MODE_PRIVATE); 	
    	recipientID_hash = new HashMap<Integer,String>();
    	this.cursorType = cursorType;
    }

	@Override
	public void bindView(View v, Context context, Cursor threads) {
		populateFields(v, context, threads);
		
	}

	@Override
	public View newView(Context context, Cursor threads, ViewGroup parent) {
		
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.list_item_thread, parent, false);
        
        populateFields(v, context, threads);
		return v;
	}

	private void populateFields(View v, Context context, Cursor threads) {
		switch(cursorType){
		case R.id.my_conversation_tab:
			setTextMessageFields(context,threads,v);
			return;
		default:
			setSharedFields(context,threads,v);
			return;
		}
	}
	private void setTextMessageFields(Context context, Cursor threads, View v) {

		TextView name_view = (TextView) v.findViewById(R.id.txt_title);
		TextView snippet_view = (TextView) v.findViewById(R.id.txt_message_info);
		ImageView read_indicator = (ImageView) v.findViewById(R.id.indicator);
		
		String name="", snippet="", recipientId="",number="";
		boolean read = true;        
		
        final int position= threads.getPosition();
        
    	read = (!threads.getString(1).equals("0"));	 
    	recipientId = threads.getString(2);
    	snippet = threads.getString(4);		    		    	
		
    	recipientID_hash.put(position, recipientId);
    	
    	number = mPrefs_recipientID_phoneNumber.getString(recipientId, null);
    	if(number==null) {
			number = ContentUtils.getAddressFromID(context, recipientId);
			Editor editor = mPrefs_recipientID_phoneNumber.edit();
			editor.putString(recipientId, number);
			editor.commit();
    	}		
    	name = mPrefs_phoneNumber_Name.getString(number, null);
    	if(name==null || name.length() == 0 || name.equals(number)) {
    		name = ContentUtils.getContactDisplayNameByNumber(context, number);
			Editor editor = mPrefs_phoneNumber_Name.edit();
			editor.putString(number, name);
			editor.commit();
    	}	
    	
		name_view.setText(name);
		snippet_view.setText(snippet);
		if(!read) {
			read_indicator.setVisibility(View.VISIBLE);
		} else {
			read_indicator.setVisibility(View.GONE);
		}  
		
	}

	private void setSharedFields(Context context, Cursor threads, View v) {
		LinearLayout row = (LinearLayout) v.findViewById(R.id.row);
		TextView name_view = (TextView) v.findViewById(R.id.txt_title);
		TextView snippet_view = (TextView) v.findViewById(R.id.txt_message_info);
		ImageView read_indicator = (ImageView) v.findViewById(R.id.indicator);
	    final ProfilePictureView their_pic = 
	    		(ProfilePictureView) v.findViewById(R.id.profile_pic);
	    final ProfilePictureView my_pic = 
	    		(ProfilePictureView) v.findViewById(R.id.profile_pic_mine);
	    
	    
	    final String number;
		String name="", facebookID="";
    	String confidante = threads.getString(0);
    	String originalRecipientName = threads.getString(1);
    	String sharer = threads.getString(2);	
    	int type = Integer.valueOf(threads.getString(4));

    	if(type == TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
    		number = confidante;
    		their_pic.setVisibility(View.GONE);
    		my_pic.setVisibility(View.VISIBLE);
    		row.setGravity(Gravity.RIGHT);
    		name_view.setGravity(Gravity.RIGHT);
    		snippet_view.setGravity(Gravity.RIGHT);
    		snippet_view.setText(originalRecipientName + " doesn't know you've shared");
    		
    	} else {
    		number = sharer;
    		my_pic.setVisibility(View.GONE);
    		their_pic.setVisibility(View.VISIBLE);  
    		name_view.setGravity(Gravity.LEFT);
    		snippet_view.setGravity(Gravity.LEFT);    		
    		snippet_view.setText(originalRecipientName + " doesn't know " 
    		+ ContentUtils.getContactDisplayNameByNumber(context, number) + " shared");
    	}
    	
    	
    	
    	name = mPrefs_phoneNumber_Name.getString(number, "");
    	if(name.length()==0 || name.equals(number)) {
    		name = ContentUtils.getContactDisplayNameByNumber(context, number);
			Editor editor = mPrefs_phoneNumber_Name.edit();
			editor.putString(number, name);
			editor.commit();
    	}		
    	
    	name_view.setText(name);
    	
    	facebookID = mPrefs_phoneNumber_FacebookID.getString(number, "");
    	if(facebookID.length()==0) {
    		ParseQuery<FacebookUser> fbQuery = ParseQuery.getQuery("FacebookUser");
    		fbQuery.whereEqualTo("phoneNumber", number);
    		fbQuery.findInBackground(new FindCallback<FacebookUser>() {
    		public void done(List<FacebookUser> results, ParseException e) {
    			if(e==null && results.size()>0) {
    				if(results.get(0).getString("facebookID")!=null && 
    						!results.get(0).getString("facebookID").equals("")) {
    					Editor editor = mPrefs_phoneNumber_FacebookID.edit();
    					editor.putString(number, results.get(0).getString("facebookID"));
    					editor.commit();     
    				}
    			}
    		  }
    		});  

    	}    	
    	facebookID = mPrefs_phoneNumber_FacebookID.getString(number, "");
    	if(type == TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
	    	if(facebookID!=null && facebookID.length()>0) my_pic.setProfileId(facebookID);
	    	else my_pic.setProfileId("0");
    	} else {
	    	if(facebookID!=null && facebookID.length()>0) their_pic.setProfileId(facebookID);
	    	else their_pic.setProfileId("0");    		
    	}
    	
    	//TODO: ADD DATA TO SHOW IF IT IS READ OR NOT
    	read_indicator.setVisibility(View.GONE);
    	
	}
}
