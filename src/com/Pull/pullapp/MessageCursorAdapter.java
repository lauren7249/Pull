package com.Pull.pullapp;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.TextBasedSmsColumns;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.Pull.pullapp.model.SMSMessage;
public class MessageCursorAdapter extends CursorAdapter {
	public boolean showCheckboxes;
	public HashMap<Integer,SMSMessage> check_hash;
	
    @SuppressWarnings("deprecation")
	public MessageCursorAdapter(Context context, Cursor cursor) {
    	super(context, cursor);
    	check_hash = new HashMap<Integer,SMSMessage>();
    }

	@Override
	public void bindView(View v, Context context, Cursor c) {
		String body="", SmsMessageId="", address="", read="";
		long date;
		final SMSMessage message;
		
		final int position= c.getPosition();

		int type = Integer.parseInt(c.getString(1).toString());
		if(type==TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX) return;
		body = c.getString(2).toString();
    	SmsMessageId = c.getString(3).toString();
    	address = c.getString(4).toString();
    	read = c.getString(5).toString();
    	date = c.getLong(6);
    	message = new SMSMessage(date, body, address, type);
		
		final ViewHolder holder; 
		holder = (ViewHolder) v.getTag();
		holder.messageBox = (LinearLayout) v.findViewById(R.id.message_box);
		holder.message = (TextView) v.findViewById(R.id.message_text);
		holder.time = (TextView) v.findViewById(R.id.message_time);
		holder.box = (CheckBox) v.findViewById(R.id.cbBox);
		holder.edit = (Button) v.findViewById(R.id.edit_message_button);	

        if (check_hash.containsKey(position)) holder.box.setChecked(true);
        else holder.box.setChecked(false);
		if(showCheckboxes) holder.box.setVisibility(View.VISIBLE);
		else holder.box.setVisibility(View.GONE);

		
		v.setTag(holder);		
		holder.message.setText(message.getMessage());
		v.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(showCheckboxes){
					holder.box.toggle();
					//message.box = ! message.box;
					if(check_hash.containsKey(position)) check_hash.remove(position);
					else check_hash.put(position,message);
					
				}
			}
		});
		
		holder.box.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(check_hash.containsKey(position)) check_hash.remove(position);
				else check_hash.put(position,message);
			};
        });		
		
		LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();

		//Check whether message is mine to show green background and align to right
		if(message.isSentByMe())
		{
			holder.messageBox.setBackgroundResource(R.drawable.outgoing);
			lp.gravity = Gravity.RIGHT;
			
		}
		//If not mine then it is from sender to show yellow background and align to left
		else
		{
			holder.messageBox.setBackgroundResource(R.drawable.incoming);
			lp.gravity = Gravity.LEFT;
		}
		
		if(message.isDelayed) {
			holder.edit.setVisibility(View.VISIBLE);

		    CharSequence relativeTime;
		    if(System.currentTimeMillis()-message.futureSendTime<DateUtils.MINUTE_IN_MILLIS){
				relativeTime = DateUtils.getRelativeDateTimeString(mContext, message.futureSendTime, DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			}else{
				relativeTime = "Now";
			}
		    holder.time.setText(relativeTime);
		} else {
			CharSequence relativeTime;
			if(System.currentTimeMillis()-message.getDate()>DateUtils.MINUTE_IN_MILLIS){
				relativeTime = DateUtils.getRelativeDateTimeString(mContext, message.getDate(), DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			}else{
				relativeTime = "Just Now";
			}
			holder.time.setText(relativeTime);
			holder.edit.setVisibility(View.GONE);
		}
		holder.messageBox.setLayoutParams(lp);		
	
	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.sms_row, parent, false);
		String body="", SmsMessageId="", address="", read="";
		long date;
		final SMSMessage message;
		final int position = c.getPosition();

		int type = Integer.parseInt(c.getString(1).toString());
		if(type==TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX) return v;
		body = c.getString(2).toString();
    	SmsMessageId = c.getString(3).toString();
    	address = c.getString(4).toString();
    	read = c.getString(5).toString();
    	date = c.getLong(6);
    	message = new SMSMessage(date, body, address, type);
		
    	//TODO: ADD BACK IN
    	if(!SmsMessageId.equals("")) {
        	ContentValues values = new ContentValues();
    		values.put("read",true);
    		context.getContentResolver().update(Uri.parse("content://sms/"),values, "_id="+SmsMessageId, null);	
    	}	

		final ViewHolder holder; 
		holder = new ViewHolder();
		holder.messageBox = (LinearLayout) v.findViewById(R.id.message_box);
		holder.message = (TextView) v.findViewById(R.id.message_text);
		holder.time = (TextView) v.findViewById(R.id.message_time);
		holder.box = (CheckBox) v.findViewById(R.id.cbBox);
		holder.edit = (Button) v.findViewById(R.id.edit_message_button);	

        if (check_hash.containsKey(position)) holder.box.setChecked(true);
        else holder.box.setChecked(false);
		if(showCheckboxes) holder.box.setVisibility(View.VISIBLE);
		else holder.box.setVisibility(View.GONE);

		
		v.setTag(holder);		
		holder.message.setText(message.getMessage());
		
		LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();

		//Check whether message is mine to show green background and align to right
		if(message.isSentByMe())
		{
			holder.messageBox.setBackgroundResource(R.drawable.outgoing);
			lp.gravity = Gravity.RIGHT;
			
		}
		//If not mine then it is from sender to show yellow background and align to left
		else
		{
			holder.messageBox.setBackgroundResource(R.drawable.incoming);
			lp.gravity = Gravity.LEFT;
		}
		
		if(message.isDelayed) {
			holder.edit.setVisibility(View.VISIBLE);

		    CharSequence relativeTime;
		    if(System.currentTimeMillis()-message.futureSendTime<DateUtils.MINUTE_IN_MILLIS){
				relativeTime = DateUtils.getRelativeDateTimeString(mContext, message.futureSendTime, DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			}else{
				relativeTime = "Now";
			}
		    holder.time.setText(relativeTime);
		} else {
			CharSequence relativeTime;
			if(System.currentTimeMillis()-message.getDate()>DateUtils.MINUTE_IN_MILLIS){
				relativeTime = DateUtils.getRelativeDateTimeString(mContext, message.getDate(), DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			}else{
				relativeTime = "Just Now";
			}
			holder.time.setText(relativeTime);
			holder.edit.setVisibility(View.GONE);
		}
		holder.messageBox.setLayoutParams(lp);	
		return v;
	}
	private static class ViewHolder
	{
		public TextView time;
		TextView message;
		LinearLayout messageBox;
		CheckBox box;
		Button edit;
	}

}