package com.Pull.pullapp.adapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.Pull.pullapp.R;
import com.Pull.pullapp.R.drawable;
import com.Pull.pullapp.R.id;
import com.Pull.pullapp.R.layout;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.util.SendUtils;
/**
 * QueuedMessageAdapter is a Custom class to implement custom row in ListView
 * 
 */
public class QueuedMessageAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<SMSMessage> mMessages;
	public boolean showCheckboxes;
	public HashMap<Long,Integer> delayedMessages;
	public QueuedMessageAdapter(Context context, ArrayList<SMSMessage> messages) {
		super();
		this.mContext = context;
		this.mMessages = messages;
		delayedMessages = new HashMap<Long,Integer>();
	}
	public int getCount() {
		return mMessages.size();
	}
	public SMSMessage getItem(int position) {		
		return mMessages.get(position);
	}

	@SuppressLint("ResourceAsColor")
	public View getView(final int position, View convertView, ViewGroup parent) {
		final SMSMessage message = this.getItem(getCount() - position - 1);

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
			holder.addPPl = (ImageView) convertView.findViewById(R.id.add_ppl);
		}
		else holder = (ViewHolder) convertView.getTag();
		
		holder.addPPl.setVisibility(View.GONE);
		
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
        
      
        if (message.box) holder.box.setChecked(true);
        else holder.box.setChecked(false);
		if(showCheckboxes) holder.box.setVisibility(View.VISIBLE);
		else holder.box.setVisibility(View.GONE);

		
		convertView.setTag(holder);		
		holder.message.setText(message.getMessage());
		
		LayoutParams lp = (LayoutParams) holder.messageBox.getLayoutParams();

		if(message.isEvent()) {
			holder.messageBox.setBackgroundResource(android.R.color.white);
			holder.messageBox.setGravity(Gravity.CENTER);
			holder.message.setGravity(Gravity.CENTER);
			holder.message.setTypeface(null, Typeface.ITALIC);
			holder.time.setTypeface(null, Typeface.ITALIC);
			holder.time.setGravity(Gravity.CENTER);
			lp.gravity = Gravity.CENTER;
		}
		else if(message.isSentByMe())
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
		

		holder.edit.setVisibility(View.VISIBLE);
	    Date date = new Date(message.getFutureSendTime());
	    
	    CharSequence relativeTime;
	    if(System.currentTimeMillis()-message.getFutureSendTime()<DateUtils.MINUTE_IN_MILLIS){
			relativeTime = DateUtils.getRelativeDateTimeString(mContext, message.getFutureSendTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
		}else{
			relativeTime = "Now";
		}
	    
        holder.edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				SendUtils.removeFromOutbox(mContext, message.getMessage(),
						message.getAddress(), message.launchedOn, true);

				
			};
        });  
        
	    holder.time.setText(relativeTime);
		holder.messageBox.setLayoutParams(lp);
		return convertView;
	}
	private static class ViewHolder
	{
		public ImageView addPPl;
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