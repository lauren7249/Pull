package com.Pull.pullapp;

import java.util.ArrayList;

import com.Pull.pullapp.model.SMSMessage;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SharedConversationMessageAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<SMSMessage> mMessages;
	
	public SharedConversationMessageAdapter(Context context, ArrayList<SMSMessage> messages) {
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

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final SMSMessage message = mMessages.get(position);

		final SMSViewHolder holder; 
		if(convertView == null)
		{
			holder = new SMSViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sms_row, parent, false);
			holder.messageBox = (LinearLayout) convertView.findViewById(R.id.message_box);
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			holder.time = (TextView) convertView.findViewById(R.id.message_time);
			holder.box = (CheckBox) convertView.findViewById(R.id.cbBox);
			holder.edit = (Button) convertView.findViewById(R.id.edit_message_button);
			convertView.setTag(holder);
		}
		else holder = (SMSViewHolder) convertView.getTag();
		return null;
	}
	
	private static class SMSViewHolder
	{
		TextView time;
		TextView message;
		LinearLayout messageBox;
		CheckBox box;
		Button edit;
	}
	
}
