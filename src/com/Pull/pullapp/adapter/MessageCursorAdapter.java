package com.Pull.pullapp.adapter;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.Pull.pullapp.R;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;
import com.Pull.pullapp.util.SendUtils;
import com.Pull.pullapp.util.UserInfoStore;
import com.parse.ParsePush;
import com.parse.ParseUser;

public class MessageCursorAdapter extends CursorAdapter {
	
	public boolean showCheckboxes;
	public TreeSet<SMSMessage> check_hash;
	public HashMap<Long,Integer> delayedMessages;
	private String other_person, other_person_name;
	private Activity activity;
	private UserInfoStore store;
	private boolean isMyConversation;
	private ContentUtils cu;
	private String conversant;
	
    @SuppressWarnings("deprecation")
	public MessageCursorAdapter(Context context, Cursor cursor, String number, 
			Activity activity, boolean isMine) {
    	super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
    	check_hash = new TreeSet<SMSMessage>();
    	delayedMessages = new HashMap<Long,Integer>();
    	store = new UserInfoStore(context);
    	other_person = ContentUtils.addCountryCode(number);
    	other_person_name = store.getName(other_person);
		this.activity = activity;
		this.isMyConversation = isMine;
		cu  = new ContentUtils();
   	    	
    }
	public MessageCursorAdapter(Context context, Cursor cursor, Activity activity, 
			boolean isMyConversation, String conversant, String other_person, String person_shared) {
    	super(context, cursor);
    	check_hash = new TreeSet<SMSMessage>();
    	delayedMessages = new HashMap<Long,Integer>();
    	store = new UserInfoStore(context);
    	this.conversant = conversant;
    	this.other_person = other_person;
    	this.other_person_name = person_shared;
		this.activity = activity;
		this.isMyConversation = isMyConversation;
   	    	
    }
	
