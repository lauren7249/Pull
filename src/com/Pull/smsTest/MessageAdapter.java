package com.Pull.smsTest;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import java.util.ArrayList;

import com.Pull.smsTest.model.SMSMessage;
import com.Pull.smsTest.util.ContentUtils;
import com.Pull.smsTest.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
/**
 * MessageAdapter is a Custom class to implement custom row in ListView
 * 
 */

public class MessageAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<SMSMessage> mMessages;
	private ArrayList<String> checked_messages; 

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

		ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sms_row, parent, false);
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			holder.box = (CheckBox) convertView.findViewById(R.id.cbBox);

		}
		else
			holder = (ViewHolder) convertView.getTag();
		
        holder.box.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                if (message.box) {
                	message.box = false;
                } 
                else {
                	message.box = true;
               
                }

				
			};
        });
        if (message.box) {

        	holder.box.setChecked(true);
        }
        else {
        	holder.box.setChecked(false);
        }      

		
		if(showCheckboxes) {
			holder.box.setVisibility(View.VISIBLE);
		} else {
			holder.box.setVisibility(View.GONE);
		}
		convertView.setTag(holder);		
		holder.message.setText(message.getMessage());
		
		LayoutParams lp = (LayoutParams) holder.message.getLayoutParams();

		//Check whether message is mine to show green background and align to right
		if(message.sentByMe)
		{
			holder.message.setBackgroundResource(R.drawable.outgoing);
			lp.gravity = Gravity.RIGHT;
			
		}
		//If not mine then it is from sender to show yellow background and align to left
		else
		{
			holder.message.setBackgroundResource(R.drawable.incoming);
			lp.gravity = Gravity.LEFT;
		}
		holder.message.setLayoutParams(lp);
		holder.message.setTextColor(R.color.textColor);	
		
		return convertView;
	}
	private static class ViewHolder
	{
		TextView message;
		CheckBox box;
	}

	public long getItemId(int position) {
		//Unimplemented, because we aren't using Sqlite.
		return 0;
	}

    
 
}
