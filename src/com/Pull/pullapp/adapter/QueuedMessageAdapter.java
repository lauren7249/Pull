package com.Pull.pullapp.adapter;
import java.util.ArrayList;
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
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.util.ContentUtils;
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
		if(message == null) Log.i("message is null","message is null " + getCount() + " " + position);
		final ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sms_row, parent, false);
			holder.messageBox = (LinearLayout) convertView.findViewById(R.id.message_box);
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			holder.time = (TextView) convertView.findViewById(R.id.message_time);
			holder.edit = (Button) convertView.findViewById(R.id.edit_message_button);
			holder.addPPl = (ImageView) convertView.findViewById(R.id.add_ppl);
		}
		else holder = (ViewHolder) convertView.getTag();
		
		holder.addPPl.setVisibility(View.GONE);
		
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(showCheckboxes){
					message.box = ! message.box;
				}
			}
		});

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
			holder.message.setTextColor(mContext.getResources().getColor(R.color.lightGreen));
			holder.time.setTextColor(mContext.getResources().getColor(R.color.lightGreen));		
			lp.gravity = Gravity.RIGHT;
			
		}
		
		holder.edit.setVisibility(View.VISIBLE);

		long difference = message.getFutureSendTime()-System.currentTimeMillis();
	    CharSequence relativeTime;
	    if(difference<1500 && difference>=-3000) {
	    	relativeTime = "Sending...";
	    	holder.edit.setVisibility(View.GONE);
	    }
	    else if(difference<DateUtils.MINUTE_IN_MILLIS && difference>0){
			relativeTime = DateUtils.getRelativeDateTimeString(mContext, message.getFutureSendTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
		}	    
	    else if(System.currentTimeMillis()-message.getFutureSendTime()<DateUtils.MINUTE_IN_MILLIS){
			relativeTime = DateUtils.getRelativeDateTimeString(mContext, message.getFutureSendTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
		} else if (System.currentTimeMillis()>message.getFutureSendTime()) {
			relativeTime = "[Never sent]";
		} else {
			relativeTime = "Now";
		}
	    String approverString = "";
	    if(message.getApprover()!=null && message.getApprover().length()>0) {
	    	approverString = ", with " + 
	    			ContentUtils.getContactDisplayNameByNumber(mContext, message.getApprover()) + "'s approval";
	    }
        holder.edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				SendUtils.removeFromOutbox(mContext, message.getMessage(),
						message.getAddress(), message.launchedOn, message.getFutureSendTime(), 
						true, message.getApprover());
				
			};
        });  
        
	    holder.time.setText(relativeTime + approverString);
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