	@Override
	public void bindView(View v, Context context, Cursor c) {
		if(isMyConversation) populateMine(context, c, v, false);
		else populateTheirs(context, c, v, false);
	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.sms_row, parent, false);
		if(isMyConversation) populateMine(context, c, v, true);
		else populateTheirs(context, c, v, true);
		return v;
	}
	private void populateTheirs(Context context, Cursor c, View v, boolean isnew) {
		String body="";
		final String address;
		long date;
		final SMSMessage message;
		final int position = c.getPosition();
		
		for(int i=0; i<c.getColumnCount(); i++) {
			//Log.i("column ", c.getColumnName(i) + ": " + c.getString(i));
		}
		int type = Integer.parseInt(c.getString(c.getColumnIndex(TextBasedSmsColumns.TYPE)).toString());

		body = c.getString(2).toString();
    	address = c.getString(4).toString();
    	date = c.getLong(6);
    	String owner = c.getString(7);
       	message = new SMSMessage(date, body, address, store.getName(address), type, store, owner);

		final ViewHolder holder; 
		if(isnew) holder = new ViewHolder();
		else holder = (ViewHolder) v.getTag();
		
		holder.messageBox = (LinearLayout) v.findViewById(R.id.message_box);
		holder.message = (TextView) v.findViewById(R.id.message_text);
		holder.time = (TextView) v.findViewById(R.id.message_time);
		holder.box = (CheckBox) v.findViewById(R.id.cbBox);
		holder.edit = (Button) v.findViewById(R.id.edit_message_button);	
		holder.their_pic = (ImageView) v.findViewById(R.id.contact_image);
		holder.my_pic = (ImageView) v.findViewById(R.id.my_image);
		holder.sharedWith = (LinearLayout) v.findViewById(R.id.shared_with);
		holder.shared_with_text = (TextView) v.findViewById(R.id.shared_with_text);
		holder.addPPl = (ImageView) v.findViewById(R.id.add_ppl);
		
		holder.sharedWith.setVisibility(View.GONE);
		holder.shared_with_text.setVisibility(View.GONE);
		holder.addPPl.setVisibility(View.GONE);
    	
    	holder.box.setChecked(false);
    	holder.box.setVisibility(View.GONE);
    	holder.message.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
    	
    	LayoutParams layoutParams=(LayoutParams) holder.addPPl.getLayoutParams();
		if(message.isSentByMe()) {
			if(type == Constants.MESSAGE_TYPE_SENT_COMMENT) {
				holder.messageBox.setBackgroundResource(R.drawable.blank_outgoing);
				holder.message.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
				holder.time.setBackgroundResource(0);
				holder.time.setTextColor(Color.GRAY);						
			} else {
				holder.messageBox.setBackgroundResource(R.drawable.outgoing);
			}
			layoutParams.gravity = Gravity.LEFT;
			holder.message.setGravity(Gravity.RIGHT);
			holder.time.setGravity(Gravity.RIGHT);				
		}else {
			if(type == Constants.MESSAGE_TYPE_RECEIVED_COMMENT) {
				holder.messageBox.setBackgroundResource(R.drawable.blank_incoming);
				holder.message.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
			}else
				holder.messageBox.setBackgroundResource(R.drawable.incoming);
			layoutParams.gravity = Gravity.RIGHT;
			holder.message.setGravity(Gravity.LEFT);
			holder.time.setGravity(Gravity.LEFT);					
		}		
		v.setTag(holder);		
		holder.message.setText(message.getMessage());	
		LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();

		if(message.isSentByMe())
		{
			holder.their_pic.setVisibility(View.GONE);	
			lp.gravity = Gravity.RIGHT;
			
		}
		//If not mine then it is from sender to show yellow background and align to left
		else
		{
	    	holder.their_pic.setVisibility(View.GONE);			
			lp.gravity = Gravity.LEFT;
		}
		
		if(type==TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX) {
		//if(message.isDelayed) {
		    final CharSequence relativeTime;
			relativeTime = DateUtils.getRelativeDateTimeString(context, date, DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);			
			holder.edit.setVisibility(View.GONE);
			holder.my_pic.setBackgroundResource(R.drawable.send);
			holder.my_pic.setVisibility(View.VISIBLE);
			holder.my_pic.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					message.setApproved();
					try {
						message.saveToParse();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				//	holder.time.setText(relativeTime);
					//holder.my_pic.setVisibility(View.GONE);
				}
				
			}) ;
			holder.messageBox.setBackgroundResource(R.drawable.comment_box_background);
			holder.messageBox.setPadding(20, 20, 20, 20);
			holder.message.setGravity(Gravity.LEFT);
			holder.time.setGravity(Gravity.LEFT);
		    holder.time.setText(relativeTime + ", after you approve");
		    holder.time.setTextColor(Color.RED);
		    holder.time.setBackgroundResource(R.drawable.comment_box_background);
		} else {
			CharSequence relativeTime;
			if(System.currentTimeMillis()-message.getDate()>DateUtils.MINUTE_IN_MILLIS){
				relativeTime = DateUtils.getRelativeDateTimeString(context, message.getDate(), 
						DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			}else{
				relativeTime = "Just Now";
			}
			holder.time.setText(relativeTime);
			holder.edit.setVisibility(View.GONE);
			holder.my_pic.setVisibility(View.GONE);
		}
		holder.messageBox.setLayoutParams(lp);		
	}

	private void populateMine(final Context context, Cursor c, View v, boolean isnew) {
		String body="";
		final String address;
		long date;
		final SMSMessage message;
		final int position = c.getPosition();

		int type = Integer.parseInt(c.getString(1).toString());
		if(type==TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX) return;
		body = c.getString(2).toString();
    	address = c.getString(4).toString();
    	date = c.getLong(6);
    	String read = c.getString(5).toString();
    	String SmsMessageId = c.getString(3).toString();
    	
    	if(!SmsMessageId.equals("") && read.equals("0")) {
        	ContentValues values = new ContentValues();
    		values.put("read",true);
    		context.getContentResolver().update(Uri.parse("content://sms/"),
    				values, "_id="+SmsMessageId, null);	
    	}	    	
    	
    	message = new SMSMessage(date, body, address, store.getName(address), 
    			type, store, ParseUser.getCurrentUser().getUsername());

		final ViewHolder holder; 
		if(isnew) holder = new ViewHolder();
		else holder = (ViewHolder) v.getTag();
		
		holder.messageBox = (LinearLayout) v.findViewById(R.id.message_box);
		holder.message = (TextView) v.findViewById(R.id.message_text);
		holder.time = (TextView) v.findViewById(R.id.message_time);
		holder.box = (CheckBox) v.findViewById(R.id.cbBox);
		holder.edit = (Button) v.findViewById(R.id.edit_message_button);	
		holder.their_pic = (ImageView) v.findViewById(R.id.contact_image);
		holder.sharedWith = (LinearLayout) v.findViewById(R.id.shared_with);
		holder.shared_with_text = (TextView) v.findViewById(R.id.shared_with_text);
		holder.addPPl = (ImageView) v.findViewById(R.id.add_ppl);
		
		holder.shared_with_text.setVisibility(View.GONE);


        if (check_hash.contains(message)) {
        	holder.box.setChecked(true);
        	holder.addPPl.setVisibility(View.GONE);
			if(message.isSentByMe()) {
				holder.messageBox.setBackgroundResource(R.drawable.outgoing_pressed);
				holder.message.setGravity(Gravity.RIGHT);
				holder.time.setGravity(Gravity.RIGHT);
			}else {
				holder.messageBox.setBackgroundResource(R.drawable.incoming_pressed); 
				holder.message.setGravity(Gravity.LEFT);
				holder.time.setGravity(Gravity.LEFT);				
			}
        }
        else {
        	holder.box.setChecked(false);
        	
        	LayoutParams layoutParams=(LayoutParams) holder.addPPl.getLayoutParams();
			if(message.isSentByMe()) {
				holder.messageBox.setBackgroundResource(R.drawable.outgoing);
				holder.message.setPadding(50, 0, 10, 0);
				holder.time.setPadding(50, 0, 10, 0);
				layoutParams.gravity = Gravity.LEFT;
				//layoutParams.leftMargin = -40;
				holder.message.setGravity(Gravity.RIGHT);
				holder.time.setGravity(Gravity.RIGHT);				
			}else {
				holder.messageBox.setBackgroundResource(R.drawable.incoming);  
				holder.message.setPadding(10, 0, 50, 0);
				holder.time.setPadding(10, 0, 50, 0);
				layoutParams.gravity = Gravity.RIGHT;
				//layoutParams.rightMargin = -40;
				holder.message.setGravity(Gravity.LEFT);
				holder.time.setGravity(Gravity.LEFT);					
			}
		//	holder.addPPl.setLayoutParams(layoutParams);
			holder.addPPl.setVisibility(View.VISIBLE);
        }
		if(showCheckboxes) holder.box.setVisibility(View.VISIBLE);
		else holder.box.setVisibility(View.GONE);
		
		v.setTag(holder);		
		holder.message.setText(message.getMessage());
		holder.messageBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(Constants.ACTION_SHARE_STATE_CHANGED);
				//broadcastIntent.putExtra(Constants.EXTRA_MESSAGE_POSITION, position);
				context.sendBroadcast(broadcastIntent);					
				holder.box.toggle();
				if(check_hash.contains(message)) 
					check_hash.remove(message);
				else 
					check_hash.add(message);
				notifyDataSetChanged();
			};
        });				
		LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();

		if(message.isSentByMe())
		{
			holder.their_pic.setVisibility(View.GONE);	
			lp.gravity = Gravity.RIGHT;
			
		}
		//If not mine then it is from sender to show yellow background and align to left
		else
		{
	    	if(store.getPhotoPath(address)!=null) {
	    		//holder.pic.setImageDrawable(Drawable.createFromPath(store.getPhotoPath(address)));
	    		cu.loadBitmap(mContext, store.getPhotoPath(address),holder.their_pic, 0);
	    		holder.their_pic.setVisibility(View.VISIBLE);
	    	}
	    	else holder.their_pic.setVisibility(View.GONE);			
			lp.gravity = Gravity.LEFT;
		}
		
		if(message.isDelayed) {
			holder.edit.setVisibility(View.VISIBLE);

		    CharSequence relativeTime;
		    if(System.currentTimeMillis()-message.getFutureSendTime()<DateUtils.MINUTE_IN_MILLIS){
				relativeTime = DateUtils.getRelativeDateTimeString(context, message.getFutureSendTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			}else{
				relativeTime = "Now";
			}
		    holder.time.setText(relativeTime);
		} else {
			CharSequence relativeTime;
			if(System.currentTimeMillis()-message.getDate()>DateUtils.MINUTE_IN_MILLIS){
				relativeTime = DateUtils.getRelativeDateTimeString(context, message.getDate(), 
						DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			}else{
				relativeTime = "Just Now";
			}
			holder.time.setText(relativeTime);
			holder.edit.setVisibility(View.GONE);
		}
		holder.messageBox.setLayoutParams(lp);

	}
	private static class ViewHolder
	{
		public ImageView my_pic;
		TextView message, time, shared_with_text;
		LinearLayout messageBox, sharedWith;
		CheckBox box;
		Button edit;
		ImageView their_pic;
		ImageView addPPl;
	}
	
	private void sendApproval(String message,long scheduledFor,String approver) {
		ParsePush push = new ParsePush();
		JSONObject data = new JSONObject();
		try {
			data.put("action", Constants.ACTION_APPROVE_MESSAGE);
			data.put("phoneNumber", other_person);
			data.put("message", message);
			data.put("scheduledFor", scheduledFor);
			data.put("approver", approver);
			push.setChannel(ContentUtils.setChannel(conversant));
			push.setData(data);
			push.sendInBackground();				
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
