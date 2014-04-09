package com.Pull.pullapp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.util.sendSMS;
import com.Pull.pullapp.R;
/**
 * MessageAdapter is a Custom class to implement custom row in ListView
 * 
 */

public class MessageAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<SMSMessage> mMessages;
	public boolean showCheckboxes;
	public MessageAdapter(Context context, ArrayList<SMSMessage> messages) {
		super();
		this.mContext = context;
		this.mMessages = messages;
	}
	public int getCount() {
		return mMessages.size();
	}
	public SMSMessage getItem(int position) {		
		return mMessages.get(position);
	}

	@SuppressLint("ResourceAsColor")
	public View getView(final int position, View convertView, ViewGroup parent) {
		final SMSMessage message = this.getItem(position);

		final ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sms_row, parent, false);
			holder.messageBox = (LinearLayout) convertView.findViewById(R.id.message_box);
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			holder.time = (TextView) convertView.findViewById(R.id.message_time);
			holder.box = (CheckBox) convertView.findViewById(R.id.cbBox);
			holder.edit = (Button) convertView.findViewById(R.id.edit_message_button);
		}
		else holder = (ViewHolder) convertView.getTag();
		
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(showCheckboxes){
					holder.box.toggle();
					message.box = ! message.box;
				}
			}
		});
		
		holder.box.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				message.box = !message.box;
			};
        });
        
        holder.edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				sendSMS.removeFromOutbox(mContext, message.getMessage(),
						message.getRecipient(), message.launchedOn, true);
			};
        });        
        if (message.box) holder.box.setChecked(true);
        else holder.box.setChecked(false);
		if(showCheckboxes) holder.box.setVisibility(View.VISIBLE);
		else holder.box.setVisibility(View.GONE);

		
		convertView.setTag(holder);		
		holder.message.setText(message.getMessage());
		
		LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();

		//Check whether message is mine to show green background and align to right
		if(message.sentByMe)
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
		    Date date = new Date(message.futureSendTime);
		    Format format = new SimpleDateFormat("HH:mm");
		    String button_text = "Sending at " + format.format(date).toString();
		    
		    CharSequence relativeTime;
		    if(System.currentTimeMillis()-message.futureSendTime<DateUtils.MINUTE_IN_MILLIS){
				relativeTime = DateUtils.getRelativeDateTimeString(mContext, message.futureSendTime, DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			}else{
				relativeTime = "Now";
			}
		    holder.time.setText(relativeTime);
		    holder.edit.setText(button_text);
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
		
		return convertView;
	}
	private static class ViewHolder
	{
		public TextView time;
		TextView message;
		LinearLayout messageBox;
		CheckBox box;
		Button edit;
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
 
}
