package com.Pull.pullapp;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.Pull.pullapp.model.SMSMessage;

public class SharedConversationMessageListAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<SMSMessage> mMessages;
	
	public SharedConversationMessageListAdapter(Context context, ArrayList<SMSMessage> messages) {
		super();
		this.mContext = context;
		this.mMessages = messages;
	}

	public SharedConversationMessageListAdapter(Context context,
			HashSet<SMSMessage> messages) {
		this.mContext = context;
		this.mMessages = new ArrayList<SMSMessage>(messages);
	}

	@Override
	public int getCount() {
		return mMessages.size();
	}

	@Override
	public Object getItem(int position) {
		return mMessages.get(position);
	}
	
	
	public void setItemList(ArrayList<SMSMessage> messages) {
		this.mMessages = messages;
		notifyDataSetChanged();
	}
	public void setItemList(HashSet<SMSMessage> messages) {
		this.mMessages = new ArrayList<SMSMessage>(messages);
		notifyDataSetChanged();
	}	

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		SMSMessage message = mMessages.get(position);
		SMSViewHolder holder; 
		
		if(message.isHashtag()){
			
			if(rowView == null)
			{	
				holder = new SMSViewHolder();
				rowView = LayoutInflater.from(mContext).inflate(R.layout.hashtag_row, parent, false);
				holder.message = (TextView) rowView.findViewById(R.id.hashtag_text_view);
				rowView.setTag(holder);
			}
			
			holder = (SMSViewHolder) rowView.getTag();
			holder.message.setText(message.getMessage());
			return rowView;
		}

		else {
			if(rowView == null)
			{
				holder = new SMSViewHolder();
				rowView = LayoutInflater.from(mContext).inflate(R.layout.shared_sms_row, parent, false);
				holder.messageBox = (LinearLayout) rowView.findViewById(R.id.message_box);
				holder.message = (TextView) rowView.findViewById(R.id.message_text);
				holder.time = (TextView) rowView.findViewById(R.id.message_time);
				
				rowView.setTag(holder);
			}
			holder = (SMSViewHolder) rowView.getTag();
			holder.message.setText(message.getMessage());
			
			if(holder.messageBox !=null) {
				if(message.isSentByMe()) holder.messageBox.setBackgroundResource(R.drawable.outgoing);
				else holder.messageBox.setBackgroundResource(R.drawable.incoming);		
			}
			LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();
			if(message.isSentByMe())lp.gravity = Gravity.RIGHT;
			else lp.gravity = Gravity.LEFT;		
	
			CharSequence relativeTime = DateUtils.getRelativeDateTimeString(mContext, message.getDate(), DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
			holder.time.setText(relativeTime);		
			
			return rowView;
		}
	}
	
	private static class SMSViewHolder
	{
		TextView time;
		TextView message;
		LinearLayout messageBox;
	}


	
}
