package com.Pull.pullapp;

import java.util.ArrayList;

import com.Pull.pullapp.model.SMSMessage;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class SharedConversationMessageListAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<SMSMessage> mMessages;
	
	public SharedConversationMessageListAdapter(Context context, ArrayList<SMSMessage> messages) {
		super();
		this.mContext = context;
		this.mMessages = messages;
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

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final SMSMessage message = mMessages.get(position);
		
	
		
		int hashtagId = message.getHashtagID();
		
		if(message.isHashtag()){
			final SMSViewHolder holder; 
			if(convertView == null)
			{
				holder = new SMSViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.hashtag_row, parent, false);
				holder.message = (TextView) convertView.findViewById(R.id.hashtag_text_view);
				convertView.setTag(holder);
			}
			else holder = (SMSViewHolder) convertView.getTag();
			holder.message.setText(message.getMessage());
			return convertView;
		}

		final SMSViewHolder holder; 
		if(convertView == null)
		{
			holder = new SMSViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.shared_sms_row, parent, false);
			holder.messageBox = (LinearLayout) convertView.findViewById(R.id.message_box);
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			holder.time = (TextView) convertView.findViewById(R.id.message_time);
			convertView.setTag(holder);
		}
		else holder = (SMSViewHolder) convertView.getTag();
		holder.message.setText(message.getMessage());
		
		LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();
		
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
		
		CharSequence relativeTime = DateUtils.getRelativeDateTimeString(mContext, message.getDate(), DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
		holder.time.setText(relativeTime);		
		
		return convertView;
	}
	
	private static class SMSViewHolder
	{
		TextView time;
		TextView message;
		LinearLayout messageBox;
	}
	
}
